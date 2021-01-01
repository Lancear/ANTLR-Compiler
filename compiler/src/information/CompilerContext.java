package information;

import compiler.Compiler;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import parser.YaplParser.*;


public class CompilerContext {

  public static int getLine(Token token) {
    return token.getLine();
  }

  public static int getLine(ParserRuleContext context) {
    return context.start.getLine();
  }

  public static int getColumn(Token token) {
    return 1 + token.getCharPositionInLine();
  }

  public static int getColumn(ParserRuleContext context) {
    return 1 + context.start.getCharPositionInLine();
  }

  public static String getText(ParserRuleContext context) {
    return Compiler.settings.sourceCode.substring(context.getStart().getStartIndex(), context.getStop().getStopIndex() + 1);
  }

  public static String getProcedureName(ParserRuleContext context) {
    if (context instanceof ProgramContext) {
      return null;
    }
    else if (context instanceof ProcedureContext) {
      return ((ProcedureContext)context).Id(0).getText();
    }

    return getProcedureName(context.getParent());
  }

  public static String getRecordName(ParserRuleContext context) {
    if (context instanceof ProgramContext) {
      return null;
    }
    else if (context instanceof RecordDeclarationContext) {
      return ((RecordDeclarationContext)context).Id().getText();
    }

    return getRecordName(context.getParent());
  }

  public static String getPath(ParserRuleContext context) {
    // root
    if (context instanceof ProgramContext) {
      return ((ProgramContext)context).Id(0).getText();
    }

    // ignore
    if (context instanceof StatementListContext || context instanceof StatementContext ) {
      return getPath(context.getParent());
    }


    String path = getPath(context.getParent());
    // limit path depth to expression
    if (path.endsWith("expression")) return path;

    String rule = Compiler.ruleNames[context.getRuleIndex()];
    int idx = 0, count = 1;
    while(context.getParent().getChild(idx) != context) {
      if (context.getClass().isInstance( context.getParent().getChild(idx) )) count++;
      idx++;
    }

    path += " > ";
    // use identifier instead of rule name if available
    if (context instanceof ProcedureContext)                path += ((ProcedureContext)context).Id(0).getText();
    else if (context instanceof ProcedureCallContext)       path += ((ProcedureCallContext)context).Id().getText();
    else if (context instanceof RecordDeclarationContext)   path += ((RecordDeclarationContext)context).Id().getText();
    else {
      // hide count when its the first or only of its type
      if (count > 1) path += count + ". ";
      path += rule;
    }

    return path;
  }

  public static String getUnderlinedCode(Token token) {
    String[] lines = Compiler.settings.sourceCode.split("\n");

    int row = getLine(token) - 1;
    int tokenCol = getColumn(token);

    String code = "";

    if (row - 5 >= 0) code += String.format("%1$4d", 1 + row - 5) + " | " + lines[row - 5] + "\n";
    if (row - 4 >= 0) code += String.format("%1$4d", 1 + row - 4) + " | " + lines[row - 4] + "\n";
    if (row - 3 >= 0) code += String.format("%1$4d", 1 + row - 3) + " | " + lines[row - 3] + "\n";
    if (row - 2 >= 0) code += String.format("%1$4d", 1 + row - 2) + " | " + lines[row - 2] + "\n";
    if (row - 1 >= 0) code += String.format("%1$4d", 1 + row - 1) + " | " + lines[row - 1] + "\n";
    code += String.format("%1$4d", 1 + row) + " | " + lines[row] + "\n";
    code += "     | ";

    for (int i = 0; i < tokenCol - 1; i++) code += " ";
    for (int i = 0; i < token.getStopIndex() - token.getStartIndex() + 1; i++) code += "^";
    code += "\n";

    if (row + 1 < lines.length) code += String.format("%1$4d", 1 + row + 1) + " | " + lines[row + 1] + "\n";
    if (row + 2 < lines.length) code += String.format("%1$4d", 1 + row + 2) + " | " + lines[row + 2] + "\n";
    if (row + 3 < lines.length) code += String.format("%1$4d", 1 + row + 3) + " | " + lines[row + 3] + "\n";
    return code;
  }

  public static String getUnderlinedCode(ParserRuleContext context) {
    String[] lines = Compiler.settings.sourceCode.split("\n");

    int row = getLine(context) - 1;
    int tokenCol = getColumn(context);

    String code = "";

    if (row - 5 >= 0) code += String.format("%1$4d", 1 + row - 5) + " | " + lines[row - 5] + "\n";
    if (row - 4 >= 0) code += String.format("%1$4d", 1 + row - 4) + " | " + lines[row - 4] + "\n";
    if (row - 3 >= 0) code += String.format("%1$4d", 1 + row - 3) + " | " + lines[row - 3] + "\n";
    if (row - 2 >= 0) code += String.format("%1$4d", 1 + row - 2) + " | " + lines[row - 2] + "\n";
    if (row - 1 >= 0) code += String.format("%1$4d", 1 + row - 1) + " | " + lines[row - 1] + "\n";
    code += String.format("%1$4d", 1 + row) + " | " + lines[row] + "\n";
    code += "     | ";

    for (int i = 0; i < tokenCol - 1; i++) code += " ";
    for (int i = 0; i < context.stop.getStopIndex() - context.start.getStartIndex() + 1; i++) code += "^";
    code += "\n";

    if (row + 1 < lines.length) code += String.format("%1$4d", 1 + row + 1) + " | " + lines[row + 1] + "\n";
    if (row + 2 < lines.length) code += String.format("%1$4d", 1 + row + 2) + " | " + lines[row + 2] + "\n";
    if (row + 3 < lines.length) code += String.format("%1$4d", 1 + row + 3) + " | " + lines[row + 3] + "\n";
    return code;
  }

}
