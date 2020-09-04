package analysis;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * This is the base class of all identifiers as well as some type/value information of expressions.
 * This class is used for passing around information inside the compiler, everything inside the compiler works with symbols.
 */
public abstract class Symbol {

  /**
   * UNDEFINED allows me to use {@code .equals()} without the chance of a {@code NullReferenceException}.
   * This can be done since identifiers in YAPL cannot contain those pointy brackets.
   */
  public static final String UNDEFINED = "<?>";
  public static final String INT = "int";
  public static final String BOOL = "bool";
  public static final String VOID = "void";
  public static final String STRING = "string";
  public static final String TRUE = "True";
  public static final String FALSE = "False";

  public String kind = UNDEFINED;
  public String name = UNDEFINED;
  public String type = UNDEFINED;

  public Symbol(String kind, String name, String type) {
    this.kind = kind;
    this.name = name;
    this.type = type;
  }

  @Override
  public String toString() {
    return kind.toUpperCase() + " " + name + ", type: " + type;
  }

  public boolean isPrimitive() {
    return (type.equals("int") || type.equals("bool"));
  }

  public boolean isVariable() {
    return (this instanceof Variable);
  }

  public boolean isParam() {
    return (this instanceof Param);
  }

  public boolean isConst() {
    return (this instanceof Const && !this.asConst().value.equals(UNDEFINED));
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

  public boolean isExpression() {
    return (this instanceof Expression);
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

    protected Const(String kind, String name, String type, String value) {
      super(kind, name, type);
      this.value = value;
    }

    @Override
    public String toString() {
      return super.toString() + ", value: " + value;
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

    @Override
    public String toString() {
      return super.toString() + ", isLocal: " + isLocal;
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
    public boolean isPure = false;
    public int uses = 0;

    public Function(String name, String type) {
      this(name, type, new ArrayList<>(), false);
    }

    public Function(String name, String type, boolean isPure) {
      super("procedure", name, type);
      this.params = new ArrayList<>();
      this.isPure = isPure;
      this.isStdLib = false;
    }

    public Function(String name, String type, List<Param> params, boolean isStdLib) {
      super("procedure", name, type);
      this.params = params;
      this.isStdLib = isStdLib;
      this.isPure = isStdLib;
    }

    @Override
    public String toString() {
      return super.toString() + ", isPure: " + isPure + ", uses: " + uses;
    }

  }

  /**
   * Used to pass around type information between methods of the {@code Analysis}
   */
  public static String TYPE = "<type>";
  public static class Type extends Symbol {

    public Type(String type) {
      super("type", TYPE, type);
    }

  }

  /**
   * Used for constant folding and passing types of expression between methods of the {@code Analysis}
   */
  public static String EXPRESSION = "<expression>";
  public static class Expression extends Const {

    public Expression(String type) {
      this(type, UNDEFINED);
    }

    public Expression(String type, String value) {
      super("expression", EXPRESSION, type, value);
    }

    public Expression(String name, String type, String value) {
      super("expression", name, type, value);
    }

    public static String nameFor(int expressionHash, String text) {
      return "<" + expressionHash + ":" + text + ">";
    }

    @Override
    public String toString() {
      String textPart = name.split(":")[1];
      String text = textPart.substring(0, textPart.length() - 1);
      return kind.toUpperCase() + " " + text + ", type: " + type + ", value: " + value;
    }
  }

  /**
   * Used to indicate a child node had an error, so the parent node can skipp all errors related to that error.
   */
  public static String ERROR = "<error>";
  public static class Error extends Symbol {

    public Error() {
      super("error", ERROR, ERROR);
    }

  }
  
}
