package symboltable.symbols;

import org.antlr.v4.runtime.Token;

public class ArrayVariableSymbol extends VariableSymbol {
  public final int dimensions;

  public ArrayVariableSymbol(String name, String type, int dimensions, boolean isParam, Token token) {
    super(name, type, isParam, token);

    if (dimensions < 1) 
      throw new IllegalArgumentException("An array-variable must at least have 1 dimension!");

    this.dimensions = dimensions;
  }
  
}
