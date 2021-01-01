package generator;

import information.YaplConstants;
import jvm_class_generator.specs.JvmClass;
import jvm_class_generator.specs.attributes.Code;
import jvm_class_generator.specs.attributes.InnerClasses;
import jvm_class_generator.specs.class_content.Field;
import jvm_class_generator.specs.class_content.Method;
import jvm_class_generator.specs.data_areas.ConstantPool;
import jvm_class_generator.specs.helpers.AccessFlags;
import jvm_class_generator.specs.helpers.ArrayType;
import jvm_class_generator.specs.helpers.Descriptor;
import stdlib.StandardLibrary;
import information.*;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;

/**
 * An implementation of the {@code Backend} interface for the JVM.
 * It uses the {@code Jvm_class_generator} to generate the actual bytecode.
 * <br><br>
 * Scope translations:<br>
 * <ul>
 *  <li>Program -> Jvm Class</li>
 *  <li>Global Variables -> static class fields</li>
 *  <li>Constants -> resolved and replaced with their value</li>
 *  <li>Records -> inner Jvm Classes</li>
 *  <li>Procedures -> static class methods</li>
 *  <li>Predefined procedures -> a StandardLibrary class-file with all procedures as static methods.</li>
 * </ul>
 */
public class JvmCodeGenerator implements CodeGenerator {
  protected final String AND = "And";
  protected final String OR = "Or";
  protected final String START = "start";
  protected final String END = "end";

  protected StandardLibrary stdlib = null;
  protected Path outputDir = null;

  protected JvmClass program = null;
  protected InnerClasses innerClasses = null;
  protected ConstantPool consts = null;

  protected Method method = null;
  protected Code code = null;

  protected List<JvmClass> records = new ArrayList<>();
  protected JvmClass record = null;
  protected String recordName = null;

  /**
   * Stores the active boolean operators in nested boolean expressions
   */
  protected Stack<String> boolOperators = new Stack<>();

  /**
   * Stores the number of operands which have been generated for each of the active boolean operators already.
   */
  protected Stack<Integer> boolOperandsCount = new Stack<>();

  protected Stack<Map<String, String>> labels;
  protected Map<Variable, Integer> locals;



  public JvmCodeGenerator(StandardLibrary stdlib, Path outputDir) {
    this.stdlib = stdlib;
    this.outputDir = outputDir;
  }



  public JvmCodeGenerator enterProgram(String name) {
    this.program = new jvm_class_generator.impl.JvmClass(capitalise(name), Descriptor.NAME_OF(Object.class), AccessFlags.PUBLIC | AccessFlags.SUPER);
    this.consts = program.constantPool();
    return this;
  }

  public void exitProgram() throws IOException {
    if (!Files.exists(outputDir)) {
      Files.createDirectories(outputDir);
    }

    Files.write( outputDir.resolve(stdlib.getName() + ".class"), stdlib.generate() );

    for (JvmClass record : records)
      Files.write( outputDir.resolve(record.name() + ".class"), record.generate() );

    Files.write( outputDir.resolve(program.name() + ".class"), program.generate() );
  }

  public JvmCodeGenerator enterMainFunction() {
    this.method = program.addMethod("main", Descriptor.MAIN, AccessFlags.PUBLIC | AccessFlags.STATIC);
    this.code = (Code)method.addAttribute("Code");
    code.addStackMapTableAttribute();

    this.labels = new Stack<>();
    labels.push(new HashMap<>());
    this.nextLabelId = 0;

    this.locals = new HashMap<>();

    return this;
  }

  public JvmCodeGenerator exitMainFunction() {
    code.vreturn();
    this.method = null;
    this.code = null;

    labels.pop();
    return this;
  }

  public JvmCodeGenerator enterFunction(Procedure sym) {
    this.method = program.addMethod(sym.name, getMethodDescriptor(sym), AccessFlags.PUBLIC | AccessFlags.STATIC);
    this.code = (Code)method.addAttribute("Code");
    code.addStackMapTableAttribute();

    this.labels = new Stack<>();
    labels.push(new HashMap<>());
    this.nextLabelId = 0;

    this.locals = new HashMap<>();
    int nextLocalId = 0;

    for (Parameter param : sym.params)
      locals.put(param, nextLocalId++);

    return this;
  }

