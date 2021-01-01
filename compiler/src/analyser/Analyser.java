package analyser;

import compiler.Compiler;
import information.YaplConstants;
import information.CompilerError;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import parser.YaplBaseListener;
import parser.YaplParser;
import parser.YaplParser.*;
import information.*;
import information.Record;
import stdlib.StandardLibrary;

import java.util.Stack;

public class Analyser extends YaplBaseListener {

  public SymbolTable symboltable = new SymbolTable();

  public Analyser(StandardLibrary stdlib) {
    for (Symbol sym : stdlib.getPredefinedSymbols()) {
      if (sym.is(Procedure.class)) sym.as(Procedure.class).isStdLib = true;
      symboltable.add(sym);
    }
  }



  /**
   * Since a super pattern does not have a way to return information to parent nodes,
   * this stack is used to share information between nodes.
   */
  protected Stack<Information> informationStack = new Stack<>();

  /**
   * Indicates if the current scope is a local scope.
   */
  protected boolean isLocal = false;

  /**
   * Used to reset {@code isLocal} to its previous value after a record,
   * since the scope containing record members isnt considered local.
   */
  protected boolean wasLocal = false;

  /**
   * Indicates if the current function has a return statement.
   */
  protected boolean hasReturn = false;

  /**
   * Indicates if the current function is pure i.e. does no IO and only accesses variables in its own scope.
   */
  protected boolean isPure = true;

  /**
   * Indicates if the {@code afterProcedureId} listener has already been called for the current procedure.
   */
  protected boolean procedureIdSet = false;

  @Override
  public void enterProgram(ProgramContext ctx) {
    symboltable.openScope();
  }

  @Override
  public void exitProgram(ProgramContext ctx) {
    final String name = ctx.Id(0).getText();
    final String endName = ctx.Id(1).getText();

    if (!endName.equals(name)) {
      Compiler.errors.add(CompilerError.EndIdentMismatch(name, endName, "program", ctx, ctx.Id(1).getSymbol()));
    }

    symboltable.closeScope();
  }

  @Override
  public void exitVarDeclaration(VarDeclarationContext ctx) {
    final Information typeInfo = informationStack.pop();
    final String type = (typeInfo.is(CompilerError.class)) ? YaplConstants.UNDEFINED : typeInfo.as(Variable.class).dataType;

    for (TerminalNode id : ctx.Id()) {
      String name = id.getText();

      if (symboltable.currScope.contains(name)) {
        Compiler.errors.add(CompilerError.SymbolExists(name, symboltable.get(name).symbolType, ctx, id.getSymbol()));
      }

      final Symbol symbol = new Variable(name, type, isLocal);
      symboltable.add(symbol);
    }
  }

  @Override
  public void exitConstDeclaration(ConstDeclarationContext ctx) {
    final String name = ctx.Id().getText();
    ConstantExpression rhs = informationStack.pop().as(ConstantExpression.class);

    if (symboltable.currScope.contains(name)) {
      Compiler.errors.add(CompilerError.SymbolExists(name, symboltable.get(name).symbolType, ctx, ctx.Id().getSymbol()));
    }

    symboltable.add(new Constant(name, rhs.dataType, rhs.value, isLocal));
  }

  @Override
  public void enterRecordDeclaration(RecordDeclarationContext ctx) {
    final String recordName = ctx.Id().getText();

    if (symboltable.currScope.contains(recordName)) {
      Compiler.errors.add(CompilerError.SymbolExists(recordName, symboltable.get(recordName).symbolType, ctx, ctx.Id().getSymbol()));
    }

    symboltable.add(new Record(recordName));
    symboltable.openScope();
    wasLocal = isLocal;
    isLocal = false;
  }

  @Override
  public void exitRecordDeclaration(RecordDeclarationContext ctx) {
    final String recordName = ctx.Id().getText();
    Record record = symboltable.get(recordName).as(Record.class);

    for (Symbol sym : symboltable.currScope.symbols.values()) {
      record.fields.put(sym.name, sym.as(Variable.class));
    }

    symboltable.closeScope();
    isLocal = wasLocal;
  }

