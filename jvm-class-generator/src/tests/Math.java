package tests;

import java.io.PrintStream;

import specs.*;
import specs.attributes.*;
import specs.class_content.*;
import specs.data_areas.*;
import specs.helpers.*;

public class Math extends Test {

  public Math() {
    super("Math", "15");
  }

  @Override
  public void generate() {
    JvmClass clazz = new impl.JvmClass(name, Descriptor.NAME_OF(Object.class), AccessFlags.PUBLIC | AccessFlags.SUPER);
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

    code
      .iconst_3()
      .iconst_5()
      .imul()
      .iconst_2()
      .iadd()
      .dup()
      .iconst_4()
      .irem()
      .isub()
      .iconst_4()
      .idiv()
      .iconst_2()
      .ishl()
      .iconst_m1()
      .iconst_2()
      .ishl()
      .imul()
      .iconst_2()
      .ishr()
      .ineg()
      .istore_1()
      .iinc(1, -1, true)
      .iload_1()
      .getStatic(systemOut)
      .swap()
      .invokeVirtual(outPrint)
      .vreturn();
  }
  
}
