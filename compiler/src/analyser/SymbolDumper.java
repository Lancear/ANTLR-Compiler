package analyser;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import parser.YaplBaseVisitor;
import parser.YaplParser.*;
import information.*;

public class SymbolDumper extends YaplBaseVisitor<Void> {

  protected SymbolTable symboltable = null;
  protected String indent = "";

  public SymbolDumper(SymbolTable symbolTable) {
    this.symboltable = symbolTable;
    symbolTable.resetCursor();
  }

  @Override
  public Void visitProgram(ProgramContext ctx) {
    final String name = ctx.Id(0).getText();
    System.out.println("PROGRAM " + name + ":");

    symboltable.enterScope();
    indent += "  ";

    // Declarations and Procedures
    for (ParseTree child : ctx.children) {
      if (child instanceof StatementListContext) break;
      visit(child);
    }

    visitStatementList( ctx.statementList() );

    indent = indent.substring(2);
    symboltable.exitScope();
    return null;
  }

  @Override
  public Void visitStatementList(StatementListContext ctx) {
    if (ctx.getParent() instanceof ProgramContext) {
      System.out.println();
      System.out.println("main:");
    }

    for (StatementContext stat : ctx.statement()) {
      visitStatement(stat);
    }

    return null;
  }

  @Override
  public Void visitBlock(BlockContext ctx) {
    symboltable.enterScope();
    indent += "  ";

    if (ctx.declarationBlock() != null)
      visitDeclarationBlock(ctx.declarationBlock());

    visitStatementList(ctx.statementList());

    indent = indent.substring(2);
    symboltable.exitScope();
    return null;
  }

  @Override
  public Void visitProcedure(ProcedureContext ctx) {
    final String name = ctx.Id(0).getText();
    System.out.println(indent + symboltable.get(name));

    symboltable.enterScope();
    indent += "  ";

    for(Symbol sym : symboltable.currScope.symbols.values()) {
      System.out.println(indent + sym);
    }

    visitBlock(ctx.block());

    System.out.println();
    indent = indent.substring(2);
    symboltable.exitScope();

    return null;
  }

  @Override
  public Void visitRecordDeclaration(RecordDeclarationContext ctx) {
    final String name = ctx.Id().getText();
    System.out.println(indent + symboltable.get(name));

    symboltable.enterScope();
    indent += "  ";

    for(Symbol sym : symboltable.currScope.symbols.values()) {
      System.out.println(indent + sym);
    }

    indent = indent.substring(2);
    symboltable.exitScope();

    return null;
  }

  @Override
  public Void visitVarDeclaration(VarDeclarationContext ctx) {
    for (TerminalNode id : ctx.Id()) {
      final String name = id.getText();
      System.out.println(indent + symboltable.get(name));
    }

    return null;
  }

  @Override
  public Void visitConstDeclaration(ConstDeclarationContext ctx) {
    final String name = ctx.Id().getText();
    System.out.println(indent + symboltable.get(name));

    return null;
  }

  @Override
  public Void visitArithmeticExpr(ArithmeticExprContext ctx) {
    final String exprName = ConstantExpression.nameOf(ctx);

    if (symboltable.contains(exprName))
      System.out.println(indent + symboltable.get(exprName));

    return null;
  }

  @Override
  public Void visitComparison(ComparisonContext ctx) {
    final String exprName = ConstantExpression.nameOf(ctx);

    if (symboltable.contains(exprName))
      System.out.println(indent + symboltable.get(exprName));

    return null;
  }

  @Override
  public Void visitEqualityComparison(EqualityComparisonContext ctx) {
    final String exprName = ConstantExpression.nameOf(ctx);

    if (symboltable.contains(exprName))
      System.out.println(indent + symboltable.get(exprName));

    return null;
  }

  @Override
  public Void visitBooleanExpr(BooleanExprContext ctx) {
    final String exprName = ConstantExpression.nameOf(ctx);

    if (symboltable.contains(exprName))
      System.out.println(indent + symboltable.get(exprName));

    return null;
  }


}
