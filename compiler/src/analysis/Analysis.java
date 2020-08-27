package analysis;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import app.CompilerErrors;
import app.CompilerErrors.CompilerError;
import parser.DetailedYaplListener;
import parser.YaplParser.*;
import stdlib.StandardLibrary;

public class Analysis implements DetailedYaplListener {

  protected Stack<Symbol> symbolTrace;
  public final SymbolTable symbolTable;
  public final List<CompilerError> errors;

  protected boolean isLocal = false;
  protected boolean wasLocal = false;
  protected boolean hasReturn = false;

  protected String programName = Symbol.UNDEFINED;
  protected String procedureName = Symbol.UNDEFINED;
  protected String recordName = Symbol.UNDEFINED;

  public Analysis(StandardLibrary stdlib) {
    this.symbolTrace = new Stack<>();
    this.symbolTable = new SymbolTable();
    this.errors = new ArrayList<>();

    stdlib.addToSymbolTable(symbolTable);
  }

  @Override
  public void enterProgram(ProgramContext ctx) {
    symbolTable.openScope();
  }

  @Override
  public void afterProgramId(ProgramContext ctx) {
    this.programName = ctx.Id(0).getText();
  }

  @Override
  public void exitProgram(ProgramContext ctx) {
    final String name = ctx.Id(0).getText();
    final String endName = ctx.Id(1).getText();

    if (!endName.equals(name)) {
      errors.add(CompilerErrors.EndIdentMismatch(programName, name, endName, "program", ctx.Id(0).getSymbol()));
    }

    symbolTable.closeScope();
  }

  @Override
  public void exitVarDeclaration(VarDeclarationContext ctx) {
    String type = symbolTrace.pop().type;

    for (TerminalNode id : ctx.Id()) {
      String name = id.getText();

      if (symbolTable.currScope.contains(name)) {
        errors.add(CompilerErrors.SymbolExists(programName, name, symbolTable.get(name).kind, id.getSymbol()));
      }

      final Symbol symbol = new Symbol.Variable(name, type, isLocal);
      symbolTable.add(symbol);
    }
  }

  @Override
  public void exitConstDeclaration(ConstDeclarationContext ctx) {
    final String name = ctx.Id().getText();
    Symbol.Expression rhs = symbolTrace.pop().asExpression();

    if (symbolTable.currScope.contains(name)) {
      errors.add(CompilerErrors.SymbolExists(programName, name, symbolTable.get(name).kind, ctx.Id().getSymbol()));
    }

    symbolTable.add(new Symbol.Const(name, rhs.type, rhs.value));
  }

  @Override
  public void afterRecordId(RecordDeclarationContext ctx) {
    this.recordName = ctx.Id().getText();

    if (symbolTable.currScope.contains(recordName)) {
      errors.add(CompilerErrors.SymbolExists(programName, recordName, symbolTable.get(recordName).kind, ctx.Id().getSymbol()));
    }

    symbolTable.add(new Symbol.Record(recordName));
    symbolTable.openScope();
    wasLocal = isLocal;
    isLocal = false;
  }

  @Override
  public void exitRecordDeclaration(RecordDeclarationContext ctx) {
    Symbol.Record record = symbolTable.get(recordName).asRecord();

    for (Symbol sym : symbolTable.currScope.symbols.values()) {
      record.fields.put(sym.name, (Symbol.Variable) sym);
    }

    symbolTable.closeScope();
    isLocal = wasLocal;
    this.recordName = Symbol.UNDEFINED;
  }

  @Override
  public void exitReturnType(ReturnTypeContext ctx) {
    if (ctx.type() != null && !symbolTrace.peek().isError()) {
      symbolTrace.pop();
      symbolTrace.push(new Symbol.Type(ctx.getText()));
    } else if (ctx.type() == null) {
      symbolTrace.push(new Symbol.Type(ctx.getText()));
    }
  }

