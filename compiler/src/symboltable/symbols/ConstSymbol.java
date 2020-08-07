package symboltable.symbols;

import org.antlr.v4.runtime.Token;

public class ConstSymbol extends Symbol {

  public final String type;
  public final String value;

  public ConstSymbol(String name, String type, String value, Token token) {
    super("constant", name, token);
    this.type = type;
    this.value = value;
  }
  
}
