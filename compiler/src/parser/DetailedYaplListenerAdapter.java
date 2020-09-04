package parser;

import org.antlr.v4.runtime.tree.TerminalNode;

import parser.YaplParser.*;

/**
 * This adapter allows you to use the {@code DetailedYaplListener} which has some additional more detailed events 
 * and removed events for nodes which are only interesting for the parser.
 */
public class DetailedYaplListenerAdapter extends YaplBaseListener {
  
  public final DetailedYaplListener listener;

  protected boolean programIdSet = false;
  protected boolean procedureIdSet = false;
  protected boolean recordIdSet = false;

  public DetailedYaplListenerAdapter(DetailedYaplListener listener) {
    this.listener = listener;
  }

  @Override
  public void enterProgram(ProgramContext ctx) {
    listener.enterProgram(ctx);
  }

  @Override
  public void exitProgram(ProgramContext ctx) {
    listener.exitProgram(ctx);
    programIdSet = false;
  }

  @Override
  public void enterDeclarationBlock(DeclarationBlockContext ctx) {
    if (!programIdSet && ctx.getParent() instanceof ProgramContext) {
      listener.afterProgramId((ProgramContext)ctx.getParent());
      programIdSet = true;
    }
  }

  @Override
  public void exitVarDeclaration(VarDeclarationContext ctx) {
    if (!recordIdSet && ctx.getParent() instanceof RecordDeclarationContext) {
      listener.afterRecordId((RecordDeclarationContext)ctx.getParent());
      recordIdSet = true;
    }

    listener.exitVarDeclaration(ctx);
  }

  @Override
  public void exitConstDeclaration(ConstDeclarationContext ctx) {
    listener.exitConstDeclaration(ctx);
  }

  @Override
  public void exitRecordDeclaration(RecordDeclarationContext ctx) {
    listener.exitRecordDeclaration(ctx);
    recordIdSet = false;
  }

  @Override
  public void enterProcedure(ProcedureContext ctx) {
    if (!programIdSet && ctx.getParent() instanceof ProgramContext) {
      listener.afterProgramId((ProgramContext)ctx.getParent());
      programIdSet = true;
    }
  }

  @Override
  public void exitProcedure(ProcedureContext ctx) {
    listener.exitProcedure(ctx);
    procedureIdSet = false;
  }

  @Override
  public void enterBlock(BlockContext ctx) {
    if (!procedureIdSet && ctx.getParent() instanceof ProcedureContext) {
      listener.afterProcedureId((ProcedureContext)ctx.getParent());
      procedureIdSet = true;
    }

    listener.enterBlock(ctx);
  }

  @Override
  public void exitBlock(BlockContext ctx) {
    listener.exitBlock(ctx);
  }

  @Override
  public void enterStatementList(StatementListContext ctx) {
    if (!programIdSet && ctx.getParent() instanceof ProgramContext) {
      listener.afterProgramId((ProgramContext)ctx.getParent());
      programIdSet = true;
    }

    listener.enterStatementList(ctx);
  }

  @Override
  public void exitStatementList(StatementListContext ctx) {
    listener.exitStatementList(ctx);
  }

  @Override
  public void exitAssignment(AssignmentContext ctx) {
    listener.exitAssignment(ctx);
  }

  @Override
  public void exitReturnStatement(ReturnStatementContext ctx) {
    listener.exitReturnStatement(ctx);
  }

  @Override
  public void exitWriteStatement(WriteStatementContext ctx) {
    listener.exitWriteStatement(ctx);
  }

  @Override
  public void exitIfStatement(IfStatementContext ctx) {
    listener.exitIfStatement(ctx);
  }
  
  @Override
  public void exitWhileStatement(WhileStatementContext ctx) {
    listener.exitWhileStatement(ctx);
  }

  public void afterExpression(ExpressionContext ctx) {
    if (ctx.getParent() instanceof SelectorContext) {
      listener.beforeSelector(ctx.getParent());
    }
  }

  @Override
  public void exitUnaryExpr(UnaryExprContext ctx) {
    listener.exitUnaryExpr(ctx);
    afterExpression(ctx);
  }

  @Override
  public void exitArithmeticExpr(ArithmeticExprContext ctx) {
    listener.exitArithmeticExpr(ctx);
    afterExpression(ctx);
  }

  @Override
  public void exitComparison(ComparisonContext ctx) {
    listener.exitComparison(ctx);
    afterExpression(ctx);
  }

  @Override
  public void exitEqualityComparison(EqualityComparisonContext ctx) {
    listener.exitEqualityComparison(ctx);
    afterExpression(ctx);
  }

  @Override
  public void exitBooleanExpr(BooleanExprContext ctx) {
    listener.exitBooleanExpr(ctx);
    afterExpression(ctx);
  }

  @Override
  public void exitCreationExpr(CreationExprContext ctx) {
    listener.exitCreationExpr(ctx);
    afterExpression(ctx);
  }

  @Override
  public void exitArrayLength(ArrayLengthContext ctx) {
    listener.exitArrayLength(ctx);
  }

  @Override
  public void exitProcedureCall(ProcedureCallContext ctx) {
    listener.exitProcedureCall(ctx);
  }

  @Override
  public void enterParam(ParamContext ctx) {
    if (!procedureIdSet) {
      listener.afterProcedureId((ProcedureContext)ctx.getParent());
      procedureIdSet = true;
    }
  }

  @Override
  public void exitParam(ParamContext ctx) {
    listener.exitParam(ctx);
  }

  @Override
  public void exitReturnType(ReturnTypeContext ctx) {
    listener.exitReturnType(ctx);
  }

  @Override
  public void exitType(TypeContext ctx) {
    listener.exitType(ctx);
  }
  
  @Override
  public void exitBaseType(BaseTypeContext ctx) {
    listener.exitBaseType(ctx);
  }

  @Override
  public void exitLiteral(LiteralContext ctx) {
    listener.exitLiteral(ctx);
  }

  @Override
  public void visitTerminal(TerminalNode node) {
    final boolean isId = (node.getSymbol().getType() == YaplParser.Id);
    if (isId && (node.getParent() instanceof SelectorContext || node.getParent() instanceof FullIdentifierContext)) {
      listener.beforeSelector(node.getParent());
    }
  }

}
