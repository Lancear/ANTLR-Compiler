package tests;

import java.io.PrintStream;

import specs.*;
import specs.attributes.*;
import specs.class_content.*;
import specs.data_areas.*;
import specs.helpers.*;

public class Consts extends Test {

  public Consts() {
    super("Consts", "-1012345\n532020\n101101101101");
  }

  @Override
  public void generate() {
    JvmClass test = new impl.JvmClass(name, Descriptor.NAME_OF(Object.class), AccessFlags.PUBLIC | AccessFlags.SUPER);
    Method main = test.addMethod("main", Descriptor.MAIN, AccessFlags.PUBLIC | AccessFlags.STATIC);
    generateMain((Code)main.addAttribute("Code"));
    generate(test);
  }

  protected void generateMain(Code code) {
    ConstantPool consts = code.constantPool();
    String System = Descriptor.NAME_OF(System.class);
    String PrintStream = Descriptor.NAME_OF(PrintStream.class);

    int systemOut = consts.addFieldref(System, "out", Descriptor.REFERENCE(PrintStream));
    int outPrint = consts.addMethodref(PrintStream, "print", Descriptor.METHOD(Descriptor.VOID, Descriptor.INT));
    int outPrintln = consts.addMethodref(PrintStream, "println", Descriptor.METHOD(Descriptor.VOID));

    code
      .getStatic(systemOut)
      .dup()
      .iconst_m1()
      .invokeVirtual(outPrint)
      .dup()
      .iconst_0()
      .invokeVirtual(outPrint)
      .dup()
      .iconst_1()
      .invokeVirtual(outPrint)
      .dup()
      .iconst_2()
      .invokeVirtual(outPrint)
      .dup()
      .iconst_3()
      .invokeVirtual(outPrint)
      .dup()
      .iconst_4()
      .invokeVirtual(outPrint)
      .dup()
      .iconst_5()
      .invokeVirtual(outPrint)
      .dup()
      .invokeVirtual(outPrintln)
      .dup()
      .bipush(53)
      .invokeVirtual(outPrint)
      .dup()
      .sipush(2020)
      .invokeVirtual(outPrint)
      .dup()
      .invokeVirtual(outPrintln)
      .dup()
      .ldc( consts.addInteger(101101) )
      .invokeVirtual(outPrint)
      .ldc_w( consts.addInteger(101101) )
      .invokeVirtual(outPrint)
      .vreturn();
  }

}
