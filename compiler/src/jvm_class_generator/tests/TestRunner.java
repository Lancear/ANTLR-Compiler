package jvm_class_generator.tests;

public class TestRunner {

  public static Test[] tests = new Test[] { 
    new StaticClassFields(),
    new InnerClasses(),
    new Consts(),
    new LoadAndStore(),
    new Math(),
    new Arrays(),
    new Conditions(),
  };

  public static void main(String[] args) {
    for (Test test : tests) {
      try {
        test(test);
      }
      catch(Exception ex) {
        System.err.println();
        System.err.println("[ERROR] " + test.name);
        ex.printStackTrace();
        System.err.println();
        System.err.println();
      }
    }
  }

  public static void test(Test test) throws Exception {
    // generate bytecode
    test.generate();

    // execute bytecode
    Process process = Runtime.getRuntime().exec("java -cp " + test.classPath + " " + test.name);
    String output = new String( process.getInputStream().readAllBytes() );
    String error = new String( process.getErrorStream().readAllBytes() );

    if (!error.isEmpty())
      throw new Exception(error);

    // compare results
    if (test.expectedOutput.equals(output)) {
      System.out.println("[SUCCESS] " + test.name);
    }
    else {
      System.out.println("[FAILED]  " + test.name);
      System.out.println("  Expected: '" + test.expectedOutput.replaceAll("\n", "\\\\n") + "'");
      System.out.println("  Result:   '" + output.replaceAll("\n", "\\\\n") + "'");
    }

  }

}
