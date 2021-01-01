package analyser;

import information.Symbol;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Stack;

/**
 * A tree of all the scopes and their symbols. Implemented as a tree instead of a stack so it can be reused for multiple compiler passes.
 * In addition to the common symbol table methods it also provides methods to traverse the tree in later passes.
 */
public class SymbolTable {

  public final Scope root;
  public Scope currScope;
  protected Stack<Integer> branches;

  public SymbolTable() {
    this.root = new Scope();
    this.currScope = root;
    this.branches = new Stack<>();
  }

  // LOOKUP METHODS
  public Symbol get(String name) {
    return get(name, currScope);
  }

  public Symbol get(String name, Scope start) {
    Scope scope = start;

    while (scope != null) {
      if (scope.symbols.containsKey(name))
        return scope.symbols.get(name);

      scope = scope.parent;
    }

    return null;
  }

  public boolean contains(String name) {
    Scope scope = currScope;

    while (scope != null) {
      if (scope.symbols.containsKey(name))
        return true;

      scope = scope.parent;
    }

    return false;
  }

  // DELETION METHODS
  public SymbolTable remove(String name) {
    Scope scope = currScope;

    while (scope != null) {
      if (scope.symbols.containsKey(name)) {
        scope.symbols.remove(name);
        return this;
      }

      scope = scope.parent;
    }

    return this;
  }

  // CREATION METHODS
  public SymbolTable openScope() {
    this.currScope = new Scope(currScope);
    return this;
  }

  public SymbolTable add(Symbol symbol) {
    currScope.symbols.put(symbol.name, symbol);
    return this;
  }

  public SymbolTable closeScope() {
    if (currScope.parent == null)
      throw new IllegalStateException("Scope has no parent! Cannot close the root scope!");

    currScope = currScope.parent;
    return this;
  }

  // TRAVERSING METHODS
  public SymbolTable resetCursor() {
    this.currScope = root;
    this.branches.clear();
    this.branches.push(0);
    return this;
  }

  public SymbolTable enterScope() {
    this.currScope = currScope.children.get(branches.peek());
    branches.push(0);
    return this;
  }

  public SymbolTable exitScope() {
    branches.pop();
    this.currScope = currScope.parent;
    branches.push( branches.pop() + 1 );
    return this;
  }



  public static class Scope {

    public final Scope parent;
    public final List<Scope> children;
    public LinkedHashMap<String, Symbol> symbols;

    public Scope() {
      this.parent = null;
      this.symbols = new LinkedHashMap<>();
      this.children = new ArrayList<>();
    }

    public Scope(Scope parent) {
      this.parent = parent;
      this.parent.children.add(this);

      this.symbols = new LinkedHashMap<>();
      this.children = new ArrayList<>();
    }

    public boolean contains(String name) {
      return symbols.containsKey(name);
    }
  }

}
