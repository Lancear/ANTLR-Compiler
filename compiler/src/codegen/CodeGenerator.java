package codegen;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import parser.YaplBaseVisitor;
import parser.YaplParser;
import parser.YaplParser.ArithmeticExprContext;
import parser.YaplParser.ArrayLengthContext;
import parser.YaplParser.AssignmentContext;
import parser.YaplParser.BlockContext;
import parser.YaplParser.BooleanExprContext;
import parser.YaplParser.ComparisonContext;
import parser.YaplParser.ConstDeclarationContext;
import parser.YaplParser.CreationExprContext;
import parser.YaplParser.EqualityComparisonContext;
import parser.YaplParser.ExpressionContext;
import parser.YaplParser.FullIdentifierContext;
import parser.YaplParser.IfStatementContext;
import parser.YaplParser.LiteralContext;
import parser.YaplParser.ParamContext;
import parser.YaplParser.ProcedureCallContext;
import parser.YaplParser.ProcedureContext;
import parser.YaplParser.ProgramContext;
import parser.YaplParser.SelectorContext;
import parser.YaplParser.StatementContext;
import parser.YaplParser.UnaryExprContext;
import parser.YaplParser.VarDeclarationContext;
import parser.YaplParser.WhileStatementContext;
import parser.YaplParser.WriteStatementContext;
import stdlib.StandardLibrary;
import symboltable.SymbolTable;
import symboltable.symbols.ConstSymbol;
import symboltable.symbols.ProcedureSymbol;
import symboltable.symbols.Symbol;
import symboltable.symbols.VariableSymbol;

public class CodeGenerator extends YaplBaseVisitor<CodeGenInfo> {

  protected final static String INT = "int";
  protected final static String BOOL = "bool";
  protected final static String STRING = "string";

  protected SymbolTable symboltable = null;
  protected Backend backend = null;
  protected boolean isLhs = false;
  protected boolean isArrayElement = false;
  protected Stack<Symbol> selectedSymbol = new Stack<>();

  public CodeGenerator(StandardLibrary stdlib, String outputDir, Backend backend) {
    this.symboltable = new SymbolTable();
    stdlib.addToSymbolTable(symboltable);

    this.backend = backend;
    backend.stdlib = stdlib;
    backend.symbolTable = symboltable;
    backend.outputDir = Paths.get(outputDir);
  }

  @Override
  public CodeGenInfo visitProgram(ProgramContext ctx) {
    backend.enterProgram( ctx.Id(0).getText() );
    
    // Declarations and Procedures
    for (ParseTree child : ctx.children) {
      if (child instanceof StatementContext)
        break;

      visit(child);
    }

    // Main method
    backend.enterMainFunction();

    for (StatementContext stat : ctx.statement()) {
      visit(stat);
    }

    backend.exitMainFunction();

    try {
      backend.exitProgram();
    } 
    catch (IOException ex) {
      ex.printStackTrace();
    }

    return null;
  }

  @Override
  public CodeGenInfo visitProcedure(ProcedureContext ctx) {
    final String returnType = (ctx.type() != null) ? visitType( ctx.type() ).type : "void";
    final ProcedureSymbol symbol = new ProcedureSymbol(ctx.Id(0).getText(), returnType, ctx.start);

    final List<String> paramTypes = new ArrayList<>();
    for (ParamContext param : ctx.param()) {
      final CodeGenInfo info = visitParam(param);
      paramTypes.add(info.type);
    }

    symbol.setParamTypes(paramTypes);

    backend.enterFunction(symbol);
    visitBlock(ctx.block());
    backend.exitFunction();
    return null;
  }

  @Override
  public CodeGenInfo visitParam(ParamContext ctx) {
    return new CodeGenInfo(ctx.type().getText());
  }

  @Override
  public CodeGenInfo visitWriteStatement(WriteStatementContext ctx) {
    String str = ctx.String().toString();
    String strWithoutQuotes = str.substring( 1, str.length() - 1 );
    
    backend
      .loadConstant(strWithoutQuotes, STRING)
      .callFunction("write");
    return null;
  }

  @Override
  public CodeGenInfo visitProcedureCall(ProcedureCallContext ctx) {
    for (ExpressionContext expr : ctx.expression()) {
      visit(expr);
    }

    backend.callFunction(ctx.Id().getText());
    return null;
  }

  @Override
  public CodeGenInfo visitLiteral(LiteralContext ctx) {
    if (ctx.Boolean() != null) {
      backend.loadConstant(ctx.Boolean().getText(), BOOL);
      return new CodeGenInfo(ctx.Boolean().getText(), BOOL);
    }
    else {
      backend.loadConstant(ctx.Number().getText(), INT);
      return new CodeGenInfo(ctx.Number().getText(), INT);
    }
  }

  @Override
  public CodeGenInfo visitConstDeclaration(ConstDeclarationContext ctx) {
    String type = (ctx.literal().Boolean() != null) ? BOOL : INT;
    Symbol symbol = new ConstSymbol(ctx.Id().getText(), type, ctx.literal().getText(), ctx.start);
    symboltable.addSymbol(symbol);
    return new CodeGenInfo(symbol);
  }

