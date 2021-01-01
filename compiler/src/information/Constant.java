package information;

public class Constant extends Variable {

  public static String SYMBOL_TYPE = "constant";

  public String value;

  protected Constant(String name, String dataType, String value) {
    this(name, dataType, value, false);
  }

  public Constant(String name, String dataType, String value, boolean isLocal) {
    super(name, dataType);
    this.isLocal = isLocal;
    this.value = value;
    this.symbolType = Constant.SYMBOL_TYPE;
  }

  @Override
  public String toString() {
    return symbolType.toUpperCase() + " " + dataType + " " + name + " = " + value;
  }

}