  public JvmCodeGenerator exitFunction() {
    final String returnDescriptor = Descriptor.METHOD_RETURN_DESCRIPTOR( method.descriptor() );
    if (returnDescriptor.equals(Descriptor.VOID))
      code.vreturn();

    method = null;
    code = null;
    return this;
  }

  public JvmCodeGenerator enterRecord(String name) {
    this.recordName = name;
    this.record = new jvm_class_generator.impl.JvmClass(getFullRecordName(name), Descriptor.NAME_OF(Object.class), AccessFlags.PUBLIC | AccessFlags.SUPER);
    return this;
  }

  public JvmCodeGenerator exitRecord() {
    // default constructor
    Method init = record.addMethod("<init>", Descriptor.METHOD(Descriptor.VOID), AccessFlags.PUBLIC);
    ((Code)init.addAttribute("Code"))
      .aload_0()
      .invokeSpecial( record.constantPool().addMethodref(Descriptor.NAME_OF(Object.class), "<init>", Descriptor.METHOD(Descriptor.VOID)) )
      .vreturn();

    // toString
    Method toString = record.addMethod("toString", Descriptor.METHOD(Descriptor.STRING), AccessFlags.PUBLIC);
    Code code = ((Code)toString.addAttribute("Code"));

    final int concat = record.constantPool().addMethodref(Descriptor.NAME_OF(String.class), "concat", Descriptor.METHOD(Descriptor.STRING, Descriptor.STRING));
    code.ldc( record.constantPool().addString(recordName + " {") );

    int fieldNr = 1;
    for (Field field : record.fields.values()) {
      code.ldc( record.constantPool().addString(field.name() + ": ") ).invokeVirtual(concat);

      if (field.descriptor().equals(Descriptor.INT)) {
        final int int2str = record.constantPool().addMethodref(Descriptor.NAME_OF(Integer.class), "toString", Descriptor.METHOD(Descriptor.STRING, Descriptor.INT));
        code
          .aload_0()
          .getField( record.constantPool().addFieldref(record.name(), field.name(), field.descriptor()) )
          .invokeStatic(int2str)
          .invokeVirtual(concat);
      }
      else if(field.descriptor().equals(Descriptor.BOOLEAN)) {
        final int bool2str = record.constantPool().addMethodref(Descriptor.NAME_OF(Boolean.class), "toString", Descriptor.METHOD(Descriptor.STRING, Descriptor.BOOLEAN));
        code
          .aload_0()
          .getField( record.constantPool().addFieldref(record.name(), field.name(), field.descriptor()) )
          .invokeStatic(bool2str)
          .invokeVirtual(concat);
      }
      else if(field.descriptor().startsWith("[")) {
        String Arrays = Descriptor.NAME_OF(Arrays.class);
        int arr2str;

        if (field.descriptor().startsWith("[[")) {
          arr2str = record.constantPool().addMethodref(Arrays, "deepToString", Descriptor.METHOD(Descriptor.STRING, Descriptor.ARRAY(Descriptor.OBJECT)));
        }
        else if (field.descriptor().equals(Descriptor.ARRAY(Descriptor.INT))) {
          arr2str = record.constantPool().addMethodref(Arrays, "toString", Descriptor.METHOD(Descriptor.STRING, Descriptor.ARRAY(Descriptor.INT)));
        }
        else if (field.descriptor().equals(Descriptor.ARRAY(Descriptor.BOOLEAN))) {
          arr2str = record.constantPool().addMethodref(Arrays, "toString", Descriptor.METHOD(Descriptor.STRING, Descriptor.ARRAY(Descriptor.BOOLEAN)));
        }
        else {
          arr2str = record.constantPool().addMethodref(Arrays, "toString", Descriptor.METHOD(Descriptor.STRING, Descriptor.ARRAY(Descriptor.OBJECT)));
        }

        code
          .aload_0()
          .getField( record.constantPool().addFieldref(record.name(), field.name(), field.descriptor()) )
          .invokeStatic(arr2str)
          .invokeVirtual(concat);
      }
      else {
        final int obj2str = record.constantPool().addMethodref(Descriptor.NAME_OF(Objects.class), "toString", Descriptor.METHOD(Descriptor.STRING, Descriptor.OBJECT));
        code
          .aload_0()
          .getField( record.constantPool().addFieldref(record.name(), field.name(), field.descriptor()) )
          .invokeStatic(obj2str)
          .invokeVirtual(concat);
      }


      if (fieldNr < record.fields.size()) {
        code.ldc( record.constantPool().addString(", ") ).invokeVirtual(concat);
      }

      fieldNr++;
    }

    code.ldc( record.constantPool().addString("}") ).invokeVirtual(concat);
    code.areturn();

    if (innerClasses == null)
      this.innerClasses = (InnerClasses)program.addAttribute("InnerClasses");

    this.records.add(record);
    this.innerClasses.add(record, record.name());
    this.record = null;
    return this;
  }