  public void afterProcedureId(ProcedureContext ctx) {
    final String procedureName = ctx.Id(0).getText();
    final Information typeInfo = informationStack.pop();
    final String type = (typeInfo.is(CompilerError.class)) ? YaplConstants.UNDEFINED : typeInfo.as(Variable.class).dataType;
    this.isPure = true;

    if (symboltable.currScope.contains(procedureName)) {
      Compiler.errors.add(CompilerError.SymbolExists(procedureName, symboltable.get(procedureName).symbolType, ctx, ctx.Id(0).getSymbol()));
    }

    symboltable.add(new Procedure(procedureName, type));
    symboltable.openScope();
    this.hasReturn = false;
  }

  @Override
  public void exitProcedure(ProcedureContext ctx) {
    final String name = ctx.Id(0).getText();
    final String endName = ctx.Id(1).getText();

    if (!endName.equals(name)) {
      Compiler.errors.add(CompilerError.EndIdentMismatch(name, endName, symboltable.get(name).symbolType, ctx, ctx.Id(1).getSymbol()));
    }

    symboltable.closeScope();
    isLocal = false;

    if (!symboltable.get(name).as(Procedure.class).returnType.equals(YaplConstants.VOID) && !hasReturn) {
      Compiler.errors.add(CompilerError.MissingReturn(name, ctx, ctx.block().stop));
    }

    symboltable.get(name).as(Procedure.class).isPure = isPure;
    this.isPure = true;
    this.procedureIdSet = false;
  }

  @Override
  public void enterBlock(BlockContext ctx) {
    if (!procedureIdSet && ctx.getParent() instanceof ProcedureContext) {
      afterProcedureId((ProcedureContext)ctx.getParent());
      this.procedureIdSet = true;
    }

    symboltable.openScope();
    if (ctx.getParent() instanceof ProcedureContext) {
      this.isLocal = true;
    }
  }

  @Override
  public void exitBlock(BlockContext ctx) {
    symboltable.closeScope();
  }

  @Override
  public void enterStatementList(StatementListContext ctx) {
    if (ctx.getParent() instanceof ProgramContext) {
      this.isLocal = true;
    }
  }

  @Override
  public void exitStatementList(StatementListContext ctx) {
    if (ctx.getParent() instanceof ProgramContext) {
      this.isLocal = false;
    }
  }

  @Override
  public void exitAssignment(AssignmentContext ctx) {
    final Information rhs = informationStack.pop();
    final Information lhs = informationStack.pop();

    if (!lhs.is(CompilerError.class) && !rhs.is(CompilerError.class) && !lhs.as(Variable.class).dataType.equals(rhs.as(Variable.class).dataType)) {
      Compiler.errors.add( CompilerError.TypeMismatchAssign(ctx, ctx.op) );
    }
  }

  @Override
  public void exitReturnStatement(ReturnStatementContext ctx) {
    final String procedureName = CompilerContext.getProcedureName(ctx);
    this.hasReturn = true;

    // inside the main code, return has an expression
    if (procedureName == null && ctx.expression() != null) {
      Compiler.errors.add( CompilerError.IllegalRetValMain(ctx.expression(), ctx.expression().start) );
    }
    // inside a function, return has an expression
    else if (procedureName != null && ctx.expression() != null) {
      final Variable returnSym = informationStack.pop().as(Variable.class);
      final Procedure procedure = symboltable.get(procedureName).as(Procedure.class);

      // function has return type void
      if (procedure.returnType.equals(YaplConstants.VOID)) {
        Compiler.errors.add( CompilerError.IllegalRetValProc(procedureName, ctx.expression(), ctx.expression().start) );
      }
      // return type and expression type do not match
      else if (!returnSym.dataType.equals(procedure.returnType) && !returnSym.is(CompilerError.class) && !procedure.returnType.equals(YaplConstants.UNDEFINED)) {
        Compiler.errors.add(
          (ctx.expression() != null)
            ? CompilerError.InvalidReturnType(procedureName, ctx.expression(), ctx.expression().start)
            : CompilerError.InvalidReturnType(procedureName, ctx, ctx.stop)
        );
      }
    }
    // inside a function, return has no expression, function has a return type other than void
    else if (ctx.expression() == null && procedureName != null && !symboltable.get(procedureName).as(Procedure.class).returnType.equals(YaplConstants.VOID) && !symboltable.get(procedureName).as(Procedure.class).returnType.equals(YaplConstants.UNDEFINED)) {
      Compiler.errors.add(
        (ctx.expression() != null)
        ? CompilerError.InvalidReturnType(procedureName, ctx.expression(), ctx.expression().start)
        : CompilerError.InvalidReturnType(procedureName, ctx, ctx.stop)
      );
    }
  }

