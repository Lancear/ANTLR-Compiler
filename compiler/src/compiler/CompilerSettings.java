package compiler;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class CompilerSettings {

  protected CompilerSettings() {}

  public boolean showUsageInfo = true;
  public boolean doSymbolDump = false;
  public boolean compile = false;
  public boolean profile = false;

  public String sourceCode = null;
  public Path outputDir = null;

  public int[] vardumpLineNrs = new int[0];
  public int[] watchLineNrs = new int[0];
  public boolean watchAll = false;
  public String[] trackedFunctions = new String[0];

  public String error = null;



  public String getUsageInfo() {
    final StringBuilder usageInfo = new StringBuilder();
    if (error != null) usageInfo.append("[ERROR] " + error + "\r\n\r\n");

    return usageInfo
      .append("[USAGE]\r\n")
      .append("Compiles the source-file to the output-directory:\r\n")
      .append("YaplCompiler <source-file> <output-directory>\r\n")
      .append("\r\n")
      .append("Dumps all information, the compiler extracts, on symbols and expressions from the source-file:\r\n")
      .append("YaplCompiler --symboldump <source-file>\r\n")
      .append("\r\n")
      .append("Compiles the source-file to the output-directory with the given options:\r\n")
      .append("YaplCompiler <options> <source-file> <output-directory>\r\n")
      .append("Options:\r\n")
      .append("--vardump <line1:line2:...>              adds variable dumps before the given lines\r\n")
      .append("--watch <line1:line2:...>                logs the expressions at the given lines\r\n")
      .append("--watch all                              logs all expressions\r\n")
      .append("--calltrace <function1:function2:...>    logs each call to the given functions\r\n")
      .toString();
  }



  public static CompilerSettings parse(String[] args) {
    if (args.length < 2 || args.length % 2 == 1 || args.length > 8) {
      final CompilerSettings settings = new CompilerSettings();
      settings.error = "Invalid number of arguments!";
      return settings;
    }

    if (args[0].equals("--symboldump")) return parseSymboldump(args);

    try {
      final CompilerSettings settings = parseProfilingOptions(Arrays.copyOf(args, args.length - 2));
      if (settings.error != null) return settings;

      Files.readString(Paths.get(args[args.length - 2]));

      settings.sourceCode = Files.readString( Paths.get(args[args.length - 2]) );
      settings.outputDir = Paths.get(args[args.length - 1]);
      settings.compile = true;
      settings.showUsageInfo = false;
      return settings;
    }
    catch (Exception ex) {
      final CompilerSettings settings = new CompilerSettings();
      settings.error = "Cannot read the source-file '" + args[args.length - 2] + "'!";
      return settings;
    }
  }



  protected static CompilerSettings parseSymboldump(String[] args) {
    final CompilerSettings settings = new CompilerSettings();

    if (args.length != 2) {
      settings.error = "Invalid number of arguments! When using --symboldump no other options can be used!";
      return settings;
    }

    try {
      settings.sourceCode = Files.readString( Paths.get(args[1]) );
      settings.doSymbolDump = true;
      settings.showUsageInfo = false;
      return settings;
    }
    catch (Exception ex) {
      settings.error = "Cannot read the source-file '" + args[1] + "'!";
      return settings;
    }
  }

  protected static CompilerSettings parseProfilingOptions(String[] args) {
    final CompilerSettings settings = new CompilerSettings();

    int argIdx = 0;
    while (argIdx < args.length) {
      if (args[argIdx].equals("--vardump")) {
        try {
          argIdx++;
          settings.vardumpLineNrs = getLineNrs(args[argIdx++]);
          settings.profile = true;
        }
        catch (Exception ex) {
          settings.error = ex.getMessage();
          return settings;
        }
      }
      else if (args[argIdx].equals("--watch")) {
        argIdx++;

        if (args[argIdx].equals("all")) {
          argIdx++;
          settings.watchAll = true;
          settings.profile = true;
        }
        else {
          try {
            settings.watchLineNrs = getLineNrs(args[argIdx++]);
            settings.profile = true;
          }
          catch (Exception ex) {
            settings.error = ex.getMessage();
            return settings;
          }
        }
      }
      else if (args[argIdx].equals("--calltrace")) {
        argIdx++;
        settings.trackedFunctions = args[argIdx++].split(":");
        settings.profile = true;
      }
      else {
        settings.error = "Unknown compiler option '" + args[argIdx] + "'!";
        return settings;
      }
    }

    settings.showUsageInfo = false;
    return settings;
  }

  protected static int[] getLineNrs(String arg) throws Exception {
    String[] lineNrs = arg.split(":");
    int[] vardumpLineNrs = new int[lineNrs.length];

    try {
      for (int i = 0; i < lineNrs.length; i++) {
        vardumpLineNrs[i] = Integer.parseInt(lineNrs[i]);
      }
    }
    catch (Exception ex) {
      throw new Exception("Invalid line numbers '" + arg + "'!");
    }

    Arrays.sort(vardumpLineNrs);
    return vardumpLineNrs;
  }

}
