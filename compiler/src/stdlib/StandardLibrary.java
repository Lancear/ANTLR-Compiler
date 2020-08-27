package stdlib;

import analysis.SymbolTable;

public abstract class StandardLibrary {
  
  public final String filename = "StandardLibrary";
  public final static StandardLibrary instance = null;

  public abstract void addToSymbolTable(SymbolTable symboltable);
  public abstract byte[] generate();

}