  @Override
  public void exitWriteStatement(WriteStatementContext ctx) {
    this.isPure = false;
  }

  @Override
  public void exitIfStatement(IfStatementContext ctx) {
    Information condInfo = informationStack.pop();

    if (!condInfo.is(CompilerError.class) && !condInfo.as(Variable.class).dataType.equals(YaplConstants.BOOL)) {
      Compiler.errors.add( CompilerError.CondNotBool(ctx.expression(), ctx.expression().start) );
    }
  }

  @Override
  public void exitWhileStatement(WhileStatementContext ctx) {
    Information condInfo = informationStack.pop();

    if (!condInfo.is(CompilerError.class) && !condInfo.as(Variable.class).dataType.equals(YaplConstants.BOOL)) {
      Compiler.errors.add( CompilerError.CondNotBool(ctx.expression(), ctx.expression().start) );
    }
  }

  @Override
  public void exitUnaryExpr(UnaryExprContext ctx) {
    final Information expr = informationStack.peek();

    if (ctx.sign != null && !expr.is(CompilerError.class) && !expr.as(Variable.class).dataType.equals(YaplConstants.INT)) {
      final CompilerError error = CompilerError.IllegalOp1Type(ctx.sign.getText(), ctx, ctx.start);
      Compiler.errors.add(error);
      informationStack.pop();
      informationStack.push(error);
    }

    if (ctx.sign != null && expr.is(Constant.class)) {
      Constant csym = expr.as(Constant.class);

      if (!csym.is(ConstantExpression.class)) {
        informationStack.pop();
        csym = new ConstantExpression(ctx, csym.dataType, csym.value);
        informationStack.push(csym);
      }


      if(!(csym.value.startsWith("-") || csym.value.startsWith("+"))) {
        csym.value = ctx.sign.getText() + csym.value;
      }
      else if (csym.value.charAt(0) != ctx.sign.getText().charAt(0)) {
        csym.value = "-" + csym.value.substring(1);
      }
      else {
        csym.value = csym.value.substring(1);
      }
    }

    afterExpression(ctx);
  }

  public void afterExpression(ExpressionContext ctx) {
    if (ctx.getParent() instanceof SelectorContext) {
      beforeSelector(ctx.getParent());
    }
  }

  @Override
  public void exitArithmeticExpr(ArithmeticExprContext ctx) {
    final Information rhs = informationStack.pop();
    final Information lhs = informationStack.pop();

    if (!lhs.is(CompilerError.class) && !rhs.is(CompilerError.class) && !(lhs.as(Variable.class).dataType.equals(YaplConstants.INT) && rhs.as(Variable.class).dataType.equals(YaplConstants.INT))) {
      final CompilerError error = CompilerError.IllegalOp2Type(ctx.op.getText(), ctx, ctx.op);
      Compiler.errors.add(error);
      informationStack.push(error);
      afterExpression(ctx);
      return;
    }

    if (lhs.is(Constant.class) && rhs.is(Constant.class)) {
      // remove sub expressions to clean up the symboltable
      if (lhs.is(ConstantExpression.class)) symboltable.remove(lhs.as(Symbol.class).name);
      if (rhs.is(ConstantExpression.class)) symboltable.remove(rhs.as(Symbol.class).name);

      Information expr = fold(ctx, ctx.op, lhs.as(Constant.class), rhs.as(Constant.class));
      informationStack.push(expr);
      if (!expr.is(CompilerError.class)) symboltable.add(expr.as(Symbol.class));
    }
    else {
      informationStack.push(new Expression(ctx, YaplConstants.INT));
    }

    afterExpression(ctx);
  }

