package parser;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.xpath.XPath;

import semantic.CompilerErrors;
import semantic.BlockNameExtractor;

public class YaplErrorListener extends BaseErrorListener {

  protected BlockNameExtractor extractor;

  public YaplErrorListener(BlockNameExtractor extractor) {
    this.extractor = extractor;
  }

  @Override
  public void syntaxError(
    Recognizer<?, ?> recognizer, 
    Object offendingSymbol, 
    int line, 
    int column,
    String msg, 
    RecognitionException ex
  ) {
    if (recognizer instanceof Parser) {
      Parser parser = (Parser)recognizer;
      Token token = (Token)offendingSymbol;

      String input = parser.getInputStream().getTokenSource().getInputStream().toString();
      String errorMessage = "Path: " + getPath(parser) + "\nMessage: " + msg + "\nCode:\n" + getUnderlinedCodeSnippet(input, token, 1);

      System.err.println( CompilerErrors.Syntax(extractor.programName, errorMessage, line, column + 1) );
      System.err.println();
    }
    else {
      System.err.println( CompilerErrors.Lexical(extractor.programName, msg, line, column + 1) );
      System.err.println();
    }
  }

  protected String getPath(Parser parser) {
    List<String> stack = parser.getRuleInvocationStack();
    Collections.reverse(stack);

    if (extractor == null)
      return String.join(" > ", stack);

    String path = "program " + extractor.programName + " > ";
    stack.remove(0);

    ParseTree currNode = extractor.parseTree;
    for (String rule : stack) {
      List<ParseTree> matches = new LinkedList<>( XPath.findAll(currNode, "*/" + rule, parser) );

      if (rule.equals("expression")) {
        path += rule + " > ";
        break;
      }

      if (rule.equals("statement")) 
        /* ignore */;
      else if (extractor.procedureName != null && rule.equals("procedure")) 
        path += rule + " " + extractor.procedureName + " > ";
      else if (extractor.recordName != null && rule.equals("recordDeclaration")) 
        path += rule + " " + extractor.recordName + " > ";
      else if (matches.size() > 1) 
        path += matches.size() + ". " + rule + " > ";
      else 
        path += rule + " > ";

      currNode = matches.get( matches.size() - 1);
    }

    return path.substring(0, path.length() - " > ".length());
  }

  protected String getUnderlinedCodeSnippet(String input, Token token, int nrOfShownLines) {
    String[] lines = input.split("\n");
    int errorLineIdx = token.getLine() - 1;

    String shownLines = "";
    for(int idx = errorLineIdx; idx > 0 && idx > errorLineIdx - nrOfShownLines; idx--) 
      shownLines = lines[idx] + "\n" + shownLines;

    String underline = "";
    
    for (int c = 0; c < token.getCharPositionInLine(); c++)
      underline += " ";

    for (int c = 0; c < token.getText().length(); c++)
      underline += "^";

    return shownLines + underline;
  }

}
