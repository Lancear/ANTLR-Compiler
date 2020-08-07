package jvm_class_generator.tests;

import java.io.PrintStream;

import jvm_class_generator.specs.*;
import jvm_class_generator.specs.attributes.*;
import jvm_class_generator.specs.class_content.*;
import jvm_class_generator.specs.data_areas.*;
import jvm_class_generator.specs.helpers.*;

public class Conditions extends Test {
  
  public Conditions() {
    super("Conditions", "goal!");
  }

  @Override
  public void generate() {
    JvmClass test = new jvm_class_generator.impl.JvmClass(name, Descriptor.NAME_OF(Object.class), AccessFlags.PUBLIC | AccessFlags.SUPER);
    Method main = test.addMethod("main", Descriptor.MAIN, AccessFlags.PUBLIC | AccessFlags.STATIC);
    generateMain((Code)main.addAttribute("Code"));
    generate(test);
  }

  protected void generateMain(Code code) {
    code.addStackMapTableAttribute();
    ConstantPool consts = code.constantPool();
    String System = Descriptor.NAME_OF(System.class);
    String PrintStream = Descriptor.NAME_OF(PrintStream.class);

    int systemOut = consts.addFieldref(System, "out", Descriptor.REFERENCE(PrintStream));
    int outPrint = consts.addMethodref(PrintStream, "print", Descriptor.METHOD(Descriptor.VOID, Descriptor.STRING));

    code
      .iconst_0()
      .ifeq("jmp1")
      .gotoLabel("fail")
      .addLabel("jmp1")

      .iconst_1()
      .ifne("jmp2")
      .gotoLabel("fail")
      .addLabel("jmp2")

      .iconst_m1()
      .iflt("jmp3")
      .gotoLabel("fail")
      .addLabel("jmp3")

      .iconst_0()
      .ifge("jmp4")
      .gotoLabel("fail")
      .addLabel("jmp4")

      .iconst_1()
      .ifge("jmp5")
      .gotoLabel("fail")
      .addLabel("jmp5")

      .iconst_1()
      .ifgt("jmp6")
      .gotoLabel("fail")
      .addLabel("jmp6")

      .iconst_0()
      .ifle("jmp7")
      .gotoLabel("fail")
      .addLabel("jmp7")

      
      .iconst_m1()
      .ifle("jmp8")
      .gotoLabel("fail")
      .addLabel("jmp8")

      .iconst_2()
      .iconst_2()
      .if_icmpeq("jmp11")
      .gotoLabel("fail")
      .addLabel("jmp11")

      .iconst_2()
      .iconst_3()
      .if_icmpne("jmp12")
      .gotoLabel("fail")
      .addLabel("jmp12")

      .iconst_2()
      .iconst_4()
      .if_icmplt("jmp13")
      .gotoLabel("fail")
      .addLabel("jmp13")

      .iconst_2()
      .iconst_2()
      .if_icmpge("jmp14")
      .gotoLabel("fail")
      .addLabel("jmp14")

      .iconst_5()
      .iconst_2()
      .if_icmpge("jmp15")
      .gotoLabel("fail")
      .addLabel("jmp15")

      .iconst_4()
      .iconst_2()
      .if_icmpgt("jmp16")
      .gotoLabel("fail")
      .addLabel("jmp16")

      .iconst_2()
      .iconst_2()
      .if_icmple("jmp17")
      .gotoLabel("fail")
      .addLabel("jmp17")

      .iconst_1()
      .iconst_2()
      .if_icmple("jmp18")
      .gotoLabel("fail")
      .addLabel("jmp18")


      .ldc( consts.addString("value") )
      .dup()
      .if_acmpeq("jmp21")
      .gotoLabel("fail")
      .addLabel("jmp21")

      .ldc( consts.addString("value") )
      .ldc( consts.addString("value") )
      .if_acmpeq("fail")

      .ldc( consts.addString("value") )
      .ldc( consts.addString("value") )
      .if_acmpne("jmp21")
      .gotoLabel("fail")
      .addLabel("jmp21")

      .ldc( consts.addString("value") )
      .dup()
      .if_acmpne("fail")


      .addLabel("goal")
      .getStatic(systemOut)
      .ldc( consts.addString("goal!") )
      .invokeVirtual(outPrint)
      .addLabel("fail")
      .vreturn();
  }

}
