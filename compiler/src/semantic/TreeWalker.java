package semantic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

  public TreeWalker(BlockNameExtractor extractor) {
    this.symbolTable = new SymbolTable();
    this.extractor = extractor;
    this.errors = new ArrayList<>();
  }

  @Override
  public Attributes visitProgram(ProgramContext ctx) {
    final String name = ctx.Id(0).getText();
    final String endName = ctx.Id(1).getText();
    final Symbol symbol = new Symbol("program", name, ctx.start);

    symbolTable.addSymbol(symbol);
    symbolTable.openScope(symbol);

    visitChildren(ctx);

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
    visitBlock( ctx.block() );

    if (!endName.equals(name)) {
      errors.add( CompilerErrors.EndIdentMismatch(extractor.programName, name, endName, symbolTable.get(name).kind, ctx.Id(0).getSymbol()) );
      return null;
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
    visitChildren(ctx);
    symbolTable.closeScope();
    return null;
  }
  
  @Override
  public Attributes visitStatement(StatementContext ctx) {
    visitChildren(ctx);
    return null;
  }

  @Override
  public Attributes visitAssignment(AssignmentContext ctx) {
    visitChildren(ctx);
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
    
    for (ExpressionContext expr : ctx.expression()) {
      visit(expr);
    }

    return new Attributes( ((ProcedureSymbol)symbolTable.get(procedureName)).returnType );
  }

  @Override
  public Attributes visitReturnStatement(ReturnStatementContext ctx) {
    visitChildren(ctx);
    return null;
  }

  @Override
  public Attributes visitIfStatement(IfStatementContext ctx) {
    visitChildren(ctx);
    return null;
  }

  @Override
  public Attributes visitWriteStatement(WriteStatementContext ctx) {
    visitChildren(ctx);
    return null;
  }

  @Override
  public Attributes visitPrimaryExpr(PrimaryExprContext ctx) {
    return visitChildren(ctx);
  }

  @Override
  public Attributes visitArrayLength(ArrayLengthContext ctx) {
    visitChildren(ctx);
    return new Attributes("int");
  }

  @Override
  public Attributes visitArithmeticExpr(ArithmeticExprContext ctx) {
    visitChildren(ctx);
    return new Attributes("int");
  }

  @Override
  public Attributes visitComparison(ComparisonContext ctx) {
    visitChildren(ctx);
    return new Attributes("bool");
  }

  @Override
  public Attributes visitBooleanExpr(BooleanExprContext ctx) {
    visitChildren(ctx);
    return new Attributes("bool");
  }

  @Override
  public Attributes visitCreationExpr(CreationExprContext ctx) {
    final String baseType = visitBaseType( ctx.baseType() ).type;
    final int dimensions = ctx.expression().size();

    String type = baseType;
    for (int counter = 0; counter < dimensions; counter++)
      type += "[]";

    for (ExpressionContext expr : ctx.expression()) {
      visit(expr);
    }

    return new Attributes(type);
  }

  String selectedType;

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
    selectedType = (symbol instanceof ConstSymbol) ?  ((ConstSymbol)symbol).type : ((VariableSymbol)symbol).type;

    if (ctx.selector() != null) {
      visitSelector( ctx.selector() );
    }

    return new Attributes(selectedType);
  }

  @Override
  public Attributes visitSelector(SelectorContext ctx) {
    if (ctx.Id() != null) {
      final VariableSymbol field = (VariableSymbol)((RecordSymbol)symbolTable.get(selectedType)).getField( ctx.Id().getText() );
      selectedType = field.type;
    }
    else {
      selectedType = selectedType.substring(0, selectedType.length() - 2);
    }

    if (ctx.selector() != null) {
      visitSelector( ctx.selector() );
    }

    return new Attributes(selectedType);
  }

}
