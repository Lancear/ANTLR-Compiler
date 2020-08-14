// Generated from ./src/parser/Yapl.g4 by ANTLR 4.8
package parser;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link YaplParser}.
 */
public interface YaplListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link YaplParser#program}.
	 * @param ctx the parse tree
	 */
	void enterProgram(YaplParser.ProgramContext ctx);
	/**
	 * Exit a parse tree produced by {@link YaplParser#program}.
	 * @param ctx the parse tree
	 */
	void exitProgram(YaplParser.ProgramContext ctx);
	/**
	 * Enter a parse tree produced by {@link YaplParser#declarationBlock}.
	 * @param ctx the parse tree
	 */
	void enterDeclarationBlock(YaplParser.DeclarationBlockContext ctx);
	/**
	 * Exit a parse tree produced by {@link YaplParser#declarationBlock}.
	 * @param ctx the parse tree
	 */
	void exitDeclarationBlock(YaplParser.DeclarationBlockContext ctx);
	/**
	 * Enter a parse tree produced by {@link YaplParser#constDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterConstDeclaration(YaplParser.ConstDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link YaplParser#constDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitConstDeclaration(YaplParser.ConstDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link YaplParser#varDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterVarDeclaration(YaplParser.VarDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link YaplParser#varDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitVarDeclaration(YaplParser.VarDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link YaplParser#recordDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterRecordDeclaration(YaplParser.RecordDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link YaplParser#recordDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitRecordDeclaration(YaplParser.RecordDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link YaplParser#procedure}.
	 * @param ctx the parse tree
	 */
	void enterProcedure(YaplParser.ProcedureContext ctx);
	/**
	 * Exit a parse tree produced by {@link YaplParser#procedure}.
	 * @param ctx the parse tree
	 */
	void exitProcedure(YaplParser.ProcedureContext ctx);
	/**
	 * Enter a parse tree produced by {@link YaplParser#param}.
	 * @param ctx the parse tree
	 */
	void enterParam(YaplParser.ParamContext ctx);
	/**
	 * Exit a parse tree produced by {@link YaplParser#param}.
	 * @param ctx the parse tree
	 */
	void exitParam(YaplParser.ParamContext ctx);
	/**
	 * Enter a parse tree produced by {@link YaplParser#procedureCall}.
	 * @param ctx the parse tree
	 */
	void enterProcedureCall(YaplParser.ProcedureCallContext ctx);
	/**
	 * Exit a parse tree produced by {@link YaplParser#procedureCall}.
	 * @param ctx the parse tree
	 */
	void exitProcedureCall(YaplParser.ProcedureCallContext ctx);
	/**
	 * Enter a parse tree produced by {@link YaplParser#returnStatement}.
	 * @param ctx the parse tree
	 */
	void enterReturnStatement(YaplParser.ReturnStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link YaplParser#returnStatement}.
	 * @param ctx the parse tree
	 */
	void exitReturnStatement(YaplParser.ReturnStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link YaplParser#block}.
	 * @param ctx the parse tree
	 */
	void enterBlock(YaplParser.BlockContext ctx);
	/**
	 * Exit a parse tree produced by {@link YaplParser#block}.
	 * @param ctx the parse tree
	 */
	void exitBlock(YaplParser.BlockContext ctx);
	/**
	 * Enter a parse tree produced by {@link YaplParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(YaplParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link YaplParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(YaplParser.StatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link YaplParser#statementList}.
	 * @param ctx the parse tree
	 */
	void enterStatementList(YaplParser.StatementListContext ctx);
	/**
	 * Exit a parse tree produced by {@link YaplParser#statementList}.
	 * @param ctx the parse tree
	 */
	void exitStatementList(YaplParser.StatementListContext ctx);
	/**
	 * Enter a parse tree produced by {@link YaplParser#assignment}.
	 * @param ctx the parse tree
	 */
	void enterAssignment(YaplParser.AssignmentContext ctx);
	/**
	 * Exit a parse tree produced by {@link YaplParser#assignment}.
	 * @param ctx the parse tree
	 */
	void exitAssignment(YaplParser.AssignmentContext ctx);
	/**
	 * Enter a parse tree produced by {@link YaplParser#ifStatement}.
	 * @param ctx the parse tree
	 */
	void enterIfStatement(YaplParser.IfStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link YaplParser#ifStatement}.
	 * @param ctx the parse tree
	 */
	void exitIfStatement(YaplParser.IfStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link YaplParser#whileStatement}.
	 * @param ctx the parse tree
	 */
	void enterWhileStatement(YaplParser.WhileStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link YaplParser#whileStatement}.
	 * @param ctx the parse tree
	 */
	void exitWhileStatement(YaplParser.WhileStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link YaplParser#writeStatement}.
	 * @param ctx the parse tree
	 */
	void enterWriteStatement(YaplParser.WriteStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link YaplParser#writeStatement}.
	 * @param ctx the parse tree
	 */
	void exitWriteStatement(YaplParser.WriteStatementContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ArithmeticExpr}
	 * labeled alternative in {@link YaplParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterArithmeticExpr(YaplParser.ArithmeticExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ArithmeticExpr}
	 * labeled alternative in {@link YaplParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitArithmeticExpr(YaplParser.ArithmeticExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Comparison}
	 * labeled alternative in {@link YaplParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterComparison(YaplParser.ComparisonContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Comparison}
	 * labeled alternative in {@link YaplParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitComparison(YaplParser.ComparisonContext ctx);
	/**
	 * Enter a parse tree produced by the {@code CreationExpr}
	 * labeled alternative in {@link YaplParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterCreationExpr(YaplParser.CreationExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code CreationExpr}
	 * labeled alternative in {@link YaplParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitCreationExpr(YaplParser.CreationExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code UnaryExpr}
	 * labeled alternative in {@link YaplParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterUnaryExpr(YaplParser.UnaryExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code UnaryExpr}
	 * labeled alternative in {@link YaplParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitUnaryExpr(YaplParser.UnaryExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code EqualityComparison}
	 * labeled alternative in {@link YaplParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterEqualityComparison(YaplParser.EqualityComparisonContext ctx);
	/**
	 * Exit a parse tree produced by the {@code EqualityComparison}
	 * labeled alternative in {@link YaplParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitEqualityComparison(YaplParser.EqualityComparisonContext ctx);
	/**
	 * Enter a parse tree produced by the {@code BooleanExpr}
	 * labeled alternative in {@link YaplParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterBooleanExpr(YaplParser.BooleanExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code BooleanExpr}
	 * labeled alternative in {@link YaplParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitBooleanExpr(YaplParser.BooleanExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link YaplParser#primaryExpr}.
	 * @param ctx the parse tree
	 */
	void enterPrimaryExpr(YaplParser.PrimaryExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link YaplParser#primaryExpr}.
	 * @param ctx the parse tree
	 */
	void exitPrimaryExpr(YaplParser.PrimaryExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link YaplParser#arrayLength}.
	 * @param ctx the parse tree
	 */
	void enterArrayLength(YaplParser.ArrayLengthContext ctx);
	/**
	 * Exit a parse tree produced by {@link YaplParser#arrayLength}.
	 * @param ctx the parse tree
	 */
	void exitArrayLength(YaplParser.ArrayLengthContext ctx);
	/**
	 * Enter a parse tree produced by {@link YaplParser#fullIdentifier}.
	 * @param ctx the parse tree
	 */
	void enterFullIdentifier(YaplParser.FullIdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link YaplParser#fullIdentifier}.
	 * @param ctx the parse tree
	 */
	void exitFullIdentifier(YaplParser.FullIdentifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link YaplParser#selector}.
	 * @param ctx the parse tree
	 */
	void enterSelector(YaplParser.SelectorContext ctx);
	/**
	 * Exit a parse tree produced by {@link YaplParser#selector}.
	 * @param ctx the parse tree
	 */
	void exitSelector(YaplParser.SelectorContext ctx);
	/**
	 * Enter a parse tree produced by {@link YaplParser#returnType}.
	 * @param ctx the parse tree
	 */
	void enterReturnType(YaplParser.ReturnTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link YaplParser#returnType}.
	 * @param ctx the parse tree
	 */
	void exitReturnType(YaplParser.ReturnTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link YaplParser#type}.
	 * @param ctx the parse tree
	 */
	void enterType(YaplParser.TypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link YaplParser#type}.
	 * @param ctx the parse tree
	 */
	void exitType(YaplParser.TypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link YaplParser#baseType}.
	 * @param ctx the parse tree
	 */
	void enterBaseType(YaplParser.BaseTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link YaplParser#baseType}.
	 * @param ctx the parse tree
	 */
	void exitBaseType(YaplParser.BaseTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link YaplParser#literal}.
	 * @param ctx the parse tree
	 */
	void enterLiteral(YaplParser.LiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link YaplParser#literal}.
	 * @param ctx the parse tree
	 */
	void exitLiteral(YaplParser.LiteralContext ctx);
}