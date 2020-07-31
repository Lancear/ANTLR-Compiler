package app;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import parser.YaplErrorListener;
import parser.YaplLexer;
import parser.YaplVisitor;
import parser.YaplParser;
import semantic.BlockNameExtractor;
import semantic.CompilerErrors;
import semantic.TreeWalker;
import semantic.CompilerErrors.CompilerError;


public class Main {
  
  public static void main(String[] args) {
    BlockNameExtractor blockNameExtractor = new BlockNameExtractor();

    try {
      if (args.length == 0)
        throw new IOException("Missing argument, input file!");
       
      if (Files.notExists( Paths.get(args[0] + ".yapl") ))
        throw new IOException("Input file not found!");

      // input
      CharStream input;
      try {
        input = CharStreams.fromFileName(args[0] + ".yapl");
      }
      catch(IOException ex) {
        throw new IOException("Cannot read the input file!", ex);
      }

      // lexer
      YaplLexer lexer = new YaplLexer(input);
      CommonTokenStream tokens = new CommonTokenStream(lexer);
      lexer.removeErrorListeners();
      lexer.addErrorListener(new YaplErrorListener(blockNameExtractor));
   
      // parser
      YaplParser parser = new YaplParser(tokens);
      parser.removeErrorListeners();
      parser.addErrorListener(new YaplErrorListener(blockNameExtractor));
      blockNameExtractor.setParser(parser);
      YaplParser.ProgramContext tree = parser.program();
  
      // semantics
      System.out.println("parsed!");

      TreeWalker visitor = new TreeWalker(blockNameExtractor);
      visitor.visit(tree);

      for (CompilerError error : visitor.errors) {
        System.err.println( error );
        System.err.println();
      }
    }
    catch (IOException ex) {
      System.err.println( CompilerErrors.Lexical(blockNameExtractor.programName, ex.getLocalizedMessage(), 0, 0) );
      System.err.println();
    }
    // catch (Exception ex) {
    //   System.err.println( CompilerErrors.Internal(blockNameExtractor.programName, ex.getLocalizedMessage(), -1, -1) );
    //   System.err.println();
    // }
  }

}
