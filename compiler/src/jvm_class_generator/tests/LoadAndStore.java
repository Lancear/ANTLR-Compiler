package jvm_class_generator.tests;

import java.io.PrintStream;

import jvm_class_generator.specs.*;
import jvm_class_generator.specs.attributes.*;
import jvm_class_generator.specs.class_content.*;
import jvm_class_generator.specs.data_areas.*;
import jvm_class_generator.specs.helpers.*;

public class LoadAndStore extends Test {

  public LoadAndStore() {
    super("LoadAndStore", "012345\n543210-1");
  }

  protected Method test;

  @Override
  public void generate() {
    JvmClass clazz = new jvm_class_generator.impl.JvmClass(name, Descriptor.NAME_OF(Object.class), AccessFlags.PUBLIC | AccessFlags.SUPER);

    test = clazz.addMethod("test", Descriptor.METHOD(Descriptor.INT), AccessFlags.PRIVATE | AccessFlags.STATIC);
    generateTest((Code)test.addAttribute("Code"));

    Method main = clazz.addMethod("main", Descriptor.MAIN, AccessFlags.PUBLIC | AccessFlags.STATIC);
    generateMain((Code)main.addAttribute("Code"));

    generate(clazz);
  }
  
  protected void generateMain(Code code) {
    ConstantPool consts = code.constantPool();
    String System = Descriptor.NAME_OF(System.class);
    String PrintStream = Descriptor.NAME_OF(PrintStream.class);

    int systemOut = consts.addFieldref(System, "out", Descriptor.REFERENCE(PrintStream));
    int outPrint = consts.addMethodref(PrintStream, "print", Descriptor.METHOD(Descriptor.VOID, Descriptor.INT));
    int testRef = consts.addMethodref(name, test.name(), test.descriptor());

    code
      .getStatic(systemOut)
      .invokeStatic(testRef)
      .invokeVirtual(outPrint)
      .vreturn();
  }
  

  protected void generateTest(Code code) {
    ConstantPool consts = code.constantPool();
    String System = Descriptor.NAME_OF(System.class);
    String PrintStream = Descriptor.NAME_OF(PrintStream.class);

    int systemOut = consts.addFieldref(System, "out", Descriptor.REFERENCE(PrintStream));
    int outPrint = consts.addMethodref(PrintStream, "print", Descriptor.METHOD(Descriptor.VOID, Descriptor.INT));
    int outPrintString = consts.addMethodref(PrintStream, "print", Descriptor.METHOD(Descriptor.VOID, Descriptor.STRING));
    int outPrintln = consts.addMethodref(PrintStream, "println", Descriptor.METHOD(Descriptor.VOID));

    code
      .iconst_0()
      .istore_0()
      .iconst_1()
      .istore_1()
      .iconst_2()
      .istore_2()
      .iconst_3()
      .istore_3()
      .iconst_4()
      .istore(4, false)
      .iconst_5()
      .istore(5, true)

      .getStatic(systemOut)
      .dup()
      .iload_0()
      .invokeVirtual(outPrint)
      .dup()
      .iload_1()
      .invokeVirtual(outPrint)
      .dup()
      .iload_2()
      .invokeVirtual(outPrint)
      .dup()
      .iload_3()
      .invokeVirtual(outPrint)
      .dup()
      .iload(4, false)
      .invokeVirtual(outPrint)
      .dup()
      .iload(5, true)
      .invokeVirtual(outPrint)
      .invokeVirtual(outPrintln)

      .ldc( consts.addString("5") )
      .astore_0()
      .ldc( consts.addString("4") )
      .astore_1()
      .ldc( consts.addString("3") )
      .astore_2()
      .ldc( consts.addString("2") )
      .astore_3()
      .ldc( consts.addString("1") )
      .astore(4, true)
      .ldc( consts.addString("0") )
      .astore(5, false)

      .getStatic(systemOut)
      .dup()
      .aload_0()
      .invokeVirtual(outPrintString)
      .dup()
      .aload_1()
      .invokeVirtual(outPrintString)
      .dup()
      .aload_2()
      .invokeVirtual(outPrintString)
      .dup()
      .aload_3()
      .invokeVirtual(outPrintString)
      .dup()
      .aload(4, false)
      .invokeVirtual(outPrintString)
      .aload(5, true)
      .invokeVirtual(outPrintString)

      .bipush(-1)
      .ireturn();
  }

}
