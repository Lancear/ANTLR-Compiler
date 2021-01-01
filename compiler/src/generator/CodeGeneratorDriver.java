package generator;

import analyser.SymbolTable;
import information.YaplConstants;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import parser.YaplBaseVisitor;
import parser.YaplParser.*;
import information.*;

import java.io.IOException;

public class CodeGeneratorDriver extends YaplBaseVisitor<Symbol> {

  protected SymbolTable symboltable = null;
  protected CodeGenerator backend = null;

  public CodeGeneratorDriver(SymbolTable symboltable, CodeGenerator backend) {
    this.symboltable = symboltable;
    symboltable.resetCursor();

    this.backend = backend;
  }

  @Override
  public Symbol visitProgram(ProgramContext ctx) {
    final String name = ctx.Id(0).getText();

    backend.enterProgram(name);
    symboltable.enterScope();

    // Declarations and Procedures
    for (ParseTree child : ctx.children) {
      if (child instanceof StatementListContext) break;
      visit(child);
    }

    // Main method
    backend.enterMainFunction();
    visitStatementList( ctx.statementList() );
    backend.exitMainFunction();

    symboltable.exitScope();

    try {
      backend.exitProgram();
    }
    catch (IOException ex) {
      ex.printStackTrace();
    }

    return null;
  }

  @Override
  public Symbol visitStatementList(StatementListContext ctx) {
    for (StatementContext stat : ctx.statement()) {
      visitStatement(stat);
    }

    return null;
  }

  @Override
  public Symbol visitWriteStatement(WriteStatementContext ctx) {
    final String stringLiteral = ctx.String().getText();
    final String value = stringLiteral.substring(1, stringLiteral.length() - 1);

    backend
      .loadConstant( new ConstantExpression(ctx, YaplConstants.STRING, value) )
      .write();

    return null;
  }

  @Override
  public Symbol visitProcedureCall(ProcedureCallContext ctx) {
    final String fnName = ctx.Id().getText();

    for (ExpressionContext expr : ctx.expression())
      visit(expr);

    backend
      .callFunction( symboltable.get(fnName).as(Procedure.class) );

    return null;
  }

  @Override
  public Symbol visitLiteral(LiteralContext ctx) {
    final ConstantExpression sym = (ctx.Boolean() != null)
      ? new ConstantExpression(ctx, YaplConstants.BOOL, ctx.Boolean().getText())
      : new ConstantExpression(ctx, YaplConstants.INT,  ctx.Number().getText());

    if (!(ctx.getParent() instanceof ConstDeclarationContext))
      backend.loadConstant(sym);

    return sym;
  }

  @Override
  public Symbol visitVarDeclaration(VarDeclarationContext ctx) {
    for (TerminalNode id : ctx.Id()) {
      final String name = id.getText();
      final Variable sym = symboltable.get(name).as(Variable.class);

      backend.allocVariable(sym);
    }

    return null;
  }

  protected boolean isAssignedTo = false;

  @Override
  public Symbol visitFullIdentifier(FullIdentifierContext ctx) {
    final String name = ctx.Id().getText();
    Symbol sym = symboltable.get(name);

    if (sym.is(Constant.class)) {
      backend.loadConstant( sym.as(Constant.class) );
      return sym;
    }

    if (ctx.selector() != null) {
      backend.load( sym.as(Variable.class) );
      sym = visitSelector(ctx.selector(), sym.as(Variable.class));
    }

    if (!isAssignedTo) {
      backend.load( sym.as(Variable.class) );
    }

    return sym;
  }

  public Symbol visitSelector(SelectorContext ctx, Variable sym) {
    if (ctx.expression() != null) {
      boolean wasAssignedTo = isAssignedTo;
      isAssignedTo = false;
      visit(ctx.expression());
      isAssignedTo = wasAssignedTo;
      sym = sym.selectElement();
    }
    else {
      final String field = ctx.Id().getText();
      sym = sym.selectField(symboltable, field);
    }

    if (ctx.selector() != null) {
      backend.load(sym);
      sym = visitSelector(ctx.selector(), sym).as(Variable.class);
    }

    return sym;
  }

  @Override
  public Symbol visitAssignment(AssignmentContext ctx) {
    isAssignedTo = true;
    final Variable sym = visitFullIdentifier( ctx.fullIdentifier() ).as(Variable.class);
    isAssignedTo = false;

    visit(ctx.expression());
    backend.store(sym);
    return null;
  }

  @Override
  public Symbol visitUnaryExpr(UnaryExprContext ctx) {
    visitPrimaryExpr(ctx.primaryExpr());

    if (ctx.sign != null) {
      final String op = ctx.sign.getText();
      backend.op1(op);
    }

    return null;
  }

