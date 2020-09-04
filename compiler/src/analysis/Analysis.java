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

/**
 * Creates a {@code SymbolTable}, with which it checks that every identifier is declared
 * and used correctly according to its type and semantics.
 * It also computes constant expressions and stores those in the SymbolTable.
 * When the analyser detects an error, it is added to the list of errors and skips to the next statement to continue its checks there.
 */
public class Analysis implements DetailedYaplListener {

  public final SymbolTable symboltable;
  public final List<CompilerError> errors;

  /**
   * Since a listener pattern does not have a way to return information to parent nodes,
   * this stack is used to share information between nodes.
   */
  protected Stack<Symbol> symboltrace;

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

  protected String programName = Symbol.UNDEFINED;
  protected String procedureName = Symbol.UNDEFINED;
  protected String recordName = Symbol.UNDEFINED;

  public Analysis(StandardLibrary stdlib) {
    this.symboltrace = new Stack<>();
    this.symboltable = new SymbolTable();
    this.errors = new ArrayList<>();

    stdlib.addToSymbolTable(symboltable);
  }

  @Override
  public void enterProgram(ProgramContext ctx) {
    symboltable.openScope();
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

    symboltable.closeScope();
  }

  @Override
  public void exitVarDeclaration(VarDeclarationContext ctx) {
    String type = symboltrace.pop().type;

    for (TerminalNode id : ctx.Id()) {
      String name = id.getText();

      if (symboltable.currScope.contains(name)) {
        errors.add(CompilerErrors.SymbolExists(programName, name, symboltable.get(name).kind, id.getSymbol()));
      }

      final Symbol symbol = new Symbol.Variable(name, type, isLocal);
      symboltable.add(symbol);
    }
  }

  @Override
  public void exitConstDeclaration(ConstDeclarationContext ctx) {
    final String name = ctx.Id().getText();
    Symbol.Expression rhs = symboltrace.pop().asExpression();

    if (symboltable.currScope.contains(name)) {
      errors.add(CompilerErrors.SymbolExists(programName, name, symboltable.get(name).kind, ctx.Id().getSymbol()));
    }

    symboltable.add(new Symbol.Const(name, rhs.type, rhs.value));
  }

  @Override
  public void afterRecordId(RecordDeclarationContext ctx) {
    this.recordName = ctx.Id().getText();

    if (symboltable.currScope.contains(recordName)) {
      errors.add(CompilerErrors.SymbolExists(programName, recordName, symboltable.get(recordName).kind, ctx.Id().getSymbol()));
    }

    symboltable.add(new Symbol.Record(recordName));
    symboltable.openScope();
    wasLocal = isLocal;
    isLocal = false;
  }

  @Override
  public void exitRecordDeclaration(RecordDeclarationContext ctx) {
    Symbol.Record record = symboltable.get(recordName).asRecord();

    for (Symbol sym : symboltable.currScope.symbols.values()) {
      record.fields.put(sym.name, (Symbol.Variable) sym);
    }

    symboltable.closeScope();
    isLocal = wasLocal;
    this.recordName = Symbol.UNDEFINED;
  }

  @Override
  public void exitReturnType(ReturnTypeContext ctx) {
    if (ctx.type() != null && !symboltrace.peek().isError()) {
      symboltrace.pop();
      symboltrace.push(new Symbol.Type(ctx.getText()));
    } else if (ctx.type() == null) {
      symboltrace.push(new Symbol.Type(ctx.getText()));
    }
  }

  @Override
  public void afterProcedureId(ProcedureContext ctx) {
    this.procedureName = ctx.Id(0).getText();
    this.isPure = true;

    if (symboltable.currScope.contains(procedureName)) {
      errors.add(CompilerErrors.SymbolExists(programName, procedureName, symboltable.get(procedureName).kind, ctx.Id(0).getSymbol()));
    }

    symboltable.add(new Symbol.Function(procedureName, symboltrace.pop().type));
    symboltable.openScope();
    hasReturn = false;
  }

