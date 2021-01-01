package information;

import compiler.Compiler;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

public class CompilerError extends Information {

  public final int errorNumber;
  public final String message;
  public final ParserRuleContext context;
  public final Token token;
  public final int row;
  public final int column;
  public final boolean underlineWholeContext;

  protected CompilerError(int errorNumber, String message, ParserRuleContext context, Token token) {
    this(errorNumber, message, context, token, false);
  }

  protected CompilerError(int errorNumber, String message, ParserRuleContext context, Token token, boolean underlineWholeContext) {
    this.errorNumber = errorNumber;
    this.message = message;
    this.context = context;
    this.token = token;
    this.row = CompilerContext.getLine(token);
    this.column = CompilerContext.getColumn(token);
    this.underlineWholeContext = underlineWholeContext;
  }

  protected CompilerError(int errorNumber, String message, int row, int column) {
    this.errorNumber = errorNumber;
    this.message = message;
    this.context = null;
    this.token = null;
    this.row = row;
    this.column = column;
    this.underlineWholeContext = false;
  }

  @Override
  public String toString() {
    String programName = (Compiler.programName != null) ? "[" + Compiler.programName + "] " : "";
    String msg = "YAPL compilation: " + programName + "ERROR " + errorNumber + " (line " + row + ", column " + column + ")";

    if (context != null) msg += "\r\nPath: " + CompilerContext.getPath(context);
    msg += "\r\nMessage: " + message;

    if (context != null && underlineWholeContext) msg += "\r\nCode:\r\n" + CompilerContext.getUnderlinedCode(context);
    else if (token != null) msg += "\r\nCode:\r\n" + CompilerContext.getUnderlinedCode(token);
    return msg + "\r\n";
  }



  /** Internal error. */
  public static CompilerError Internal(String message) {
    return new CompilerError(1, message, -1, -1);
  }

  public static CompilerError Internal(String message, ParserRuleContext context, Token token) {
    return new CompilerError(1, message, context, token);
  }

  /** Lexical error. */
  public static CompilerError Lexical(String message, int row, int column) {
    return new CompilerError(2, message, row, column);
  }

  /** Syntax error. */
  public static CompilerError Syntax(String message, ParserRuleContext context, Token token) {
    return new CompilerError(3, message, context, token);
  }

  /* Symbol check errors */

  /** Symbol already declared. */
  public static CompilerError SymbolExists(String identifier, String kind, ParserRuleContext context, Token token) {
    return new CompilerError(10, "Symbol '" + identifier + "' already declared in the current scope (as " + kind + ")", context, token);
  }

  /** Identifier not declared. */
  public static CompilerError IdentNotDecl(String identifier, ParserRuleContext context, Token token) {
    return new CompilerError(11, "Identifier '" + identifier + "' not declared", context, token);
  }

  /** Illegal use of symbol. */
  public static CompilerError SymbolIllegalUse(String identifier, String kind, ParserRuleContext context, Token token) {
    return new CompilerError(12, "Illegal use of " + kind + " '" + identifier + "'", context, token);
  }

  /** End identifier does not match program|procedure. */
  public static CompilerError EndIdentMismatch(String name, String endName, String kind, ParserRuleContext context, Token token) {
    return new CompilerError(13, "End '" + endName + "' does not match " + kind + " '" + name + "'", context, token);
  }

  /* Type check errors */

  /** ConstExpression before '[' is not an array type. */
  public static CompilerError SelectorNotArray(ParserRuleContext context, Token token) {
    return new CompilerError(20, "ConstExpression before ’[’ is not an array type", context, token, true);
  }

  /** Array index or dimension is not an integer type. */
  public static CompilerError BadArraySelector(ParserRuleContext context, Token token) {
    return new CompilerError(21, "Array index or dimension is not an integer type", context, token, true);
  }

