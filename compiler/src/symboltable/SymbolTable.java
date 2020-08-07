package symboltable;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import symboltable.symbols.Symbol;

public class SymbolTable {
  
  protected Stack<Scope> scopes;

  public class Scope {
    public final Symbol parent;
    public final Map<String, Symbol> symbols;
    public final boolean isStandardLibrary;

    public Scope() {
      this.symbols = new HashMap<>();
      this.parent = null;
      this.isStandardLibrary = false;
    }

    public Scope(boolean isStandardLibrary) {
      this.symbols = new HashMap<>();
      this.parent = null;
      this.isStandardLibrary = isStandardLibrary;
    }

    public Scope(Symbol parent) {
      this.symbols = new HashMap<>();
      this.parent = parent;
      this.isStandardLibrary = false;
    }
  }

  public SymbolTable() {
    this.scopes = new Stack<>();
  }

  public void openScope() {
    scopes.push( new Scope() );
  }

  public void openScope(boolean isStandardLibrary) {
    scopes.push( new Scope(isStandardLibrary) );
  }

  public void openScope(Symbol parent) {
    scopes.push( new Scope(parent) );
  }

  public void closeScope() {
    scopes.pop();
  }

  public void addSymbol(Symbol symbol) {
    symbol.scope = scopes.peek();
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
