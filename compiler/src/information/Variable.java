package information;

import analyser.SymbolTable;

public class Variable extends Symbol {

  public static String SYMBOL_TYPE = "variable";

  public String dataType;
  public boolean isLocal;
  public Variable parent;

  protected Variable(String name, String dataType) {
    this(name, dataType, false, null);
  }

  public Variable(String name, String dataType, boolean isLocal) {
    this(name, dataType, isLocal, null);
  }

  protected Variable(String name, String dataType, boolean isLocal, Variable parent) {
    super(name, Variable.SYMBOL_TYPE);
    this.dataType = dataType;
    this.isLocal = isLocal;
    this.parent = parent;
  }

  public Variable selectElement() {
    if (!isArray()) return null;
    else return new Variable(name + "[]", dataType.substring(0, dataType.length() - "[]".length()), isLocal);
  }

  public Variable selectField(SymbolTable symbolTable, String field) {
    if (!symbolTable.get(dataType).is(Record.class)) return null;
    Record record = (Record)symbolTable.get(dataType);

    if (!record.fields.containsKey(field)) return null;
    return new Variable(name + "." + field, record.fields.get(field).dataType, isLocal, this);
  }

  public boolean isPrimitive() {
    return (dataType.equals(YaplConstants.INT) || dataType.equals(YaplConstants.BOOL));
  }

  public boolean isArray() {
    return dataType.endsWith("[]");
  }

  @Override
  public String toString() {
    return symbolType.toUpperCase() + " " + dataType + " " + name;
  }

}