  @Override
  public void afterProcedureId(ProcedureContext ctx) {
    this.procedureName = ctx.Id(0).getText();

    if (symbolTable.currScope.contains(procedureName)) {
      errors.add(CompilerErrors.SymbolExists(programName, procedureName, symbolTable.get(procedureName).kind, ctx.Id(0).getSymbol()));
    }

    symbolTable.add(new Symbol.Function(procedureName, symbolTrace.pop().type));
    symbolTable.openScope();
    hasReturn = false;
  }

  @Override
  public void exitParam(ParamContext ctx) {
    final String name = ctx.Id().getText();
    Symbol.Param sym = new Symbol.Param(name, symbolTrace.pop().type);

    if (symbolTable.currScope.contains(name)) {
      errors.add(CompilerErrors.SymbolExists(programName, name, symbolTable.get(name).kind, ctx.Id().getSymbol()));
    } else {
      symbolTable.add(sym);
    }

    symbolTable.get(procedureName, symbolTable.currScope.parent).asFunction().params.add(sym);
  }

  @Override
  public void exitProcedure(ProcedureContext ctx) {
    final String name = ctx.Id(0).getText();
    final String endName = ctx.Id(1).getText();

    if (!endName.equals(name)) {
      errors.add(CompilerErrors.EndIdentMismatch(programName, name, endName, symbolTable.get(name).kind, ctx.Id(0).getSymbol()));
    }

    symbolTable.closeScope();
    isLocal = false;

    if (!symbolTable.get(name).type.equals(Symbol.VOID) && !hasReturn) {
      errors.add(CompilerErrors.MissingReturn(programName, name, ctx.block().stop));
    }

    this.procedureName = Symbol.UNDEFINED;
  }

  @Override
  public void enterBlock(BlockContext ctx) {
    symbolTable.openScope();
    if (ctx.getParent() instanceof ProcedureContext) {
      isLocal = true;
    }
  }

  @Override
  public void enterStatementList(StatementListContext ctx) {
    if (ctx.getParent() instanceof ProgramContext) {
      isLocal = true;
    }
  }

  @Override
  public void exitStatementList(StatementListContext ctx) {
    if (ctx.getParent() instanceof ProgramContext) {
      isLocal = false;
    }
  }

  @Override
  public void exitBlock(BlockContext ctx) {
    symbolTable.closeScope();
  }

  @Override
  public void exitWriteStatement(WriteStatementContext ctx) {

  }

  @Override
  public void exitReturnStatement(ReturnStatementContext ctx) {
    this.hasReturn = true;

    if (procedureName == Symbol.UNDEFINED && ctx.expression() != null) {
      errors.add( CompilerErrors.IllegalRetValMain(programName, ctx.expression().start) );
    }
    else if (procedureName != Symbol.UNDEFINED && ctx.expression() != null) {
      final Symbol returnSym = symbolTrace.pop();
      
      if (symbolTable.get(procedureName).type.equals(Symbol.VOID)) {
        errors.add( CompilerErrors.IllegalRetValProc(programName, procedureName, ctx.expression().start) );
      }
      else if (!returnSym.type.equals(symbolTable.get(procedureName).type) && !returnSym.isError()) {
        Token token = (ctx.expression() != null) ? ctx.expression().start : ctx.stop;
        errors.add( CompilerErrors.InvalidReturnType(programName, procedureName, token) );
      }
    }
    else if (ctx.expression() == null && procedureName != Symbol.UNDEFINED && !symbolTable.get(procedureName).type.equals(Symbol.VOID)) {
      Token token = (ctx.expression() != null) ? ctx.expression().start : ctx.stop;
      errors.add( CompilerErrors.InvalidReturnType(programName, procedureName, token) );
    }
  }

