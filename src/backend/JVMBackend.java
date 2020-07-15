package backend;

import targets.JVM;

public class JVMBackend implements Backend {

  private final JVM jvm;

  public JVMBackend() {
    this.jvm = new JVM();
  }

  public byte[] generate() {
    return jvm.generate();
  }

}
