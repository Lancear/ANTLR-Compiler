package stdlib;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

import jvm_class_generator.specs.JvmClass;
import jvm_class_generator.specs.attributes.Code;
import jvm_class_generator.specs.class_content.Method;
import jvm_class_generator.specs.data_areas.ConstantPool;
import jvm_class_generator.specs.helpers.AccessFlags;
import jvm_class_generator.specs.helpers.Descriptor;
import analysis.SymbolTable;
import analysis.Symbol;

/**
 * A JVM implementation for the {@code StandardLibrary}. It can easily be extended to give yapl programmers more IO functions.
 */
public class JvmStandardLibrary extends StandardLibrary {
  public final static StandardLibrary instance = new JvmStandardLibrary();
  protected JvmStandardLibrary() { }

  @Override
  public void addToSymbolTable(SymbolTable symboltable) {
    symboltable.add( new Symbol.Function("writeint", Symbol.VOID, List.of(new Symbol.Param("i", Symbol.INT)), true) );
    symboltable.add( new Symbol.Function("writebool", Symbol.VOID, List.of(new Symbol.Param("b", Symbol.BOOL)), true) );
    symboltable.add( new Symbol.Function("writeln", Symbol.VOID, List.of(), true) );
    symboltable.add( new Symbol.Function("readint", Symbol.INT, List.of(), true) );
  }

  @Override
  public byte[] generate() {
    JvmClass stdlib = new jvm_class_generator.impl.JvmClass(filename, Descriptor.NAME_OF(Object.class), AccessFlags.PUBLIC | AccessFlags.SUPER);
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
