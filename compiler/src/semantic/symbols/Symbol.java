package semantic.symbols;

import org.antlr.v4.runtime.Token;

public class Symbol {
  public final String kind;
  public final String name;
  public final Token token;

  public Symbol(String kind, String name, Token token) {
    this.kind = kind;
    this.name = name;
    this.token = token;
  }
}
