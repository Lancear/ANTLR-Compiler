package app;

import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import targets.JVM;
import util.HexFormatter;

public class Main {

  public static void main(String[] args) {
    String mainClassName = "Main";

    JVM mainClass = new JVM();
    mainClass.enterClass(mainClassName);
    mainClass.enterMethod("main", "(["+ JVM.fieldDescriptorOf(String.class) + ")V");

    final int out = mainClass.addFieldRefToConstantPool(JVM.binaryClassNameOf(System.class), "out", JVM.fieldDescriptorOf(PrintStream.class));
    final int println = mainClass.addMethodRefToConstantPool(JVM.binaryClassNameOf(PrintStream.class), "println", "(" + JVM.fieldDescriptorOf(String.class) + ")V");
    final int msg = mainClass.addStringToConstantPool("Yo waddup JVM world!");

    //mainClass.iconst_1();
   // mainClass.ifeq("end");
    mainClass.getStatic(out);
    mainClass.ldc(msg);
    mainClass.invokeVirtual(println);
    //mainClass.addLabel("end");
    mainClass.returnVoid();
    mainClass.exitMethod();
    byte[] mainClassBytecode = mainClass.generate();

    try {
      Files.write(Paths.get("./output/" + mainClassName + ".class"), mainClassBytecode);
    } 
    catch (Exception ex) {
      ex.printStackTrace();
    }

    System.out.println();
    System.out.println("--- " + mainClassName + " ---------------");
    System.out.println( HexFormatter.toHexString(mainClassBytecode) );
    System.out.println("(" + mainClassBytecode.length + " Bytes)");
  }

}
