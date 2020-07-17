package app;

import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import targets.JVM;
import util.HexFormatter;

public class Main {

  public static void main(String[] args) {
    String mainName = "Main";

    JVM main = new JVM();
    main.enterClass(mainName);
    main.enterMethod("main", "(["+ JVM.fieldDescriptorOf(String.class) + ")V");

    final int out = main.addFieldRefToConstantPool(JVM.binaryClassNameOf(System.class), "out", JVM.fieldDescriptorOf(PrintStream.class));
    final int println = main.addMethodRefToConstantPool(JVM.binaryClassNameOf(PrintStream.class), "println", "(" + JVM.fieldDescriptorOf(String.class) + ")V");
    final int msg = main.addStringToConstantPool("Yo waddup JVM world!");

    main.iconst_1();
    main.ifeq("end");
    main.getStatic(out, JVM.fieldDescriptorOf(PrintStream.class));
    main.ldc(msg, JVM.fieldDescriptorOf(String.class));
    main.invokeVirtual(println, "(" + JVM.fieldDescriptorOf(String.class) + ")V");
    main.addLabel("end");
    main.returnVoid();
    main.exitMethod();
    byte[] mainBytecode = main.generate();

    try {
      Files.write(Paths.get("./output/" + mainName + ".class"), mainBytecode);
    } 
    catch (Exception ex) {
      ex.printStackTrace();
    }

    System.out.println();
    System.out.println("--- " + mainName + " ---------------");
    System.out.println( HexFormatter.toHexString(mainBytecode) );
    System.out.println("(" + mainBytecode.length + " Bytes)");
  }

}
