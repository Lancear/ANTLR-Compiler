package analysis;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public abstract class Symbol {

  public static String UNDEFINED = "<?>";
  public static String INT = "int";
  public static String BOOL = "bool";
  public static String VOID = "void";
  public static String STRING = "string";

  public String kind = UNDEFINED;
  public String name = UNDEFINED;
  public String type = UNDEFINED;

  public Symbol(String kind, String name, String type) {
    this.kind = kind;
    this.name = name;
    this.type = type;
  }

  public boolean isPrimitive() {
    return (type.equals("int") || type.equals("bool"));
  }

  public boolean isVariable() {
    return (this instanceof Variable);
  }

  public boolean isConst() {
    return (this instanceof Const);
  }

  public boolean isRecord() {
    return (this instanceof Record);
  }

  public boolean isArray() {
    return type.endsWith("[]");
  }

  public boolean isFunction() {
    return (this instanceof Function);
  }

  public boolean isError() {
    return (this instanceof Error);
  }

  public Variable asVariable() {
    return (Variable)this;
  }

  public Const asConst() {
    return (Const)this;
  }

  public Function asFunction() {
    return (Function)this;
  }

  public Record asRecord() {
    return (Record)this;
  }

  public Expression asExpression() {
    return (Expression)this;
  }

  public static class Const extends Symbol {

    public String value = UNDEFINED;
    
    public Const(String name, String type, String value) {
      super("constant", name, type);
      this.value = value;
    }

  }

  public static class Variable extends Symbol {

    public boolean isLocal;
    public String recordType = UNDEFINED;

    public Variable(String name, String type) {
      this(name, type, true);
    }

    public Variable(String name, String type, boolean isLocal) {
      super("variable", name, type);
      this.isLocal = isLocal;
    }

    protected Variable(String name, String type, boolean isLocal, String recordType) {
      this(name, type, isLocal);
      this.recordType = recordType;
    }

    protected Variable(String kind, String name, String type, boolean isLocal) {
      super(kind, name, type);
      this.isLocal = isLocal;
    }

    public Symbol selectField(SymbolTable symbolTable, String field) {
      if (!(symbolTable.get(type) instanceof Record)) 
        return new Error();

      Record record = (Record)symbolTable.get(type);

      if (!record.fields.containsKey(field))
        return new Error();

      return new Variable(name + "." + field, record.fields.get(field).type, isLocal, type);
    }

    public Variable selectElement() {
      if (!type.contains("[]"))
        return null;

      return new Variable(name + "[]", type.substring(0, type.length() - 2), isLocal);
    }

  }

  public static class Param extends Variable {

    public Param(String name, String type) {
      super("parameter", name, type, true);
    }

  }

  public static class Record extends Symbol {

    public final LinkedHashMap<String, Variable> fields;

    public Record(String name) {
      this(name, new LinkedHashMap<>());
    }

    public Record(String name, LinkedHashMap<String, Variable> fields) {
      super("record", name, name);
      this.fields = fields;
    }

  }

  public static class Function extends Symbol {

    public final List<Param> params;
    public final boolean isStdLib;

    public Function(String name, String type) {
      this(name, type, new ArrayList<>(), false);
    }

    public Function(String name, String type, List<Param> params) {
      this(name, type, params, false);
    }

    public Function(String name, String type, List<Param> params, boolean isStdLib) {
      super("procedure", name, type);
      this.params = params;
      this.isStdLib = isStdLib;
    }

  }

  public static String TYPE = "<type>";
  public static class Type extends Symbol {

    public Type(String type) {
      super("type", TYPE, type);
    }

  }

  public static String EXPRESSION = "<expression>";
  public static class Expression extends Symbol {

    public String value = UNDEFINED;

    public Expression(String type) {
      this(type, UNDEFINED);
    }

    public Expression(String type, String value) {
      super("expression", EXPRESSION, type);
      this.value = value;
    }

  }

  public static String ERROR = "<error>";
  public static class Error extends Symbol {

    public Error() {
      super("error", ERROR, ERROR);
    }

  }
  
}
