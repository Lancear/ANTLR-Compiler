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
  private static class CONSTANT_POOL {
    public final static int UTF8_TAG = 0x01;
    public final static int CLASS_TAG = 0x07;
  }

  // ARRAY TYPE //
  public final static class ARRAY_TYPE {
    public final static int BOOLEAN = 4;
    public final static int INT = 10;
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
    this.className = "Unnamed";

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


  // Constant Pool Helpers //
  public String binaryClassnameOf(Class<?> c) {
    return c.getCanonicalName().replaceAll("[.]", "/");
  }

  private HashMap<String, Integer> utf8Constants = new HashMap<>();
  public int addUtf8ToConstantPool(String value) {
    if (utf8Constants.containsKey(value)) return utf8Constants.get(value);

    constants.writeByte(CONSTANT_POOL.UTF8_TAG);
    constants.writeUTF(value);
    int constIdx = ++nrOfConstants;

    utf8Constants.put(value, constIdx);
    return constIdx;
  }

  private HashMap<String, Integer> classConstants = new HashMap<>();
  public int addClassToConstantPool(String name) {
    if (classConstants.containsKey(name)) return classConstants.get(name);

    int nameIdx = addUtf8ToConstantPool(name);

    constants.writeByte(CONSTANT_POOL.CLASS_TAG);
    constants.writeShort(nameIdx);
    int constIdx = ++nrOfConstants;

    classConstants.put(name, constIdx);
    return constIdx;
  }


  // Instructions //
  // Constants //
  public void iconst_m1() {
    final int opcode = 0x02;
    code.writeByte(opcode);

    incStackSize();
  }

  public void iconst_0() {
    final int opcode = 0x03;
    code.writeByte(opcode);

    incStackSize();
  }

  public void iconst_1() {
    final int opcode = 0x04;
    code.writeByte(opcode);

    incStackSize();
  }

  public void iconst_2() {
    final int opcode = 0x05;
    code.writeByte(opcode);

    incStackSize();
  }

  public void iconst_3() {
    final int opcode = 0x06;
    code.writeByte(opcode);

    incStackSize();
  }

  public void iconst_4() {
    final int opcode = 0x07;
    code.writeByte(opcode);

    incStackSize();
  }

  public void iconst_5() {
    final int opcode = 0x08;
    code.writeByte(opcode);

    incStackSize();
  }

  public void bipush(int b) {
    final int opcode = 0x10;
    code.writeByte(opcode);
    code.writeByte(b);

    incStackSize();
  }

  public void sipush(int s) {
    final int opcode = 0x11;
    code.writeByte(opcode);
    code.writeShort(s);

    incStackSize();
  }

  public void ldc(int idx) {
    final int opcode = 0x12;
    code.writeByte(opcode);
    code.writeByte(idx);

    incStackSize();
  }

  public void ldc_w(int idx) {
    final int opcode = 0x13;
    code.writeByte(opcode);
    code.writeShort(idx);

    incStackSize();
  }

  // Loads //
  public void iload(int idx) {
    final int opcode = 0x15;
    code.writeByte(opcode);
    code.writeByte(idx);

    incStackSize();
    if (idx > maxLocalsSize) maxLocalsSize = idx;
  }

  public void aload(int idx) {
    final int opcode = 0x19;
    code.writeByte(opcode);
    code.writeByte(idx);

    incStackSize();
    if (idx > maxLocalsSize) maxLocalsSize = idx;
  }

  public void iload_0() {
    final int opcode = 0x1a;
    code.writeByte(opcode);

    incStackSize();
  }

  public void iload_1() {
    final int opcode = 0x1b;
    code.writeByte(opcode);

    incStackSize();
  }

  public void iload_2() {
    final int opcode = 0x1c;
    code.writeByte(opcode);

    incStackSize();
  }

  public void iload_3() {
    final int opcode = 0x1d;
    code.writeByte(opcode);

    incStackSize();
  }

  public void aload_0() {
    final int opcode = 0x2a;
    code.writeByte(opcode);

    incStackSize();
  }

  public void aload_1() {
    final int opcode = 0x2b;
    code.writeByte(opcode);

    incStackSize();
  }

  public void aload_2() {
    final int opcode = 0x2c;
    code.writeByte(opcode);

    incStackSize();
  }

  public void aload_3() {
    final int opcode = 0x2d;
    code.writeByte(opcode);

    incStackSize();
  }

  public void iaload() {
    final int opcode = 0x2e;
    code.writeByte(opcode);

    stackSize--;
  }

  public void aaload() {
    final int opcode = 0x32;
    code.writeByte(opcode);

    stackSize--;
  }

  public void baload() {
    final int opcode = 0x33;
    code.writeByte(opcode);

    stackSize--;
  }

  // Stores //
  public void istore(int idx) {
    final int opcode = 0x36;
    code.writeByte(opcode);
    code.writeByte(idx);

    stackSize--;
    if (idx > maxLocalsSize) maxLocalsSize = idx;
  }

  public void astore(int idx) {
    final int opcode = 0x3a;
    code.writeByte(opcode);
    code.writeByte(idx);

    stackSize--;
    if (idx > maxLocalsSize) maxLocalsSize = idx;
  }

  public void istore_0() {
    final int opcode = 0x3b;
    code.writeByte(opcode);

    stackSize--;
  }

  public void istore_1() {
    final int opcode = 0x3c;
    code.writeByte(opcode);

    stackSize--;
  }

  public void istore_2() {
    final int opcode = 0x3d;
    code.writeByte(opcode);

    stackSize--;
  }

  public void istore_3() {
    final int opcode = 0x3e;
    code.writeByte(opcode);

    stackSize--;
  }

  public void astore_0() {
    final int opcode = 0x4b;
    code.writeByte(opcode);

    stackSize--;
  }

  public void astore_1() {
    final int opcode = 0x4c;
    code.writeByte(opcode);

    stackSize--;
  }

  public void astore_2() {
    final int opcode = 0x4d;
    code.writeByte(opcode);

    stackSize--;
  }

  public void astore_3() {
    final int opcode = 0x4e;
    code.writeByte(opcode);

    stackSize--;
  }

  public void iastore() {
    final int opcode = 0x4f;
    code.writeByte(opcode);

    stackSize -= 3;
  }

  public void aastore() {
    final int opcode = 0x53;
    code.writeByte(opcode);

    stackSize -= 3;
  }

  public void bastore() {
    final int opcode = 0x54;
    code.writeByte(opcode);

    stackSize -= 3;
  }

  // Stack //
  public void pop() {
    final int opcode = 0x57;
    code.writeByte(opcode);

    stackSize--;
  }

  public void dup() {
    final int opcode = 0x59;
    code.writeByte(opcode);

    incStackSize();
  }

  public void swap() {
    final int opcode = 0x5f;
    code.writeByte(opcode);
  }

  // Math //
  public void iadd() {
    final int opcode = 0x60;
    code.writeByte(opcode);

    stackSize--;
  }

  public void isub() {
    final int opcode = 0x64;
    code.writeByte(opcode);

    stackSize--;
  }

  public void imul() {
    final int opcode = 0x68;
    code.writeByte(opcode);

    stackSize--;
  }

  public void idiv() {
    final int opcode = 0x6c;
    code.writeByte(opcode);

    stackSize--;
  }

  public void irem() {
    final int opcode = 0x70;
    code.writeByte(opcode);

    stackSize--;
  }

  public void ineg() {
    final int opcode = 0x74;
    code.writeByte(opcode);
  }

  public void ishl() {
    final int opcode = 0x78;
    code.writeByte(opcode);

    stackSize--;
  }

  public void ishr() {
    final int opcode = 0x7a;
    code.writeByte(opcode);

    stackSize--;
  }

  public void iinc(int idx, int c) {
    final int opcode = 0x84;
    code.writeByte(opcode);
  }

  // Control //
  public void returnVoid() {
    final int opcode = 0xb1;
    code.writeByte(opcode);

    stackSize = 0;
  }

  public void ireturn() {
    final int opcode = 0xac;
    code.writeByte(opcode);

    stackSize = 0;
  }

  public void areturn() {
    final int opcode = 0xb0;
    code.writeByte(opcode);

    stackSize = 0;
  }

  // References //
  public void getStatic(int idx) {
    final int opcode = 0xb2;
    code.writeByte(opcode);
    code.writeShort(idx);

    incStackSize();
  }

  public void putStatic(int idx) {
    final int opcode = 0xb3;
    code.writeByte(opcode);
    code.writeShort(idx);

    stackSize--;
  }

  public void getField(int idx) {
    final int opcode = 0xb4;
    code.writeByte(opcode);
    code.writeShort(idx);
  }

  public void putField(int idx) {
    final int opcode = 0xb5;
    code.writeByte(opcode);
    code.writeShort(idx);

    stackSize -= 2;
  }

  public void invokeStatic(int idx) {
    final int opcode = 0xb8;
    code.writeByte(opcode);
    code.writeShort(idx);

    // TODO: calculate stack change
  }

  public void anew(int idx) {
    final int opcode = 0xbb;
    code.writeByte(opcode);
    code.writeShort(idx);

    incStackSize();
  }

  public void newArray(int aType) {
    final int opcode = 0xbc;
    code.writeByte(opcode);
    code.writeByte(aType);
  }

  public void anewArray(int idx) {
    final int opcode = 0xbd;
    code.writeByte(opcode);
    code.writeShort(idx);
  }

  public void arraylength() {
    final int opcode = 0xbe;
    code.writeByte(opcode);
  }

  // Extended //
  public void wide(String mnemonic, int idx) {
    int opcode;

    switch (mnemonic) {
      case "iload": 
        opcode = 0x15;
        incStackSize();
        break;
      
      case "aload": 
        opcode = 0x19;
        incStackSize();
        break;

      case "istore": 
        opcode = 0x36;
        stackSize--;
        break;

      case "astore": 
        opcode = 0x3a;
        stackSize--;
        break;

      default: 
        throw new IllegalArgumentException("Valid Mnemonics are: iload / aload / istore / astore");
    }

    code.writeByte(opcode);
    code.writeShort(idx);
    
    if (idx > maxLocalsSize) maxLocalsSize = idx;
  }

  public void wide(int idx, int c) {
    final int opcode = 0x84;
    code.writeByte(opcode);
    code.writeShort(idx);
    code.writeShort(c);
  }


  // Helpers //
  private void incStackSize() {
    stackSize++;
    if (stackSize > maxStackSize) maxStackSize = stackSize;
  }

}
