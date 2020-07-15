package app;

import java.nio.file.Files;
import java.nio.file.Paths;

import backend.Backend;
import backend.JVMBackend;
import util.HexFormatter;

public class Main {

  public static void main(String[] args) {
    Backend backend = new JVMBackend();
    byte[] bytecode = backend.generate();

    try {
      Files.write(Paths.get("./output/Test1.class"), bytecode);
    } 
    catch (Exception ex) {
      ex.printStackTrace();
    }

    System.out.println();
    System.out.println("--- Test1 ---------------");
    System.out.println( HexFormatter.toHexString(bytecode) );
  }

}
