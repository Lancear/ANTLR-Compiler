package profiler;

import analysis.Symbol;
import analysis.SymbolTable;
import analysis.SymbolTable.Scope;
import codegen.Backend;

public interface ProfilerBackend extends Backend {
  
  public abstract ProfilerBackend vardump(SymbolTable symboltable, int line);
  public abstract ProfilerBackend dumpScope(SymbolTable symboltable, Scope scope);
  public abstract ProfilerBackend dumpSymbol(SymbolTable symboltable, Symbol symbol);
  public abstract ProfilerBackend dumpTopOfStack(SymbolTable symboltable, Symbol symbol);

}