  @Override
  public void exitComparison(ComparisonContext ctx) {
    final Information rhs = informationStack.pop();
    final Information lhs = informationStack.pop();

    if (!lhs.is(CompilerError.class) && !rhs.is(CompilerError.class) && !(lhs.as(Variable.class).dataType.equals(YaplConstants.INT) && rhs.as(Variable.class).dataType.equals(YaplConstants.INT))) {
      final CompilerError error = CompilerError.IllegalRelOpType(ctx.op.getText(), ctx, ctx.op);
      Compiler.errors.add(error);
      informationStack.push(error);
      afterExpression(ctx);
      return;
    }

    if (lhs.is(Constant.class) && rhs.is(Constant.class)) {
      // remove sub expressions to clean up the symboltable
      if (lhs.is(ConstantExpression.class)) symboltable.remove(lhs.as(Symbol.class).name);
      if (rhs.is(ConstantExpression.class)) symboltable.remove(rhs.as(Symbol.class).name);

      Information expr = fold(ctx, ctx.op, lhs.as(Constant.class), rhs.as(Constant.class));
      informationStack.push(expr);
      if (!expr.is(CompilerError.class)) symboltable.add(expr.as(Symbol.class));
    }
    else {
      informationStack.push(new Expression(ctx, YaplConstants.BOOL) );
    }

    afterExpression(ctx);
  }

  @Override
  public void exitEqualityComparison(EqualityComparisonContext ctx) {
    final Information rhs = informationStack.pop();
    final Information lhs = informationStack.pop();

    if (!lhs.is(CompilerError.class) && !rhs.is(CompilerError.class) && !(lhs.as(Variable.class).dataType.equals(rhs.as(Variable.class).dataType))) {
      final CompilerError error = CompilerError.IllegalEqualOpType(ctx.op.getText(), ctx, ctx.op);
      Compiler.errors.add(error);
      informationStack.push(error);
      afterExpression(ctx);
      return;
    }

    if (lhs.is(Constant.class) && rhs.is(Constant.class)) {
      // remove sub expressions to clean up the symboltable
      if (lhs.is(ConstantExpression.class)) symboltable.remove(lhs.as(Symbol.class).name);
      if (rhs.is(ConstantExpression.class)) symboltable.remove(rhs.as(Symbol.class).name);

      Information expr = fold(ctx, ctx.op, lhs.as(Constant.class), rhs.as(Constant.class));
      informationStack.push(expr);
      if (!expr.is(CompilerError.class)) symboltable.add(expr.as(Symbol.class));
    }
    else {
      informationStack.push(new Expression(ctx, YaplConstants.BOOL) );
    }

    afterExpression(ctx);
  }

  @Override
  public void exitBooleanExpr(BooleanExprContext ctx) {
    final Information rhs = informationStack.pop();
    final Information lhs = informationStack.pop();

    if (!lhs.is(CompilerError.class) && !rhs.is(CompilerError.class) && !(lhs.as(Variable.class).dataType.equals(YaplConstants.BOOL) && rhs.as(Variable.class).dataType.equals(YaplConstants.BOOL))) {
      final CompilerError error = CompilerError.IllegalOp2Type(ctx.op.getText(), ctx, ctx.op);
      Compiler.errors.add(error);
      informationStack.push(error);
      afterExpression(ctx);
      return;
    }

    if (lhs.is(Constant.class) && rhs.is(Constant.class)) {
      // remove sub expressions to clean up the symboltable
      if (lhs.is(ConstantExpression.class)) symboltable.remove(lhs.as(Symbol.class).name);
      if (rhs.is(ConstantExpression.class)) symboltable.remove(rhs.as(Symbol.class).name);

      Information expr = fold(ctx, ctx.op, lhs.as(Constant.class), rhs.as(Constant.class));
      informationStack.push(expr);
      if (!expr.is(CompilerError.class)) symboltable.add(expr.as(Symbol.class));
    }
    else {
      informationStack.push(new Expression(ctx, YaplConstants.BOOL) );
    }

    afterExpression(ctx);
  }

