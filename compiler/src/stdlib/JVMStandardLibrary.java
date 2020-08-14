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

public class JVMStandardLibrary extends StandardLibrary {
  public final static StandardLibrary instance = new JVMStandardLibrary();
  protected JVMStandardLibrary() { }

  @Override
  public void addToSymbolTable(SymbolTable symboltable) {
    symboltable.add( new Symbol.Function("writeint", "void", List.of(new Symbol.Param("i", "int")), true) );
    symboltable.add( new Symbol.Function("writebool", "void", List.of(new Symbol.Param("b", "bool")), true) );
    symboltable.add( new Symbol.Function("writeln", "void", List.of(), true) );
    symboltable.add( new Symbol.Function("readint", "int", List.of(), true) );
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

    int printString = consts.addMethodref(PrintStream, "print", Descriptor.METHOD(Descriptor.VOID, Descriptor.STRING));
    int printInt = consts.addMethodref(PrintStream, "print", Descriptor.METHOD(Descriptor.VOID, Descriptor.INT));
    int printBool = consts.addMethodref(PrintStream, "print", Descriptor.METHOD(Descriptor.VOID, Descriptor.BOOLEAN));
    int println = consts.addMethodref(PrintStream, "println", Descriptor.METHOD(Descriptor.VOID));
    int scannerConstructor = consts.addMethodref(Scanner, "<init>", Descriptor.METHOD(Descriptor.VOID, Descriptor.REFERENCE(InputStream)));
    int nextInt = consts.addMethodref(Scanner, "nextInt", Descriptor.METHOD(Descriptor.INT));

    Method writeMethod = stdlib.addMethod("write", Descriptor.METHOD(Descriptor.VOID, Descriptor.STRING), AccessFlags.PUBLIC | AccessFlags.STATIC);
    Code write = (Code)writeMethod.addAttribute("Code");
    write
      .getStatic(systemOut)
      .aload_0()
      .invokeVirtual(printString)
      .vreturn();

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
