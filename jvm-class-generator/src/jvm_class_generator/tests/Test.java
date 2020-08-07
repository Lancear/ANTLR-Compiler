package jvm_class_generator.tests;

import java.nio.file.Files;
import java.nio.file.Paths;

import jvm_class_generator.specs.JvmClass;

public abstract class Test {

  public final String name;
  public final String expectedOutput;
  public final String classPath = "./output/tests/";


  public Test(String name, String expectedOutput) {
    this.name = name;
    this.expectedOutput = expectedOutput;
  }

  public abstract void generate();

  protected void generate(JvmClass clazz) {
    generate(clazz, this.name);
  }

  protected void generate(JvmClass clazz, String name) {
    try {
      if (!Files.exists( Paths.get(classPath) )) {
        Files.createDirectories( Paths.get(classPath) );
      }

      Files.write( Paths.get(classPath + "/" + name + ".class"), clazz.generate() );
    } 
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }

}
