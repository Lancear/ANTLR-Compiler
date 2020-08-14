package codegen;

import java.io.IOException;
import java.nio.file.Paths;

import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import analysis.Symbol;
import analysis.SymbolTable;
import parser.YaplBaseVisitor;
import parser.YaplParser.*;
import stdlib.StandardLibrary;

public class CodeGenerator extends YaplBaseVisitor<Symbol> {

  protected final static String INT = "int";
  protected final static String BOOL = "bool";
  protected final static String STRING = "string";

  protected SymbolTable symboltable = null;
  protected Backend backend = null;

  public CodeGenerator(StandardLibrary stdlib, SymbolTable symbolTable, String outputDir, Backend backend) {
    backend.stdlib = stdlib;
    this.symboltable = symbolTable;
    
    symbolTable.resetCursor();
    symbolTable.add( new Symbol.Function("write", "void", List.of(new Symbol.Param("str", "string")), true) );

    this.backend = backend;
    backend.symbolTable = symboltable;
    backend.outputDir = Paths.get(outputDir);
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
      .loadConstant( new Symbol.Const("<literal>", "string", value) )
      .callFunction( symboltable.get("write").asFunction() );

    return null;
  }

  @Override
  public Symbol visitProcedureCall(ProcedureCallContext ctx) {
    final String fnName = ctx.Id().getText();

    for (ExpressionContext expr : ctx.expression())
      visit(expr);

    backend
      .callFunction( symboltable.get(fnName).asFunction() );

    return null;
  }

  @Override
  public Symbol visitLiteral(LiteralContext ctx) {
    final Symbol.Const sym = (ctx.Boolean() != null) 
      ? new Symbol.Const("<literal>", "bool", ctx.Boolean().getText())
      : new Symbol.Const("<literal>", "int", ctx.Number().getText());


    if (!(ctx.getParent() instanceof ConstDeclarationContext))
      backend.loadConstant(sym);

    return sym;
  }

  @Override
  public Symbol visitVarDeclaration(VarDeclarationContext ctx) {
    for (TerminalNode id : ctx.Id()) {
      final String name = id.getText();
      final Symbol.Variable sym = symboltable.get(name).asVariable();

      backend.allocVariable(sym);
    }

    return null;
  }

  protected boolean isAssignedTo = false;

  @Override
  public Symbol visitFullIdentifier(FullIdentifierContext ctx) {
    final String name = ctx.Id().getText();
    Symbol sym = symboltable.get(name);

    if (sym.isConst()) {
      backend.loadConstant( sym.asConst() );
      return sym;
    }

    if (ctx.selector() != null) {
      backend.load( sym.asVariable() );
      sym = visitSelector(ctx.selector(), sym.asVariable());
    }

    if (!isAssignedTo) {
      backend.load( sym.asVariable() );
    }

    return sym;
  }

  public Symbol visitSelector(SelectorContext ctx, Symbol.Variable sym) {
    boolean wasAssignedTo = isAssignedTo;
    isAssignedTo = false;
    visit(ctx.expression());
    isAssignedTo = wasAssignedTo;
    return sym.selectElement();
  }

  @Override
  public Symbol visitAssignment(AssignmentContext ctx) {
    isAssignedTo = true;
    final Symbol.Variable sym = visitFullIdentifier( ctx.fullIdentifier() ).asVariable();
    isAssignedTo = false;

    System.out.println(ctx.getText() + " (sym.isLocal: " + sym.isLocal + ")");

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
    visit(ctx.expression(0));
    visit(ctx.expression(1));
    
    final String op = ctx.op.getText();
    backend.op2(op);
    return null;
  }

  protected boolean isBoolExpr = false;

  @Override
  public Symbol visitComparison(ComparisonContext ctx) {
    boolean isBoolRoot = !isBoolExpr;

    if (isBoolRoot) {
      backend.start();
      isBoolExpr = true;
    }

    visit(ctx.expression(0));
    visit(ctx.expression(1));
    
    final String op = ctx.op.getText();
    backend.op2(op);

    if (isBoolRoot) {
      backend.end();
      isBoolExpr = false;
    }

    return null;
  }

  @Override
  public Symbol visitEqualityComparison(EqualityComparisonContext ctx) {
    boolean isBoolRoot = !isBoolExpr;

    if (isBoolRoot) {
      backend.start();
      isBoolExpr = true;
    }

    visit(ctx.expression(0));
    visit(ctx.expression(1));
    
    final String op = ctx.op.getText();
    backend.op2(op);

    if (isBoolRoot) {
      backend.end();
      isBoolExpr = false;
    }
    
    return null;
  }

  @Override
  public Symbol visitBooleanExpr(BooleanExprContext ctx) {
    boolean isBoolRoot = !isBoolExpr;

    if (isBoolRoot) {
      backend.start();
      isBoolExpr = true;
    }

    final String op = ctx.op.getText();
    if (!(ctx.expression(0) instanceof BooleanExprContext || ctx.expression(1) instanceof BooleanExprContext)) {
      backend.op2(op);
      visit(ctx.expression(0));
      visit(ctx.expression(1));
    }
    else {
      visit(ctx.expression(0));
      backend.op2(op);
      visit(ctx.expression(1));
    }

    if (isBoolRoot) {
      backend.end();
      isBoolExpr = false;
    }

    return null;
  }

  @Override
  public Symbol visitCreationExpr(CreationExprContext ctx) {
    for(ExpressionContext expr : ctx.expression())
      visit(expr);

    final String baseType = ctx.baseType().getText();
    backend.newArray(baseType);
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
    backend.start();
    visit(ctx.expression());
    backend.branch();
    visitStatementList(ctx.statementList(0));

    if (ctx.elseStatementList != null) {
      backend.elseBranch();
      visitStatementList(ctx.elseStatementList);
    }

    backend.end();
    return null;
  }

  @Override
  public Symbol visitWhileStatement(WhileStatementContext ctx) {
    backend.start();
    visit(ctx.expression());
    backend.branch();
    visitStatementList(ctx.statementList());
    backend.loop();
    backend.end();
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
    backend.enterFunction(symboltable.get(name).asFunction());
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
    
    backend.returnFunction();
    return null;
  }

}
