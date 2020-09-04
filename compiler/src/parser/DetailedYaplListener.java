package parser;

import org.antlr.v4.runtime.tree.ParseTree;

import parser.YaplParser.*;

/**
 * This YaplListener interface has some additional more detailed events 
 * and removed events for nodes which are only interesting for the parser.
 */
public interface DetailedYaplListener {

  void enterProgram(ProgramContext ctx);  
	void afterProgramId(ProgramContext ctx);
	void exitProgram(ProgramContext ctx);

	void exitConstDeclaration(ConstDeclarationContext ctx);
	void exitVarDeclaration(VarDeclarationContext ctx);
	void afterRecordId(RecordDeclarationContext ctx);
	void exitRecordDeclaration(RecordDeclarationContext ctx);

	void exitReturnType(ReturnTypeContext ctx);
	void afterProcedureId(ProcedureContext ctx);
	void exitParam(ParamContext ctx);
	void exitProcedure(ProcedureContext ctx);

	void enterBlock(BlockContext ctx);
	void exitBlock(BlockContext ctx);
	void enterStatementList(StatementListContext ctx);
	void exitStatementList(StatementListContext ctx);
  
	void exitWriteStatement(WriteStatementContext ctx);
	void exitReturnStatement(ReturnStatementContext ctx);
	void exitAssignment(AssignmentContext ctx);
	void exitIfStatement(IfStatementContext ctx);
	void exitWhileStatement(WhileStatementContext ctx);
	
	void exitUnaryExpr(UnaryExprContext ctx);
	void exitArithmeticExpr(ArithmeticExprContext ctx);
	void exitComparison(ComparisonContext ctx);
	void exitEqualityComparison(EqualityComparisonContext ctx);
	void exitBooleanExpr(BooleanExprContext ctx);
	void exitProcedureCall(ProcedureCallContext ctx);
	void exitCreationExpr(CreationExprContext ctx);
	void exitArrayLength(ArrayLengthContext ctx);

	void exitType(TypeContext ctx);
	void exitBaseType(BaseTypeContext ctx);
	void exitLiteral(LiteralContext ctx);
	void beforeSelector(ParseTree parent);
	
}
