package targets;

import java.util.HashMap;

import util.DynamicByteBuffer;

public class JVM {

  // (Program) CLASS STRUCTURE //
  private final static int MAGIC_NUMBER = 0xcafebabe;
  private final static int majorVersion = 58;
  private final static int minorVersion = 0;
  private final static int accessFlags = 0x0001 | 0x0020; // public + super (super is always set since Java 8)

  // CONSTANT POOL //
  private class CONSTANT_POOL {
    public final static int UTF8_TAG = 0x01;
    public final static int CLASS_TAG = 0x07;
  }


  // Code Generation Working Variables //
  private String className;

  private DynamicByteBuffer constants;
  private int nrOfConstants;

  private DynamicByteBuffer methods;
  private int nrOfMethods;

  private DynamicByteBuffer code;
  private int stackSize;
  private int maxStackSize;
  private int localsSize;
  private int maxLocalsSize;
  

  public JVM() {
    this.constants = new DynamicByteBuffer();
    this.nrOfConstants = 0;

    this.methods = new DynamicByteBuffer();
    this.nrOfMethods = 0;
  }


  public byte[] generate() {
    DynamicByteBuffer bytecode = new DynamicByteBuffer();

    int classIdx = addClassToConstantPool(className);
    String superClassName = binaryClassnameOf(Object.class);
    int superIdx = addClassToConstantPool(superClassName);


    bytecode.writeInt(MAGIC_NUMBER);
    bytecode.writeShort(minorVersion);
    bytecode.writeShort(majorVersion);

    // the constant_pool_count item is equal to the number of entries in the constant_pool table plus one
    bytecode.writeShort(nrOfConstants + 1);
    bytecode.write(constants.toByteArray());

    bytecode.writeShort(accessFlags);
    bytecode.writeShort(classIdx);
    bytecode.writeShort(superIdx);

    bytecode.writeShort(0); // interfaces
    bytecode.writeShort(0); // fields
    
    bytecode.writeShort(nrOfMethods);
    bytecode.write(methods.toByteArray());

    bytecode.writeShort(0); // attributes
    
    return bytecode.toByteArray();
  }
  

  // Structure //
  public void enterProgram(String name) {
    this.className = name;
  }

  public void enterFunction(String name, String descriptor) {
    final int accessFlags = 0x0001 | 0x0008; // public + static

    // method name & descriptor
    int nameIdx = addUtf8ToConstantPool(name);
    int descriptorIdx = addUtf8ToConstantPool(descriptor);

    // method structure
    methods.writeShort(accessFlags);
    methods.writeShort(nameIdx);
    methods.writeShort(descriptorIdx);
    // attributes are added at exitFunction

    this.nrOfMethods++;
    this.code = new DynamicByteBuffer();
    this.stackSize = 0;
    this.maxStackSize = this.stackSize;
    this.localsSize = 1; // string[] arg
    this.maxLocalsSize = this.localsSize;
  }

  public void exitFunction() {
    methods.writeShort(1); // 1 attribute: (Code)
    
    // code attribute
    methods.writeShort( addUtf8ToConstantPool("Code") );
    // code size + 2B stack size + 2B local size + 4B code size 
    // + 2B exception table size + 2B attributes table count + attributes table size
    methods.writeInt(code.size() + 12);
    methods.writeShort(maxStackSize);
    methods.writeShort(maxLocalsSize);
    methods.writeInt(code.size());
    methods.write(code.toByteArray());
    methods.writeShort(0); // exception table size
    methods.writeShort(0); // attributes table count
  }


  // Instructions //
  public void returnVoid() {
    final int opcode = 0xb1;
    code.writeByte(opcode);

    stackSize = 0;
  }

  public void returnInt() {
    final int opcode = 0xac;
    code.writeByte(opcode);

    stackSize = 0;
  }

  public void returnRef() {
    final int opcode = 0xb0;
    code.writeByte(opcode);

    stackSize = 0;
  }

  public void iconstm1() {
    final int opcode = 0x02;
    code.writeByte(opcode);

    incStackSize();
  }

  public void iconst0() {
    final int opcode = 0x03;
    code.writeByte(opcode);

    incStackSize();
  }

  public void iconst1() {
    final int opcode = 0x04;
    code.writeByte(opcode);

    incStackSize();
  }

  public void iconst2() {
    final int opcode = 0x05;
    code.writeByte(opcode);

    incStackSize();
  }

  public void iconst3() {
    final int opcode = 0x06;
    code.writeByte(opcode);

    incStackSize();
  }

  public void iconst4() {
    final int opcode = 0x07;
    code.writeByte(opcode);

    incStackSize();
  }

  public void iconst5() {
    final int opcode = 0x08;
    code.writeByte(opcode);

    incStackSize();
  }


  // Helpers //
  public String binaryClassnameOf(Class<?> c) {
    return c.getCanonicalName().replaceAll("[.]", "/");
  }

  private HashMap<String, Integer> utf8Constants = new HashMap<>();
  private int addUtf8ToConstantPool(String value) {
    if (utf8Constants.containsKey(value)) return utf8Constants.get(value);

    constants.writeByte(CONSTANT_POOL.UTF8_TAG);
    constants.writeUTF(value);
    int constIdx = ++nrOfConstants;

    utf8Constants.put(value, constIdx);
    return constIdx;
  }

  private HashMap<String, Integer> classConstants = new HashMap<>();
  private int addClassToConstantPool(String name) {
    if (classConstants.containsKey(name)) return classConstants.get(name);

    int nameIdx = addUtf8ToConstantPool(name);

    constants.writeByte(CONSTANT_POOL.CLASS_TAG);
    constants.writeShort(nameIdx);
    int constIdx = ++nrOfConstants;

    classConstants.put(name, constIdx);
    return constIdx;
  }

  private void incStackSize() {
    stackSize++;
    if (stackSize > maxStackSize) maxStackSize = stackSize;
  }

  private void incLocalsSize() {
    localsSize++;
    if (localsSize > maxLocalsSize) maxLocalsSize = localsSize;
  }

}