  public JvmCodeGenerator loadConstant(Constant sym) {
    if (sym.dataType.equals(YaplConstants.STRING)) {
      code.ldc( consts.addString(sym.value) );
      return this;
    }

    if (sym.dataType.equals(YaplConstants.BOOL)) {
      if (sym.value.equals(YaplConstants.TRUE)) code.iconst_1();
      else code.iconst_0();

      if (boolOperators.size() > 0) connectBoolOperand();
      return this;
    }

    final int value = Integer.parseInt(sym.value);
    if (value == -1) code.iconst_m1();
    else if (value == 0) code.iconst_0();
    else if (value == 1) code.iconst_1();
    else if (value == 2) code.iconst_2();
    else if (value == 3) code.iconst_3();
    else if (value == 4) code.iconst_4();
    else if (value == 5) code.iconst_5();
    else if ((value & 0x7f) == value) code.bipush(value);
    else if ((value & 0x7fff) == value) code.sipush(value);
    else code.ldc( consts.addInteger(value) );
    return this;
  }

  public JvmCodeGenerator allocVariable(Variable sym) {
    if (!sym.isLocal) {
      if (record != null) record.addField(sym.name, getTypeDescriptor(sym.dataType), AccessFlags.PUBLIC);
      else program.addField(sym.name, getTypeDescriptor(sym.dataType), AccessFlags.PUBLIC | AccessFlags.STATIC);
    }
    else {
      locals.put(sym, code.allocLocal( getTypeDescriptor(sym.dataType) ));

      if (sym.isPrimitive()) {
        loadConstant(new ConstantExpression(null, "int", "0"));
        store(sym);
      }
      else if (sym.isArray()) {
        String[] typeParts = sym.dataType.split("\\[");
        String baseType = typeParts[0];
        int dimensions = typeParts.length - 1;

        for (int i = 0; i < dimensions; i++)
          code.iconst_0();

        newArray(baseType, dimensions);
        store(sym);
      }
      else {
        final String recordName = getFullRecordName(sym.dataType);

        code
          .anew( consts.addClass( getFullRecordName(sym.dataType) ) )
          .dup()
          .invokeSpecial( consts.addMethodref(recordName, "<init>", Descriptor.METHOD(Descriptor.VOID)) );

        store(sym);
      }
    }

    return this;
  }

  public JvmCodeGenerator store(Variable sym) {
    if (sym.name.endsWith("[]")) {
      if (sym.dataType.equals(YaplConstants.BOOL)) code.bastore();
      else if (sym.dataType.equals(YaplConstants.INT)) code.iastore();
      else code.aastore();
    }
    else if (sym.name.contains(".")) {
      final String record = getFullRecordName(sym.parent.dataType);
      final String[] name = sym.name.split("\\.");
      code.putField( consts.addFieldref(record, name[name.length - 1], getTypeDescriptor(sym.dataType)) );
    }
    else {
      if (sym.isLocal) {
        if (sym.isPrimitive()) {
          if (locals.get(sym) == 0) code.istore_0();
          else if (locals.get(sym) == 1) code.istore_1();
          else if (locals.get(sym) == 2) code.istore_2();
          else if (locals.get(sym) == 3) code.istore_3();
          else code.istore(locals.get(sym), locals.get(sym) > 0xff);
        }
        else {
          if (locals.get(sym) == 0) code.astore_0();
          else if (locals.get(sym) == 1) code.astore_1();
          else if (locals.get(sym) == 2) code.astore_2();
          else if (locals.get(sym) == 3) code.astore_3();
          else code.astore(locals.get(sym), locals.get(sym) > 0xff);
        }
      }
      else {
        final int fieldRef = consts.addFieldref(program.name(), sym.name, getTypeDescriptor(sym.dataType));
        code.putStatic(fieldRef);
      }
    }

    return this;
  }

