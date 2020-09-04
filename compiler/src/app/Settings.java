package app;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;

public class Settings {
  
  public CharStream sourceCode = null;
  public Path outputDirectory = null;
  public boolean defaultCompile = false;
  public boolean symboldump = false;
  public boolean profile = false;
  public boolean watchAll = false;
  public int[] vardumpLineNrs = new int[0];
  public int[] watchLineNrs = new int[0];
  public String[] trackedFunctions = new String[0];

  public static Settings parse(String[] args) {
    Settings settings = new Settings();

    if (args.length == 2 && args[0].equals("--symboldump")) {
      settings.sourceCode = getSourceCode(args[1]);
      if (settings.sourceCode == null) return null;

      settings.symboldump = true;
      return settings;
    }
    else if (args.length == 2) {
      settings.sourceCode = getSourceCode(args[0]);
      if (settings.sourceCode == null) return null;

      settings.outputDirectory = Paths.get(args[1]);
      settings.defaultCompile = true;
      return settings;
    }
    else if (args.length > 2) {
      int argIdx = 0;
      settings.profile = true;
      
      while (argIdx < args.length - 2) {
        if (args[argIdx].equals("--vardump")) {
          argIdx++;
          settings.vardumpLineNrs = getLineNrs(args[argIdx++]);
          if (settings.vardumpLineNrs == null) return null;
        }
        else if (args[argIdx].equals("--calltrace")) {
          argIdx++;
          settings.trackedFunctions = args[argIdx++].split(":");
        }
        else if (args[argIdx].equals("--watch")) {
          argIdx++;

          if (args[argIdx].equals("all")) {
            settings.watchAll = true;
            argIdx++;
          }
          else {
            settings.watchLineNrs = getLineNrs(args[argIdx++]);
            if (settings.watchLineNrs == null) return null;
          }          
        }
        else {
          System.err.println("Invalid option `" + args[argIdx] + "`!");
          return null;
        }
      }

      settings.sourceCode = getSourceCode(args[argIdx++]);
      if (settings.sourceCode == null) return null;

      settings.outputDirectory = Paths.get(args[argIdx++]);
      return settings;
    }
    else {
      System.out.println("[USAGE]");
      System.out.println("Compiles the source-file to the output-directory:");
      System.out.println("yapl-compiler <source-file> <output-directory>");
      System.out.println();
      System.out.println("Dumps all information, the compiler extracts, on symbols and expressions from the source-file:");
      System.out.println("yapl-compiler --symboldump <source-file>");
      System.out.println();
      System.out.println("Compiles the source-file to the output-directory with the given options:");
      System.out.println("yapl-compiler <options> <output-directory>");
      System.out.println("Options:");
      System.out.println("--vardump <line1:line2:...>              adds variable dumps before the given lines");
      System.out.println("--watch <line1:line2:...>                logs the expressions at the given lines");
      System.out.println("--watch all                              logs the expressions at all lines");
      System.out.println("--calltrace <function1:function2:...>    logs each call to the given functions");
      return null;
    }
  }

  protected static CharStream getSourceCode(String filename) {
    try {
      return CharStreams.fromFileName(filename);
    }
    catch (Exception ex) {
      System.err.println("Cannot read the input file `" + filename + "`!");
      return null;
    }
  }

  protected static int[] getLineNrs(String lineNrsString) {
    String[] lineNrs = lineNrsString.split(":");
    int[] vardumpLineNrs = new int[lineNrs.length];

    try {
      for (int i = 0; i < lineNrs.length; i++) {
        vardumpLineNrs[i] = Integer.parseInt(lineNrs[i]);
      }
    }
    catch (Exception ex) {
      System.err.println("Invalid line numbers `" + lineNrsString + "`!");
      return null;
    }

    Arrays.sort(vardumpLineNrs);
    return vardumpLineNrs;
  }

}
