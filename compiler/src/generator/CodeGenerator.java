package generator;

import information.Constant;
import information.Procedure;
import information.Variable;

import java.io.IOException;

/**
 * Represents a simplified version of an intermediate representation which is implemented by all targets.
 */
public interface CodeGenerator {

  CodeGenerator enterProgram(String name);
  void exitProgram() throws IOException;

  CodeGenerator enterMainFunction();
  CodeGenerator exitMainFunction();
  CodeGenerator enterFunction(Procedure sym);
  CodeGenerator exitFunction();

  CodeGenerator enterRecord(String name);
  CodeGenerator exitRecord();

  CodeGenerator loadConstant(Constant sym);
  CodeGenerator allocVariable(Variable sym);
  CodeGenerator store(Variable sym);
  CodeGenerator load(Variable sym);

  CodeGenerator write();
  CodeGenerator callFunction(Procedure fn);
  CodeGenerator op1(String op);
  CodeGenerator op2(String op);

  CodeGenerator startBranchingBlock();
  CodeGenerator branch();
  CodeGenerator elseBranch();
  CodeGenerator loop();
  CodeGenerator endBranchingBlock();
  CodeGenerator returnFromFunction();

  CodeGenerator newArray(String baseType, int dimensions);
  CodeGenerator arraylength();

  CodeGenerator newRecord(String type);

}
