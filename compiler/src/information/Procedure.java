package information;

import java.util.ArrayList;
import java.util.List;

public class Procedure extends Symbol {

  public static String SYMBOL_TYPE = "procedure";

  public List<Parameter> params;
  public String returnType;
  public boolean isStdLib;
  public boolean isPure;
  public int uses;

  public Procedure(String name, String returnType) {
    this(name, returnType, new ArrayList<>());
  }

  public Procedure(String name, String returnType, List<Parameter> params) {
    this(name, returnType, params, false, false);
  }

  public Procedure(String name, String returnType, List<Parameter> params, boolean isStdLib, boolean isPure) {
    super(name, Procedure.SYMBOL_TYPE);
    this.returnType = returnType;
    this.params = params;
    this.isStdLib = isStdLib;
    this.isPure = isPure;
    this.uses = 0;
  }

  @Override
  public String toString() {
    return symbolType.toUpperCase() + " " + returnType + " " + name + " // isPure: " + isPure + ", uses: " + uses;
  }

}
