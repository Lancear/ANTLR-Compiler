package codegen;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import jvm_class_generator.specs.JvmClass;
import jvm_class_generator.specs.attributes.Code;
import jvm_class_generator.specs.class_content.Method;
import jvm_class_generator.specs.data_areas.ConstantPool;
import jvm_class_generator.specs.helpers.AccessFlags;
import jvm_class_generator.specs.helpers.ArrayType;
import jvm_class_generator.specs.helpers.Descriptor;
import symboltable.symbols.ProcedureSymbol;
import symboltable.symbols.VariableSymbol;

public class JvmBackend extends Backend {
  public final static JvmBackend instance = new JvmBackend();

  protected JvmBackend() {
  }

  protected JvmClass program = null;
  protected ConstantPool consts = null;
  protected Method method = null;
  protected Code code = null;

  protected Stack<Map<String, Integer>> locals = new Stack<>();
  protected int nextLocal;

  protected Stack<Map<String, String>> labels = new Stack<>();
  protected int nextLabelId = 0;

  protected String booleanOp = null;

  public JvmBackend enterProgram(String name) {
    symbolTable.addSymbol( new ProcedureSymbol("write", "void", List.of("string"), null) );

    name = name.substring(0, 1).toUpperCase() + name.substring(1);
    program = new jvm_class_generator.impl.JvmClass(name, Descriptor.NAME_OF(Object.class), AccessFlags.PUBLIC | AccessFlags.SUPER);
    consts = program.constantPool();

    locals.push(new HashMap<>());
    nextLocal = 1;

    return this;
  }

  public void exitProgram() throws IOException {
    if (!Files.exists(outputDir)) {
      Files.createDirectories(outputDir);
    }

    Files.write( outputDir.resolve(stdlib.filename + ".class"), stdlib.generate() );
    Files.write( outputDir.resolve(program.name() + ".class"), program.generate() );
  }

  public JvmBackend enterMainFunction() {
    method = program.addMethod("main", Descriptor.MAIN, AccessFlags.PUBLIC | AccessFlags.STATIC);
    code = (Code)method.addAttribute("Code");
    code.addStackMapTableAttribute();

    labels.push(new HashMap<>());
    return this;
  }

  public JvmBackend exitMainFunction() {
    code.vreturn();

    method = null;
    code = null;
    locals.clear();
    labels.clear();
    return this;
  }

  public JvmBackend enterFunction(ProcedureSymbol symbol) {
    method = program.addMethod(symbol.name, getMethodDescriptor(symbol), AccessFlags.PUBLIC | AccessFlags.STATIC);
    code = (Code)method.addAttribute("Code");
    code.addStackMapTableAttribute();

    labels.clear();
    labels.push(new HashMap<>());
    
    locals.push(new HashMap<>());
    nextLocal = symbol.getParamTypes().size();
    return this;
  }

  public JvmBackend exitFunction() {
    code.vreturn();

    method = null;
    code = null;
    locals.clear();
    labels.clear();
    return this;
  }


  public JvmBackend loadConstant(String value, String type) {
    if (type.equals("string")) {
      code.ldc( consts.addString(value) );
    }
    else if (type.equals("bool")) {
      if (value.equals("True")) {
        code.iconst_1(); 
      }
      else {
        code.iconst_0();
      }

      if ("And".equals(booleanOp)) {
        code.ifeq(labels.peek().get("false"));
      }
      else if ("Or".equals(booleanOp)) {
        code.ifne(labels.peek().get("false"));
      }
    }
    else if (type.equals("int")) {
      int intValue = Integer.parseInt(value);

      if (intValue == -1) code.iconst_m1();
      else if (intValue == 0) code.iconst_0();
      else if (intValue == 1) code.iconst_1();
      else if (intValue == 2) code.iconst_2();
      else if (intValue == 3) code.iconst_3();
      else if (intValue == 4) code.iconst_4();
      else if (intValue == 5) code.iconst_5();
      else if ((intValue & 0x7f) == intValue) code.bipush(intValue);
      else if ((intValue & 0x7fff) == intValue) code.sipush(intValue);
      else code.ldc( consts.addInteger(intValue) );
    }

    return this;
  }

  public JvmBackend callFunction(String name) {
    ProcedureSymbol symbol = (ProcedureSymbol)symbolTable.get(name);
    boolean isStdLib = symbol.scope.isStandardLibrary;

    endBooleanExpression();
    code.invokeStatic( consts.addMethodref(isStdLib ? stdlib.filename : program.name(), name, getMethodDescriptor(symbol)) );
    return this;
  }