  @Override
  public void exitCreationExpr(CreationExprContext ctx) {
    CompilerError error = null;
    StringBuilder arraySuffix = new StringBuilder();

    for (int i = 0; i < ctx.expression().size(); i++) {
      final Information expr = informationStack.pop();

      if (!expr.is(CompilerError.class) && !expr.as(Variable.class).dataType.equals(YaplConstants.INT)) {
        error = CompilerError.BadArraySelector(ctx.expression(ctx.expression().size() - 1 - i), ctx.expression(ctx.expression().size() - 1 - i).start);
        Compiler.errors.add(error);
      }

      arraySuffix.append("[]");
    }

    final Information baseType = informationStack.pop();
    if (ctx.expression().size() == 0 && !baseType.is(CompilerError.class) && baseType.as(Variable.class).isPrimitive()) {
      error = CompilerError.InvalidNewType(ctx.baseType(), ctx.baseType().start);
      Compiler.errors.add(error);
    }

    informationStack.push( (error != null) ? error : new Expression(ctx,baseType.as(Variable.class).dataType + arraySuffix) );
    afterExpression(ctx);
  }

  @Override
  public void exitArrayLength(ArrayLengthContext ctx) {
    final Information id = informationStack.pop();

    if (!id.is(CompilerError.class) && !id.as(Variable.class).isArray()) {
      final CompilerError error = CompilerError.ArrayLenNotArray(ctx.fullIdentifier(), ctx.fullIdentifier().start);
      Compiler.errors.add(error);
      informationStack.push(error);
    }

    informationStack.push( new Expression(ctx, YaplConstants.INT) );
  }

  @Override
  public void exitProcedureCall(ProcedureCallContext ctx) {
    final String fnName = ctx.Id().getText();
    CompilerError error = null;

    if (!symboltable.contains(fnName)) {
      error = CompilerError.IdentNotDecl(fnName, ctx, ctx.Id().getSymbol());
      Compiler.errors.add(error);
    }

    if (error == null && !symboltable.get(fnName).is(Procedure.class)) {
      error = CompilerError.SymbolIllegalUse(fnName, symboltable.get(fnName).symbolType, ctx, ctx.Id().getSymbol());
      Compiler.errors.add(error);
    }

    if (error != null) {
      // clean up symboltrace
      for (int i = 0; i < ctx.expression().size(); i++) informationStack.pop();

      // expressions should return a symbol for the type checks
      if (ctx.getParent() instanceof PrimaryExprContext) informationStack.push(error);
      return;
    }

    Procedure fn = symboltable.get(fnName).as(Procedure.class);

    if (ctx.expression().size() < fn.params.size()) {
      error = CompilerError.TooFewArgs(fnName, ctx, ctx.stop);
      Compiler.errors.add(error);

      // clean up symboltrace
      for (int i = 0; i < ctx.expression().size(); i++) informationStack.pop();

      // expressions should return a symbol for the type checks
      if (ctx.getParent() instanceof PrimaryExprContext) informationStack.push(error);
      return;
    }

    // reverse the argument symbols since a stack is LIFO
    Stack<Information> args = new Stack<>();
    for (int i = 0; i < ctx.expression().size(); i++)
      args.push( informationStack.pop() );

    int idx = 0;
    while (args.size() > 0) {
      final Information arg = args.pop();

      if (!arg.is(CompilerError.class) && (idx >= fn.params.size() || !fn.params.get(idx).dataType.equals(arg.as(Variable.class).dataType))) {
        error = CompilerError.ArgNotApplicable(fnName, idx + 1, ctx.expression(idx), ctx.expression(idx).start);
        Compiler.errors.add(error);
      }

      idx++;
    }

    if (fn.returnType.equals(YaplConstants.VOID) && ctx.getParent() instanceof PrimaryExprContext) {
      error = CompilerError.ProcNotFuncExpr(fnName, ctx, ctx.Id().getSymbol());
      Compiler.errors.add(error);
    }

    fn.uses++;
    if (!fn.isPure) this.isPure = false;

    // expressions should put information on the symboltrace for type checks
    if (ctx.getParent() instanceof PrimaryExprContext) {
      if (error != null) informationStack.push(error);
      else informationStack.push(
        (fn.returnType.equals(YaplConstants.UNDEFINED))
        ? CompilerError.Internal("The Datatype of '" + fn.name + "' was erroneous on declaration, ignore further datatype errors!")
        : new Expression(ctx, fn.returnType)
      );
    }
  }

