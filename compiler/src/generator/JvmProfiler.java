package generator;

import analyser.SymbolTable;
import information.YaplConstants;
import jvm_class_generator.specs.helpers.Descriptor;
import stdlib.StandardLibrary;
import information.*;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;

public class JvmProfiler extends JvmCodeGenerator implements Profiler {

  public JvmProfiler(StandardLibrary stdlib, Path outputDir) {
    super(stdlib, outputDir);
  }

  public JvmProfiler vardump(SymbolTable symboltable, int line) {
    loadConstant(new ConstantExpression(null, YaplConstants.STRING, "[PROFILER, line " + line + "] ================================================")).write();
    callFunction(symboltable.get("writeln").as(Procedure.class));
    vardump(symboltable, symboltable.currScope);
    return this;
  }

  public JvmProfiler dumpScope(SymbolTable symboltable, SymbolTable.Scope scope) {
    for (Symbol sym : scope.symbols.values()) {
      if (sym.isUserDefinedVariable()) {
        dumpSymbol(symboltable, sym.as(Variable.class));
      }
    }

    return this;
  }

  public JvmProfiler dumpSymbol(SymbolTable symboltable, Variable sym) {
    loadConstant(new ConstantExpression(null, YaplConstants.STRING, sym.dataType + " ")).write();
    loadConstant(new ConstantExpression(null, YaplConstants.STRING, sym.name + ": ")).write();

    if (sym.is(Constant.class)) {
      loadConstant( new ConstantExpression(null, YaplConstants.STRING, sym.as(Constant.class).value) ).write();
    }
    else {
      load(sym);

      if (sym.isPrimitive()) {
        dumpPrimitive(symboltable, sym);
      }
      else if(sym.isArray()) {
        dumpArray(sym);
      }
      else {
        dumpObject(sym);
      }
    }

    callFunction(symboltable.get("writeln").as(Procedure.class));
    return this;
  }

  public JvmProfiler dumpTopOfStack(SymbolTable symboltable, Variable sym) {
    code.dup();

    if (sym.isPrimitive()) {
      dumpPrimitive(symboltable, sym);
    }
    else if(sym.isArray()) {
      dumpArray(sym);
    }
    else {
      dumpObject(sym);
    }

    return this;
  }



  protected int vardump(SymbolTable symboltable, SymbolTable.Scope scope) {
    if (scope.parent.parent == null) {
      loadConstant(new ConstantExpression(null, YaplConstants.STRING, "Globals:")).write();
      callFunction(symboltable.get("writeln").as(Procedure.class));

      dumpScope(symboltable, scope);
      callFunction(symboltable.get("writeln").as(Procedure.class));
      return 0;
    }

    final int shouldVardump = vardump(symboltable, scope.parent);

    if (shouldVardump < 2) {
      if (!program.hasMethod("main", Descriptor.MAIN) && shouldVardump == 0) {
        loadConstant(new ConstantExpression(null, YaplConstants.STRING, "Params:")).write();
        callFunction(symboltable.get("writeln").as(Procedure.class));

        dumpScope(symboltable, scope);
        callFunction(symboltable.get("writeln").as(Procedure.class));
        return 1;
      }
      else {
        loadConstant(new ConstantExpression(null, YaplConstants.STRING, "Locals:")).write();
        callFunction(symboltable.get("writeln").as(Procedure.class));

        dumpScope(symboltable, scope);
        callFunction(symboltable.get("writeln").as(Procedure.class));
        return 2;
      }
    }

    return 2;
  }

  protected void dumpPrimitive(SymbolTable symboltable, Variable sym) {
    if (sym.dataType.equals("int")) callFunction(symboltable.get("writeint").as(Procedure.class));
    else callFunction(symboltable.get("writebool").as(Procedure.class));
  }

  protected void dumpArray(Variable sym) {
    String Arrays = Descriptor.NAME_OF(Arrays.class);
    int toString;

    if (sym.selectElement().isArray()) {
      toString = consts.addMethodref(Arrays, "deepToString", Descriptor.METHOD(Descriptor.STRING, Descriptor.ARRAY(Descriptor.OBJECT)));
    }
    else if (sym.selectElement().dataType.equals(YaplConstants.INT)) {
      toString = consts.addMethodref(Arrays, "toString", Descriptor.METHOD(Descriptor.STRING, Descriptor.ARRAY(Descriptor.INT)));
    }
    else if (sym.selectElement().dataType.equals(YaplConstants.BOOL)) {
      toString = consts.addMethodref(Arrays, "toString", Descriptor.METHOD(Descriptor.STRING, Descriptor.ARRAY(Descriptor.BOOLEAN)));
    }
    else {
      toString = consts.addMethodref(Arrays, "toString", Descriptor.METHOD(Descriptor.STRING, Descriptor.ARRAY(Descriptor.OBJECT)));
    }

    code.invokeStatic(toString);
    write();
  }

  protected void dumpObject(Symbol sym) {
    final int obj2str = consts.addMethodref(Descriptor.NAME_OF(Objects.class), "toString", Descriptor.METHOD(Descriptor.STRING, Descriptor.OBJECT));
    code.invokeStatic(obj2str);
    write();
  }

}
