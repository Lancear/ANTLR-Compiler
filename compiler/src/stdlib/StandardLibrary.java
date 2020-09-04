package stdlib;

import analysis.SymbolTable;

/**
 * The base class for all {@code StandardLibrary} implementations. 
 * Its methods provide the symbols for the standard library methods and their target code.
 * It does not define the methods the standard library must provide to the yapl programmer to keep that flexible for the future.
 */
public abstract class StandardLibrary {
  
  public final String filename = "StandardLibrary";
  public final static StandardLibrary instance = null;
  protected StandardLibrary() { }

  public abstract void addToSymbolTable(SymbolTable symboltable);
  public abstract byte[] generate();

}