  @Override
  public void enterParam(ParamContext ctx) {
    if (!procedureIdSet) {
      afterProcedureId((ProcedureContext)ctx.getParent());
      this.procedureIdSet = true;
    }
  }

  @Override
  public void exitParam(ParamContext ctx) {
    final String name = ctx.Id().getText();
    final Parameter sym = new Parameter(name, informationStack.pop().as(Variable.class).dataType);

    if (symboltable.currScope.contains(name)) {
      Compiler.errors.add(CompilerError.SymbolExists(name, symboltable.get(name).symbolType, ctx, ctx.Id().getSymbol()));
    }
    else {
      symboltable.add(sym);
    }

    final Procedure procedure = symboltable.get(CompilerContext.getProcedureName(ctx), symboltable.currScope.parent).as(Procedure.class);
    procedure.params.add(sym);
  }

  @Override
  public void exitReturnType(ReturnTypeContext ctx) {
    if (ctx.type() != null && !informationStack.peek().is(CompilerError.class)) {
      informationStack.pop();
      informationStack.push(new Expression(ctx, ctx.getText()));
    }
    else if (ctx.type() == null) {
      informationStack.push(new Expression(ctx, YaplConstants.VOID));
    }
  }

  @Override
  public void exitType(TypeContext ctx) {
    if (!informationStack.peek().is(CompilerError.class)) {
      informationStack.pop();
      informationStack.push(new Expression(ctx, ctx.getText()));
    }
  }

  @Override
  public void exitBaseType(BaseTypeContext ctx) {
    final String type = ctx.getText();

    if (ctx.Id() != null && !symboltable.contains(type)) {
      final CompilerError error = CompilerError.IdentNotDecl(type, ctx, ctx.Id().getSymbol());
      Compiler.errors.add(error);
      informationStack.push(error);
    }
    else if (ctx.Id() != null && !symboltable.get(type).is(Record.class)) {
      final CompilerError error = CompilerError.SymbolIllegalUse(type, symboltable.get(type).symbolType, ctx, ctx.Id().getSymbol());
      Compiler.errors.add(error);
      informationStack.push(error);
    }
    else {
      informationStack.push(new Expression(ctx, type));
    }
  }

  @Override
  public void exitLiteral(LiteralContext ctx) {
    informationStack.push(
      (ctx.Boolean() != null)
      ? new ConstantExpression(ctx, YaplConstants.BOOL, ctx.Boolean().getText())
      : new ConstantExpression(ctx, YaplConstants.INT, ctx.Number().getText())
    );
  }

  @Override
  public void visitTerminal(TerminalNode node) {
    final boolean isId = (node.getSymbol().getType() == YaplParser.Id);
    if (isId && (node.getParent() instanceof SelectorContext || node.getParent() instanceof FullIdentifierContext)) {
      beforeSelector(node.getParent());
    }
  }