  public JvmCodeGenerator load(Variable sym) {
    if (sym.name.endsWith("[]")) {
      if (sym.dataType.equals(YaplConstants.BOOL)) code.baload();
      else if (sym.dataType.equals(YaplConstants.INT)) code.iaload();
      else code.aaload();
    }
    else if (sym.name.contains(".")) {
      final String record = getFullRecordName(sym.parent.dataType);
      final String[] name = sym.name.split("\\.");
      code.getField( consts.addFieldref(record, name[name.length - 1], getTypeDescriptor(sym.dataType)) );
    }
    else {
      if (sym.isLocal) {
        if (sym.isPrimitive()) {
          if (locals.get(sym) == 0) code.iload_0();
          else if (locals.get(sym) == 1) code.iload_1();
          else if (locals.get(sym) == 2) code.iload_2();
          else if (locals.get(sym) == 3) code.iload_3();
          else code.iload(locals.get(sym), locals.get(sym) > 0xff);
        }
        else {
          if (locals.get(sym) == 0) code.aload_0();
          else if (locals.get(sym) == 1) code.aload_1();
          else if (locals.get(sym) == 2) code.aload_2();
          else if (locals.get(sym) == 3) code.aload_3();
          else code.aload(locals.get(sym), locals.get(sym) > 0xff);
        }
      }
      else {
        final int fieldRef = consts.addFieldref(program.name(), sym.name, getTypeDescriptor(sym.dataType));
        code.getStatic(fieldRef);
      }
    }

    if (sym.dataType.equals(YaplConstants.BOOL) && boolOperators.size() > 0) connectBoolOperand();
    return this;
  }

  public JvmCodeGenerator write() {
    String System = Descriptor.NAME_OF(System.class);
    String PrintStream = Descriptor.NAME_OF(PrintStream.class);

    int systemOut = consts.addFieldref(System, "out", Descriptor.REFERENCE(PrintStream));
    int printString = consts.addMethodref(PrintStream, "print", Descriptor.METHOD(Descriptor.VOID, Descriptor.STRING));

    code
      .getStatic(systemOut)
      .swap()
      .invokeVirtual(printString);

    return this;
  }

  public JvmCodeGenerator callFunction(Procedure fn) {
    final String className = fn.isStdLib ? stdlib.getName() : program.name();
    final int methodRef = consts.addMethodref(className, fn.name, getMethodDescriptor(fn));
    code.invokeStatic(methodRef);

    if (fn.returnType.equals(YaplConstants.BOOL) && boolOperators.size() > 0) connectBoolOperand();
    return this;
  }

  public JvmCodeGenerator op1(String operator) {
    if (operator.equals("-")) {
      code.ineg();
    }

    return this;
  }

  public JvmCodeGenerator startBranchingBlock() {
    labels.push(new HashMap<>());

    // create a start point to reset the stack to after each conditional branch
    // its a simple trick to make sure the stackmaptables are correct
    // if we would not reset the stack after each conditional branch the stackmaptable
    // would contain the stacks of all those branches on top of each other and therefore be invalid
    final String startLbl = nextLabel();
    labels.peek().put(START, startLbl);
    code.addLabel(startLbl);

    labels.peek().put(END, nextLabel());
    return this;
  }

  public JvmCodeGenerator endBranchingBlock() {
    // resets the stack to the state at the start
    code.addLabel(labels.peek().get(END), labels.peek().get(START));
    labels.pop();
    return this;
  }

