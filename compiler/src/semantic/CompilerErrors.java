package semantic;

import org.antlr.v4.runtime.Token;

import semantic.symbols.Symbol;

public abstract class CompilerErrors {

	public static class CompilerError {
		public final int errorNumber;
		public final String programName;
		public final String message;
		public final int line;
		public final int column;

		public CompilerError(int errorNumber, String programName, String message, int line, int column) {
			this.errorNumber = errorNumber;
			this.programName = programName;
			this.message = message;
			this.line = line;
			this.column = column;
		}

		@Override
		public String toString() {
			return "YAPL compilation: " + (programName != null ? "[" + programName + "] " : "") + "ERROR " + errorNumber + " (line " + line + ", column " + column + ")\n" + message;
		}

	}
	
	/** Internal error. */
	public static CompilerError Internal(String programName, String message, int line, int column) {
		return new CompilerError(1, programName, message, line, column);
	}

	/** Lexical error. */
	public static CompilerError Lexical(String programName, String message, int line, int column) {
		return new CompilerError(2, programName, message, line, column);
	}

	/** Syntax error. */
	public static CompilerError Syntax(String programName, String message, int line, int column) {
		return new CompilerError(3, programName, message, line, column);
	}
	
	/* Symbol check errors */
	
	/** Symbol already declared. */
	public static CompilerError SymbolExists(String programName, String identifier, String kind, Token token) {
		return new CompilerError(10, programName, "symbol '" + identifier + "' already declared in the current scope (as " + kind + ")", token.getLine(), token.getCharPositionInLine() + 1);
	}

	/** Identifier not declared. */
	public static CompilerError IdentNotDecl(String programName, String identifier, Token token) {
		return new CompilerError(11, programName, "identifier '" + identifier + "' not declared", token.getLine(), token.getCharPositionInLine() + 1);
	}

	/** Illegal use of symbol. */
	public static CompilerError SymbolIllegalUse(String programName, String identifier, String kind, Token token) {
		return new CompilerError(12, programName, "illegal use of " + kind + " '" + identifier + "'", token.getLine(), token.getCharPositionInLine() + 1);
	}

	/** End identifier does not match program|procedure. */
	public static CompilerError EndIdentMismatch(String programName, String name, String endName, String kind, Token token) {
		return new CompilerError(13, programName, "End '" + endName + "' does not match " + kind + " '" + name + "'", token.getLine(), token.getCharPositionInLine() + 1);
	}
	
	/* Type check errors */
	
	/** Expression before '[' is not an array type. */
	public static CompilerError SelectorNotArray(String programName, Token token) {
		return new CompilerError(20, programName, "expression before ’[’ is not an array type", token.getLine(), token.getCharPositionInLine() + 1);
	}

	/** Array index or dimension is not an integer type. */
	public static CompilerError BadArraySelector(String programName, Token token) {
		return new CompilerError(21, programName, "Array index or dimension is not an integer type", token.getLine(), token.getCharPositionInLine() + 1);
	}

	/** Expression after '#' is not an array type. */
	public static CompilerError ArrayLenNotArray(String programName, Token token) {
		return new CompilerError(22, programName, "Expression after '#' is not an array type", token.getLine(), token.getCharPositionInLine() + 1);
	}

	/** Illegal operand type for unary operator. */
	public static CompilerError IllegalOp1Type(String programName, String op, Token token) {
		return new CompilerError(23, programName, "Illegal operand type for unary operator '" + op + "'", token.getLine(), token.getCharPositionInLine() + 1);
	}

	/** Illegal operand types for binary operator. */
	public static CompilerError IllegalOp2Type(String programName, String op, Token token) {
		return new CompilerError(24, programName, "Illegal operand types for binary operator '" + op + "'", token.getLine(), token.getCharPositionInLine() + 1);
	}

	/** Illegal operand type for relational operator. */
	public static CompilerError IllegalRelOpType(String programName, String op, Token token) {
		return new CompilerError(25, programName, "Illegal operand types for relational operator '" + op + "'", token.getLine(), token.getCharPositionInLine() + 1);
	}

	/** Illegal operand type for equality operator. */
	public static CompilerError IllegalEqualOpType(String programName, String op, Token token) {
		return new CompilerError(26, programName, "Illegal operand types for equality operator '" + op + "'", token.getLine(), token.getCharPositionInLine() + 1);
	}

