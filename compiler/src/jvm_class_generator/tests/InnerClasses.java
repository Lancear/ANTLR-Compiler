package jvm_class_generator.tests;

import java.io.PrintStream;

import jvm_class_generator.specs.*;
import jvm_class_generator.specs.attributes.*;
import jvm_class_generator.specs.class_content.*;
import jvm_class_generator.specs.data_areas.*;
import jvm_class_generator.specs.helpers.*;

public class InnerClasses extends Test {

  public InnerClasses() {
    super("InnerClasses", "1\n2\n3\n");
  }

  protected String nodeName = name + "$" + "Node";
  protected Field nodeVal;
  protected Field nodeNext;
  protected Method nodeInit;


  @Override
  public void generate() {
    JvmClass Node = new jvm_class_generator.impl.JvmClass(nodeName, Descriptor.NAME_OF(Object.class), AccessFlags.PROTECTED | AccessFlags.SUPER);
    nodeVal = Node.addField("value", Descriptor.INT, AccessFlags.PUBLIC);
    nodeNext = Node.addField("next", Descriptor.REFERENCE(nodeName), AccessFlags.PUBLIC);
    nodeInit = Node.addMethod("<init>", Descriptor.METHOD(Descriptor.VOID, Descriptor.INT), AccessFlags.PUBLIC);
    generateInit((Code)nodeInit.addAttribute("Code"));
    generate(Node, nodeName);

    JvmClass Main = new jvm_class_generator.impl.JvmClass(name, Descriptor.NAME_OF(Object.class), AccessFlags.PUBLIC | AccessFlags.SUPER);
    ( (jvm_class_generator.specs.attributes.InnerClasses)Main.addAttribute("InnerClasses") ).add(Node, nodeName);
    Method main = Main.addMethod("main", Descriptor.MAIN, AccessFlags.PUBLIC | AccessFlags.STATIC);
    generateMain((Code)main.addAttribute("Code"));
    generate(Main);
  }

  private void generateInit(Code code) {
    ConstantPool consts = code.constantPool();
    String Object = Descriptor.NAME_OF(Object.class);

    int nodeValRef = consts.addFieldref(nodeName, nodeVal.name(), nodeVal.descriptor());
    int objectInit = consts.addMethodref(Object, "<init>", Descriptor.METHOD(Descriptor.VOID));

    code
      .aload_0()
      .dup()
      .invokeSpecial(objectInit)
      .iload_1()
      .putField(nodeValRef)
      .vreturn();
  }

  protected void generateMain(Code code) {
    code.addStackMapTableAttribute();
    ConstantPool consts = code.constantPool();
    String System = Descriptor.NAME_OF(System.class);
    String PrintStream = Descriptor.NAME_OF(PrintStream.class);

    int systemOut = consts.addFieldref(System, "out", Descriptor.REFERENCE(PrintStream));
    int outPrintln = consts.addMethodref(PrintStream, "println", Descriptor.METHOD(Descriptor.VOID, Descriptor.INT));
    int node = consts.addClass(nodeName);
    int nodeInitRef = consts.addMethodref(nodeName, nodeInit.name(), nodeInit.descriptor());
    int nodeNextRef = consts.addFieldref(nodeName, nodeNext.name(), nodeNext.descriptor());
    int nodeValRef = consts.addFieldref(nodeName, nodeVal.name(), nodeVal.descriptor());

    code
      .anew(node)
      .dup()
      .dup()
      .iconst_1()
      .invokeSpecial(nodeInitRef)
      .anew(node)
      .dup()
      .dup()
      .iconst_2()
      .invokeSpecial(nodeInitRef)
      .anew(node)
      .dup()
      .iconst_3()
      .invokeSpecial(nodeInitRef)
      .putField(nodeNextRef)
      .putField(nodeNextRef)
      .iconst_0()
      .istore_1()
      .addLabel("loopStart")
      .dup()
      .getField(nodeValRef)
      .getStatic(systemOut)
      .swap()
      .invokeVirtual(outPrintln)
      .getField(nodeNextRef)
      .iinc(1, 1, false)
      .iload_1()
      .iconst_3()
      .if_icmplt("loopStart")
      .vreturn();
  }
  
}
