package codegen;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import analysis.Symbol;
import jvm_class_generator.specs.JvmClass;
import jvm_class_generator.specs.attributes.Code;
import jvm_class_generator.specs.attributes.InnerClasses;
import jvm_class_generator.specs.class_content.Method;
import jvm_class_generator.specs.data_areas.ConstantPool;
import jvm_class_generator.specs.helpers.AccessFlags;
import jvm_class_generator.specs.helpers.ArrayType;
import jvm_class_generator.specs.helpers.Descriptor;

public class JvmBackend extends Backend {
  public final static JvmBackend instance = new JvmBackend();

  protected final String UNDEFINED = "<?>";
  protected final String AND = "And";
  protected final String OR = "Or";
  protected final String START = "start";
  protected final String END = "end";

  protected JvmBackend() {
  }

  protected JvmClass program = null;
  protected ConstantPool consts = null;
  protected Method method = null;
  protected Code code = null;
  protected InnerClasses innerClasses = null;
  protected JvmClass record = null;
  protected List<JvmClass> records = new ArrayList<>();
  protected Stack<String> boolOperators = new Stack<>();
  protected Stack<Integer> boolOperandsCount = new Stack<>();

  protected Stack<Map<String, String>> labels;
  protected Map<Symbol.Variable, Integer> locals;

  public JvmBackend enterProgram(String name) {
    this.program = new jvm_class_generator.impl.JvmClass(capitalise(name), Descriptor.NAME_OF(Object.class), AccessFlags.PUBLIC | AccessFlags.SUPER);
    this.consts = program.constantPool();
    return this;
  }

  public void exitProgram() throws IOException {
    if (!Files.exists(outputDir)) {
      Files.createDirectories(outputDir);
    }

    Files.write( outputDir.resolve(stdlib.filename + ".class"), stdlib.generate() );

    for (JvmClass record : records)
      Files.write( outputDir.resolve(record.name() + ".class"), record.generate() );

    Files.write( outputDir.resolve(program.name() + ".class"), program.generate() );
  }

  public JvmBackend enterMainFunction() {
    this.method = program.addMethod("main", Descriptor.MAIN, AccessFlags.PUBLIC | AccessFlags.STATIC);
    this.code = (Code)method.addAttribute("Code");
    code.addStackMapTableAttribute();

    this.labels = new Stack<>();
    labels.push(new HashMap<>());
    this.nextLabelId = 0;

    this.locals = new HashMap<>();
    this.nextLocalId = 0;
    nextLocal(); // args 
    return this;
  }

  public JvmBackend exitMainFunction() {
    code.vreturn();
    this.method = null;
    this.code = null;

    labels.pop();
    return this;
  }

  public JvmBackend enterFunction(Symbol.Function sym) {
    this.method = program.addMethod(sym.name, getMethodDescriptor(sym), AccessFlags.PUBLIC | AccessFlags.STATIC);
    this.code = (Code)method.addAttribute("Code");
    code.addStackMapTableAttribute();

    this.labels = new Stack<>();
    labels.push(new HashMap<>());
    this.nextLabelId = 0;

    this.locals = new HashMap<>();
    this.nextLocalId = 0;

    for (Symbol.Param param : sym.params)
      locals.put(param, nextLocal());

    return this;
  }

  public JvmBackend exitFunction() {
    final String returnDescriptor = Descriptor.METHOD_RETURN_DESCRIPTOR( method.descriptor() );
    if (returnDescriptor.equals(Descriptor.VOID))
      code.vreturn();

    method = null;
    code = null;
    return this;
  }

  public JvmBackend enterRecord(String name) {
    this.record = new jvm_class_generator.impl.JvmClass(getFullRecordName(name), Descriptor.NAME_OF(Object.class), AccessFlags.PUBLIC | AccessFlags.SUPER);
    return this;
  }

  public JvmBackend exitRecord() {
    Method init = record.addMethod("<init>", Descriptor.METHOD(Descriptor.VOID), AccessFlags.PUBLIC);
    ((Code)init.addAttribute("Code"))
      .aload_0()
      .invokeSpecial( record.constantPool().addMethodref(Descriptor.NAME_OF(Object.class), "<init>", Descriptor.METHOD(Descriptor.VOID)) )
      .vreturn();
    
    if (innerClasses == null)
      this.innerClasses = (InnerClasses)program.addAttribute("InnerClasses");
    
    this.records.add(record);
    this.innerClasses.add(record, record.name());
    this.record = null;
    return this;
  }

