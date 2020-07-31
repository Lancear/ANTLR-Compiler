package semantic.symbols;

import java.util.Collections;
import java.util.List;

import org.antlr.v4.runtime.Token;

public class ProcedureSymbol extends Symbol {

  public final String returnType;
  protected List<String> paramTypes;

  public ProcedureSymbol(String name, String returnType, Token token) {
    super("procedure", name, token);
    this.returnType = returnType;
  }

  public ProcedureSymbol(String name, String returnType, List<String> paramTypes, Token token) {
    super("procedure", name, token);
    this.returnType = returnType;
    this.paramTypes = paramTypes;
  }

  public void setParamTypes(List<String> paramTypes) {
    if (this.paramTypes != null) 
      throw new IllegalStateException("Fields have already been set!");

    this.paramTypes = Collections.unmodifiableList(paramTypes);
  }

  public List<String> getParamTypes() {
    return this.paramTypes;
  }
  
}
