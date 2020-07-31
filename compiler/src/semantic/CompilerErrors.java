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
	public static CompilerError SelectorNotArray(String programName, String message, int line, int column) {
		return new CompilerError(20, programName, message, line, column);
	}

	/** Array index or dimension is not an integer type. */
	public static CompilerError BadArraySelector(String programName, String message, int line, int column) {
		return new CompilerError(21, programName, message, line, column);
	}

	/** Expression after '#' is not an array type. */
	public static CompilerError ArrayLenNotArray(String programName, String message, int line, int column) {
		return new CompilerError(22, programName, message, line, column);
	}

	/** Illegal operand type for unary operator. */
	public static CompilerError IllegalOp1Type(String programName, String message, int line, int column) {
		return new CompilerError(23, programName, message, line, column);
	}

	/** Illegal operand type for binary operator. */
	public static CompilerError IllegalOp2Type(String programName, String message, int line, int column) {
		return new CompilerError(24, programName, message, line, column);
	}

	/** Illegal operand type for relational operator. */
	public static CompilerError IllegalRelOpType(String programName, String message, int line, int column) {
		return new CompilerError(25, programName, message, line, column);
	}

	/** Illegal operand type for equality operator. */
	public static CompilerError IllegalEqualOpType(String programName, String message, int line, int column) {
		return new CompilerError(26, programName, message, line, column);
	}

	/** Using procedure (not a function) in expression. */
	public static CompilerError ProcNotFuncExpr(String programName, String message, int line, int column) {
		return new CompilerError(27, programName, message, line, column);
	}

	/** Read-only l-value in assignment. */
	public static CompilerError ReadonlyAssign(String programName, String message, int line, int column) {
		return new CompilerError(28, programName, message, line, column);
	}

	/** Type mismatch in assignment. */
	public static CompilerError TypeMismatchAssign(String programName, String message, int line, int column) {
		return new CompilerError(29, programName, message, line, column);
	}

	/** Argument not applicable to procedure. */
	public static CompilerError ArgNotApplicable(String programName, String message, int line, int column) {
		return new CompilerError(30, programName, message, line, column);
	}

	/** Read-only argument passed to read-write procedure. */
	public static CompilerError ReadonlyArg(String programName, String message, int line, int column) {
		return new CompilerError(31, programName, message, line, column);
	}

	/** Too few arguments for procedure. */
	public static CompilerError TooFewArgs(String programName, String message, int line, int column) {
		return new CompilerError(32, programName, message, line, column);
	}

	/** Condition is not a boolean expression. */
	public static CompilerError CondNotBool(String programName, String message, int line, int column) {
		return new CompilerError(33, programName, message, line, column);
	}

	/** Readonly not followed by reference type. */
	public static CompilerError ReadonlyNotReference(String programName, String message, int line, int column) {
		return new CompilerError(34, programName, message, line, column);
	} 

	/** Missing return statement in function. */
	public static CompilerError MissingReturn(String programName, String message, int line, int column) {
		return new CompilerError(35, programName, message, line, column);
	}

	/** Returning none or invalid type from function. */
	public static CompilerError InvalidReturnType(String programName, String message, int line, int column) {
		return new CompilerError(36, programName, message, line, column);
	}

	/** Illegal return value in procedure (not a function). */
	public static CompilerError IllegalRetValProc(String programName, String message, int line, int column) {
		return new CompilerError(37, programName, message, line, column);
	}

	/** Illegal return value in main program. */
	public static CompilerError IllegalRetValMain(String programName, String message, int line, int column) {
		return new CompilerError(38, programName, message, line, column);
	}

	/** Expression before '.' is not a record type. */
	public static CompilerError SelectorNotRecord(String programName, String message, int line, int column) {
		return new CompilerError(39, programName, message, line, column);
	}

	/** invalid field of record */
	public static CompilerError InvalidRecordField(String programName, String message, int line, int column) {
		return new CompilerError(40, programName, message, line, column);
	}

	/** invalid type used with 'new' operator */
	public static CompilerError InvalidNewType(String programName, String message, int line, int column) {
		return new CompilerError(41, programName, message, line, column);
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
