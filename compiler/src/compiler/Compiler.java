package compiler;

import analyser.Analyser;
import analyser.SymbolDumper;
import generator.*;
import information.CompilerError;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import parser.ErrorListener;
import parser.YaplLexer;
import parser.YaplParser;
import parser.YaplParser.ProgramContext;
import stdlib.DefaultJvmStandardLibrary;
import stdlib.StandardLibrary;
import analyser.SymbolTable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Compiler {

  public static CompilerSettings settings;
  public static String programName = null;
  public static String[] ruleNames = new String[0];

  public static List<CompilerError> errors = new ArrayList<>();

  public static void main(String[] args) {
    Compiler.settings = CompilerSettings.parse(args);

    if (settings.showUsageInfo) {
      System.out.println(settings.getUsageInfo());
      if (settings.error != null) System.exit(1);
      else return;
    }

    System.out.println("Compiling...");

    StandardLibrary stdlib = DefaultJvmStandardLibrary.instance;
    ProgramContext parseTree = parse(settings.sourceCode);
    if (errors.size() > 0) abort(errors);

    SymbolTable symbolTable = analyse(parseTree, stdlib);
    if (errors.size() > 0) abort(errors);

    if (settings.doSymbolDump) dumpSymbols(parseTree, symbolTable);
    else if (settings.profile) profile(parseTree, symbolTable, stdlib, settings.outputDir);
    else if (settings.compile) compile(parseTree, symbolTable, stdlib, settings.outputDir);

    System.out.println("YAPL compilation: OK");
  }

  public static ProgramContext parse(String sourceCode) {
    YaplLexer lexer = new YaplLexer( CharStreams.fromString(sourceCode) );
    lexer.removeErrorListeners();
    lexer.addErrorListener(new ErrorListener());
    CommonTokenStream tokens = new CommonTokenStream(lexer);

    YaplParser parser = new YaplParser(tokens);
    parser.removeErrorListeners();
    parser.addErrorListener(new ErrorListener());
    ProgramContext parseTree = parser.program();

    ruleNames = parser.getRuleNames();
    programName = parseTree.Id(0).getText();
    return parseTree;
  }

  public static SymbolTable analyse(ProgramContext parseTree, StandardLibrary stdlib) {
    Analyser analyser = new Analyser(stdlib);
    ParseTreeWalker.DEFAULT.walk(analyser, parseTree);
    return analyser.symboltable;
  }

  public static void dumpSymbols(ProgramContext parseTree, SymbolTable symbolTable) {
    SymbolDumper symbolDumper = new SymbolDumper(symbolTable);
    symbolDumper.visit(parseTree);
  }

  public static void profile(ProgramContext parseTree, SymbolTable symbolTable, StandardLibrary stdlib, Path outputDir) {
    Profiler profiler = new JvmProfiler(stdlib, outputDir);
    ProfilerDriver profilerDriver = new ProfilerDriver(symbolTable, profiler);
    profilerDriver.visit(parseTree);
  }

  public static void compile(ProgramContext parseTree, SymbolTable symbolTable, StandardLibrary stdlib, Path outputDir) {
    CodeGenerator codeGenerator = new JvmCodeGenerator(stdlib, outputDir);
    CodeGeneratorDriver codeGeneratorDriver = new CodeGeneratorDriver(symbolTable, codeGenerator);
    codeGeneratorDriver.visit(parseTree);
  }

  protected static void abort(List<CompilerError> errors) {
    System.err.println();

    for (CompilerError error : errors) {
      System.err.println(error);
    }

    System.exit(1);
  }

}
