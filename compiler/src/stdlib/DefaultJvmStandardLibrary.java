package stdlib;

import information.YaplConstants;
import jvm_class_generator.specs.JvmClass;
import jvm_class_generator.specs.attributes.Code;
import jvm_class_generator.specs.class_content.Method;
import jvm_class_generator.specs.data_areas.ConstantPool;
import jvm_class_generator.specs.helpers.AccessFlags;
import jvm_class_generator.specs.helpers.Descriptor;
import information.Parameter;
import information.Procedure;
import information.Symbol;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

public class DefaultJvmStandardLibrary implements StandardLibrary {
  protected DefaultJvmStandardLibrary() {}
  public static StandardLibrary instance = new DefaultJvmStandardLibrary();

  public String getName() {
    return "StandardLibrary";
  }

  public Symbol[] getPredefinedSymbols() {
    Symbol[] symbols = new Symbol[4];
    symbols[0] = new Procedure("writeint", YaplConstants.VOID, List.of(new Parameter("i", YaplConstants.INT)), true, false);
    symbols[1] = new Procedure("writebool", YaplConstants.VOID, List.of(new Parameter("b", YaplConstants.BOOL)), true, false);
    symbols[2] = new Procedure("writeln", YaplConstants.VOID, List.of(), true, false);
    symbols[3] = new Procedure("readint", YaplConstants.INT, List.of(), true, false);
    return symbols;
  }

  public byte[] generate() {
    JvmClass stdlib = new jvm_class_generator.impl.JvmClass(getName(), Descriptor.NAME_OF(Object.class), AccessFlags.PUBLIC | AccessFlags.SUPER);
    ConstantPool consts = stdlib.constantPool();

    String System = Descriptor.NAME_OF(System.class);
    String PrintStream = Descriptor.NAME_OF(PrintStream.class);
    String InputStream = Descriptor.NAME_OF(InputStream.class);
    String Scanner = Descriptor.NAME_OF(Scanner.class);

    int systemOut = consts.addFieldref(System, "out", Descriptor.REFERENCE(PrintStream));
    int systemIn = consts.addFieldref(System, "in", Descriptor.REFERENCE(InputStream));

    int printInt = consts.addMethodref(PrintStream, "print", Descriptor.METHOD(Descriptor.VOID, Descriptor.INT));
    int printBool = consts.addMethodref(PrintStream, "print", Descriptor.METHOD(Descriptor.VOID, Descriptor.BOOLEAN));
    int println = consts.addMethodref(PrintStream, "println", Descriptor.METHOD(Descriptor.VOID));
    int scannerConstructor = consts.addMethodref(Scanner, "<init>", Descriptor.METHOD(Descriptor.VOID, Descriptor.REFERENCE(InputStream)));
    int nextInt = consts.addMethodref(Scanner, "nextInt", Descriptor.METHOD(Descriptor.INT));

    Method writeintMethod = stdlib.addMethod("writeint", Descriptor.METHOD(Descriptor.VOID, Descriptor.INT), AccessFlags.PUBLIC | AccessFlags.STATIC);
    Code writeInt = (Code)writeintMethod.addAttribute("Code");
    writeInt
      .getStatic(systemOut)
      .iload_0()
      .invokeVirtual(printInt)
      .vreturn();

    Method writeboolMethod = stdlib.addMethod("writebool", Descriptor.METHOD(Descriptor.VOID, Descriptor.BOOLEAN), AccessFlags.PUBLIC | AccessFlags.STATIC);
    Code writebool = (Code)writeboolMethod.addAttribute("Code");
    writebool
      .getStatic(systemOut)
      .iload_0()
      .invokeVirtual(printBool)
      .vreturn();

    Method writelnMethod = stdlib.addMethod("writeln", Descriptor.METHOD(Descriptor.VOID), AccessFlags.PUBLIC | AccessFlags.STATIC);
    Code writeln = (Code)writelnMethod.addAttribute("Code");
    writeln
      .getStatic(systemOut)
      .invokeVirtual(println)
      .vreturn();

    Method readintMethod = stdlib.addMethod("readint", Descriptor.METHOD(Descriptor.INT), AccessFlags.PUBLIC | AccessFlags.STATIC);
    Code readint = (Code)readintMethod.addAttribute("Code");
    readint
      .anew( consts.addClass(Scanner) )
      .dup()
      .getStatic(systemIn)
      .invokeSpecial(scannerConstructor)
      .invokeVirtual(nextInt)
      .ireturn();

    return stdlib.generate();
  }
}
