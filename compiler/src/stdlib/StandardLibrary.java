package stdlib;

import analysis.SymbolTable;

/**
 * Standard library including {@code Write <string>}, but
 * {@code Write <string>} should not be added to the symbolTable.
 */
public abstract class StandardLibrary {
  
  public final String filename = "StandardLibrary";
  public final static StandardLibrary instance = null;

  public abstract void addToSymbolTable(SymbolTable symboltable);
  public abstract byte[] generate();

}