  @Override
  public void exitAssignment(AssignmentContext ctx) {
    final Symbol rhs = symbolTrace.pop();
    final Symbol lhs = symbolTrace.pop();

    if (!lhs.isError() && !rhs.isError() && !lhs.type.equals(rhs.type)) {
      errors.add( CompilerErrors.TypeMismatchAssign(programName, ctx.op) );
    }
  }

  @Override
  public void exitIfStatement(IfStatementContext ctx) {
    Symbol sym = symbolTrace.pop();

    if (!sym.isError() && !sym.type.equals(Symbol.BOOL)) {
      errors.add( CompilerErrors.CondNotBool(programName, ctx.expression().start) );
    }
  }

  @Override
  public void exitWhileStatement(WhileStatementContext ctx) {
    Symbol sym = symbolTrace.pop();

    if (!sym.isError() && !sym.type.equals(Symbol.BOOL)) {
      errors.add( CompilerErrors.CondNotBool(programName, ctx.expression().start) );
    }
  }

  @Override
  public void exitUnaryExpr(UnaryExprContext ctx) {
    final Symbol sym = symbolTrace.peek();

    if (ctx.sign != null && !sym.isError() && !sym.type.equals(Symbol.INT)) {
      errors.add( CompilerErrors.IllegalOp1Type(programName, ctx.sign.getText(), ctx.sign) );
      symbolTrace.pop();
      symbolTrace.push( new Symbol.Error() );
    }
  }

  @Override
  public void exitArithmeticExpr(ArithmeticExprContext ctx) {
    final Symbol rhs = symbolTrace.pop();
    final Symbol lhs = symbolTrace.pop();

    if (!lhs.isError() && !rhs.isError() && !(lhs.type.equals(Symbol.INT) && rhs.type.equals(Symbol.INT))) {
      errors.add( CompilerErrors.IllegalOp2Type(programName, ctx.op.getText(), ctx.op) );
      symbolTrace.push( new Symbol.Error() );
    }

    symbolTrace.push( new Symbol.Expression(Symbol.INT) );
  }

  @Override
  public void exitComparison(ComparisonContext ctx) {
    final Symbol rhs = symbolTrace.pop();
    final Symbol lhs = symbolTrace.pop();

    if (!lhs.isError() && !rhs.isError() && !(lhs.type.equals(Symbol.INT) && rhs.type.equals(Symbol.INT))) {
      errors.add( CompilerErrors.IllegalRelOpType(programName, ctx.op.getText(), ctx.op) );
      symbolTrace.push( new Symbol.Error() );
    }

    symbolTrace.push( new Symbol.Expression(Symbol.BOOL) );
  }

  @Override
  public void exitEqualityComparison(EqualityComparisonContext ctx) {
    final Symbol rhs = symbolTrace.pop();
    final Symbol lhs = symbolTrace.pop();

    if (!lhs.isError() && !rhs.isError() && !(lhs.type.equals(rhs.type))) {
      errors.add( CompilerErrors.IllegalEqualOpType(programName, ctx.op.getText(), ctx.op) );
      symbolTrace.push( new Symbol.Error() );
    }

    symbolTrace.push( new Symbol.Expression(Symbol.BOOL) );
  }

  @Override
  public void exitBooleanExpr(BooleanExprContext ctx) {
    final Symbol rhs = symbolTrace.pop();
    final Symbol lhs = symbolTrace.pop();

    if (!lhs.isError() && !rhs.isError() && !(lhs.type.equals(Symbol.BOOL) && rhs.type.equals(Symbol.BOOL))) {
      errors.add( CompilerErrors.IllegalOp2Type(programName, ctx.op.getText(), ctx.op) );
      symbolTrace.push( new Symbol.Error() );
    }

    symbolTrace.push( new Symbol.Expression(Symbol.BOOL) );
  }

