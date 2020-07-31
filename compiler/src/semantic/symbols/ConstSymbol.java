package semantic.symbols;

import org.antlr.v4.runtime.Token;

public class ConstSymbol extends Symbol {

  public final String type;

  public ConstSymbol(String name, String type, Token token) {
    super("constant", name, token);
    this.type = type;
  }
  
}
