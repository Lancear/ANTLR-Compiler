package codegen;

import java.io.IOException;
import java.nio.file.Path;

import stdlib.StandardLibrary;
import symboltable.SymbolTable;
import symboltable.symbols.ProcedureSymbol;
import symboltable.symbols.VariableSymbol;

public abstract class Backend {
  
  public StandardLibrary stdlib = null;
  public SymbolTable symbolTable = null;
  public Path outputDir = null;

  public abstract Backend enterProgram(String name);
  public abstract void exitProgram() throws IOException;

  public abstract Backend enterMainFunction();
  public abstract Backend exitMainFunction();
  public abstract Backend enterFunction(ProcedureSymbol symbol);
  public abstract Backend exitFunction();

  public abstract Backend loadConstant(String value, String type);
  public abstract Backend callFunction(String name);
  
  public abstract Backend allocLocal(VariableSymbol symbol);
  public abstract Backend store(VariableSymbol symbol, boolean isArrayElement); /*v*/
  public abstract Backend load(VariableSymbol symbol, boolean isArrayElement); /*v*/

  public abstract Backend op1(String op);
  public abstract Backend op2(String op);
  public abstract Backend startCompareOp();
  public abstract Backend compareOp(String operator);

  public abstract Backend newArray(String baseType); /*v*/
  public abstract Backend arraylength();

  public abstract Backend ifThen();
  public abstract Backend elseThen();
  public abstract Backend endIf();
  public abstract Backend startWhile();
  public abstract Backend whileDo();
  public abstract Backend endWhile();
  public abstract Backend startBlock();
  public abstract Backend endBlock();

}
