package targets;

import java.util.HashMap;

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
  private short thisClass;
  private short superClass;
  private short codeAttributeNameIdx;

  private DynamicByteBuffer constants;
  private short nrOfConstants;

  private DynamicByteBuffer methods;
  private short nrOfMethods;

  private DynamicByteBuffer code;
  private short stackSize;
  private short localsSize;
  


  public JVM() {
    this.constants = new DynamicByteBuffer();
    this.nrOfConstants = 0;

    this.methods = new DynamicByteBuffer();
    this.nrOfMethods = 0;
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
    
    bytecode.writeShort(nrOfMethods);
    bytecode.write(methods.toByteArray());

    bytecode.writeShort(0); // attributes
    
    return bytecode.toByteArray();
  }
  

  // Structure //
  public void enterProgram(String name) {
    // class name & descriptor
    constants.writeByte(CONSTANT_POOL.UTF8_TAG);
    constants.writeUTF(name);
    short classNameIdx = ++nrOfConstants;
    constants.writeByte(CONSTANT_POOL.CLASS_TAG);
    constants.writeShort(classNameIdx);
    this.thisClass = ++nrOfConstants;

    // super name & descriptor
    String objectClassName = binaryClassnameOf(Object.class);
    constants.writeByte(CONSTANT_POOL.UTF8_TAG);
    constants.writeUTF(objectClassName);
    short superNameIdx = ++nrOfConstants;
    constants.writeByte(CONSTANT_POOL.CLASS_TAG);
    constants.writeShort(superNameIdx);
    this.superClass = ++nrOfConstants;

    // code attribute name
    constants.writeByte(CONSTANT_POOL.UTF8_TAG);
    constants.writeUTF("Code");
    this.codeAttributeNameIdx = ++nrOfConstants;
  }

  public void enterFunction(String name) {
    final short accessFlags = 0x0001 | 0x0008; // public + static

    // method name
    constants.writeByte(CONSTANT_POOL.UTF8_TAG);
    constants.writeUTF(name);
    short nameIdx = ++nrOfConstants;

    // method descriptor
    String descriptor = "([L"+ binaryClassnameOf(String.class) + ";)V";
    constants.writeByte(CONSTANT_POOL.UTF8_TAG);
    constants.writeUTF(descriptor);
    short descriptorIdx = ++nrOfConstants;

    // method structure
    methods.writeShort(accessFlags);
    methods.writeShort(nameIdx);
    methods.writeShort(descriptorIdx);
    // attributes are added at exitFunction

    this.nrOfMethods++;
    this.code = new DynamicByteBuffer();
    this.stackSize = 0;
    this.localsSize = 1; // string[] arg
  }

  public void exitFunction() {
    methods.writeShort(1); // 1 attribute: (Code)
    
    // code attribute
    methods.writeShort(codeAttributeNameIdx);
    // code size + 2B stack size + 2B local size + 4B code size 
    // + 2B exception table size + 2B attributes table count + attributes table size
    methods.writeInt(code.size() + 12);
    methods.writeShort(stackSize);
    methods.writeShort(localsSize);
    methods.writeInt(code.size());
    methods.write(code.toByteArray());
    methods.writeShort(0); // exception table size
    methods.writeShort(0); // attributes table count
  }


  // Instructions //
  public void returnVoid() {
    final byte opcode = (byte)0xb1;
    code.writeByte(opcode);
  }


  // Helpers //
  private String binaryClassnameOf(Class c) {
    return c.getCanonicalName().replaceAll("[.]", "/");
  }
}
