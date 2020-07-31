package semantic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import semantic.symbols.ProcedureSymbol;
import semantic.symbols.Symbol;

public class SymbolTable {
  
  protected Stack<Scope> scopes;

  protected class Scope {
    public final Symbol parent;
    public final Map<String, Symbol> symbols;

    public Scope() {
      this.symbols = new HashMap<>();
      this.parent = null;
    }

    public Scope(Symbol parent) {
      this.symbols = new HashMap<>();
      this.parent = parent;
    }
  }

  public SymbolTable() {
    this.scopes = new Stack<>();
    openScope();

    addSymbol( new ProcedureSymbol("writeint", "void", List.of("int"), null) );
    addSymbol( new ProcedureSymbol("writebool", "void", List.of("bool"), null) );
    addSymbol( new ProcedureSymbol("writeln", "void", List.of(), null) );
    addSymbol( new ProcedureSymbol("readint", "int", List.of(), null) );
  }

  public void openScope() {
    scopes.push( new Scope() );
  }

  public void openScope(Symbol parent) {
    scopes.push( new Scope(parent) );
  }

  public void closeScope() {
    scopes.pop();
  }

  public void addSymbol(Symbol symbol) {
    scopes.peek().symbols.put(symbol.name, symbol);
  }

  public boolean contains(String name) {
    final int topIdx = scopes.size() - 1;

    for (int scopeIdx = topIdx; scopeIdx >= 0; scopeIdx--) {
      if (scopes.get(scopeIdx).symbols.containsKey(name))
        return true;
    }

    return false;
  }

  public boolean containsInScope(String name) {
    return scopes.peek().symbols.containsKey(name);
  }

  public Symbol get(String name) {
    final int topIdx = scopes.size() - 1;

    for (int scopeIdx = topIdx; scopeIdx >= 0; scopeIdx--) {
      if (scopes.get(scopeIdx).symbols.containsKey(name))
        return scopes.get(scopeIdx).symbols.get(name);
    }

    return null;
  }

}
