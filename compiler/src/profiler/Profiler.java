package profiler;

import java.util.Arrays;

import analysis.Symbol;
import analysis.SymbolTable;
import app.Settings;
import codegen.CodeGenerator;
import parser.YaplParser.ArithmeticExprContext;
import parser.YaplParser.ArrayLengthContext;
import parser.YaplParser.AssignmentContext;
import parser.YaplParser.BlockContext;
import parser.YaplParser.BooleanExprContext;
import parser.YaplParser.ComparisonContext;
import parser.YaplParser.CreationExprContext;
import parser.YaplParser.EqualityComparisonContext;
import parser.YaplParser.ExpressionContext;
import parser.YaplParser.FullIdentifierContext;
import parser.YaplParser.IfStatementContext;
import parser.YaplParser.LiteralContext;
import parser.YaplParser.ProcedureCallContext;
import parser.YaplParser.ReturnStatementContext;
import parser.YaplParser.StatementContext;
import parser.YaplParser.UnaryExprContext;
import parser.YaplParser.WhileStatementContext;
import parser.YaplParser.WriteStatementContext;

public class Profiler extends CodeGenerator {

  protected ProfilerBackend backend = null;

  protected int lastVardumpedLine = 0;
  protected int vardumpLineIdx = 0;
  protected int watchLineIdx = 0;
  protected Settings settings;

  public Profiler(SymbolTable symbolTable, Settings settings, ProfilerBackend backend) {
    super(symbolTable, backend);
    this.settings = settings;
    this.backend = backend;
  }

  @Override
  public Symbol visitAssignment(AssignmentContext ctx) {
    vardump(ctx.start.getLine());

    int line = ctx.expression().getStart().getLine();
    updateWatchIdx(line);
    if (shouldWatch(line)) {
      backend.loadConstant( new Symbol.Expression("string", "[PROFILER, line " + line + "] " + ctx.fullIdentifier().getText() + " = ") ).write();

      isAssignedTo = true;
      final Symbol.Variable sym = visitFullIdentifier( ctx.fullIdentifier() ).asVariable();
      isAssignedTo = false;

      visitExpression(ctx.expression(), true);
      backend.callFunction(symboltable.get("writeln").asFunction());

      backend.store(sym);
      return null;
    }

    return super.visitAssignment(ctx);
  }

  @Override
  public Symbol visitWriteStatement(WriteStatementContext ctx) {
    vardump(ctx.start.getLine());
    return super.visitWriteStatement(ctx);
  }

  @Override
  public Symbol visitProcedureCall(ProcedureCallContext ctx) {
    if (ctx.getParent() instanceof StatementContext) vardump(ctx.start.getLine());

    final Symbol.Function fn = symboltable.get(ctx.Id().getText()).asFunction();
    updateWatchIdx(ctx.start.getLine());

    if (shouldWatch(ctx.start.getLine()) || Arrays.asList(settings.trackedFunctions).contains(fn.name)) {
      backend.loadConstant( new Symbol.Expression("string", "[PROFILER, line " + ctx.start.getLine() + "] " + fn.name + "(") ).write();

      int paramIdx = 1;
      for (ExpressionContext expr : ctx.expression()) {
        visitExpression(expr, true);

        if (paramIdx < fn.params.size())
          backend.loadConstant(new Symbol.Expression("string", ", ")).write();
      }

      backend.loadConstant(new Symbol.Expression("string", ")")).write();
      backend.callFunction(symboltable.get("writeln").asFunction());
      backend.callFunction( symboltable.get(fn.name).asFunction() );
      return null;
    } 

    return super.visitProcedureCall(ctx);
  }

  @Override
  public Symbol visitIfStatement(IfStatementContext ctx) {
    vardump(ctx.start.getLine());

    int line = ctx.expression().getStart().getLine();
    updateWatchIdx(line);
    if (shouldWatch(line)) {
      backend.loadConstant( new Symbol.Expression("string", "[PROFILER, line " + line + "] if ") ).write();

      backend.startBranchingBlock();
      visitExpression(ctx.expression(), true);
      backend.callFunction(symboltable.get("writeln").asFunction());

      backend.branch();
      visitStatementList(ctx.statementList(0));

      if (ctx.elseStatementList != null) {
        backend.elseBranch();
        visitStatementList(ctx.elseStatementList);
      }

      backend.endBranchingBlock();
      return null;
    }

    Symbol result = super.visitIfStatement(ctx);
    vardump(ctx.stop.getLine());
    return result;
  }

