package app;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import analysis.Analysis;
import analysis.SymbolTable;
import app.CompilerErrors.CompilerError;
import codegen.CodeGenerator;
import codegen.JvmBackend;
import parser.YaplErrorListener;
import parser.YaplLexer;
import parser.YaplParser;
import parser.YaplParser.ProgramContext;
import profiler.JvmProfilerBackend;
import profiler.Profiler;
import profiler.SymbolDump;
import stdlib.JvmStandardLibrary;
import stdlib.StandardLibrary;

/**
 * The glue of the compiler which puts all components together to compile yapl to the given target language
 */
public class Main {
  
  public static void main(String[] args) {
    Settings settings = Settings.parse(args);
    if (settings == null) return;

    compile(settings);
  }

  protected static void compile(Settings settings) {
    YaplErrorListener errorListener = new YaplErrorListener();

    try {
      ProgramContext tree = parse(settings.sourceCode, errorListener);
      if (tree == null) return;

      SymbolTable symboltable = analyse(tree, JvmStandardLibrary.instance);
      if (symboltable == null) return;

      if (settings.defaultCompile) {
        CodeGenerator codegen = new CodeGenerator(symboltable, new JvmBackend(JvmStandardLibrary.instance, settings.outputDirectory));
        codegen.visit(tree);
      }
      else if (settings.profile) {
        Profiler debugger = new Profiler(symboltable, settings, new JvmProfilerBackend(JvmStandardLibrary.instance, settings.outputDirectory));
        debugger.visit(tree);
      }

      System.out.println("YAPL compilation: [" + errorListener.programName + "] OK");

      if (settings.symboldump) {
        new SymbolDump(symboltable).visit(tree);
      }
    }
    catch (Exception ex) {
      System.err.println( CompilerErrors.Internal(errorListener.programName, ex.getLocalizedMessage(), -1, -1) );
      ex.printStackTrace();
      System.err.println();
    }
  }

  protected static ProgramContext parse(CharStream input, YaplErrorListener errorListener) {
    YaplLexer lexer = new YaplLexer(input);
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    lexer.removeErrorListeners();
    lexer.addErrorListener(errorListener);

    YaplParser parser = new YaplParser(tokens);
    parser.removeErrorListeners();
    parser.addErrorListener(errorListener);
    parser.addParseListener(errorListener);
    return (errorListener.error) ? null : parser.program();
  }

  protected static SymbolTable analyse(ProgramContext tree, StandardLibrary stdlib) {
    Analysis analysis = new Analysis(stdlib);
    ParseTreeWalker.DEFAULT.walk(new parser.DetailedYaplListenerAdapter(analysis), tree);

    for (CompilerError error : analysis.errors) {
      System.err.println( error );
      System.err.println();
    }

    return (analysis.errors.size() > 0) ? null : analysis.symboltable;
  }

}
