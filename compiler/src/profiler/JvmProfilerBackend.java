package profiler;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;

import analysis.Symbol;
import analysis.SymbolTable;
import analysis.SymbolTable.Scope;
import codegen.JvmBackend;
import jvm_class_generator.specs.helpers.Descriptor;
import stdlib.StandardLibrary;

public class JvmProfilerBackend extends JvmBackend implements ProfilerBackend {
  
  public JvmProfilerBackend(StandardLibrary stdlib, Path outputDir) {
    super(stdlib, outputDir);
  }

  public JvmProfilerBackend vardump(SymbolTable symboltable, int line) {
    loadConstant(new Symbol.Expression(Symbol.STRING, "[PROFILER, line " + line + "] ================================================")).write();
    callFunction(symboltable.get("writeln").asFunction());
    vardump(symboltable, symboltable.currScope);
    return this;
  }

  protected int vardump(SymbolTable symboltable, Scope scope) {
    if (scope.parent.parent == null) {
      loadConstant(new Symbol.Expression(Symbol.STRING, "Globals:")).write();
      callFunction(symboltable.get("writeln").asFunction());
      
      dumpScope(symboltable, scope);
      callFunction(symboltable.get("writeln").asFunction());
      return 0;
    }

    final int shouldVardump = vardump(symboltable, scope.parent);

    if (shouldVardump < 2) {
      if (!program.hasMethod("main", Descriptor.MAIN) && shouldVardump == 0) {
        loadConstant(new Symbol.Expression(Symbol.STRING, "Params:")).write();
        callFunction(symboltable.get("writeln").asFunction());
        
        dumpScope(symboltable, scope);
        callFunction(symboltable.get("writeln").asFunction());
        return 1;
      }
      else {
        loadConstant(new Symbol.Expression(Symbol.STRING, "Locals:")).write();
        callFunction(symboltable.get("writeln").asFunction());
        
        dumpScope(symboltable, scope);
        callFunction(symboltable.get("writeln").asFunction());
        return 2;
      }
    }

    return 2;
  }

  public JvmProfilerBackend dumpScope(SymbolTable symboltable, Scope scope) {
    for (Symbol sym : scope.symbols.values()) {
      if (!sym.isExpression() && !sym.isFunction() && !sym.isRecord()) {
        dumpSymbol(symboltable, sym);
      }
    }

    return this;
  }

  public JvmProfilerBackend dumpSymbol(SymbolTable symboltable, Symbol sym) {
    loadConstant(new Symbol.Expression(Symbol.STRING, sym.type + " ")).write();
    loadConstant(new Symbol.Expression(Symbol.STRING, sym.name + ": ")).write();
      
    if (sym.isConst()) {
      loadConstant( new Symbol.Expression(Symbol.STRING, sym.asConst().value) ).write();
    }
    else {
      load(sym.asVariable());
      
      if (sym.isPrimitive()) {
        dumpPrimitive(symboltable, sym);
      }
      else if(sym.isArray()) {
        dumpArray(symboltable, sym);
      }
      else {
        dumpObject(symboltable, sym);
      }
    }

    callFunction(symboltable.get("writeln").asFunction());
    return this;
  }

  public JvmProfilerBackend dumpTopOfStack(SymbolTable symboltable, Symbol sym) {
    code.dup();

    if (sym.isPrimitive()) {
      dumpPrimitive(symboltable, sym);
    }
    else if(sym.isArray()) {
      dumpArray(symboltable, sym);
    }
    else {
      dumpObject(symboltable, sym);
    }

    return this;
  }

  protected void dumpPrimitive(SymbolTable symboltable, Symbol sym) {
    if (sym.type.equals("int")) callFunction(symboltable.get("writeint").asFunction());
    else callFunction(symboltable.get("writebool").asFunction());
  }

  protected void dumpArray(SymbolTable symboltable, Symbol sym) {
    String Arrays = Descriptor.NAME_OF(Arrays.class);
    int toString;

    if (sym.asVariable().selectElement().isArray()) {
      toString = consts.addMethodref(Arrays, "deepToString", Descriptor.METHOD(Descriptor.STRING, Descriptor.ARRAY(Descriptor.OBJECT)));
    }
    else if (sym.asVariable().selectElement().type.equals(Symbol.INT)) {
      toString = consts.addMethodref(Arrays, "toString", Descriptor.METHOD(Descriptor.STRING, Descriptor.ARRAY(Descriptor.INT)));
    }
    else if (sym.asVariable().selectElement().type.equals(Symbol.BOOL)) {
      toString = consts.addMethodref(Arrays, "toString", Descriptor.METHOD(Descriptor.STRING, Descriptor.ARRAY(Descriptor.BOOLEAN)));
    }
    else {
      toString = consts.addMethodref(Arrays, "toString", Descriptor.METHOD(Descriptor.STRING, Descriptor.ARRAY(Descriptor.OBJECT)));
    }

    code.invokeStatic(toString);
    write();
  }

  protected void dumpObject(SymbolTable symboltable, Symbol sym) {
    final int obj2str = consts.addMethodref(Descriptor.NAME_OF(Objects.class), "toString", Descriptor.METHOD(Descriptor.STRING, Descriptor.OBJECT));
    code.invokeStatic(obj2str);
    write();
  }

}