  @Override
  public void exitParam(ParamContext ctx) {
    final String name = ctx.Id().getText();
    Symbol.Param sym = new Symbol.Param(name, symboltrace.pop().type);

    if (symboltable.currScope.contains(name)) {
      errors.add(CompilerErrors.SymbolExists(programName, name, symboltable.get(name).kind, ctx.Id().getSymbol()));
    } else {
      symboltable.add(sym);
    }

    symboltable.get(procedureName, symboltable.currScope.parent).asFunction().params.add(sym);
  }

  @Override
  public void exitProcedure(ProcedureContext ctx) {
    final String name = ctx.Id(0).getText();
    final String endName = ctx.Id(1).getText();

    if (!endName.equals(name)) {
      errors.add(CompilerErrors.EndIdentMismatch(programName, name, endName, symboltable.get(name).kind, ctx.Id(0).getSymbol()));
    }

    symboltable.closeScope();
    isLocal = false;

    if (!symboltable.get(name).type.equals(Symbol.VOID) && !hasReturn) {
      errors.add(CompilerErrors.MissingReturn(programName, name, ctx.block().stop));
    }

    symboltable.get(name).asFunction().isPure = isPure;
    this.procedureName = Symbol.UNDEFINED;
    this.isPure = true;
  }

  @Override
  public void enterBlock(BlockContext ctx) {
    symboltable.openScope();
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
    symboltable.closeScope();
  }

  @Override
  public void exitWriteStatement(WriteStatementContext ctx) {
    this.isPure = false;
  }

  @Override
  public void exitReturnStatement(ReturnStatementContext ctx) {
    this.hasReturn = true;

    // inside the main code
    if (procedureName.equals(Symbol.UNDEFINED) && ctx.expression() != null) {
      errors.add( CompilerErrors.IllegalRetValMain(programName, ctx.expression().start) );
    }
    // inside a function, return has an expression
    else if (!procedureName.equals(Symbol.UNDEFINED) && ctx.expression() != null) {
      final Symbol returnSym = symboltrace.pop();
      
      // function has resturn type void
      if (symboltable.get(procedureName).type.equals(Symbol.VOID)) {
        errors.add( CompilerErrors.IllegalRetValProc(programName, procedureName, ctx.expression().start) );
      }
      // return type and expression type do not match
      else if (!returnSym.type.equals(symboltable.get(procedureName).type) && !returnSym.isError()) {
        Token token = (ctx.expression() != null) ? ctx.expression().start : ctx.stop;
        errors.add( CompilerErrors.InvalidReturnType(programName, procedureName, token) );
      }
    }
    // inside a function, return has no expression, function has a return type other than void
    else if (ctx.expression() == null && !procedureName.equals(Symbol.UNDEFINED) && !symboltable.get(procedureName).type.equals(Symbol.VOID)) {
      Token token = (ctx.expression() != null) ? ctx.expression().start : ctx.stop;
      errors.add( CompilerErrors.InvalidReturnType(programName, procedureName, token) );
    }
  }

  @Override
  public void exitAssignment(AssignmentContext ctx) {
    final Symbol rhs = symboltrace.pop();
    final Symbol lhs = symboltrace.pop();

    if (!lhs.isError() && !rhs.isError() && !lhs.type.equals(rhs.type)) {
      errors.add( CompilerErrors.TypeMismatchAssign(programName, ctx.op) );
    }
  }

  @Override
  public void exitIfStatement(IfStatementContext ctx) {
    Symbol sym = symboltrace.pop();

    if (!sym.isError() && !sym.type.equals(Symbol.BOOL)) {
      errors.add( CompilerErrors.CondNotBool(programName, ctx.expression().start) );
    }
  }

  @Override
  public void exitWhileStatement(WhileStatementContext ctx) {
    Symbol sym = symboltrace.pop();

    if (!sym.isError() && !sym.type.equals(Symbol.BOOL)) {
      errors.add( CompilerErrors.CondNotBool(programName, ctx.expression().start) );
    }
  }

