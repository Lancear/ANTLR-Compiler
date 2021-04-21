package information;

/**
 * This is the base class of all identifiers as well as some type/value information of expressions.
 * This class is used for passing around information inside the compiler, everything inside the compiler works with symbols.
 */
public abstract class Symbol extends Information {

  public String name = YaplConstants.UNDEFINED;
  public String symbolType = YaplConstants.UNDEFINED;

  public Symbol(String name, String symbolType) {
    this.name = name;
    this.symbolType = symbolType;
  }

  public boolean isUserDefinedVariable() {
    return (this instanceof Variable && !(this instanceof ConstantExpression) && !(this instanceof Expression));
  }

  @Override
  public String toString() {
    return symbolType.toUpperCase() + " " + name;
  }

}