  public void beforeSelector(ParseTree ctx) {
    // first part of the full identifier
    if (ctx instanceof FullIdentifierContext) {
      FullIdentifierContext fictx = (FullIdentifierContext)ctx;
      final String name = fictx.Id().getText();

      if (!symboltable.contains(name)) {
        CompilerError error = CompilerError.IdentNotDecl(name, fictx, fictx.Id().getSymbol());
        Compiler.errors.add(error);
        informationStack.push(error);
        return;
      }

      final boolean constAllowed = !(fictx.getParent() instanceof AssignmentContext || fictx.getParent() instanceof ArrayLengthContext);
      if (!(symboltable.get(name).isUserDefinedVariable() && (constAllowed || !symboltable.get(name).is(Constant.class)))) {
        CompilerError error = CompilerError.SymbolIllegalUse(name, symboltable.get(name).symbolType, fictx, fictx.Id().getSymbol());
        Compiler.errors.add(error);
        informationStack.push(error);
        return;
      }

      if (!symboltable.get(name).is(Constant.class) && !symboltable.get(name).as(Variable.class).isLocal)
        this.isPure = false;

      informationStack.push(
        (symboltable.get(name).as(Variable.class).dataType.equals(YaplConstants.UNDEFINED))
        ? CompilerError.Internal("The Datatype of '" + name + "' was erroneous on declaration, ignore further datatype errors!")
        : symboltable.get(name)
      );
    }
    // (optional) selector part(s) of a full identifier
    else {
      SelectorContext sctx = (SelectorContext)ctx;

      // record selector
      if (sctx.Id() != null) {
        Information id = informationStack.pop();

        if (!id.is(CompilerError.class) && (!symboltable.contains(id.as(Variable.class).dataType) || !symboltable.get(id.as(Variable.class).dataType).is(Record.class))) {
          final CompilerError error = CompilerError.SelectorNotRecord(sctx, sctx.Id().getSymbol());
          Compiler.errors.add(error);
          informationStack.push(error);
          return;
        }

        Symbol selId = id.as(Variable.class).selectField(symboltable, sctx.Id().getText());

        if (selId == null) {
          final CompilerError error = CompilerError.InvalidRecordField(sctx.Id().getText(), id.as(Variable.class).dataType, sctx, sctx.Id().getSymbol());
          Compiler.errors.add(error);
          informationStack.push(error);
          return;
        }

        informationStack.push(selId);
      }
      // array selector
      else {
        Information expr = informationStack.pop();
        Information id = informationStack.peek();

        if (!id.is(CompilerError.class) && !id.as(Variable.class).isArray()) {
          final CompilerError error = CompilerError.SelectorNotArray(sctx, sctx.start);
          Compiler.errors.add(error);
          informationStack.pop();
          informationStack.push(error);
          return;
        }

        if (!expr.is(CompilerError.class) && !expr.as(Variable.class).dataType.equals(YaplConstants.INT)) {
          final CompilerError error = CompilerError.BadArraySelector(sctx.expression(), sctx.expression().start);
          Compiler.errors.add(error);
          informationStack.pop();
          informationStack.push(error);
          return;
        }

        if (!expr.is(CompilerError.class) && !id.is(CompilerError.class)) {
          informationStack.pop();
          informationStack.push( id.as(Variable.class).selectElement() );
        }
        else if (expr.is(CompilerError.class)) {
          informationStack.pop();
          informationStack.push(expr);
        }
      }
    }
  }