  @Override
  public void exitUnaryExpr(UnaryExprContext ctx) {
    final Symbol sym = symboltrace.peek();

    if (ctx.sign != null && !sym.isError() && !sym.type.equals(Symbol.INT)) {
      errors.add( CompilerErrors.IllegalOp1Type(programName, ctx.sign.getText(), ctx.sign) );
      symboltrace.pop();
      symboltrace.push( new Symbol.Error() );
    }

    if (ctx.sign != null && sym.isConst()) {
      Symbol.Const csym = sym.asConst();

      if (!csym.isExpression()) {
        symboltrace.pop();
        final String exprName = Symbol.Expression.nameFor(ctx.hashCode(), ctx.getText());
        csym = new Symbol.Expression(exprName, csym.type, csym.value);
        symboltrace.push(csym);
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
  }

  @Override
  public void exitArithmeticExpr(ArithmeticExprContext ctx) {
    final String op = ctx.op.getText();
    final Symbol rhs = symboltrace.pop();
    final Symbol lhs = symboltrace.pop();

    if (!lhs.isError() && !rhs.isError() && !(lhs.type.equals(Symbol.INT) && rhs.type.equals(Symbol.INT))) {
      errors.add( CompilerErrors.IllegalOp2Type(programName, ctx.op.getText(), ctx.op) );
      symboltrace.push( new Symbol.Error() );
      return;
    }

    if (lhs.isConst() && rhs.isConst()) {
      // remove sub expressions to clean up the symboltable
      if (lhs.isExpression()) symboltable.remove(lhs.name);
      if (rhs.isExpression()) symboltable.remove(rhs.name);

      final String exprName = Symbol.Expression.nameFor(ctx.hashCode(), ctx.getText());
      Symbol sym = fold(exprName, op, lhs.asConst(), rhs.asConst());
      symboltrace.push(sym);
      if (!sym.isError()) symboltable.add(sym);
    }
    else {
      symboltrace.push( new Symbol.Expression(Symbol.INT) );
    }
  }

  @Override
  public void exitComparison(ComparisonContext ctx) {
    final String op = ctx.op.getText();
    final Symbol rhs = symboltrace.pop();
    final Symbol lhs = symboltrace.pop();

    if (!lhs.isError() && !rhs.isError() && !(lhs.type.equals(Symbol.INT) && rhs.type.equals(Symbol.INT))) {
      errors.add( CompilerErrors.IllegalRelOpType(programName, ctx.op.getText(), ctx.op) );
      symboltrace.push( new Symbol.Error() );
      return;
    }

    if (lhs.isConst() && rhs.isConst()) {
      // remove sub expressions to clean up the symboltable
      if (lhs.isExpression()) symboltable.remove(lhs.name);
      if (rhs.isExpression()) symboltable.remove(rhs.name);

      final String exprName = Symbol.Expression.nameFor(ctx.hashCode(), ctx.getText());
      Symbol sym = fold(exprName, op, lhs.asConst(), rhs.asConst());
      symboltrace.push(sym);
      if (!sym.isError()) symboltable.add(sym);
    }
    else {
      symboltrace.push( new Symbol.Expression(Symbol.BOOL) );
    }
  }

  @Override
  public void exitEqualityComparison(EqualityComparisonContext ctx) {
    final String op = ctx.op.getText();
    final Symbol rhs = symboltrace.pop();
    final Symbol lhs = symboltrace.pop();

    if (!lhs.isError() && !rhs.isError() && !(lhs.type.equals(rhs.type))) {
      errors.add( CompilerErrors.IllegalEqualOpType(programName, ctx.op.getText(), ctx.op) );
      symboltrace.push( new Symbol.Error() );
      return;
    }

    if (lhs.isConst() && rhs.isConst()) {
      // remove sub expressions to clean up the symboltable
      if (lhs.isExpression()) symboltable.remove(lhs.name);
      if (rhs.isExpression()) symboltable.remove(rhs.name);

      final String exprName = Symbol.Expression.nameFor(ctx.hashCode(), ctx.getText());
      Symbol sym = fold(exprName, op, lhs.asConst(), rhs.asConst());
      symboltrace.push(sym);
      if (!sym.isError()) symboltable.add(sym);
    }
    else {
      symboltrace.push( new Symbol.Expression(Symbol.BOOL) );
    }
  }

  @Override
  public void exitBooleanExpr(BooleanExprContext ctx) {
    final String op = ctx.op.getText();
    final Symbol rhs = symboltrace.pop();
    final Symbol lhs = symboltrace.pop();

    if (!lhs.isError() && !rhs.isError() && !(lhs.type.equals(Symbol.BOOL) && rhs.type.equals(Symbol.BOOL))) {
      errors.add( CompilerErrors.IllegalOp2Type(programName, ctx.op.getText(), ctx.op) );
      symboltrace.push( new Symbol.Error() );
      return;
    }

    if (lhs.isConst() && rhs.isConst()) {
      // remove sub expressions to clean up the symboltable
      if (lhs.isExpression()) symboltable.remove(lhs.name);
      if (rhs.isExpression()) symboltable.remove(rhs.name);
      
      final String exprName = Symbol.Expression.nameFor(ctx.hashCode(), ctx.getText());
      Symbol sym = fold(exprName, op, lhs.asConst(), rhs.asConst());
      symboltrace.push(sym);
      if (!sym.isError()) symboltable.add(sym);
    }
    else {
      symboltrace.push( new Symbol.Expression(Symbol.BOOL) );
    }
  }

  /**
   * Creates an expression symbol with the computed value of the expression
   */
  protected Symbol fold(String name, String op, Symbol.Const lhs, Symbol.Const rhs) {
    int ilhs, irhs;
    boolean blhs, brhs;
    
    switch (op) {
      case "+":
        ilhs = Integer.parseInt(lhs.value);
        irhs = Integer.parseInt(rhs.value);
        return new Symbol.Expression(name, Symbol.INT, "" + (ilhs + irhs));
        
      case "-":
        ilhs = Integer.parseInt(lhs.value);
        irhs = Integer.parseInt(rhs.value);
        return new Symbol.Expression(name, Symbol.INT, "" + (ilhs - irhs));
        
      case "*":
        ilhs = Integer.parseInt(lhs.value);
        irhs = Integer.parseInt(rhs.value);
        return new Symbol.Expression(name, Symbol.INT, "" + (ilhs * irhs));
        
      case "/":
        ilhs = Integer.parseInt(lhs.value);
        irhs = Integer.parseInt(rhs.value);
        return new Symbol.Expression(name, Symbol.INT, "" + (ilhs / irhs));
      
      case "%":
        ilhs = Integer.parseInt(lhs.value);
        irhs = Integer.parseInt(rhs.value);
        return new Symbol.Expression(name, Symbol.INT, "" + (ilhs % irhs));
      
      case "==":
        if (lhs.type.equals(Symbol.INT)) {
          ilhs = Integer.parseInt(lhs.value);
          irhs = Integer.parseInt(rhs.value);
          return new Symbol.Expression(name, Symbol.BOOL, (ilhs == irhs) ? Symbol.TRUE : Symbol.FALSE);
        }
        else {
          blhs = lhs.value.equals(Symbol.TRUE) ? true : false;
          brhs = rhs.value.equals(Symbol.TRUE) ? true : false;
          return new Symbol.Expression(name, Symbol.BOOL, (blhs == brhs) ? Symbol.TRUE : Symbol.FALSE);
        }

      case "!=":
        if (lhs.type.equals(Symbol.INT)) {
          ilhs = Integer.parseInt(lhs.value);
          irhs = Integer.parseInt(rhs.value);
          return new Symbol.Expression(name, Symbol.BOOL, (ilhs != irhs) ? Symbol.TRUE : Symbol.FALSE);
        }
        else {
          blhs = lhs.value.equals(Symbol.TRUE) ? true : false;
          brhs = rhs.value.equals(Symbol.TRUE) ? true : false;
          return new Symbol.Expression(name, Symbol.BOOL, (blhs != brhs) ? Symbol.TRUE : Symbol.FALSE);
        }

      case "<":
        ilhs = Integer.parseInt(lhs.value);
        irhs = Integer.parseInt(rhs.value);
        return new Symbol.Expression(name, Symbol.BOOL, (ilhs < irhs) ? Symbol.TRUE : Symbol.FALSE);

      case ">":
        ilhs = Integer.parseInt(lhs.value);
        irhs = Integer.parseInt(rhs.value);
        return new Symbol.Expression(name, Symbol.BOOL, (ilhs > irhs) ? Symbol.TRUE : Symbol.FALSE);

      case "<=":
        ilhs = Integer.parseInt(lhs.value);
        irhs = Integer.parseInt(rhs.value);
        return new Symbol.Expression(name, Symbol.BOOL, (ilhs <= irhs) ? Symbol.TRUE : Symbol.FALSE);

      case ">=":
        ilhs = Integer.parseInt(lhs.value);
        irhs = Integer.parseInt(rhs.value);
        return new Symbol.Expression(name, Symbol.BOOL, (ilhs >= irhs) ? Symbol.TRUE : Symbol.FALSE);

      case "And":
        blhs = lhs.value.equals(Symbol.TRUE) ? true : false;
        brhs = rhs.value.equals(Symbol.TRUE) ? true : false;
        return new Symbol.Expression(name, Symbol.BOOL, (blhs && brhs) ? Symbol.TRUE : Symbol.FALSE);

      case "Or":
        blhs = lhs.value.equals(Symbol.TRUE) ? true : false;
        brhs = rhs.value.equals(Symbol.TRUE) ? true : false;
        return new Symbol.Expression(name, Symbol.BOOL, (blhs || brhs) ? Symbol.TRUE : Symbol.FALSE);

      default:
        return new Symbol.Error();
    }
  }

  @Override
  public void exitProcedureCall(ProcedureCallContext ctx) {
    final String fnName = ctx.Id().getText();
    boolean error = false;

    if (!symboltable.contains(fnName)) {
      errors.add( CompilerErrors.IdentNotDecl(programName, fnName, ctx.Id().getSymbol()) );
      error = true;
    }

    if (!error && !symboltable.get(fnName).isFunction()) {
      errors.add( CompilerErrors.SymbolIllegalUse(programName, fnName, symboltable.get(fnName).kind, ctx.Id().getSymbol()) );
      error = true;
    }

    if (error) {
      // clean up symboltrace
      for (int i = 0; i < ctx.expression().size(); i++)
        symboltrace.pop();

      // expressions should return a symbol for the type checks
      if (ctx.getParent() instanceof PrimaryExprContext)
        symboltrace.push( new Symbol.Error() );
        
      return;
    }

    Symbol.Function fn = symboltable.get(fnName).asFunction();

    if (ctx.expression().size() < fn.params.size()) {
      errors.add( CompilerErrors.TooFewArgs(programName, fnName, ctx.stop) );
      
      // clean up symboltrace
      for (int i = 0; i < ctx.expression().size(); i++)
        symboltrace.pop();

      // expressions should return a symbol for the type checks
      if (ctx.getParent() instanceof PrimaryExprContext)
        symboltrace.push( new Symbol.Error() );
      return;
    }

    // reverse the argument symbols since a stack is LIFO
    Stack<Symbol> args = new Stack<>();
    for (int i = 0; i < ctx.expression().size(); i++)
      args.push( symboltrace.pop() );

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

    fn.uses++;
    if (!fn.isPure) this.isPure = false;
    
    // expressions should return symbols for the type
    if (ctx.getParent() instanceof PrimaryExprContext)
      symboltrace.push( error ? new Symbol.Error() : new Symbol.Expression(fn.type) );
  }

  @Override
  public void exitCreationExpr(CreationExprContext ctx) {
    boolean error = false;
    String arraySuffix = "";
    
    for (int i = 0; i < ctx.expression().size(); i++) {
      final Symbol expr = symboltrace.pop();

      if (!expr.isError() && !expr.type.equals(Symbol.INT)) {
        errors.add( CompilerErrors.BadArraySelector(programName, ctx.expression(ctx.expression().size() - 1 - i).start) );
        error = true;
      }

      arraySuffix += "[]";
    }

    final Symbol baseType = symboltrace.pop();
    if (ctx.expression().size() == 0 && !baseType.isError() && (baseType.type.equals(Symbol.INT) || baseType.type.equals(Symbol.BOOL))) {
      errors.add( CompilerErrors.InvalidNewType(programName, ctx.baseType().start) );
      error = true;
    }

    symboltrace.push( error ? new Symbol.Error() : new Symbol.Expression(baseType.type + arraySuffix) );
  }

  @Override
  public void exitArrayLength(ArrayLengthContext ctx) {
    final Symbol id = symboltrace.pop();

    if (!id.isError() && !id.asVariable().isArray()) {
      errors.add( CompilerErrors.ArrayLenNotArray(programName, ctx.fullIdentifier().start) );
      symboltrace.push( new Symbol.Error() );
    }

    symboltrace.push( new Symbol.Expression(Symbol.INT) );
  }

  @Override
  public void beforeSelector(ParseTree ctx) {
    // first part of the full identifier
    if (ctx instanceof FullIdentifierContext) {
      FullIdentifierContext fictx = (FullIdentifierContext)ctx;
      final String name = fictx.Id().getText();

      if (!symboltable.contains(name)) {
        errors.add( CompilerErrors.IdentNotDecl(programName, name, fictx.Id().getSymbol()) );
        symboltrace.push( new Symbol.Error() );
        return;
      }

      final boolean constAllowed = !(fictx.getParent() instanceof AssignmentContext || fictx.getParent() instanceof ArrayLengthContext);
      if (!symboltable.get(name).isVariable() && !(constAllowed && symboltable.get(name).isConst())) {
        errors.add( CompilerErrors.SymbolIllegalUse(programName, name, symboltable.get(name).kind, fictx.Id().getSymbol()) );
        symboltrace.push( new Symbol.Error() );
        return;
      }

      if (symboltable.get(name).isVariable() && !symboltable.get(name).asVariable().isLocal)
        this.isPure = false;

      symboltrace.push( symboltable.get(name) );
    }
    // (optional) selector part(s) of a full identifier
    else {
      SelectorContext sctx = (SelectorContext)ctx;

      // record selector
      if (sctx.Id() != null) {
        Symbol id = symboltrace.pop();

        if (!symboltable.contains(id.type) || !symboltable.get(id.type).isRecord()) {
          errors.add( CompilerErrors.SelectorNotRecord(programName, sctx.start) );
          symboltrace.push( new Symbol.Error() );
          return;
        }
  
        Symbol selId = id.asVariable().selectField(symboltable, sctx.Id().getText());
  
        if (selId.isError()) {
          errors.add( CompilerErrors.InvalidRecordField(programName, sctx.Id().getText(), id.type, sctx.Id().getSymbol()) );
          symboltrace.push( new Symbol.Error() );
          return;
        }
  
        symboltrace.push(selId);
      }
      // array selector
      else {
        Symbol expr = symboltrace.pop();
        Symbol id = symboltrace.peek();

        if (!id.isError() && !id.isArray()) {
          errors.add( CompilerErrors.SelectorNotArray(programName, sctx.start) );
          symboltrace.pop();
          symboltrace.push( new Symbol.Error() );
          return;
        }
  
        if (!expr.isError() && !expr.type.equals(Symbol.INT)) {
          errors.add( CompilerErrors.BadArraySelector(programName, sctx.expression().start) );
          symboltrace.pop();
          symboltrace.push( new Symbol.Error() );
          return;
        }
  
        if (!expr.isError() && !id.isError()) {
          symboltrace.pop();
          symboltrace.push( id.asVariable().selectElement() );
        }
        else if (expr.isError()) {
          symboltrace.pop();
          symboltrace.push( new Symbol.Error() );
        }
      }
    }
  }

  @Override
  public void exitType(TypeContext ctx) {
    if (!symboltrace.peek().isError()) {
      symboltrace.pop();
      symboltrace.push( new Symbol.Type(ctx.getText()) );
    }
  }

  @Override
  public void exitBaseType(BaseTypeContext ctx) {
    final String type = ctx.getText();

    if (ctx.Id() != null && !symboltable.contains(type)) {
      errors.add( CompilerErrors.IdentNotDecl(programName, type, ctx.Id().getSymbol()) );
      symboltrace.push( new Symbol.Error() );
    } 
    else if (ctx.Id() != null && !symboltable.get(type).isRecord()) {
      errors.add( CompilerErrors.SymbolIllegalUse(programName, type, symboltable.get(type).kind, ctx.Id().getSymbol()) );
      symboltrace.push( new Symbol.Error() );
    } 
    else {
      symboltrace.push( new Symbol.Type(type) );
    }
  }

  @Override
  public void exitLiteral(LiteralContext ctx) {
    if (ctx.Boolean() != null)
      symboltrace.push(new Symbol.Expression(Symbol.BOOL, ctx.Boolean().getText()));
    else
      symboltrace.push(new Symbol.Expression(Symbol.INT, ctx.Number().getText()));
  }

}