  @Override
  public Symbol visitPrimaryExpr(PrimaryExprContext ctx) {
    if (ctx.literal() != null) visitLiteral(ctx.literal());
    else if (ctx.fullIdentifier() != null) visitFullIdentifier(ctx.fullIdentifier());
    else if (ctx.procedureCall() != null) visitProcedureCall(ctx.procedureCall());
    else if (ctx.arrayLength() != null) visitArrayLength(ctx.arrayLength());
    else if (ctx.expression() != null) visit(ctx.expression());
    return null;
  }

  @Override
  public Symbol visitArithmeticExpr(ArithmeticExprContext ctx) {
    final String exprName = ConstantExpression.nameOf(ctx);

    if (symboltable.contains(exprName) && symboltable.get(exprName).is(Constant.class)) {
      backend.loadConstant( symboltable.get(exprName).as(Constant.class) );
    }
    else {
      final String op = ctx.op.getText();
      visit(ctx.expression(0));
      visit(ctx.expression(1));
      backend.op2(op);
    }

    return null;
  }

  @Override
  public Symbol visitComparison(ComparisonContext ctx) {
    final String exprName = ConstantExpression.nameOf(ctx);

    if (symboltable.contains(exprName) && symboltable.get(exprName).is(Constant.class)) {
      backend.loadConstant( symboltable.get(exprName).as(Constant.class) );
    }
    else {
      final String op = ctx.op.getText();
      visit(ctx.expression(0));
      visit(ctx.expression(1));
      backend.op2(op);
    }

    return null;
  }

  @Override
  public Symbol visitEqualityComparison(EqualityComparisonContext ctx) {
    final String exprName = ConstantExpression.nameOf(ctx);

    if (symboltable.contains(exprName) && symboltable.get(exprName).is(Constant.class)) {
      backend.loadConstant( symboltable.get(exprName).as(Constant.class) );
    }
    else {
      final String op = ctx.op.getText();
      visit(ctx.expression(0));
      visit(ctx.expression(1));
      backend.op2(op);
    }

    return null;
  }

  /**
   * op first here, since the backend needs to know how to connect the 2 operand expressions in advance for lazy boolean evaluation.
   */
  @Override
  public Symbol visitBooleanExpr(BooleanExprContext ctx) {
    final String exprName = ConstantExpression.nameOf(ctx);

    if (symboltable.contains(exprName) && symboltable.get(exprName).is(Constant.class)) {
      backend.loadConstant( symboltable.get(exprName).as(Constant.class) );
    }
    else {
      final String op = ctx.op.getText();
      backend.op2(op);
      visit(ctx.expression(0));
      visit(ctx.expression(1));
    }

    return null;
  }

  @Override
  public Symbol visitCreationExpr(CreationExprContext ctx) {
    if (ctx.expression().size() > 0) {
      for (ExpressionContext expr : ctx.expression())
        visit(expr);

      final String baseType = ctx.baseType().getText();
      backend.newArray(baseType, ctx.expression().size());
    }
    else {
      final String type = ctx.baseType().getText();
      backend.newRecord(type);
    }

    return null;
  }

  @Override
  public Symbol visitArrayLength(ArrayLengthContext ctx) {
    visitFullIdentifier(ctx.fullIdentifier());
    backend.arraylength();
    return null;
  }

  @Override
  public Symbol visitIfStatement(IfStatementContext ctx) {
    backend.startBranchingBlock();
    visit(ctx.expression());
    backend.branch();
    visitStatementList(ctx.statementList(0));

    if (ctx.elseStatementList != null) {
      backend.elseBranch();
      visitStatementList(ctx.elseStatementList);
    }

    backend.endBranchingBlock();
    return null;
  }

  @Override
  public Symbol visitWhileStatement(WhileStatementContext ctx) {
    backend.startBranchingBlock();
    visit(ctx.expression());
    backend.branch();
    visitStatementList(ctx.statementList());
    backend.loop();
    backend.endBranchingBlock();
    return null;
  }

  @Override
  public Symbol visitBlock(BlockContext ctx) {
    symboltable.enterScope();

    if (ctx.declarationBlock() != null)
      visitDeclarationBlock(ctx.declarationBlock());

    visitStatementList(ctx.statementList());
    symboltable.exitScope();
    return null;
  }

  @Override
  public Symbol visitProcedure(ProcedureContext ctx) {
    final String name = ctx.Id(0).getText();
    backend.enterFunction(symboltable.get(name).as(Procedure.class));
    symboltable.enterScope();
    visitBlock(ctx.block());
    symboltable.exitScope();
    backend.exitFunction();
    return null;
  }

  @Override
  public Symbol visitReturnStatement(ReturnStatementContext ctx) {
    if (ctx.expression() != null)
      visit(ctx.expression());

    backend.returnFromFunction();
    return null;
  }

  @Override
  public Symbol visitRecordDeclaration(RecordDeclarationContext ctx) {
    final String name = ctx.Id().getText();

    symboltable.enterScope();
    backend.enterRecord(name);

    for (VarDeclarationContext field : ctx.varDeclaration())
      visitVarDeclaration(field);

    backend.exitRecord();
    symboltable.exitScope();
    return null;
  }

}