  public JvmBackend loadConstant(Symbol.Const sym) {
    if (sym.type.equals(Symbol.STRING)) {
      code.ldc( consts.addString(sym.value) );
      return this;
    }
    
    if (sym.type.equals(Symbol.BOOL)) {
      if (sym.value.equals(Symbol.TRUE)) code.iconst_1();
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

  public JvmBackend allocVariable(Symbol.Variable sym) {
    if (!sym.isLocal) {
      if (record != null) record.addField(sym.name, getTypeDescriptor(sym.type), AccessFlags.PUBLIC);
      else program.addField(sym.name, getTypeDescriptor(sym.type), AccessFlags.PUBLIC | AccessFlags.STATIC);
    }

    return this;
  }

  public JvmBackend store(Symbol.Variable sym) {
    if (sym.name.endsWith("[]")) {
      if (sym.type.equals(Symbol.BOOL)) code.bastore();
      else if (sym.type.equals(Symbol.INT)) code.iastore();
      else code.aastore();
    }
    else if (sym.name.contains(".")) {
      final String record = getFullRecordName(sym.recordType);
      final String[] name = sym.name.split("\\.");
      code.putField( consts.addFieldref(record, name[name.length - 1], getTypeDescriptor(sym.type)) );
    }
    else {
      if (sym.isLocal) {
        if (!locals.containsKey(sym))
          locals.put(sym, nextLocal());

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
        final int fieldRef = consts.addFieldref(program.name(), sym.name, getTypeDescriptor(sym.type));
        code.putStatic(fieldRef);
      }
    }
    
    return this;
  }

  public JvmBackend load(Symbol.Variable sym) {
    if (sym.name.endsWith("[]")) {
      if (sym.type.equals(Symbol.BOOL)) code.baload();
      else if (sym.type.equals(Symbol.INT)) code.iaload();
      else code.aaload();
    }
    else if (sym.name.contains(".")) {
      final String record = getFullRecordName(sym.recordType);
      final String[] name = sym.name.split("\\.");
      code.getField( consts.addFieldref(record, name[name.length - 1], getTypeDescriptor(sym.type)) );
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
        final int fieldRef = consts.addFieldref(program.name(), sym.name, getTypeDescriptor(sym.type));
        code.getStatic(fieldRef);
      }
    }

    if (sym.type.equals(Symbol.BOOL) && boolOperators.size() > 0) connectBoolOperand();
    return this;
  }

  public JvmBackend write() {
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

  public JvmBackend callFunction(Symbol.Function fn) {
    final String className = fn.isStdLib ? stdlib.filename : program.name();
    final int methodRef = consts.addMethodref(className, fn.name, getMethodDescriptor(fn));
    code.invokeStatic(methodRef);

    if (fn.type.equals(Symbol.BOOL) && boolOperators.size() > 0) connectBoolOperand();
    return this;
  }

  public JvmBackend op1(String operator) {
    if (operator.equals("-")) {
      code.ineg();
    }

    return this;
  }

  public JvmBackend startBranchingBlock() {
    labels.push(new HashMap<>());

    final String startLbl = nextLabel();
    labels.peek().put(START, startLbl);
    code.addLabel(startLbl);

    labels.peek().put(END, nextLabel());
    return this;
  }

  public JvmBackend endBranchingBlock() {
    code.addLabel(labels.peek().get(END), labels.peek().get(START));
    labels.pop();
    return this;
  }

  public JvmBackend op2(String op) {
    String resetlbl, truelbl, endlbl;

    switch (op) {
      case "+":
        code.iadd();
        break;

      case "-":
        code.isub();
        break;

      case "*":
        code.imul();
        break;

      case "/":
        code.idiv();
        break;

      case "%":
        code.irem();
        break;

      case OR:
        boolOperators.push(OR);
        boolOperandsCount.push(0);
        labels.push(new HashMap<>());

        labels.peek().put(START, nextLabel());
        labels.peek().put(Symbol.TRUE, nextLabel());
        labels.peek().put(END, nextLabel());

        code.addLabel(labels.peek().get(START));
        break;

      case AND:
        boolOperators.push(AND);
        boolOperandsCount.push(0);
        labels.push(new HashMap<>());

        labels.peek().put(START, nextLabel());
        labels.peek().put(Symbol.FALSE, nextLabel());
        labels.peek().put(END, nextLabel());

        code.addLabel(labels.peek().get(START));
        break;

      case "==":
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
        break;

      case "!=":
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
        break;

      case ">":
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
        break;

      case "<":
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
        break;

      case ">=":
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
        break;

      case "<=":
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
        break;
    }

    return this;
  }

  public JvmBackend newArray(String baseType, int dimensions) {
    if (dimensions > 1) {
      String descriptor = getBaseTypeDescriptor(baseType);

      for (int i = 0; i < dimensions; i++)
        descriptor = Descriptor.ARRAY(descriptor);

      code.multianewArray( consts.addClass(descriptor), dimensions );
    }
    else if (baseType.equals(Symbol.INT) || baseType.equals(Symbol.BOOL)) {
      int arrType = baseType.equals(Symbol.INT) ? ArrayType.INT : ArrayType.BOOLEAN;
      code.newArray(arrType);
    }
    else {
      code.anewArray( consts.addClass(getFullRecordName(baseType)) );
    }

    return this;
  }

  public JvmBackend arraylength() {
    code.arraylength();
    return this;
  }

  public JvmBackend branch() {
    code.ifeq(labels.peek().get(END));
    return this;
  }

  public JvmBackend elseBranch() {
    String elseLbl = labels.peek().get(END);
    String endLbl = nextLabel();
    labels.peek().put(END, endLbl);

    code
      .gotoLabel(endLbl)
      .addLabel(elseLbl);

    return this;
  }

  public JvmBackend loop() {
    code.gotoLabel(labels.peek().get(START));
    return this;
  }

  @Override
  public Backend returnFunction() {
    final String returnDescriptor = Descriptor.METHOD_RETURN_DESCRIPTOR( method.descriptor() );

    switch (returnDescriptor) {
      case Descriptor.BOOLEAN:
      case Descriptor.INT:
        code.ireturn();
        break;

      case Descriptor.VOID:
        code.vreturn();
        break;

      default:
        code.areturn();
    }

    return this;
  }

  public JvmBackend newRecord(String type) {
    final String name = getFullRecordName(type);

    code
      .anew( consts.addClass( getFullRecordName(type) ) )
      .dup()
      .invokeSpecial( consts.addMethodref(name, "<init>", Descriptor.METHOD(Descriptor.VOID)) );

    return this;
  }



  protected void connectBoolOperand() {
    int operandNr = boolOperandsCount.pop() + 1;
    boolOperandsCount.push(operandNr);

    if (boolOperators.peek().equals(OR)) code.ifne(labels.peek().get(Symbol.TRUE));
    else code.ifeq(labels.peek().get(Symbol.FALSE));

    if (operandNr == 2) {
      if (boolOperators.peek().equals(OR)) {
        code
          .iconst_0()
          .gotoLabel(labels.peek().get(END))
          .addLabel(labels.peek().get(Symbol.TRUE), labels.peek().get(START))
          .iconst_1()
          .addLabel(labels.peek().get(END));
      }
      else {
        code
          .iconst_1()
          .gotoLabel(labels.peek().get(END))
          .addLabel(labels.peek().get(Symbol.FALSE), labels.peek().get(START))
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

  protected int nextLocalId = 0;
  protected int nextLocal() {
    return nextLocalId++;
  }

  protected String getMethodDescriptor(Symbol.Function sym) {
    String returnDescriptor = getTypeDescriptor(sym.type);
    String[] paramDescriptors = new String[ sym.params.size() ];

    for (int idx = 0; idx < paramDescriptors.length; idx++) {
      paramDescriptors[idx] = getTypeDescriptor( sym.params.get(idx).type );
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
    switch (type) {
      case Symbol.INT:
        return Descriptor.INT;

      case Symbol.BOOL:
        return Descriptor.BOOLEAN;

      case Symbol.STRING:
        return Descriptor.STRING;

      case Symbol.VOID:
        return Descriptor.VOID;

      default:
        return Descriptor.REFERENCE(getFullRecordName(type));
    }
  }

  protected String getFullRecordName(String record) {
    return program.name() + "$" + capitalise(record);
  }

  protected String capitalise(String str) {
    return str.substring(0, 1).toUpperCase() + str.substring(1);
  }

}