  @Override
  public void exitProcedureCall(ProcedureCallContext ctx) {
    final String fnName = ctx.Id().getText();
    boolean error = false;

    if (!symbolTable.contains(fnName)) {
      errors.add( CompilerErrors.IdentNotDecl(programName, fnName, ctx.Id().getSymbol()) );
      error = true;
    }

    if (!symbolTable.get(fnName).isFunction()) {
      errors.add( CompilerErrors.SymbolIllegalUse(programName, fnName, symbolTable.get(fnName).kind, ctx.Id().getSymbol()) );
      error = true;
    }

    if (error) {
      // clean up symboltrace
      for (int i = 0; i < ctx.expression().size(); i++)
        symbolTrace.pop();

      // expressions should return symbols for the type
      if (ctx.getParent() instanceof PrimaryExprContext)
        symbolTrace.push( new Symbol.Error() );
      return;
    }

    Symbol.Function fn = symbolTable.get(fnName).asFunction();

    if (ctx.expression().size() < fn.params.size()) {
      errors.add( CompilerErrors.TooFewArgs(programName, fnName, ctx.stop) );
      
      // clean up symboltrace
      for (int i = 0; i < ctx.expression().size(); i++)
        symbolTrace.pop();

      // expressions should return symbols for the type
      if (ctx.getParent() instanceof PrimaryExprContext)
        symbolTrace.push( new Symbol.Error() );
      return;
    }

    Stack<Symbol> args = new Stack<>();
    for (int i = 0; i < ctx.expression().size(); i++)
      args.push( symbolTrace.pop() );

    int idx = 0;
    while (args.size() > 0) {
      final Symbol arg = args.pop();

      if (!arg.isError() && (idx >= fn.params.size() || !fn.params.get(idx).type.equals(arg.type))) {
        errors.add( CompilerErrors.ArgNotApplicable(programName, fnName, idx + 1, ctx.expression(idx).start) );
        error = true;
      }

      idx++;
    }

    if (fn.type.equals(Symbol.VOID) && ctx.getParent() instanceof PrimaryExprContext) {
      errors.add( CompilerErrors.ProcNotFuncExpr(programName, fnName, ctx.start) );
      error = true;
    }
    
    // expressions should return symbols for the type
    if (ctx.getParent() instanceof PrimaryExprContext)
      symbolTrace.push( error ? new Symbol.Error() : new Symbol.Expression(fn.type) );
  }

  @Override
  public void exitCreationExpr(CreationExprContext ctx) {
    boolean error = false;
    String arraySuffix = "";
    
    for (int i = 0; i < ctx.expression().size(); i++) {
      final Symbol expr = symbolTrace.pop();

      if (!expr.isError() && !expr.type.equals(Symbol.INT)) {
        errors.add( CompilerErrors.BadArraySelector(programName, ctx.expression(ctx.expression().size() - 1 - i).start) );
        error = true;
      }

      arraySuffix += "[]";
    }

    final Symbol baseType = symbolTrace.pop();
    if (ctx.expression().size() == 0 && !baseType.isError() && (baseType.type.equals(Symbol.INT) || baseType.type.equals(Symbol.BOOL))) {
      errors.add( CompilerErrors.InvalidNewType(programName, ctx.baseType().start) );
      error = true;
    }

    symbolTrace.push( error ? new Symbol.Error() : new Symbol.Expression(baseType.type + arraySuffix) );
  }

  @Override
  public void exitArrayLength(ArrayLengthContext ctx) {
    final Symbol id = symbolTrace.pop();

    if (!id.isError() && !id.asVariable().isArray()) {
      errors.add( CompilerErrors.ArrayLenNotArray(programName, ctx.fullIdentifier().start) );
      symbolTrace.push( new Symbol.Error() );
    }

    symbolTrace.push( new Symbol.Expression(Symbol.INT) );
  }

