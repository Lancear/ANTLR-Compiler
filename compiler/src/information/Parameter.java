package information;

public class Parameter extends Variable {

  public static String SYMBOL_TYPE = "parameter";

  public Parameter(String name, String dataType) {
    super(name, dataType, true);
    this.symbolType = Parameter.SYMBOL_TYPE;
  }

  @Override
  public String toString() {
    return symbolType.toUpperCase() + " " + dataType + " " + name;
  }

}
