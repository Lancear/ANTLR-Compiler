package parser;

import compiler.Compiler;
import information.CompilerError;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.BitSet;

public class ErrorListener extends YaplBaseListener implements ANTLRErrorListener {

  public ParseTree root = null;

  @Override
  public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int column, String msg, RecognitionException ex) {
    Token token = (Token) offendingSymbol;

    if (recognizer instanceof Parser) {
      ParserRuleContext context = ((Parser)recognizer).getRuleContext();
      Compiler.errors.add(CompilerError.Syntax(msg, context, token));
    }
    else {
      Compiler.errors.add(CompilerError.Lexical(msg, line, column + 1));
    }
  }



  @Override
  public void reportAmbiguity(Parser parser, DFA dfa, int i, int i1, boolean b, BitSet bitSet, ATNConfigSet atnConfigSet) {

  }

  @Override
  public void reportAttemptingFullContext(Parser parser, DFA dfa, int i, int i1, BitSet bitSet, ATNConfigSet atnConfigSet) {

  }

  @Override
  public void reportContextSensitivity(Parser parser, DFA dfa, int i, int i1, int i2, ATNConfigSet atnConfigSet) {

  }
}
