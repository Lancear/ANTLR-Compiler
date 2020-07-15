package targets;

import util.DynamicByteBuffer;

public class JVM {

  // (Program) CLASS STRUCTURE //
  private final static int MAGIC_NUMBER = 0xcafebabe;
  private final static short majorVersion = 58;
  private final static short minorVersion = 0;
  private final static short accessFlags = 0x0001 | 0x0020; // public + super (super is always set since Java 8)

  // CONSTANT POOL //
  private class CONSTANT_POOL {
    public final static short UTF8_TAG = 0x01;
    public final static short CLASS_TAG = 0x07;
  }

  // Code Generation Working Variables //
  private short thisClass = 0;
  private short superClass = 0;
  private DynamicByteBuffer constants;
  private short nrOfConstants = 0;
  private DynamicByteBuffer methods;
  private short nrOfMethods = 0;


  public JVM() {
    this.constants = new DynamicByteBuffer();
    this.methods = new DynamicByteBuffer();
    enterProgram("Test1");
  }


  public byte[] generate() {
    DynamicByteBuffer bytecode = new DynamicByteBuffer();

    bytecode.writeInt(MAGIC_NUMBER);
    bytecode.writeShort(minorVersion);
    bytecode.writeShort(majorVersion);

    // the constant_pool_count item is equal to the number of entries in the constant_pool table plus one
    bytecode.writeShort(nrOfConstants + 1);
    bytecode.write(constants.toByteArray());

    bytecode.writeShort(accessFlags);
    bytecode.writeShort(thisClass);
    bytecode.writeShort(superClass);

    bytecode.writeShort(0); // interfaces
    bytecode.writeShort(0); // fields
    bytecode.writeShort(0); // methods
    bytecode.writeShort(0); // attributes
    
    return bytecode.toByteArray();
  }
  

  // Structure //
  public void enterProgram(String programName) {
    // set the class name & descriptor
    constants.writeByte(CONSTANT_POOL.UTF8_TAG);
    constants.writeUTF(programName);
    int classNameIdx = ++nrOfConstants;
    constants.writeByte(CONSTANT_POOL.CLASS_TAG);
    constants.writeShort(classNameIdx);
    this.thisClass = ++nrOfConstants;

    // set the super name & descriptor
    String objectClassName = Object.class.getCanonicalName().replaceAll("[.]", "/");
    constants.writeByte(CONSTANT_POOL.UTF8_TAG);
    constants.writeUTF(objectClassName);
    int superNameIdx = ++nrOfConstants;
    constants.writeByte(CONSTANT_POOL.CLASS_TAG);
    constants.writeShort(superNameIdx);
    this.superClass = ++nrOfConstants;
  }

  public void enterFunction(String name) {

  }

  public void exitFunction(String name) {

  }


  // Instructions //
  
}