  @Override
  public void beforeSelector(ParseTree ctx) {
    // first part of the full identifier
    if (ctx instanceof FullIdentifierContext) {
      FullIdentifierContext fictx = (FullIdentifierContext)ctx;
      final String name = fictx.Id().getText();

      if (!symbolTable.contains(name)) {
        errors.add( CompilerErrors.IdentNotDecl(programName, name, fictx.Id().getSymbol()) );
        symbolTrace.push( new Symbol.Error() );
        return;
      }

      final boolean constAllowed = !(fictx.getParent() instanceof AssignmentContext || fictx.getParent() instanceof ArrayLengthContext);
      if (!symbolTable.get(name).isVariable() && !(constAllowed && symbolTable.get(name).isConst())) {
        errors.add( CompilerErrors.SymbolIllegalUse(programName, name, symbolTable.get(name).kind, fictx.Id().getSymbol()) );
        symbolTrace.push( new Symbol.Error() );
        return;
      }

      symbolTrace.push( symbolTable.get(name) );
    }
    // selector part of the identifier
    else {
      SelectorContext sctx = (SelectorContext)ctx;

      // record selector
      if (sctx.Id() != null) {
        Symbol id = symbolTrace.pop();

        if (!symbolTable.contains(id.type) || !symbolTable.get(id.type).isRecord()) {
          errors.add( CompilerErrors.SelectorNotRecord(programName, sctx.start) );
          symbolTrace.push( new Symbol.Error() );
          return;
        }
  
        Symbol selId = id.asVariable().selectField(symbolTable, sctx.Id().getText());
  
        if (selId.isError()) {
          errors.add( CompilerErrors.InvalidRecordField(programName, sctx.Id().getText(), id.type, sctx.Id().getSymbol()) );
          symbolTrace.push( new Symbol.Error() );
          return;
        }
  
        symbolTrace.push(selId);
      }
      // array selector
      else {
        Symbol expr = symbolTrace.pop();
        Symbol id = symbolTrace.peek();

        if (!id.isError() && !id.isArray()) {
          errors.add( CompilerErrors.SelectorNotArray(programName, sctx.start) );
          symbolTrace.pop();
          symbolTrace.push( new Symbol.Error() );
          return;
        }
  
        if (!expr.isError() && !expr.type.equals(Symbol.INT)) {
          errors.add( CompilerErrors.BadArraySelector(programName, sctx.expression().start) );
          symbolTrace.pop();
          symbolTrace.push( new Symbol.Error() );
          return;
        }
  
        if (!expr.isError() && !id.isError()) {
          symbolTrace.pop();
          symbolTrace.push( id.asVariable().selectElement() );
        }
        else if (expr.isError()) {
          symbolTrace.pop();
          symbolTrace.push( new Symbol.Error() );
        }
      }
    }
  }

  @Override
  public void exitType(TypeContext ctx) {
    if (!symbolTrace.peek().isError()) {
      symbolTrace.pop();
      symbolTrace.push( new Symbol.Type(ctx.getText()) );
    }
  }

  @Override
  public void exitBaseType(BaseTypeContext ctx) {
    final String type = ctx.getText();

    if (ctx.Id() != null && !symbolTable.contains(type)) {
      errors.add( CompilerErrors.IdentNotDecl(programName, type, ctx.Id().getSymbol()) );
      symbolTrace.push( new Symbol.Error() );
    } 
    else if (ctx.Id() != null && !symbolTable.get(type).isRecord()) {
      errors.add( CompilerErrors.SymbolIllegalUse(programName, type, symbolTable.get(type).kind, ctx.Id().getSymbol()) );
      symbolTrace.push( new Symbol.Error() );
    } 
    else {
      symbolTrace.push( new Symbol.Type(type) );
    }
  }

  @Override
  public void exitLiteral(LiteralContext ctx) {
    if (ctx.Boolean() != null)
      symbolTrace.push(new Symbol.Expression(Symbol.BOOL, ctx.Boolean().getText()));
    else
      symbolTrace.push(new Symbol.Expression(Symbol.INT, ctx.Number().getText()));
  }

}
