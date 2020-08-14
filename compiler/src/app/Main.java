package app;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import analysis.Analysis;
import app.CompilerErrors.CompilerError;
import codegen.CodeGenerator;
import codegen.JvmBackend;
import parser.YaplErrorListener;
import parser.YaplLexer;
import parser.YaplParser;
// import semantic.SemanticChecker;
// import app.CompilerErrors.CompilerError;
// import codegen.CodeGenerator;
// import codegen.JvmBackend;
import stdlib.JVMStandardLibrary;


public class Main {
  
  public static void main(String[] args) {
    YaplErrorListener errorListener = new YaplErrorListener();

    try {
      if (args.length < 1)
        throw new IOException("Missing argument, input file!");

      if (args.length < 2)
        throw new IOException("Missing argument, output directory!");
       
      if (Files.notExists( Paths.get(args[0]) ))
        throw new IOException("Input file not found!");

      // input
      CharStream input;
      try {
        input = CharStreams.fromFileName(args[0]);
      }
      catch(IOException ex) {
        throw new IOException("Cannot read the input file!", ex);
      }

      // lexer
      YaplLexer lexer = new YaplLexer(input);
      CommonTokenStream tokens = new CommonTokenStream(lexer);
      lexer.removeErrorListeners();
      lexer.addErrorListener(errorListener);
   
      // parser
      YaplParser parser = new YaplParser(tokens);
      parser.removeErrorListeners();
      parser.addErrorListener(errorListener);
      parser.addParseListener(errorListener);
      YaplParser.ProgramContext tree = parser.program();
      
      if (!errorListener.error)
        System.out.println("YAPL compilation: [" + errorListener.programName + "] OK");
      else
        return;

      // semantic analysis
      Analysis analysis = new Analysis(parser, JVMStandardLibrary.instance);
      ParseTreeWalker.DEFAULT.walk(new parser.DetailedYaplListenerAdapter(analysis), tree);

      for (CompilerError error : analysis.errors) {
        System.err.println( error );
        System.err.println();
      }

      // terminate the compiler if an error occured
      if (analysis.errors.size() != 0) return;

      // code generation
      CodeGenerator codegen = new CodeGenerator(JVMStandardLibrary.instance, analysis.symbolTable, args[1], JvmBackend.instance);
      codegen.visit(tree);
    }
    catch (IOException ex) {
      System.err.println( CompilerErrors.Lexical(errorListener.programName, ex.getLocalizedMessage(), 0, 0) );
      System.err.println();
    }
    // catch (Exception ex) {
    //   System.err.println( CompilerErrors.Internal(blockNameExtractor.programName, ex.getLocalizedMessage(), -1, -1) );
    //   System.err.println();
    // }
  }

}
