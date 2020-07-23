package specs;

/**
 * Provides a common interface for every structure that can generate the corresponding JVM bytecode.
 */
public interface BytecodeStructure {
  
  public byte[] generate();

}