  public JvmBackend allocLocal(VariableSymbol symbol) {
    locals.peek().put(symbol.name, nextLocal++);
    return this;
  }

  public JvmBackend store(VariableSymbol symbol, boolean isArrayElement) {
    endBooleanExpression();

    if (isArrayElement) {
      if (symbol.type.startsWith("int")) code.iastore();
      else if (symbol.type.startsWith("bool")) code.bastore();
      return this;
    }

    final int local = getLocal(symbol.name);

    if (symbol.type.endsWith("[]")) {
      code.astore(local, local > 0xff);
      return this;
    }

    if (local == 0) code.istore_0();
    else if (local == 1) code.istore_1();
    else if (local == 2) code.istore_2();
    else if (local == 3) code.istore_3();
    else code.istore(local, local > 0xff);
    return this;
  }

  public JvmBackend load(VariableSymbol symbol, boolean isArrayElement) {
    final int local = getLocal(symbol.name);

    if (isArrayElement) {
      if (symbol.type.startsWith("int")) {
        code.iaload(); 
      }
      else if (symbol.type.startsWith("bool")) {
        code.baload();

        if ("And".equals(booleanOp)) {
          code.ifeq(labels.peek().get("false"));
        }
        else if ("Or".equals(booleanOp)) {
          code.ifne(labels.peek().get("false"));
        }
      }

      return this;
    }

    if (symbol.type.endsWith("[]")) {
      code.aload(local, local > 0xff);
      return this;
    }

    if (local == 0) code.iload_0();
    else if (local == 1) code.iload_1();
    else if (local == 2) code.iload_2();
    else if (local == 3) code.iload_3();
    else code.iload(local, local > 0xff);

    if (symbol.type.startsWith("bool")) {
      if ("And".equals(booleanOp)) {
        code.ifeq(labels.peek().get("false"));
      }
      else if ("Or".equals(booleanOp)) {
        code.ifne(labels.peek().get("false"));
      }
    }
    return this;
  }

  public JvmBackend op1(String operator) {
    if (operator.equals("-")) {
      code.ineg();
    }

    return this;
  }

  public JvmBackend op2(String operator) {
    switch (operator) {
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

      case "Or":
        if (booleanOp == null) {
          labels.peek().put("start", "" + nextLabelId++);
          labels.peek().put("true", "" + nextLabelId++);
          labels.peek().put("end", "" + nextLabelId++);
          code.addLabel(labels.peek().get("start"));
        }
        else if (booleanOp.equals("And")) {          
          code
            .iconst_1()
            .gotoLabel(labels.peek().get("end"))
            .addLabel(labels.peek().get("false"), labels.peek().get("start"));

          labels.peek().put("true", "" + nextLabelId++);
        }

        booleanOp = operator;
        break;

      case "And":
        if (booleanOp == null) {
          labels.peek().put("start", "" + nextLabelId++);
          labels.peek().put("false", "" + nextLabelId++);
          labels.peek().put("end", "" + nextLabelId++);
          code.addLabel(labels.peek().get("start"));
        }
        else if (booleanOp.equals("Or")) {          
          code
            .iconst_0()
            .gotoLabel(labels.peek().get("end"))
            .addLabel(labels.peek().get("true"), labels.peek().get("start"));

          labels.peek().put("false", "" + nextLabelId++);
        }

        booleanOp = operator;
        break;
    }

    return this;
  }

  public JvmBackend startCompareOp() {
    if (booleanOp == null) {
      labels.peek().put("start", "" + nextLabelId++);
      code.addLabel(labels.peek().get("start"));

      labels.peek().put("true", "" + nextLabelId++);
      labels.peek().put("end", "" + nextLabelId++);
    }

    return this;
  }