  public Information fold(ParserRuleContext context, Token op, Constant lhs, Constant rhs) {
    int ilhs, irhs;
    boolean blhs, brhs;

    switch (op.getText()) {
      case "+":
        ilhs = Integer.parseInt(lhs.value);
        irhs = Integer.parseInt(rhs.value);
        return new ConstantExpression(context, YaplConstants.INT, "" + (ilhs + irhs));

      case "-":
        ilhs = Integer.parseInt(lhs.value);
        irhs = Integer.parseInt(rhs.value);
        return new ConstantExpression(context, YaplConstants.INT, "" + (ilhs - irhs));

      case "*":
        ilhs = Integer.parseInt(lhs.value);
        irhs = Integer.parseInt(rhs.value);
        return new ConstantExpression(context, YaplConstants.INT, "" + (ilhs * irhs));

      case "/":
        ilhs = Integer.parseInt(lhs.value);
        irhs = Integer.parseInt(rhs.value);
        return new ConstantExpression(context, YaplConstants.INT, "" + (ilhs / irhs));

      case "%":
        ilhs = Integer.parseInt(lhs.value);
        irhs = Integer.parseInt(rhs.value);
        return new ConstantExpression(context, YaplConstants.INT, "" + (ilhs % irhs));

      case "==":
        if (lhs.dataType.equals(YaplConstants.INT)) {
          ilhs = Integer.parseInt(lhs.value);
          irhs = Integer.parseInt(rhs.value);
          return new ConstantExpression(context, YaplConstants.BOOL, (ilhs == irhs) ? YaplConstants.TRUE : YaplConstants.FALSE);
        }
        else {
          blhs = lhs.value.equals(YaplConstants.TRUE);
          brhs = rhs.value.equals(YaplConstants.TRUE);
          return new ConstantExpression(context, YaplConstants.BOOL, (blhs == brhs) ? YaplConstants.TRUE : YaplConstants.FALSE);
        }

      case "!=":
        if (lhs.dataType.equals(YaplConstants.INT)) {
          ilhs = Integer.parseInt(lhs.value);
          irhs = Integer.parseInt(rhs.value);
          return new ConstantExpression(context, YaplConstants.BOOL, (ilhs != irhs) ? YaplConstants.TRUE : YaplConstants.FALSE);
        }
        else {
          blhs = lhs.value.equals(YaplConstants.TRUE);
          brhs = rhs.value.equals(YaplConstants.TRUE);
          return new ConstantExpression(context, YaplConstants.BOOL, (blhs != brhs) ? YaplConstants.TRUE : YaplConstants.FALSE);
        }

      case "<":
        ilhs = Integer.parseInt(lhs.value);
        irhs = Integer.parseInt(rhs.value);
        return new ConstantExpression(context, YaplConstants.BOOL, (ilhs < irhs) ? YaplConstants.TRUE : YaplConstants.FALSE);

      case ">":
        ilhs = Integer.parseInt(lhs.value);
        irhs = Integer.parseInt(rhs.value);
        return new ConstantExpression(context, YaplConstants.BOOL, (ilhs > irhs) ? YaplConstants.TRUE : YaplConstants.FALSE);

      case "<=":
        ilhs = Integer.parseInt(lhs.value);
        irhs = Integer.parseInt(rhs.value);
        return new ConstantExpression(context, YaplConstants.BOOL, (ilhs <= irhs) ? YaplConstants.TRUE : YaplConstants.FALSE);

      case ">=":
        ilhs = Integer.parseInt(lhs.value);
        irhs = Integer.parseInt(rhs.value);
        return new ConstantExpression(context, YaplConstants.BOOL, (ilhs >= irhs) ? YaplConstants.TRUE : YaplConstants.FALSE);

      case "And":
        blhs = lhs.value.equals(YaplConstants.TRUE);
        brhs = rhs.value.equals(YaplConstants.TRUE);
        return new ConstantExpression(context, YaplConstants.BOOL, (blhs && brhs) ? YaplConstants.TRUE : YaplConstants.FALSE);

      case "Or":
        blhs = lhs.value.equals(YaplConstants.TRUE);
        brhs = rhs.value.equals(YaplConstants.TRUE);
        return new ConstantExpression(context, YaplConstants.BOOL, (blhs || brhs) ? YaplConstants.TRUE : YaplConstants.FALSE);

      default:
        CompilerError error = CompilerError.Internal("Unknown op '" + op.getText() + "' for constant folding!", context, op);
        Compiler.errors.add(error);
        return error;
    }
  }

}
