package codegen;

import symboltable.symbols.ConstSymbol;
import symboltable.symbols.Symbol;
import symboltable.symbols.VariableSymbol;

public class CodeGenInfo {
  public String type = null;
  public String value = null;
  public Symbol symbol = null;

  public CodeGenInfo(String type) {
    this.type = type;
  }

  public CodeGenInfo(String type, String value) {
    this.type = type;
    this.value = value;
  }

  public CodeGenInfo(Symbol symbol) {
    this.symbol = symbol;

    if (symbol instanceof VariableSymbol) {
      this.type = ((VariableSymbol)symbol).type;
    }
    else if (symbol instanceof ConstSymbol) {
      this.type = ((ConstSymbol)symbol).type;
      this.value = ((ConstSymbol)symbol).value;
    }
  }
}
