package symboltable.symbols;

import org.antlr.v4.runtime.Token;

public class VariableSymbol extends Symbol {

  public final String type;

  public VariableSymbol(String name, String type, boolean isParam, Token token) {
    super(isParam ? "parameter" : "variable", name, token);
    this.type = type;
  }
  
}
