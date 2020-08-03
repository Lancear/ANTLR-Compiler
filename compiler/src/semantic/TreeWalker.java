package semantic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

import parser.YaplBaseVisitor;
import parser.YaplParser.*;
import semantic.CompilerErrors.CompilerError;
import semantic.symbols.*;

public class TreeWalker extends YaplBaseVisitor<Attributes> {

  protected SymbolTable symbolTable;
  protected String currType;
  protected BlockNameExtractor extractor;
  public List<CompilerError> errors;
  public ProcedureSymbol currProcedure;

  public TreeWalker(BlockNameExtractor extractor) {
    this.symbolTable = new SymbolTable();
    this.extractor = extractor;
    this.errors = new ArrayList<>();
    this.currProcedure = null;
  }

  @Override
  public Attributes visitProgram(ProgramContext ctx) {
    final String name = ctx.Id(0).getText();
    final String endName = ctx.Id(1).getText();
    final Symbol symbol = new Symbol("program", name, ctx.start);

    symbolTable.addSymbol(symbol);
    symbolTable.openScope(symbol);

    for (ParseTree child : ctx.children) {
      if (child instanceof StatementContext)
        break;

      visit(child);
    }

    for (StatementContext stat : ctx.statement()) {
      Attributes statAttrs = visitStatement(stat);

      if (stat.returnStatement() != null) {
        if (currProcedure == null && !statAttrs.type.equals("void")) {
          errors.add( CompilerErrors.IllegalRetValMain(extractor.programName, stat.returnStatement().expression().start) );
        }
        else if (currProcedure != null) {
          if (currProcedure.returnType.equals("void") && !statAttrs.type.equals("void")) {
            errors.add( CompilerErrors.IllegalRetValProc(extractor.programName, currProcedure.name, stat.returnStatement().expression().start) );
          }
          else if (!statAttrs.type.equals(currProcedure.returnType) && !statAttrs.type.equals("<error>")) {
            Token token = (stat.returnStatement().expression() != null) ? stat.returnStatement().expression().start : stat.returnStatement().stop;
            errors.add( CompilerErrors.InvalidReturnType(extractor.programName, currProcedure.name, token) );
          }
        }
      }
    }

    if (!endName.equals(name)) {
      errors.add( CompilerErrors.EndIdentMismatch(extractor.programName, name, endName, symbolTable.get(name).kind, ctx.Id(0).getSymbol()) );
      return null;
    }

    symbolTable.closeScope();
    return null;
  }

  @Override
  public Attributes visitDeclarationBlock(DeclarationBlockContext ctx) {
    visitChildren(ctx);
    return null;
  }

  @Override
  public Attributes visitProcedure(ProcedureContext ctx) {
    final String name = ctx.Id(0).getText();
    final String endName = ctx.Id(1).getText();

    if (symbolTable.containsInScope(name)) {
      errors.add( CompilerErrors.SymbolExists(extractor.programName, name, symbolTable.get(name).kind, ctx.Id(0).getSymbol()) );
      return null;
    }

    final String returnType = (ctx.type() != null) ? visitType( ctx.type() ).type : "void";
    final ProcedureSymbol symbol = new ProcedureSymbol(name, returnType, ctx.start);
    symbolTable.addSymbol(symbol);
    symbolTable.openScope(symbol);

    final List<String> paramTypes = new ArrayList<>();
    for (ParamContext param : ctx.param()) {
      final Attributes attrs = visitParam(param);
      paramTypes.add(attrs.type);
    }

    symbol.setParamTypes(paramTypes);

    currProcedure = symbol;
    final Attributes attrs = visitBlock( ctx.block() );
    currProcedure = null;

    if (!returnType.equals("void") && !attrs.hasReturn) {
      errors.add( CompilerErrors.MissingReturn(extractor.programName, name, ctx.block().stop) );
    }

    if (!endName.equals(name)) {
      errors.add( CompilerErrors.EndIdentMismatch(extractor.programName, name, endName, symbolTable.get(name).kind, ctx.Id(0).getSymbol()) );
    }

    symbolTable.closeScope();
    return null;
  }

