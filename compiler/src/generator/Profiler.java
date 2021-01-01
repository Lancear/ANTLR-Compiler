package generator;

import analyser.SymbolTable;
import information.Variable;

public interface Profiler extends CodeGenerator {

  Profiler vardump(SymbolTable symboltable, int line);
  Profiler dumpScope(SymbolTable symboltable, SymbolTable.Scope scope);
  Profiler dumpSymbol(SymbolTable symboltable, Variable symbol);
  Profiler dumpTopOfStack(SymbolTable symboltable, Variable symbol);

}
