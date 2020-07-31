// Generated from ./src/parser/Yapl.g4 by ANTLR 4.8
package parser;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link YaplParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface YaplVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link YaplParser#program}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProgram(YaplParser.ProgramContext ctx);
	/**
	 * Visit a parse tree produced by {@link YaplParser#declarationBlock}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeclarationBlock(YaplParser.DeclarationBlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link YaplParser#constDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstDeclaration(YaplParser.ConstDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link YaplParser#varDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVarDeclaration(YaplParser.VarDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link YaplParser#recordDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRecordDeclaration(YaplParser.RecordDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link YaplParser#procedure}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProcedure(YaplParser.ProcedureContext ctx);
	/**
	 * Visit a parse tree produced by {@link YaplParser#param}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParam(YaplParser.ParamContext ctx);
	/**
	 * Visit a parse tree produced by {@link YaplParser#procedureCall}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProcedureCall(YaplParser.ProcedureCallContext ctx);
	/**
	 * Visit a parse tree produced by {@link YaplParser#returnStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReturnStatement(YaplParser.ReturnStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link YaplParser#block}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlock(YaplParser.BlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link YaplParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement(YaplParser.StatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link YaplParser#assignment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignment(YaplParser.AssignmentContext ctx);
	/**
	 * Visit a parse tree produced by {@link YaplParser#selector}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelector(YaplParser.SelectorContext ctx);
	/**
	 * Visit a parse tree produced by {@link YaplParser#ifStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIfStatement(YaplParser.IfStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link YaplParser#whileStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhileStatement(YaplParser.WhileStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link YaplParser#writeStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWriteStatement(YaplParser.WriteStatementContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ArithmeticExpr}
	 * labeled alternative in {@link YaplParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArithmeticExpr(YaplParser.ArithmeticExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Comparison}
	 * labeled alternative in {@link YaplParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparison(YaplParser.ComparisonContext ctx);
	/**
	 * Visit a parse tree produced by the {@code CreationExpr}
	 * labeled alternative in {@link YaplParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreationExpr(YaplParser.CreationExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code UnaryExpr}
	 * labeled alternative in {@link YaplParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnaryExpr(YaplParser.UnaryExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code BooleanExpr}
	 * labeled alternative in {@link YaplParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBooleanExpr(YaplParser.BooleanExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link YaplParser#primaryExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimaryExpr(YaplParser.PrimaryExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link YaplParser#arrayLength}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayLength(YaplParser.ArrayLengthContext ctx);
	/**
	 * Visit a parse tree produced by {@link YaplParser#fullIdentifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFullIdentifier(YaplParser.FullIdentifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link YaplParser#type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitType(YaplParser.TypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link YaplParser#baseType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBaseType(YaplParser.BaseTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link YaplParser#literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLiteral(YaplParser.LiteralContext ctx);
}