  @Override
  public Attributes visitVarDeclaration(VarDeclarationContext ctx) {
    final Attributes attrs = visitType( ctx.type() );
    final int dimensions = attrs.type.split("\\[").length;
    attrs.names = new ArrayList<>();
    
    for (TerminalNode id : ctx.Id()) {
      String name = id.getText();

      if (symbolTable.containsInScope(name)) {
        errors.add( CompilerErrors.SymbolExists(extractor.programName, name, symbolTable.get(name).kind, id.getSymbol()) );
        name = "<error>";
      }
      
      final Symbol symbol = (dimensions != 0) 
        ? new ArrayVariableSymbol(name, attrs.type, dimensions, false, ctx.start) 
        : new VariableSymbol(name, attrs.type, false, ctx.start);

      symbolTable.addSymbol(symbol);
      attrs.names.add(name);
    }

    return attrs;
  }

  @Override
  public Attributes visitConstDeclaration(ConstDeclarationContext ctx) {
    final String name = ctx.Id().getText();
    final Attributes attrs = visitLiteral( ctx.literal() );
    attrs.name = name;

    if (symbolTable.containsInScope(name)) {
      errors.add( CompilerErrors.SymbolExists(extractor.programName, name, symbolTable.get(name).kind, ctx.Id().getSymbol()) );
      return attrs;
    }

    final Symbol symbol = new ConstSymbol(name, attrs.type, ctx.start);
    symbolTable.addSymbol(symbol);

    return attrs;
  }

  @Override
  public Attributes visitRecordDeclaration(RecordDeclarationContext ctx) {
    final String name = ctx.Id().getText();

    if (symbolTable.containsInScope(name)) {
      errors.add( CompilerErrors.SymbolExists(extractor.programName, name, symbolTable.get(name).kind, ctx.Id().getSymbol()) );
      return null;
    }

    final RecordSymbol symbol = new RecordSymbol(name, ctx.start);
    symbolTable.addSymbol(symbol);
    symbolTable.openScope(symbol);

    final Map<String, VariableSymbol> fields = new HashMap<>();
    for (VarDeclarationContext varDecl : ctx.varDeclaration()) {
      final Attributes attrs = visitVarDeclaration(varDecl);

      for (String fieldName : attrs.names) {
        fields.put( fieldName, (VariableSymbol)symbolTable.get(fieldName) );
      }
    }

    symbol.setFields(fields);
    symbolTable.closeScope();
    return null;
  }

  @Override
  public Attributes visitBaseType(BaseTypeContext ctx) {
    String type = ctx.getText();
    
    if (ctx.Id() != null && !symbolTable.contains(type)) {
      errors.add( CompilerErrors.IdentNotDecl(extractor.programName, type, ctx.Id().getSymbol()) );
      return new Attributes("<error>");
    }

    if (ctx.Id() != null && !(symbolTable.get(type) instanceof RecordSymbol)) {
      errors.add( CompilerErrors.SymbolIllegalUse(extractor.programName,type, symbolTable.get(type).kind, ctx.Id().getSymbol()) );
      return new Attributes("<error>");
    }

    return new Attributes( ctx.getText() );
  }

  @Override
  public Attributes visitType(TypeContext ctx) {
    visit( ctx.baseType() );

    return new Attributes( ctx.getText() );
  }

  @Override
  public Attributes visitLiteral(LiteralContext ctx) {
    return new Attributes( (ctx.Boolean() != null) ? "bool" : "int" );
  }

  @Override
  public Attributes visitParam(ParamContext ctx) {
    final String name = ctx.Id().getText();
    final Attributes attrs = visitType( ctx.type() );

    if (symbolTable.containsInScope(name)) {
      errors.add( CompilerErrors.SymbolExists(extractor.programName, name, symbolTable.get(name).kind, ctx.Id().getSymbol()) );
      return attrs;
    }

    final int dimensions = attrs.type.split("\\[").length;
    final Symbol symbol = (dimensions != 0) 
      ? new ArrayVariableSymbol(name, attrs.type, dimensions, true, ctx.start) 
      : new VariableSymbol(name, attrs.type, true, ctx.start);

    symbolTable.addSymbol(symbol);
    return attrs;
  }

