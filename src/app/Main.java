package app;

import java.nio.file.Files;
import java.nio.file.Paths;

import targets.JVM;
import util.HexFormatter;

public class Main {

  public static void main(String[] args) {
    JVM target = new JVM();

    String programName = "TestMethodStructure";

    target.enterProgram(programName);
    target.enterFunction("main", "([L"+ target.binaryClassnameOf(String.class) + ";)V");
    target.returnVoid();
    target.exitFunction();

    byte[] bytecode = target.generate();


    try {
      Files.write(Paths.get("./output/" + programName + ".class"), bytecode);
    } 
    catch (Exception ex) {
      ex.printStackTrace();
    }

    System.out.println();
    System.out.println("--- " + programName + " ---------------");
    System.out.println( HexFormatter.toHexString(bytecode) );
    System.out.println("(" + bytecode.length + " Bytes)");
  }

}