  @Override
  public Symbol visitWhileStatement(WhileStatementContext ctx) {
    vardump(ctx.start.getLine());
    
    int line = ctx.expression().getStart().getLine();
    updateWatchIdx(line);
    if (shouldWatch(line)) {
      backend.startBranchingBlock();
      backend.loadConstant( new Symbol.Expression("string", "[PROFILER, line " + line + "] while ") ).write();
      visitExpression(ctx.expression(), true);
      backend.callFunction(symboltable.get("writeln").asFunction());

      backend.branch();
      visitStatementList(ctx.statementList());
      backend.loop();
      backend.endBranchingBlock();
      return null;
    }

    Symbol result = super.visitWhileStatement(ctx);
    vardump(ctx.stop.getLine());
    return result;
  }

  @Override
  public Symbol visitReturnStatement(ReturnStatementContext ctx) {
    vardump(ctx.start.getLine());
    
    if (ctx.expression() != null) {
      int line = ctx.expression().getStart().getLine();
      updateWatchIdx(line);
      if (shouldWatch(line)) {
        backend.loadConstant( new Symbol.Expression("string", "[PROFILER, line " + line + "] return ") ).write();
        visitExpression(ctx.expression(), true);
        backend.callFunction(symboltable.get("writeln").asFunction());
        backend.returnFromFunction();
        return null;
      }
    }
      
    return super.visitReturnStatement(ctx);
  }

  @Override
  public Symbol visitBlock(BlockContext ctx) {
    vardump(ctx.start.getLine());
    Symbol result = super.visitBlock(ctx);
    vardump(ctx.stop.getLine());
    return result;
  }



  public Symbol visitExpression(ExpressionContext ctx, boolean watch) {
    if (ctx instanceof ArithmeticExprContext) return visitArithmeticExpr((ArithmeticExprContext)ctx, watch);
    else if (ctx instanceof ComparisonContext) return visitComparison((ComparisonContext)ctx, watch);
    else if (ctx instanceof EqualityComparisonContext) return visitEqualityComparison((EqualityComparisonContext)ctx, watch);
    else if (ctx instanceof BooleanExprContext) return visitBooleanExpr((BooleanExprContext)ctx, watch);
    else if (ctx instanceof UnaryExprContext) return visitUnaryExpr((UnaryExprContext)ctx, watch);
    else if (ctx instanceof CreationExprContext) return visitCreationExpr((CreationExprContext)ctx, watch);
    return null; 
  }

  public Symbol visitArithmeticExpr(ArithmeticExprContext ctx, boolean watch) {
    final String exprName = Symbol.Expression.nameFor(ctx.hashCode(), ctx.getText());

    if (symboltable.contains(exprName) && symboltable.get(exprName).isConst()) {
      backend.loadConstant( symboltable.get(exprName).asConst() );
      if (watch) backend.dumpTopOfStack(symboltable, symboltable.get(exprName));
    }
    else {
      final String op = ctx.op.getText();
      visitExpression(ctx.expression(0), watch);
      if (watch) backend.loadConstant( new Symbol.Expression("string", " " + ctx.op.getText() + " ") ).write();
      visitExpression(ctx.expression(1), watch);
      backend.op2(op);
    }

    return null;
  }

  public Symbol visitComparison(ComparisonContext ctx, boolean watch) {
    final String exprName = Symbol.Expression.nameFor(ctx.hashCode(), ctx.getText());

    if (symboltable.contains(exprName) && symboltable.get(exprName).isConst()) {
      backend.loadConstant( symboltable.get(exprName).asConst() );
      if (watch) backend.dumpTopOfStack(symboltable, symboltable.get(exprName));
    }
    else {
      final String op = ctx.op.getText();
      visitExpression(ctx.expression(0), watch);
      if (watch) backend.loadConstant( new Symbol.Expression("string", " " + ctx.op.getText() + " ") ).write();
      visitExpression(ctx.expression(1), watch);
      backend.op2(op);
    }

    return null;
  }

  public Symbol visitEqualityComparison(EqualityComparisonContext ctx, boolean watch) {
    final String exprName = Symbol.Expression.nameFor(ctx.hashCode(), ctx.getText());

    if (symboltable.contains(exprName) && symboltable.get(exprName).isConst()) {
      backend.loadConstant( symboltable.get(exprName).asConst() );
      if (watch) backend.dumpTopOfStack(symboltable, symboltable.get(exprName));
    }
    else {
      final String op = ctx.op.getText();
      visitExpression(ctx.expression(0), watch);
      if (watch) backend.loadConstant( new Symbol.Expression("string", " " + ctx.op.getText() + " ") ).write();
      visitExpression(ctx.expression(1), watch);
      backend.op2(op);
    }
    
    return null;
  }

