package impl;

import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import specs.helpers.AccessFlags;
import specs.data_areas.ConstantPool;
import specs.helpers.Descriptor;
import specs.JvmClass;
import specs.class_content.Method;
import specs.attributes.Code;
import util.HexFormatter;

public class Backend {
  
  public static void main(String[] args) {
    String name = "TestA";
    String object = Object.class.getCanonicalName().replaceAll("[.]", "/");
    
    JvmClass test = new impl.JvmClass(name, object, AccessFlags.PUBLIC | AccessFlags.SUPER);
    test.addField("testField", Descriptor.BOOLEAN, AccessFlags.PUBLIC | AccessFlags.STATIC);

    Method main = test.addMethod("main", "(" + Descriptor.ARRAY(Descriptor.STRING) + ")V", AccessFlags.PUBLIC | AccessFlags.STATIC);
    Code code = (Code)main.addAttribute("Code");   
    code.addStackMapTableAttribute(); 

    ConstantPool constants = test.constantPool();
    int out = constants.addFieldref(Descriptor.NAME_OF(System.class), "out", Descriptor.REFERENCE( Descriptor.NAME_OF(PrintStream.class) ));
    int println = constants.addMethodref(Descriptor.NAME_OF(PrintStream.class), "println", Descriptor.METHOD(Descriptor.VOID, Descriptor.STRING));
    int msg = constants.addString("Yo waddup refactored JVM world!");
    
    code
      .iconst_1()
      .ifeq("end")
      .getStatic(out)
      .ldc(msg)
      .invokeVirtual(println)
      .addLabel("end")
      .vreturn();
    
    byte[] bytecode = test.generate();

    try {
      Files.write(Paths.get("./output/" + name + ".class"), bytecode);
    } 
    catch (Exception ex) {
      ex.printStackTrace();
    }

    System.out.println();
    System.out.println("--- " + name + " ---------------");
    System.out.println( HexFormatter.toHexString(bytecode) );
    System.out.println("(" + bytecode.length + " Bytes)");
  }

}
