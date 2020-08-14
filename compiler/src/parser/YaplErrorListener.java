package parser;

import java.util.BitSet;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.xpath.XPath;

import app.CompilerErrors;
import parser.YaplParser.ProcedureContext;
import parser.YaplParser.ProgramContext;
import parser.YaplParser.RecordDeclarationContext;

public class YaplErrorListener extends YaplBaseListener implements ANTLRErrorListener {

  public ParseTree parseTree;
  public String programName = null;
  public String procedureName = null;
  public String recordName = null;

  public boolean error = false;

  @Override
  public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int column, String msg,
      RecognitionException ex) {
    error = true;

    if (recognizer instanceof Parser) {
      Parser parser = (Parser) recognizer;
      Token token = (Token) offendingSymbol;

      String input = parser.getInputStream().getTokenSource().getInputStream().toString();
      String errorMessage = "Path: " + getPath(parser) + "\nMessage: " + msg + "\nCode:\n"
          + getUnderlinedCodeSnippet(input, token, 1);

      System.err.println(CompilerErrors.Syntax(programName, errorMessage, line, column + 1));
      System.err.println();
    } else {
      System.err.println(CompilerErrors.Lexical(programName, msg, line, column + 1));
      System.err.println();
    }
  }

  @Override
  public void visitTerminal(TerminalNode node) {
    if (parseTree == null)
      parseTree = node.getParent();

    ParseTree parent = node.getParent();

    if (parent instanceof ProgramContext) {
      ProgramContext program = (ProgramContext) parent;

      if (program.Id(0) != null)
        programName = program.Id(0).getText();
    }

    if (parent instanceof ProcedureContext) {
      ProcedureContext procedure = (ProcedureContext) parent;

      if (procedure.Id(0) != null)
        procedureName = procedure.Id(0).getText();
    }

    if (parent instanceof RecordDeclarationContext) {
      RecordDeclarationContext record = (RecordDeclarationContext) parent;

      if (record.Id() != null)
        recordName = record.Id().getText();
    }
  }

  protected String getPath(Parser parser) {
    List<String> stack = parser.getRuleInvocationStack();
    Collections.reverse(stack);

    String path = "program " + programName + " > ";
    stack.remove(0);

    ParseTree currNode = parseTree;
    for (String rule : stack) {
      List<ParseTree> matches = new LinkedList<>(XPath.findAll(currNode, "*/" + rule, parser));

      if (rule.equals("expression")) {
        path += rule + " > ";
        break;
      }

      if (rule.equals("statement"))
        /* ignore */;
      else if (procedureName != null && rule.equals("procedure"))
        path += rule + " " + procedureName + " > ";
      else if (recordName != null && rule.equals("recordDeclaration"))
        path += rule + " " + recordName + " > ";
      else if (matches.size() > 1)
        path += matches.size() + ". " + rule + " > ";
      else
        path += rule + " > ";

      currNode = matches.get(matches.size() - 1);
    }

    return path.substring(0, path.length() - " > ".length());
  }

  protected String getUnderlinedCodeSnippet(String input, Token token, int nrOfShownLines) {
    String[] lines = input.split("\n");
    int errorLineIdx = token.getLine() - 1;

    String shownLines = "";
    for (int idx = errorLineIdx; idx > 0 && idx > errorLineIdx - nrOfShownLines; idx--)
      shownLines = lines[idx] + "\n" + shownLines;

    String underline = "";

    for (int c = 0; c < token.getCharPositionInLine(); c++)
      underline += " ";

    for (int c = 0; c < token.getText().length(); c++)
      underline += "^";

    return shownLines + underline;
  }

  @Override
  public void reportAmbiguity(Parser arg0, DFA arg1, int arg2, int arg3, boolean arg4, BitSet arg5, ATNConfigSet arg6) {

  }

  @Override
  public void reportAttemptingFullContext(Parser arg0, DFA arg1, int arg2, int arg3, BitSet arg4, ATNConfigSet arg5) {

  }

  @Override
  public void reportContextSensitivity(Parser arg0, DFA arg1, int arg2, int arg3, int arg4, ATNConfigSet arg5) {

  }

}