  @Override
  public Attributes visitBlock(BlockContext ctx) {
    symbolTable.openScope();

    if (ctx.declarationBlock() != null)
      visitDeclarationBlock(ctx.declarationBlock());

    int nrOfReturns = 0;
    for (StatementContext stat : ctx.statement()) {
      Attributes statAttrs = visitStatement(stat);

      if (stat.returnStatement() != null) {
        nrOfReturns++;

        if (currProcedure == null && !statAttrs.type.equals("void")) {
          errors.add( CompilerErrors.IllegalRetValMain(extractor.programName, stat.returnStatement().expression().start) );
        }
        else if (currProcedure != null) {
          if (currProcedure.returnType.equals("void") && !statAttrs.type.equals("void")) {
            errors.add( CompilerErrors.IllegalRetValProc(extractor.programName, currProcedure.name, stat.returnStatement().expression().start) );
          }
          else if (!statAttrs.type.equals(currProcedure.returnType) && !statAttrs.type.equals("<error>")) {
            Token token = (stat.returnStatement().expression() != null) ? stat.returnStatement().expression().start : stat.returnStatement().stop;
            errors.add( CompilerErrors.InvalidReturnType(extractor.programName, currProcedure.name, token) );
          }
        }
      }
    }

    symbolTable.closeScope();
    return new Attributes(nrOfReturns > 0);
  }
  
  @Override
  public Attributes visitStatement(StatementContext ctx) {
    if (ctx.assignment() != null) {
      return visitAssignment(ctx.assignment());
    }
    else if (ctx.procedureCall() != null) {
      return visitProcedureCall(ctx.procedureCall());
    }
    else if (ctx.returnStatement() != null) {
      return visitReturnStatement(ctx.returnStatement());
    }
    else if (ctx.ifStatement() != null) {
      return visitIfStatement(ctx.ifStatement());
    }
    else if (ctx.whileStatement() != null) {
      return visitWhileStatement(ctx.whileStatement());
    }
    else if (ctx.writeStatement() != null) {
      return visitWriteStatement(ctx.writeStatement());
    }
    else if (ctx.block() != null) {
      return visitBlock(ctx.block());
    }

    throw new IllegalStateException("The if-else-if structure above should handle all possible scenarios!");
  }

  @Override
  public Attributes visitAssignment(AssignmentContext ctx) {
    final Attributes lhs = visit(ctx.fullIdentifier());
    final Attributes rhs = visit(ctx.expression());

    if (!lhs.type.equals(rhs.type) && !lhs.type.equals("<error>") && !rhs.type.equals("<error>")) {
      errors.add( CompilerErrors.TypeMismatchAssign(extractor.programName, ctx.op) );
      return null;
    }

    return null;
  }

  @Override
  public Attributes visitProcedureCall(ProcedureCallContext ctx) {
    final String procedureName = ctx.Id().getText();

    if (!symbolTable.contains(procedureName)) {
      errors.add( CompilerErrors.IdentNotDecl(extractor.programName, procedureName, ctx.Id().getSymbol()) );
      return new Attributes("<error>");
    }

    if (!(symbolTable.get(procedureName) instanceof ProcedureSymbol)) {
      errors.add( CompilerErrors.SymbolIllegalUse(extractor.programName, procedureName, symbolTable.get(procedureName).kind, ctx.Id().getSymbol()) );
      return new Attributes("<error>");
    }
    
    ProcedureSymbol symbol = (ProcedureSymbol)symbolTable.get(procedureName);

    if (ctx.expression().size() < symbol.getParamTypes().size()) {
      errors.add( CompilerErrors.TooFewArgs(extractor.programName, procedureName, ctx.stop) );
      return new Attributes("<error>");
    }

    int idx = 0;
    for (ExpressionContext expr : ctx.expression()) {
      Attributes attrs = visit(expr);

      if (!attrs.type.equals( symbol.getParamType(idx) ) && !attrs.type.equals("<error>")) {
        errors.add( CompilerErrors.ArgNotApplicable(extractor.programName, procedureName, idx + 1, expr.start) );
        return new Attributes("<error>");
      }

      idx++;
    }

    return new Attributes( ((ProcedureSymbol)symbolTable.get(procedureName)).returnType );
  }

