package semantic;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import parser.YaplBaseListener;
import parser.YaplParser.ProcedureContext;
import parser.YaplParser.ProgramContext;
import parser.YaplParser.RecordDeclarationContext;

public class BlockNameExtractor extends YaplBaseListener {
  
  public ParseTree parseTree;
  public String programName = null;
  public String procedureName = null;
  public String recordName = null;
  
  protected Parser parser;

  public BlockNameExtractor() {
    parser = null;
  }

  public void setParser(Parser parser) {
    this.parser = parser;
    parser.addParseListener(this);
  }

  @Override
  public void visitTerminal(TerminalNode node) {
    if (parser == null) return;

    if (parseTree == null)
      parseTree = node.getParent();

    ParseTree parent = node.getParent();

    if (parent instanceof ProgramContext) {
      ProgramContext program = (ProgramContext)parent;

      if (program.Id(0) != null)
        programName = program.Id(0).getText();
    }

    if (parent instanceof ProcedureContext) {
      ProcedureContext procedure = (ProcedureContext)parent;

      if (procedure.Id(0) != null)
        procedureName = procedure.Id(0).getText();
    }

    if (parent instanceof RecordDeclarationContext) {
      RecordDeclarationContext record = (RecordDeclarationContext)parent;

      if (record.Id() != null)
        recordName = record.Id().getText();
    }
  }

}