	/** Using procedure (not a function) in expression. */
	public static CompilerError ProcNotFuncExpr(String programName, String procedure, Token token) {
		return new CompilerError(27, programName, "Using procedure '" + procedure + "' (not a function) in expression", token.getLine(), token.getCharPositionInLine() + 1);
	}

	/** Read-only l-value in assignment. */
	public static CompilerError ReadonlyAssign(String programName, String message, int line, int column) {
		return new CompilerError(28, programName, message, line, column);
	}

	/** Type mismatch in assignment. */
	public static CompilerError TypeMismatchAssign(String programName, Token token) {
		return new CompilerError(29, programName, "Type mismatch in assignment", token.getLine(), token.getCharPositionInLine() + 1);
	}

	/** Argument not applicable to procedure. */
	public static CompilerError ArgNotApplicable(String programName, String procedure, int argNr, Token token) {
		return new CompilerError(30, programName, "Argument #" + argNr + " not applicable to procedure '" + procedure + "'", token.getLine(), token.getCharPositionInLine() + 1);
	}

	/** Read-only argument passed to read-write procedure. */
	public static CompilerError ReadonlyArg(String programName, String message, int line, int column) {
		return new CompilerError(31, programName, message, line, column);
	}

	/** Too few arguments for procedure. */
	public static CompilerError TooFewArgs(String programName, String procedure, Token token) {
		return new CompilerError(32, programName, "Too few arguments for procedure '" + procedure + "'", token.getLine(), token.getCharPositionInLine() + 1);
	}

	/** Condition is not a boolean expression. */
	public static CompilerError CondNotBool(String programName, Token token) {
		return new CompilerError(33, programName, "Condition is not a boolean expression", token.getLine(), token.getCharPositionInLine() + 1);
	}

	/** Readonly not followed by reference type. */
	public static CompilerError ReadonlyNotReference(String programName, String message, int line, int column) {
		return new CompilerError(34, programName, message, line, column);
	} 

	/** Missing return statement in function. */
	public static CompilerError MissingReturn(String programName, String procedure, Token token) {
		return new CompilerError(35, programName, "Missing return statement in function '" + procedure + "'", token.getLine(), token.getCharPositionInLine() + 1);
	}

	/** Returning none or invalid type from function. */
	public static CompilerError InvalidReturnType(String programName, String procedure, Token token) {
		return new CompilerError(36, programName, "Returning none or invalid type from function '" + procedure + "'", token.getLine(), token.getCharPositionInLine() + 1);
	}

	/** Illegal return value in procedure (not a function). */
	public static CompilerError IllegalRetValProc(String programName, String procedure, Token token) {
		return new CompilerError(37, programName, "illegal return value in procedure '" + procedure + "' (not a function)", token.getLine(), token.getCharPositionInLine() + 1);
	}

	/** Illegal return value in main program. */
	public static CompilerError IllegalRetValMain(String programName, Token token) {
		return new CompilerError(38, programName, "Illegal return value in main program", token.getLine(), token.getCharPositionInLine() + 1);
	}

	/** Expression before '.' is not a record type. */
	public static CompilerError SelectorNotRecord(String programName, Token token) {
		return new CompilerError(39, programName, "Expression before ’.’ is not a record type ", token.getLine(), token.getCharPositionInLine() + 1);
	}

	/** invalid field of record */
	public static CompilerError InvalidRecordField(String programName, String field, String record, Token token) {
		return new CompilerError(40, programName, " invalid field '" + field + "' of record '" + record + "' ", token.getLine(), token.getCharPositionInLine() + 1);
	}

	/** invalid type used with 'new' operator */
	public static CompilerError InvalidNewType(String programName, Token token) {
		return new CompilerError(41, programName, "invalid type used with ’new’", token.getLine(), token.getCharPositionInLine() + 1);
	}



	/* Code generation errors */
	
	/** Too many registers used. */
	public static CompilerError NoMoreRegs(String programName, String message, int line, int column) {
		return new CompilerError(50, programName, message, line, column);
	}
	
	/** Too many array dimensions. */
	public static CompilerError TooManyDims(String programName, String message, int line, int column) {
		return new CompilerError(51, programName, message, line, column);
	}

}