  @Override
  public Attributes visitReturnStatement(ReturnStatementContext ctx) {
    return (ctx.expression() != null) ? visit( ctx.expression() ) : new Attributes("void");
  }

  @Override
  public Attributes visitIfStatement(IfStatementContext ctx) {
    Attributes attrs = visit( ctx.expression() );

    if (!attrs.type.equals("bool") && !attrs.type.equals("<error>")) {
      errors.add( CompilerErrors.CondNotBool(extractor.programName, ctx.expression().start) );
      return null;
    }

    int nrOfReturns = 0;
    for (StatementContext stat : ctx.statement()) {
      Attributes statAttrs = visitStatement(stat);

      if (stat.returnStatement() != null) {
        nrOfReturns++;

        if (currProcedure == null && !statAttrs.type.equals("void")) {
          errors.add( CompilerErrors.IllegalRetValMain(extractor.programName, stat.returnStatement().expression().start) );
        }
        else if (currProcedure != null) {
          if (currProcedure.returnType.equals("void") && !statAttrs.type.equals("void")) {
            errors.add( CompilerErrors.IllegalRetValProc(extractor.programName, currProcedure.name, stat.returnStatement().expression().start) );
          }
          else if (!statAttrs.type.equals(currProcedure.returnType) && !statAttrs.type.equals("<error>")) {
            Token token = (stat.returnStatement().expression() != null) ? stat.returnStatement().expression().start : stat.returnStatement().stop;
            errors.add( CompilerErrors.InvalidReturnType(extractor.programName, currProcedure.name, token) );
          }
        }
      }
    }

    return new Attributes(nrOfReturns > 0);
  }

  @Override
  public Attributes visitWhileStatement(WhileStatementContext ctx) {
    Attributes attrs = visit( ctx.expression() );

    if (!attrs.type.equals("bool") && !attrs.type.equals("<error>")) {
      errors.add( CompilerErrors.CondNotBool(extractor.programName, ctx.expression().start) );
      return null;
    }

    int nrOfReturns = 0;
    for (StatementContext stat : ctx.statement()) {
      Attributes statAttrs = visitStatement(stat);

      if (stat.returnStatement() != null) {
        nrOfReturns++;

        if (currProcedure == null && !statAttrs.type.equals("void")) {
          errors.add( CompilerErrors.IllegalRetValMain(extractor.programName, stat.returnStatement().expression().start) );
        }
        else if (currProcedure != null) {
          if (currProcedure.returnType.equals("void") && !statAttrs.type.equals("void")) {
            errors.add( CompilerErrors.IllegalRetValProc(extractor.programName, currProcedure.name, stat.returnStatement().expression().start) );
          }
          else if (!statAttrs.type.equals(currProcedure.returnType) && !statAttrs.type.equals("<error>")) {
            Token token = (stat.returnStatement().expression() != null) ? stat.returnStatement().expression().start : stat.returnStatement().stop;
            errors.add( CompilerErrors.InvalidReturnType(extractor.programName, currProcedure.name, token) );
          }
        }
      }
    }

    return new Attributes(nrOfReturns > 0);
  }

  @Override
  public Attributes visitWriteStatement(WriteStatementContext ctx) {
    visitChildren(ctx);
    return null;
  }