  public Symbol visitBooleanExpr(BooleanExprContext ctx, boolean watch) {
    final String exprName = Symbol.Expression.nameFor(ctx.hashCode(), ctx.getText());

    if (symboltable.contains(exprName) && symboltable.get(exprName).isConst()) {
      backend.loadConstant( symboltable.get(exprName).asConst() );
      if (watch) backend.dumpTopOfStack(symboltable, symboltable.get(exprName));
    }
    else {
      final String op = ctx.op.getText();
      backend.op2(op);
      visitExpression(ctx.expression(0), watch);
      if (watch) backend.loadConstant( new Symbol.Expression("string", " " + ctx.op.getText() + " ") ).write();
      visitExpression(ctx.expression(1), watch);
    }
    
    return null;
  }

  public Symbol visitUnaryExpr(UnaryExprContext ctx, boolean watch) {
    if (ctx.sign != null && watch) {
      backend.loadConstant( new Symbol.Expression("string", ctx.sign.getText()) ).write();
    }

    if (ctx.primaryExpr().literal() != null) visitLiteral(ctx.primaryExpr().literal(), watch);
    else if (ctx.primaryExpr().fullIdentifier() != null) visitFullIdentifier(ctx.primaryExpr().fullIdentifier(), watch);
    else if (ctx.primaryExpr().procedureCall() != null) visitProcedureCall(ctx.primaryExpr().procedureCall(), watch);
    else if (ctx.primaryExpr().arrayLength() != null) visitArrayLength(ctx.primaryExpr().arrayLength(), watch);
    else if (ctx.primaryExpr().expression() != null) {
      if (watch) backend.loadConstant( new Symbol.Expression("string", "(") ).write();
      visitExpression(ctx.primaryExpr().expression(), watch);
      if (watch) backend.loadConstant( new Symbol.Expression("string", ")") ).write();
    }


    if (ctx.sign != null) {
      final String op = ctx.sign.getText();
      backend.op1(op);
    }

    return null;
  }

  public Symbol visitCreationExpr(CreationExprContext ctx, boolean watch) {
    if (ctx.expression().size() > 0) {
      for (ExpressionContext expr : ctx.expression())
        visitExpression(expr, watch);

      final String baseType = ctx.baseType().getText();
      backend.newArray(baseType, ctx.expression().size());
    }
    else {
      final String type = ctx.baseType().getText();
      backend.newRecord(type);
    }

    return null;
  }

  public Symbol visitArrayLength(ArrayLengthContext ctx, boolean watch) {
    Symbol result = super.visitArrayLength(ctx);
    if (watch) backend.dumpTopOfStack(symboltable, new Symbol.Expression("int"));
    return result;
  }

  public Symbol visitProcedureCall(ProcedureCallContext ctx, boolean watch) {
    final Symbol.Function fn = symboltable.get(ctx.Id().getText()).asFunction();
    backend.loadConstant( new Symbol.Expression("string", fn.name + "(") ).write();

    int paramIdx = 1;
    for (ExpressionContext expr : ctx.expression()) {
      visitExpression(expr, true);

      if (paramIdx < fn.params.size())
        backend.loadConstant(new Symbol.Expression("string", ", ")).write();
    }

    backend.loadConstant(new Symbol.Expression("string", ")")).write();
    backend.callFunction( symboltable.get(fn.name).asFunction() );
    return null;
}

  public Symbol visitFullIdentifier(FullIdentifierContext ctx, boolean watch) {
    Symbol result = super.visitFullIdentifier(ctx);
    if (watch) backend.dumpTopOfStack(symboltable, result);
    return result;
  }

  public Symbol visitLiteral(LiteralContext ctx, boolean watch) {
    Symbol result = super.visitLiteral(ctx);
    if (watch) backend.dumpTopOfStack(symboltable, result);
    return result;
  }



  protected void vardump(int line) {
    boolean hasLinesToVardump = vardumpLineIdx < settings.vardumpLineNrs.length && lastVardumpedLine < settings.vardumpLineNrs[vardumpLineIdx];

    if (hasLinesToVardump && settings.vardumpLineNrs[vardumpLineIdx] <= line) {
      backend.vardump(symboltable, settings.vardumpLineNrs[vardumpLineIdx]);
      lastVardumpedLine = line;
      vardumpLineIdx++;
    }
  }

  protected void updateWatchIdx(int line) {
    while (watchLineIdx < settings.watchLineNrs.length && line > settings.watchLineNrs[watchLineIdx]) {
      watchLineIdx++;
    }
  }

  protected boolean shouldWatch(int line) {
    return (settings.watchAll || watchLineIdx < settings.watchLineNrs.length && line == settings.watchLineNrs[watchLineIdx]);
  }
}