  public JvmBackend compareOp(String operator) {
    if ("And".equals(booleanOp)) {
      switch (operator) {
        case "==":
          code.if_icmpne(labels.peek().get("false"));
          break;

        case "!=":
          code.if_icmpeq(labels.peek().get("false"));
          break;

        case ">":
          code.if_icmple(labels.peek().get("false"));
          break;

        case "<":
          code.if_icmpge(labels.peek().get("false"));
          break;

        case ">=":
          code.if_icmplt(labels.peek().get("false"));
          break;

        case "<=":
          code.if_icmpgt(labels.peek().get("false"));
          break;
      }
    }
    else {
      switch (operator) {
        case "==":
          code.if_icmpeq(labels.peek().get("true"));
          break;

        case "!=":
          code.if_icmpne(labels.peek().get("true"));
          break;

        case ">":
          code.if_icmpgt(labels.peek().get("true"));
          break;

        case "<":
          code.if_icmplt(labels.peek().get("true"));
          break;

        case ">=":
          code.if_icmpge(labels.peek().get("true"));
          break;

        case "<=":
          code.if_icmple(labels.peek().get("true"));
          break;
      }

      if (booleanOp == null) {
        code
          .iconst_0()
          .gotoLabel(labels.peek().get("end"))
          .addLabel(labels.peek().get("true"), labels.peek().get("start"))
          .iconst_1()
          .addLabel(labels.peek().get("end"));
      }
    }

    return this;
  }

  public JvmBackend newArray(String baseType) {
    int arrType = "int".equals(baseType) ? ArrayType.INT : ArrayType.BOOLEAN;
    code.newArray(arrType);
    return this;
  }

  public JvmBackend arraylength() {
    code.arraylength();
    return this;
  }

  public JvmBackend ifThen() {
    labels.push(new HashMap<>());
    labels.peek().put("else", "" + nextLabelId++);
    code.ifeq(labels.peek().get("else"));    
    return this;
  }

  public JvmBackend elseThen() {
    labels.peek().put("endIf", "" + nextLabelId++);

    code
      .gotoLabel(labels.peek().get("endIf"))
      .addLabel(labels.peek().get("else"));
    return this;
  }

  public JvmBackend endIf() {
    if (labels.peek().containsKey("endIf")) {
      code.addLabel(labels.peek().get("endIf"));
    }
    else {
      code.addLabel(labels.peek().get("else"));
    }

    return this;
  }

  public JvmBackend startWhile() {
    labels.push(new HashMap<>());
    labels.peek().put("while", "" + nextLabelId++);
    labels.peek().put("endWhile", "" + nextLabelId++);
    code.addLabel(labels.peek().get("while"));
    return this;
  }

  public JvmBackend whileDo() {
    code.ifeq(labels.peek().get("endWhile"));
    return this;
  }

  public JvmBackend endWhile() {
    code
      .gotoLabel(labels.peek().get("while"))
      .addLabel(labels.peek().get("endWhile"), labels.peek().get("while"));
    return this;
  }

  public JvmBackend startBlock() {
    locals.push(new HashMap<>());
    return this;
  }

  public JvmBackend endBlock() {
    locals.pop();
    return this;
  }



  protected int getLocal(String localName) {
    final int topIdx = locals.size() - 1;

    for (int scopeIdx = topIdx; scopeIdx >= 0; scopeIdx--) {
      if (locals.get(scopeIdx).containsKey(localName))
        return locals.get(scopeIdx).get(localName);
    }

    return -1;
  }

  protected void endBooleanExpression() {
    if (booleanOp != null) {
      if (booleanOp.equals("And")) {          
        code
          .iconst_1()
          .gotoLabel(labels.peek().get("end"))
          .addLabel(labels.peek().get("false"), labels.peek().get("start"))
          .iconst_0()
          .addLabel(labels.peek().get("end"));
      }
      else if (booleanOp.equals("Or")) {          
        code
          .iconst_0()
          .gotoLabel(labels.peek().get("end"))
          .addLabel(labels.peek().get("true"), labels.peek().get("start"))
          .iconst_1()
          .addLabel(labels.peek().get("end"));
      }

      booleanOp = null;
    }
  }

  protected String getMethodDescriptor(ProcedureSymbol symbol) {
    String returnDescriptor = getTypeDescriptor(symbol.returnType);
    String[] paramDescriptors = new String[ symbol.getParamTypes().size() ];

    for (int idx = 0; idx < paramDescriptors.length; idx++) {
      paramDescriptors[idx] = getTypeDescriptor(symbol.getParamType(idx));
    }

    return Descriptor.METHOD(returnDescriptor, paramDescriptors);
  }

  protected String getTypeDescriptor(String type) {
    switch (type) {
      case "int":
        return Descriptor.INT;

      case "bool":
        return Descriptor.BOOLEAN;

      case "string":
        return Descriptor.STRING;

      case "void":
        return Descriptor.VOID;
    }

    return "?";
  }

}