  @Override
  public Attributes visitUnaryExpr(UnaryExprContext ctx) {
    final Attributes attrs = visitPrimaryExpr( ctx.primaryExpr() );

    if (ctx.sign != null && !attrs.type.equals("int") && !attrs.type.equals("<error>")) {
      errors.add( CompilerErrors.IllegalOp1Type(extractor.programName, ctx.sign.getText(), ctx.sign) );
      return new Attributes("<error>");
    }
    
    return attrs;
  }

  @Override
  public Attributes visitPrimaryExpr(PrimaryExprContext ctx) {
    if (ctx.procedureCall() != null) {
      Attributes attrs = visitProcedureCall(ctx.procedureCall());

      if (attrs.type.equals("void")) {
        errors.add( CompilerErrors.ProcNotFuncExpr(extractor.programName, ctx.procedureCall().Id().getText(), ctx.start) );
        return new Attributes("<error>");
      }

      return attrs;
    }
    else if (ctx.literal() != null) {
      return visitLiteral(ctx.literal());
    }
    else if (ctx.arrayLength() != null) {
      return visitArrayLength(ctx.arrayLength());
    }
    else if (ctx.fullIdentifier() != null) {
      return visitFullIdentifier(ctx.fullIdentifier());
    }
    else if (ctx.expression() != null) {
      return visit(ctx.expression());
    }

    throw new IllegalStateException("The if-else-if structure above should handle all possible scenarios!");
  }

  @Override
  public Attributes visitArrayLength(ArrayLengthContext ctx) {
    Attributes attrs = visitFullIdentifier(ctx.fullIdentifier());

    if (!attrs.type.endsWith("[]") && !attrs.type.equals("<error>")) {
      errors.add( CompilerErrors.ArrayLenNotArray(extractor.programName, ctx.fullIdentifier().start) );
      return new Attributes("<error>");
    }

    return new Attributes("int");
  }

  @Override
  public Attributes visitArithmeticExpr(ArithmeticExprContext ctx) {
    final Attributes lhs = visit(ctx.expression(0));
    final Attributes rhs = visit(ctx.expression(1));

    if (!(lhs.type.equals("int") && rhs.type.equals("int")) && !lhs.type.equals("<error>") && !rhs.type.equals("<error>")) {
      errors.add( CompilerErrors.IllegalOp2Type(extractor.programName, ctx.op.getText(), ctx.op) );
      return new Attributes("<error>");
    }

    return new Attributes("int");
  }

  @Override
  public Attributes visitComparison(ComparisonContext ctx) {
    final Attributes lhs = visit(ctx.expression(0));
    final Attributes rhs = visit(ctx.expression(1));

    if (!(lhs.type.equals("int") && rhs.type.equals("int")) && !lhs.type.equals("<error>") && !rhs.type.equals("<error>")) {
      errors.add( CompilerErrors.IllegalRelOpType(extractor.programName, ctx.op.getText(), ctx.op) );
      return new Attributes("<error>");
    }

    return new Attributes("bool");
  }

  @Override
  public Attributes visitEqualityComparison(EqualityComparisonContext ctx) {
    final Attributes lhs = visit(ctx.expression(0));
    final Attributes rhs = visit(ctx.expression(1));

    if (!lhs.type.equals(rhs.type) && !lhs.type.equals("<error>") && !rhs.type.equals("<error>")) {
      errors.add( CompilerErrors.IllegalEqualOpType(extractor.programName, ctx.op.getText(), ctx.op) );
      return new Attributes("<error>");
    }

    return new Attributes("bool");
  }

  @Override
  public Attributes visitBooleanExpr(BooleanExprContext ctx) {
    final Attributes lhs = visit(ctx.expression(0));
    final Attributes rhs = visit(ctx.expression(1));

    if (!(lhs.type.equals("bool") && rhs.type.equals("bool")) && !lhs.type.equals("<error>") && !rhs.type.equals("<error>")) {
      errors.add( CompilerErrors.IllegalOp2Type(extractor.programName, ctx.op.getText(), ctx.op) );
      return new Attributes("<error>");
    }

    return new Attributes("bool");
  }