  /** ConstExpression after '#' is not an array type. */
  public static CompilerError ArrayLenNotArray(ParserRuleContext context, Token token) {
    return new CompilerError(22, "ConstExpression after '#' is not an array type", context, token, true);
  }

  /** Illegal operand type for unary operator. */
  public static CompilerError IllegalOp1Type(String op, ParserRuleContext context, Token token) {
    return new CompilerError(23, "Illegal operand type for unary operator '" + op + "'", context, token, true);
  }

  /** Illegal operand types for binary operator. */
  public static CompilerError IllegalOp2Type(String op, ParserRuleContext context, Token token) {
    return new CompilerError(24, "Illegal operand types for binary operator '" + op + "'", context, token);
  }

  /** Illegal operand type for relational operator. */
  public static CompilerError IllegalRelOpType(String op, ParserRuleContext context, Token token) {
    return new CompilerError(25, "Illegal operand types for relational operator '" + op + "'", context, token);
  }

  /** Illegal operand type for equality operator. */
  public static CompilerError IllegalEqualOpType(String op, ParserRuleContext context, Token token) {
    return new CompilerError(26, "Illegal operand types for equality operator '" + op + "'", context, token);
  }

  /** Using procedure (not a function) in expression. */
  public static CompilerError ProcNotFuncExpr(String procedure, ParserRuleContext context, Token token) {
    return new CompilerError(27, "Using procedure '" + procedure + "' (not a function) in expression", context, token, true);
  }

  /** Type mismatch in assignment. */
  public static CompilerError TypeMismatchAssign(ParserRuleContext context, Token token) {
    return new CompilerError(29, "Type mismatch in assignment", context, token);
  }

  /** Argument not applicable to procedure. */
  public static CompilerError ArgNotApplicable(String procedure, int argNr, ParserRuleContext context, Token token) {
    return new CompilerError(30, "Argument #" + argNr + " not applicable to procedure '" + procedure + "'", context, token, true);
  }

  /** Too few arguments for procedure. */
  public static CompilerError TooFewArgs(String procedure, ParserRuleContext context, Token token) {
    return new CompilerError(32, "Too few arguments for procedure '" + procedure + "'", context, token, true);
  }

  /** Condition is not a boolean expression. */
  public static CompilerError CondNotBool(ParserRuleContext context, Token token) {
    return new CompilerError(33, "Condition is not a boolean expression", context, token, true);
  }

  /** Missing return statement in function. */
  public static CompilerError MissingReturn(String procedure, ParserRuleContext context, Token token) {
    return new CompilerError(35, "Missing return statement in function '" + procedure + "'", context, token);
  }

  /** Returning none or invalid type from function. */
  public static CompilerError InvalidReturnType(String procedure, ParserRuleContext context, Token token) {
    return new CompilerError(36, "Returning none or invalid type from function '" + procedure + "'", context, token, true);
  }

  /** Illegal return value in procedure (not a function). */
  public static CompilerError IllegalRetValProc(String procedure, ParserRuleContext context, Token token) {
    return new CompilerError(37, "Illegal return value in procedure '" + procedure + "' (not a function)", context, token, true);
  }

  /** Illegal return value in main program. */
  public static CompilerError IllegalRetValMain(ParserRuleContext context, Token token) {
    return new CompilerError(38, "Illegal return value in main program", context, token, true);
  }

  /** ConstExpression before '.' is not a record type. */
  public static CompilerError SelectorNotRecord(ParserRuleContext context, Token token) {
    return new CompilerError(39, "ConstExpression before ’.’ is not a record type ", context, token);
  }

  /** invalid field of record */
  public static CompilerError InvalidRecordField(String field, String record, ParserRuleContext context, Token token) {
    return new CompilerError(40, "Invalid field '" + field + "' of record '" + record + "' ", context, token);
  }

  /** invalid type used with 'new' operator */
  public static CompilerError InvalidNewType(ParserRuleContext context, Token token) {
    return new CompilerError(41, "Invalid type used with ’new’", context, token, true);
  }
  
}
