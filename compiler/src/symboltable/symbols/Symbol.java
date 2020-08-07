package symboltable.symbols;

import org.antlr.v4.runtime.Token;

import symboltable.SymbolTable.Scope;

public class Symbol {
  public final String kind;
  public final String name;
  public final Token token;
  public Scope scope = null;

  public Symbol(String kind, String name, Token token) {
    this.kind = kind;
    this.name = name;
    this.token = token;
  }
}