  @Override
  public Attributes visitCreationExpr(CreationExprContext ctx) {
    final String baseType = visitBaseType( ctx.baseType() ).type;
    final int dimensions = ctx.expression().size();

    String type = baseType;
    boolean error = false;
    for (int counter = 0; counter < dimensions; counter++)
      type += "[]";

    for (ExpressionContext expr : ctx.expression()) {
      Attributes attrs = visit(expr);
      if (!attrs.type.equals("int") && !attrs.type.equals("<error>")) {
        errors.add( CompilerErrors.BadArraySelector(extractor.programName, expr.start) );
        error = true;
      }
    }

    if (dimensions == 0 && !(symbolTable.get(baseType) instanceof RecordSymbol)) {
      errors.add( CompilerErrors.InvalidNewType(extractor.programName, ctx.baseType().start) );
      error = true;
    }

    return error ? new Attributes("<error>") : new Attributes(type);
  }

  Stack<String> selectedType = new Stack<>();

  @Override
  public Attributes visitFullIdentifier(FullIdentifierContext ctx) {
    final String name = ctx.Id().getText();

    if (!symbolTable.contains(name)) {
      errors.add( CompilerErrors.IdentNotDecl(extractor.programName, name, ctx.Id().getSymbol()) );
      return new Attributes("<error>");
    }

    final boolean constAllowed = !(ctx.getParent() instanceof AssignmentContext || ctx.getParent() instanceof ArrayLengthContext);

    if (!(symbolTable.get(name) instanceof VariableSymbol || (constAllowed && symbolTable.get(name) instanceof ConstSymbol))) {
      errors.add( CompilerErrors.SymbolIllegalUse(extractor.programName, name, symbolTable.get(name).kind, ctx.Id().getSymbol()) );
      return new Attributes("<error>");
    }

    Symbol symbol = symbolTable.get(name);
    selectedType.push( (symbol instanceof ConstSymbol) ?  ((ConstSymbol)symbol).type : ((VariableSymbol)symbol).type );

    if (ctx.selector() != null) {
      visitSelector( ctx.selector() );
    }

    final String top = selectedType.pop();
    return new Attributes(top);
  }

  @Override
  public Attributes visitSelector(SelectorContext ctx) {
    if (ctx.Id() != null) {
      if (!(symbolTable.get(selectedType.peek()) instanceof RecordSymbol)) {
        errors.add( CompilerErrors.SelectorNotRecord(extractor.programName, ctx.start) );
        selectedType.pop();
        selectedType.push("<error>");
        return new Attributes("<error>");
      }

      RecordSymbol record = (RecordSymbol)symbolTable.get(selectedType.peek());
      final VariableSymbol field = (VariableSymbol)record.getField( ctx.Id().getText() );

      if (field == null) {
        errors.add( CompilerErrors.InvalidRecordField(extractor.programName, ctx.Id().getText(), selectedType.peek(), ctx.Id().getSymbol()) );
        selectedType.pop();
        selectedType.push("<error>");
        return new Attributes("<error>");
      }

      selectedType.pop();
      selectedType.push(field.type);
    }
    else {
      if (!selectedType.peek().endsWith("[]") && !selectedType.peek().equals("<error>")) {
        errors.add( CompilerErrors.SelectorNotArray(extractor.programName, ctx.start) );
        selectedType.pop();
        selectedType.push("<error>");
        return new Attributes("<error>");
      }

      Attributes attrs = visit(ctx.expression());
      if (!attrs.type.equals("int") && !attrs.type.equals("<error>")) {
        errors.add( CompilerErrors.BadArraySelector(extractor.programName, ctx.expression().start) );
        selectedType.pop();
        selectedType.push("<error>");
        return new Attributes("<error>");
      }

      if (!selectedType.peek().equals("<error>")) {
        final String top = selectedType.pop();
        selectedType.push(top.substring(0, top.length() - 2));
      }
    }

    if (ctx.selector() != null) {
      visitSelector( ctx.selector() );
    }

    return new Attributes(selectedType.peek());
  }

}