  public JvmCodeGenerator op2(String op) {
    String resetlbl, truelbl, endlbl;

    switch (op) {
      case "+" -> code.iadd();
      case "-" -> code.isub();
      case "*" -> code.imul();
      case "/" -> code.idiv();
      case "%" -> code.irem();

      case OR -> {
        boolOperators.push(OR);
        boolOperandsCount.push(0);
        labels.push(new HashMap<>());
        labels.peek().put(START, nextLabel());
        labels.peek().put(YaplConstants.TRUE, nextLabel());
        labels.peek().put(END, nextLabel());
        code.addLabel(labels.peek().get(START));
      }

      case AND -> {
        boolOperators.push(AND);
        boolOperandsCount.push(0);
        labels.push(new HashMap<>());
        labels.peek().put(START, nextLabel());
        labels.peek().put(YaplConstants.FALSE, nextLabel());
        labels.peek().put(END, nextLabel());
        code.addLabel(labels.peek().get(START));
      }

      case "==" -> {
        resetlbl = nextLabel();
        truelbl = nextLabel();
        endlbl = nextLabel();
        code
          .if_icmpeq(truelbl)
          .addLabel(resetlbl)
          .iconst_0()
          .gotoLabel(endlbl)
          .addLabel(truelbl, resetlbl)
          .iconst_1()
          .addLabel(endlbl);
        if (boolOperators.size() > 0) connectBoolOperand();
      }

      case "!=" -> {
        resetlbl = nextLabel();
        truelbl = nextLabel();
        endlbl = nextLabel();
        code
          .if_icmpne(truelbl)
          .addLabel(resetlbl)
          .iconst_0()
          .gotoLabel(endlbl)
          .addLabel(truelbl, resetlbl)
          .iconst_1()
          .addLabel(endlbl);
        if (boolOperators.size() > 0) connectBoolOperand();
      }

      case ">" -> {
        resetlbl = nextLabel();
        truelbl = nextLabel();
        endlbl = nextLabel();
        code
          .if_icmpgt(truelbl)
          .addLabel(resetlbl)
          .iconst_0()
          .gotoLabel(endlbl)
          .addLabel(truelbl, resetlbl)
          .iconst_1()
          .addLabel(endlbl);
        if (boolOperators.size() > 0) connectBoolOperand();
      }

      case "<" -> {
        resetlbl = nextLabel();
        truelbl = nextLabel();
        endlbl = nextLabel();
        code
          .if_icmplt(truelbl)
          .addLabel(resetlbl)
          .iconst_0()
          .gotoLabel(endlbl)
          .addLabel(truelbl, resetlbl)
          .iconst_1()
          .addLabel(endlbl);
        if (boolOperators.size() > 0) connectBoolOperand();
      }

      case ">=" -> {
        resetlbl = nextLabel();
        truelbl = nextLabel();
        endlbl = nextLabel();
        code
          .if_icmpge(truelbl)
          .addLabel(resetlbl)
          .iconst_0()
          .gotoLabel(endlbl)
          .addLabel(truelbl, resetlbl)
          .iconst_1()
          .addLabel(endlbl);
        if (boolOperators.size() > 0) connectBoolOperand();
      }

      case "<=" -> {
        resetlbl = nextLabel();
        truelbl = nextLabel();
        endlbl = nextLabel();
        code
          .if_icmple(truelbl)
          .addLabel(resetlbl)
          .iconst_0()
          .gotoLabel(endlbl)
          .addLabel(truelbl, resetlbl)
          .iconst_1()
          .addLabel(endlbl);
        if (boolOperators.size() > 0) connectBoolOperand();
      }
    }

    return this;
  }

  public JvmCodeGenerator newArray(String baseType, int dimensions) {
    if (dimensions > 1) {
      String descriptor = getBaseTypeDescriptor(baseType);

      for (int i = 0; i < dimensions; i++)
        descriptor = Descriptor.ARRAY(descriptor);

      code.multianewArray( consts.addClass(descriptor), dimensions );
    }
    else if (baseType.equals(YaplConstants.INT) || baseType.equals(YaplConstants.BOOL)) {
      int arrType = baseType.equals(YaplConstants.INT) ? ArrayType.INT : ArrayType.BOOLEAN;
      code.newArray(arrType);
    }
    else {
      code.anewArray( consts.addClass(getFullRecordName(baseType)) );
    }

    return this;
  }

  public JvmCodeGenerator arraylength() {
    code.arraylength();
    return this;
  }

  public JvmCodeGenerator branch() {
    // skip to label END if the condition is false
    code.ifeq(labels.peek().get(END));
    return this;
  }

