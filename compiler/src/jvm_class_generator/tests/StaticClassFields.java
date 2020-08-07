package jvm_class_generator.tests;

import java.io.PrintStream;

import jvm_class_generator.specs.*;
import jvm_class_generator.specs.attributes.*;
import jvm_class_generator.specs.class_content.*;
import jvm_class_generator.specs.data_areas.*;
import jvm_class_generator.specs.helpers.*;

public class StaticClassFields extends Test {

  public StaticClassFields() {
    super("StaticClassFields", "5");
  }

  protected Field field;

  @Override
  public void generate() {
    JvmClass test = new jvm_class_generator.impl.JvmClass(name, Descriptor.NAME_OF(Object.class), AccessFlags.PUBLIC | AccessFlags.SUPER);
    
    field = test.addField("number", Descriptor.INT, AccessFlags.PUBLIC | AccessFlags.STATIC);
    
    Method main = test.addMethod("main", Descriptor.MAIN, AccessFlags.PUBLIC | AccessFlags.STATIC);
    generateMain((Code)main.addAttribute("Code"));

    generate(test);
  }

  protected void generateMain(Code code) {
    ConstantPool consts = code.constantPool();
    String System = Descriptor.NAME_OF(System.class);
    String PrintStream = Descriptor.NAME_OF(PrintStream.class);

    int testField = consts.addFieldref(name, field.name(), field.descriptor());
    int systemOut = consts.addFieldref(System, "out", Descriptor.REFERENCE(PrintStream));
    int outPrint = consts.addMethodref(PrintStream, "print", Descriptor.METHOD(Descriptor.VOID, Descriptor.INT));

    code
      .iconst_5()
      .putStatic(testField)
      .getStatic(systemOut)
      .getStatic(testField)
      .invokeVirtual(outPrint)
      .vreturn();
  }

}
