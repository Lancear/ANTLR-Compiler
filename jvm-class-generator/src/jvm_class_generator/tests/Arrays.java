package jvm_class_generator.tests;

import java.io.PrintStream;

import jvm_class_generator.specs.*;
import jvm_class_generator.specs.attributes.*;
import jvm_class_generator.specs.class_content.*;
import jvm_class_generator.specs.data_areas.*;
import jvm_class_generator.specs.helpers.*;

public class Arrays extends Test {

  public Arrays() {
    super("Arrays", "23101101works-lol!");
  }

  protected Method test;

  @Override
  public void generate() {
    JvmClass clazz = new jvm_class_generator.impl.JvmClass(name, Descriptor.NAME_OF(Object.class), AccessFlags.PUBLIC | AccessFlags.SUPER);

    test = clazz.addMethod("test", Descriptor.METHOD(Descriptor.STRING), AccessFlags.PROTECTED | AccessFlags.STATIC);
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
    int outPrint = consts.addMethodref(PrintStream, "print", Descriptor.METHOD(Descriptor.VOID, Descriptor.STRING));
    int testRef = consts.addMethodref(name, test.name(), test.descriptor());

    code
      .getStatic(systemOut)
      .invokeStatic(testRef)
      .invokeVirtual(outPrint)
      .vreturn();
  }
  

  protected void generateTest(Code code) {
    code.addStackMapTableAttribute();
    ConstantPool consts = code.constantPool();
    String System = Descriptor.NAME_OF(System.class);
    String PrintStream = Descriptor.NAME_OF(PrintStream.class);

    int systemOut = consts.addFieldref(System, "out", Descriptor.REFERENCE(PrintStream));
    int outPrint = consts.addMethodref(PrintStream, "print", Descriptor.METHOD(Descriptor.VOID, Descriptor.INT));
    int outPrintString = consts.addMethodref(PrintStream, "print", Descriptor.METHOD(Descriptor.VOID, Descriptor.STRING));

    code
      .iconst_2()
      .newArray(ArrayType.INT)
      .dup()
      .arraylength()
      .getStatic(systemOut)
      .swap()
      .invokeVirtual(outPrint)
      .dup()
      .iconst_0()
      .dup()
      .iastore()
      .dup()
      .iconst_1()
      .dup()
      .iastore()
      .astore_0()

      .iconst_3()
      .newArray(ArrayType.BOOLEAN)
      .dup()
      .arraylength()
      .getStatic(systemOut)
      .swap()
      .invokeVirtual(outPrint)
      .dup()
      .iconst_0()
      .iconst_1()
      .bastore()
      .dup()
      .iconst_1()
      .iconst_0()
      .bastore()
      .dup()
      .iconst_2()
      .iconst_1()
      .bastore()
      .astore_1()

      .iconst_1()
      .anewArray( consts.addClass( Descriptor.NAME_OF(String.class) ) )
      .dup()
      .arraylength()
      .getStatic(systemOut)
      .swap()
      .invokeVirtual(outPrint)
      .dup()
      .iconst_0()
      .ldc( consts.addString("works-") ) 
      .aastore()
      .astore_2()

      .aload_0()
      .dup()
      .iconst_0()
      .iaload()
      .getStatic(systemOut)
      .swap()
      .invokeVirtual(outPrint)
      .iconst_1()
      .iaload()
      .getStatic(systemOut)
      .swap()
      .invokeVirtual(outPrint)

      .aload_1()
      .dup()
      .iconst_0()
      .baload()
      .getStatic(systemOut)
      .swap()
      .invokeVirtual(outPrint)
      .dup()
      .iconst_1()
      .baload()
      .getStatic(systemOut)
      .swap()
      .invokeVirtual(outPrint)
      .iconst_2()
      .baload()
      .getStatic(systemOut)
      .swap()
      .invokeVirtual(outPrint)

      .aload_2()
      .iconst_0()
      .aaload()
      .getStatic(systemOut)
      .swap()
      .invokeVirtual(outPrintString)

      .gotoLabel("end")
      .getStatic(systemOut)
      .ldc( consts.addString("If you see me, you lost! WHAHAHAHA!") )
      .invokeVirtual(outPrintString)
      .addLabel("end")
      .ldc( consts.addString("lol!") )
      .areturn();
  }
  
}