  public JvmCodeGenerator elseBranch() {
    // use label END as the else label (since it is jumped to if the codnition is false)
    // and create a new END label which the if-branch jumps to at the end
    String elseLbl = labels.peek().get(END);
    String endLbl = nextLabel();
    labels.peek().put(END, endLbl);

    code
      .gotoLabel(endLbl)
      .addLabel(elseLbl);

    return this;
  }

  public JvmCodeGenerator loop() {
    code.gotoLabel(labels.peek().get(START));
    return this;
  }

  public CodeGenerator returnFromFunction() {
    final String returnDescriptor = Descriptor.METHOD_RETURN_DESCRIPTOR( method.descriptor() );

    switch (returnDescriptor) {
      case Descriptor.BOOLEAN, Descriptor.INT -> code.ireturn();
      case Descriptor.VOID -> code.vreturn();
      default -> code.areturn();
    }

    return this;
  }

  public JvmCodeGenerator newRecord(String type) {
    final String name = getFullRecordName(type);

    code
      .anew( consts.addClass( getFullRecordName(type) ) )
      .dup()
      .invokeSpecial( consts.addMethodref(name, "<init>", Descriptor.METHOD(Descriptor.VOID)) );

    return this;
  }



  /**
   * Is called after each boolean operand and adds the correct branching for lazy evaluation according to the current boolean operator.<br>
   * OR skips the second operand if the first one is true<br>
   * AND skips the second operand if the first one is false
   */
  protected void connectBoolOperand() {
    int operandNr = boolOperandsCount.pop() + 1;
    boolOperandsCount.push(operandNr);

    if (boolOperators.peek().equals(OR)) code.ifne(labels.peek().get(YaplConstants.TRUE));
    else code.ifeq(labels.peek().get(YaplConstants.FALSE));

    if (operandNr == 2) {
      if (boolOperators.peek().equals(OR)) {
        code
          .iconst_0()
          .gotoLabel(labels.peek().get(END))
          .addLabel(labels.peek().get(YaplConstants.TRUE), labels.peek().get(START))
          .iconst_1()
          .addLabel(labels.peek().get(END));
      }
      else {
        code
          .iconst_1()
          .gotoLabel(labels.peek().get(END))
          .addLabel(labels.peek().get(YaplConstants.FALSE), labels.peek().get(START))
          .iconst_0()
          .addLabel(labels.peek().get(END));
      }

      labels.pop();
      boolOperators.pop();
      boolOperandsCount.pop();

      if (boolOperandsCount.size() > 0) connectBoolOperand();
    }
  }


  protected int nextLabelId = 0;
  protected String nextLabel() {
    return "" + nextLabelId++;
  }

  protected String getMethodDescriptor(Procedure sym) {
    String returnDescriptor = getTypeDescriptor(sym.returnType);
    String[] paramDescriptors = new String[ sym.params.size() ];

    for (int idx = 0; idx < paramDescriptors.length; idx++) {
      paramDescriptors[idx] = getTypeDescriptor( sym.params.get(idx).dataType );
    }

    return Descriptor.METHOD(returnDescriptor, paramDescriptors);
  }

  protected String getTypeDescriptor(String type) {
    final String baseType = type.split("\\[")[0];
    final int dimensions = type.split("\\[").length - 1;

    String descriptor = getBaseTypeDescriptor(baseType);
    for (int i = 0; i < dimensions; i++) {
      descriptor = Descriptor.ARRAY(descriptor);
    }

    return descriptor;
  }

  protected String getBaseTypeDescriptor(String type) {
    return switch (type) {
      case YaplConstants.INT -> Descriptor.INT;
      case YaplConstants.BOOL -> Descriptor.BOOLEAN;
      case YaplConstants.STRING -> Descriptor.STRING;
      case YaplConstants.VOID -> Descriptor.VOID;
      default -> Descriptor.REFERENCE(getFullRecordName(type));
    };
  }

  /**
   * For the JVM inner classes are stored in their own files with the naming convention <parent_class>$<inner_class>
   */
  protected String getFullRecordName(String record) {
    return program.name() + "$" + capitalise(record);
  }

  protected String capitalise(String str) {
    return str.substring(0, 1).toUpperCase() + str.substring(1);
  }
}