  @Override
  public CodeGenInfo visitVarDeclaration(VarDeclarationContext ctx) {
    String type = ctx.type().getText();

    for (TerminalNode id : ctx.Id()) {
      VariableSymbol symbol = new VariableSymbol(id.getText(), type, false, id.getSymbol());
      symboltable.addSymbol(symbol);
      backend.allocLocal(symbol);
    }

    return new CodeGenInfo(type);
  }

  @Override
  public CodeGenInfo visitAssignment(AssignmentContext ctx) {
    isArrayElement = false;
    isLhs = true;
    VariableSymbol symbol = (VariableSymbol)visitFullIdentifier(ctx.fullIdentifier()).symbol;
    isLhs = false;
    visit(ctx.expression());

    backend.store(symbol, isArrayElement);
    return null;
  }

  @Override
  public CodeGenInfo visitUnaryExpr(UnaryExprContext ctx) {
    CodeGenInfo expr = visit(ctx.primaryExpr());
    
    if (ctx.sign != null) 
      backend.op1(ctx.sign.getText());

    return new CodeGenInfo(expr != null ? expr.type : null);
  }

  @Override
  public CodeGenInfo visitArithmeticExpr(ArithmeticExprContext ctx) {
    CodeGenInfo lhs = visit(ctx.expression(0));
    CodeGenInfo rhs = visit(ctx.expression(1));
    CodeGenInfo info = new CodeGenInfo(INT);
    
    backend.op2(ctx.op.getText());
    return info;
  }

  @Override
  public CodeGenInfo visitBooleanExpr(BooleanExprContext ctx) {
    if (!(ctx.expression(0) instanceof BooleanExprContext))
      backend.op2(ctx.op.getText());

    CodeGenInfo lhs = visit(ctx.expression(0));

    if (ctx.expression(0) instanceof BooleanExprContext)
      backend.op2(ctx.op.getText());

    CodeGenInfo rhs = visit(ctx.expression(1));
    CodeGenInfo info = new CodeGenInfo(BOOL);    
    return info;
  }

  @Override
  public CodeGenInfo visitComparison(ComparisonContext ctx) {
    backend.startCompareOp();
    CodeGenInfo lhs = visit(ctx.expression(0));
    CodeGenInfo rhs = visit(ctx.expression(1));
    CodeGenInfo info = new CodeGenInfo(BOOL);
    
    backend.compareOp(ctx.op.getText());
    return info;
  }

  @Override
  public CodeGenInfo visitEqualityComparison(EqualityComparisonContext ctx) {
    backend.startCompareOp();
    CodeGenInfo lhs = visit(ctx.expression(0));
    CodeGenInfo rhs = visit(ctx.expression(1));
    CodeGenInfo info = new CodeGenInfo(BOOL);
    
    backend.compareOp(ctx.op.getText());
    return info;
  }

  @Override
  public CodeGenInfo visitFullIdentifier(FullIdentifierContext ctx) {
    selectedSymbol.push(symboltable.get(ctx.Id().getText()));

    if (selectedSymbol.peek() instanceof ConstSymbol) {
      ConstSymbol constSymbol = (ConstSymbol)selectedSymbol.peek();
      backend.loadConstant(constSymbol.value, constSymbol.type);
      selectedSymbol.pop();
      return new CodeGenInfo(constSymbol.type, constSymbol.value);
    }

    if (!isLhs || ctx.selector() != null) {
      backend.load((VariableSymbol)selectedSymbol.peek(), false);
    }

    if (ctx.selector() != null) {
      isArrayElement = true;
      return visit(ctx.selector());
    }

    return new CodeGenInfo(selectedSymbol.pop());
  }

  @Override
  public CodeGenInfo visitSelector(SelectorContext ctx) {
    boolean lhs = isLhs;
    isLhs = false;
    visit(ctx.expression());
    isLhs = lhs;

    if (!isLhs || ctx.selector() != null) {
      backend.load((VariableSymbol)selectedSymbol.peek(), isArrayElement);
    }

    return new CodeGenInfo(selectedSymbol.peek());
  }

  @Override
  public CodeGenInfo visitCreationExpr(CreationExprContext ctx) {
    if (ctx.expression().size() == 1) {
      visit(ctx.expression(0));
      backend.newArray(ctx.baseType().getText());
    }

    return null;
  }

  @Override
  public CodeGenInfo visitArrayLength(ArrayLengthContext ctx) {
    visit(ctx.fullIdentifier());
    backend.arraylength();
    return null;
  }

  @Override
  public CodeGenInfo visitIfStatement(IfStatementContext ctx) {
    visit(ctx.expression());
    backend.ifThen();

    boolean elseStat = false;
    for (StatementContext stat : ctx.statement()) {
      if (!elseStat && stat.start.getTokenIndex() > ctx.elseThen.getTokenIndex()) {
        elseStat = true;
        backend.elseThen();
      }

      visit(stat);
    }

    backend.endIf();
    return null;
  }

  @Override
  public CodeGenInfo visitWhileStatement(WhileStatementContext ctx) {
    backend.startWhile();
    visit(ctx.expression());
    backend.whileDo();

    for (StatementContext stat : ctx.statement()) {
      visit(stat);
    }

    backend.endWhile();
    return null;
  }

  @Override
  public CodeGenInfo visitBlock(BlockContext ctx) {
    backend.startBlock();

    visitDeclarationBlock(ctx.declarationBlock());
    for (StatementContext stat : ctx.statement()) {
      visit(stat);
    }

    backend.endBlock();
    return null;
  }

}
