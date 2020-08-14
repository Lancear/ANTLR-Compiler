package codegen;

import java.io.IOException;
import java.nio.file.Path;

import analysis.Symbol;
import analysis.SymbolTable;
import stdlib.StandardLibrary;

public abstract class Backend {
  
  public StandardLibrary stdlib = null;
  public SymbolTable symbolTable = null;
  public Path outputDir = null;

  public abstract Backend enterProgram(String name);
  public abstract void exitProgram() throws IOException;

  public abstract Backend enterMainFunction();
  public abstract Backend exitMainFunction();
  public abstract Backend enterFunction(Symbol.Function sym);
  public abstract Backend exitFunction();

  public abstract Backend enterRecord(String name);
  public abstract Backend exitRecord();


  public abstract Backend loadConstant(Symbol.Const sym);  
  public abstract Backend allocVariable(Symbol.Variable sym);
  public abstract Backend store(Symbol.Variable sym);
  public abstract Backend load(Symbol.Variable sym);

  public abstract Backend callFunction(Symbol.Function fn);
  public abstract Backend op1(String op);
  public abstract Backend op2(String op);

  public abstract Backend start();
  public abstract Backend end();
  public abstract Backend branch();
  public abstract Backend elseBranch();
  public abstract Backend loop();
  public abstract Backend returnFunction();

  public abstract Backend newArray(String baseType, int dimensions);
  public abstract Backend arraylength();
  
  public abstract Backend newRecord(String type);

}
