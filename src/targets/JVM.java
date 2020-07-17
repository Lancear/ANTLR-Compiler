package targets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import util.DynamicByteBuffer;

public class JVM {

  // (Program) CLASS STRUCTURE //
  private final static int MAGIC_NUMBER = 0xcafebabe;
  private final static int majorVersion = 58;
  private final static int minorVersion = 0;
  private final static int accessFlags = 0x0001 | 0x0020; // public + super (super is always set since Java 8)
  private final static int branchPlaceholder = 0xdead;

  // CONSTANT POOL //
  private static class CONSTANT_POOL {
    public final static int UTF8_TAG = 0x01;
    public final static int INTEGER_TAG = 0x03;
    public final static int CLASS_TAG = 0x07;
    public final static int STRING_TAG = 0x08;
    public final static int FIELDREF_TAG = 0x09;
    public final static int METHODREF_TAG = 0x0a;
    public final static int NAMEANDTYPE_TAG = 0x0c;
  }

  // ARRAY TYPE //
  public final static class ARRAY_TYPE {
    public final static int BOOLEAN = 4;
    public final static int INT = 10;
  }

  // STACKMAP ENTRY // 
  private static class StackMapFrame {
    public final static int FULL_FRAME = 255;
    public final static class VerificationTypeInfo {
      public final static int INTEGER = 1;
      public final static int OBJECT = 7;
    }

    public int codeOffset;
    public String[] stack;
    public String[] locals;

    public StackMapFrame(int codeOffset, Stack<String> stack, HashMap<Integer, String> locals) {
      this.codeOffset = codeOffset;
      this.stack = stack.toArray(new String[stack.size()]);
      this.locals = new String[locals.size()];

      for (int idx : locals.keySet()) {
        this.locals[idx] = locals.get(idx);
      }
    }
  }

  // Code Generation Working Variables //
  private String className;

  private DynamicByteBuffer constants;
  private int nrOfConstants;

  private DynamicByteBuffer methods;
  private int nrOfMethods;

  private DynamicByteBuffer fields;
  private int nrOfFields;

  private DynamicByteBuffer innerClasses;
  private int nrOfInnerClasses;

  private DynamicByteBuffer code;
  private Stack<String> stack;
  private int maxStackSize;
  private HashMap<Integer, String> locals;
  private int maxLocalsSize;

  private HashMap<Integer, String> labels;
  private HashMap<String, Integer> addresses;
  // sorted automatically since labels can only be added in order
  private ArrayList<StackMapFrame> stackMapFrames;
  

  public JVM() {
    this.className = "Unnamed";

    this.constants = new DynamicByteBuffer();
    this.nrOfConstants = 0;

    this.methods = new DynamicByteBuffer();
    this.nrOfMethods = 0;

    this.fields = new DynamicByteBuffer();
    this.nrOfFields = 0;

    this.innerClasses = new DynamicByteBuffer();
    this.nrOfInnerClasses = 0;
  }


  public byte[] generate() {
    DynamicByteBuffer bytecode = new DynamicByteBuffer();

    // Last Minute ConstantPool Entries //
    int classIdx = addClassToConstantPool(className);
    String superClassName = binaryClassNameOf(Object.class);
    int superIdx = addClassToConstantPool(superClassName);
    if (nrOfInnerClasses != 0) addUtf8ToConstantPool("InnerClasses");

    bytecode.writeInt(MAGIC_NUMBER);
    bytecode.writeShort(minorVersion);
    bytecode.writeShort(majorVersion);

    // the constant_pool_count item is equal to the number of entries in the constant_pool table plus one
    bytecode.writeShort(nrOfConstants + 1);
    bytecode.write( constants.toByteArray() );

    bytecode.writeShort(accessFlags);
    bytecode.writeShort(classIdx);
    bytecode.writeShort(superIdx);

    bytecode.writeShort(0); // interfaces

    bytecode.writeShort(nrOfFields);
    bytecode.write( fields.toByteArray() );
    bytecode.writeShort(nrOfMethods);
    bytecode.write( methods.toByteArray() );

    // attributes
    if (nrOfInnerClasses == 0) {
      bytecode.writeShort(0);
    }
    else {
      bytecode.writeShort(1); // attributes: 1 InnerClasses
      bytecode.writeShort( addUtf8ToConstantPool("InnerClasses") );
      // innerClassses + 2B number_of_classes
      bytecode.writeInt( innerClasses.size() + 2 );
      bytecode.writeShort(nrOfInnerClasses);
      bytecode.write(innerClasses.toByteArray());
    }

    return bytecode.toByteArray();
  }
  

  // Structure //
  public void enterClass(String name) {
    this.className = name;
  }

  public void enterMethod(String name, String descriptor) {
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
    this.stack = new Stack<>();
    this.maxStackSize = stack.size();
    this.locals = new HashMap<>();
    updateLocalsFor(descriptor, 0);
    this.maxLocalsSize = locals.size();
    this.labels = new HashMap<>();
    this.addresses = new HashMap<>();
    this.stackMapFrames = new ArrayList<>();
  }

  public void exitMethod() {
    methods.writeShort(1); // 1 attribute: (Code)
    
    // code attribute
    methods.writeShort( addUtf8ToConstantPool("Code") );

    // attributes
    if (stackMapFrames.size() == 0) {
       // code size + 2B stack size + 2B local size + 4B code size 
      // + 2B exception table size + 2B attributes table count
      methods.writeInt(code.size() + 12);
      methods.writeShort(maxStackSize);
      methods.writeShort(maxLocalsSize);
      methods.writeInt(code.size());
      methods.write( backpatch( code.toByteArray() ) );
      methods.writeShort(0); // exception table size
      methods.writeShort(0);
    }
    else {
      DynamicByteBuffer stackMapTable = new DynamicByteBuffer();
      int nrOfStackMapFrames = 0;

      int currOffset = 0;

      for (StackMapFrame stackmap : stackMapFrames) {
        int offsetDelta = stackmap.codeOffset - currOffset;
        if (offsetDelta < 0)
          throw new IllegalStateException("Labels should be added in order, so the offsets should be sorted ascending!");

        if (offsetDelta != 0) {
          nrOfStackMapFrames++;
          if (currOffset != 0) offsetDelta--;

          // generate only full frames for simplicity
          stackMapTable.writeByte(StackMapFrame.FULL_FRAME);
          stackMapTable.writeShort(offsetDelta);

          stackMapTable.writeShort(stackmap.locals.length);
          for (int idx = 0; idx < stackmap.locals.length; idx++) {
            System.out.println(stackmap.locals[idx]);
            if (stackmap.locals[idx].equals("I")) {
              stackMapTable.writeByte(StackMapFrame.VerificationTypeInfo.INTEGER);
            }
            else {
              stackMapTable.writeByte(StackMapFrame.VerificationTypeInfo.OBJECT);
              stackMapTable.writeShort( addClassToConstantPool(stackmap.locals[idx]) );
            }
          }

          System.out.println("stack");

          stackMapTable.writeShort(stackmap.stack.length);
          for (int idx = 0; idx < stackmap.stack.length; idx++) {
            System.out.println(stackmap.stack[idx]);
            if (stackmap.stack[idx].equals("I")) {
              stackMapTable.writeByte(StackMapFrame.VerificationTypeInfo.INTEGER);
            }
            else {
              stackMapTable.writeByte(StackMapFrame.VerificationTypeInfo.OBJECT);
              stackMapTable.writeShort( addClassToConstantPool(stackmap.stack[idx]) );
            }
          }
          
          currOffset = stackmap.codeOffset;
        }
      }

      // code size + 2B stack size + 2B local size + 4B code size 
      // + 2B exception table size + 2B attributes table count + attributes table size
      methods.writeInt(code.size() + 12 + (stackMapTable.size() + 8));
      methods.writeShort(maxStackSize);
      methods.writeShort(maxLocalsSize + 1);
      methods.writeInt(code.size());
      methods.write( backpatch( code.toByteArray() ) );
      methods.writeShort(0); // exception table size

      // attributes: 1 StackMapTable
      methods.writeShort(1);
      methods.writeShort( addUtf8ToConstantPool("StackMapTable") );
      // stackMapTable + 2B number_of_entries
      methods.writeInt( stackMapTable.size() + 2 );
      methods.writeShort(nrOfStackMapFrames);
      methods.write(stackMapTable.toByteArray());
    }
  }

  public void addField(String name, String descriptor, boolean staticFlag) {
    int accessFlags = 0x0001; // public
    if (staticFlag) accessFlags |= 0x0008;  // static

    // method name & descriptor
    int nameIdx = addUtf8ToConstantPool(name);
    int descriptorIdx = addUtf8ToConstantPool(descriptor);

    // method structure
    fields.writeShort(accessFlags);
    fields.writeShort(nameIdx);
    fields.writeShort(descriptorIdx);
    fields.writeShort(0); // attributes
    this.nrOfFields++;
  }

  public void addInnerClass(String name, String fileName) {
    final int accessFlags = 0x0001 | 0x0008; // public + static

    innerClasses.writeShort( addClassToConstantPool(fileName) );
    innerClasses.writeShort( addClassToConstantPool(this.className) );
    innerClasses.writeShort( addUtf8ToConstantPool(name) );
    innerClasses.writeShort(accessFlags);
    nrOfInnerClasses++;
  }

  public void addLabel(String label) {
    addresses.put(label, code.size());
    stackMapFrames.add( new StackMapFrame(code.size(), stack, locals) );
  }

  public byte[] backpatch(byte[] bytecode) {
    for (int jump : labels.keySet()) {
      String label = labels.get(jump);

      if (!addresses.containsKey(label)) 
        throw new IllegalStateException("Unresolved label: '" + label + "'");

      int address = addresses.get(label) - jump;
      bytecode[jump + 1] = (byte)(address >> 8 & 0xff);
      bytecode[jump + 2] = (byte)(address & 0xff);
    }

    return bytecode;
  }


  // Constant Pool Helpers //
  public static String binaryClassNameOf(Class<?> c) {
    return c.getCanonicalName().replaceAll("[.]", "/");
  }

  public static String fieldDescriptorOf(Class<?> c) {
    return "L" + binaryClassNameOf(c) + ";";
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

  private HashMap<Integer, Integer> integerConstants = new HashMap<>();
  public int addIntegerToConstantPool(int value) {
    if (integerConstants.containsKey(value)) return integerConstants.get(value);

    constants.writeByte(CONSTANT_POOL.INTEGER_TAG);
    constants.writeInt(value);
    int constIdx = ++nrOfConstants;

    integerConstants.put(value, constIdx);
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

  private HashMap<String, Integer> stringConstants = new HashMap<>();
  public int addStringToConstantPool(String value) {
    if (stringConstants.containsKey(value)) return stringConstants.get(value);

    int stringIdx = addUtf8ToConstantPool(value);

    constants.writeByte(CONSTANT_POOL.STRING_TAG);
    constants.writeShort(stringIdx);
    int constIdx = ++nrOfConstants;

    stringConstants.put(value, constIdx);
    return constIdx;
  }

  private HashMap<String, Integer> nameAndTypeConstants = new HashMap<>();
  public int addNameAndTypeToConstantPool(String name, String descriptor) {
    String nameAndType = name + ":" + descriptor;
    if (nameAndTypeConstants.containsKey(nameAndType)) return nameAndTypeConstants.get(nameAndType);

    int nameIdx = addUtf8ToConstantPool(name);
    int descriptorIdx = addUtf8ToConstantPool(descriptor);

    constants.writeByte(CONSTANT_POOL.NAMEANDTYPE_TAG);
    constants.writeShort(nameIdx);
    constants.writeShort(descriptorIdx);
    int constIdx = ++nrOfConstants;

    nameAndTypeConstants.put(nameAndType, constIdx);
    return constIdx;
  }

  private HashMap<String, Integer> fieldRefConstants = new HashMap<>();
  public int addFieldRefToConstantPool(String className, String fieldName, String fieldType) {
    String fieldRef = className + "." + fieldName + ":" + fieldType;
    if (fieldRefConstants.containsKey(fieldRef)) return fieldRefConstants.get(fieldRef);

    int classIdx = addClassToConstantPool(className);
    int nameAndTypeIdx = addNameAndTypeToConstantPool(fieldName, fieldType);

    constants.writeByte(CONSTANT_POOL.FIELDREF_TAG);
    constants.writeShort(classIdx);
    constants.writeShort(nameAndTypeIdx);
    int constIdx = ++nrOfConstants;

    fieldRefConstants.put(fieldRef, constIdx);
    return constIdx;
  }

  private HashMap<String, Integer> methodRefConstants = new HashMap<>();
  public int addMethodRefToConstantPool(String className, String methodName, String methodType) {
    String methodRef = className + "." + methodName + ":" + methodType;
    if (methodRefConstants.containsKey(methodRef)) return methodRefConstants.get(methodRef);

    int classIdx = addClassToConstantPool(className);
    int nameAndTypeIdx = addNameAndTypeToConstantPool(methodName, methodType);

    constants.writeByte(CONSTANT_POOL.METHODREF_TAG);
    constants.writeShort(classIdx);
    constants.writeShort(nameAndTypeIdx);
    int constIdx = ++nrOfConstants;

    methodRefConstants.put(methodRef, constIdx);
    return constIdx;
  }


  // Instructions //
  // Constants //
  public void iconst_m1() {
    final int opcode = 0x02;
    code.writeByte(opcode);

    stack.push("I");
    if (stack.size() > maxStackSize) maxStackSize = stack.size();
  }

  public void iconst_0() {
    final int opcode = 0x03;
    code.writeByte(opcode);

    stack.push("I");
    if (stack.size() > maxStackSize) maxStackSize = stack.size();
  }

  public void iconst_1() {
    final int opcode = 0x04;
    code.writeByte(opcode);

    stack.push("I");
    if (stack.size() > maxStackSize) maxStackSize = stack.size();
  }

  public void iconst_2() {
    final int opcode = 0x05;
    code.writeByte(opcode);

    stack.push("I");
    if (stack.size() > maxStackSize) maxStackSize = stack.size();
  }

  public void iconst_3() {
    final int opcode = 0x06;
    code.writeByte(opcode);

    stack.push("I");
    if (stack.size() > maxStackSize) maxStackSize = stack.size();
  }

  public void iconst_4() {
    final int opcode = 0x07;
    code.writeByte(opcode);

    stack.push("I");
    if (stack.size() > maxStackSize) maxStackSize = stack.size();
  }

  public void iconst_5() {
    final int opcode = 0x08;
    code.writeByte(opcode);

    stack.push("I");
    if (stack.size() > maxStackSize) maxStackSize = stack.size();
  }

  public void bipush(int b) {
    final int opcode = 0x10;
    code.writeByte(opcode);
    code.writeByte(b);

    stack.push("I");
    if (stack.size() > maxStackSize) maxStackSize = stack.size();
  }

  public void sipush(int s) {
    final int opcode = 0x11;
    code.writeByte(opcode);
    code.writeShort(s);

    stack.push("I");
    if (stack.size() > maxStackSize) maxStackSize = stack.size();
  }

  public void ldc(int idx, String descriptor) {
    final int opcode = 0x12;
    code.writeByte(opcode);
    code.writeByte(idx);

    stack.push(descriptor);
    if (stack.size() > maxStackSize) maxStackSize = stack.size();
  }

  public void ldc_w(int idx, String descriptor) {
    final int opcode = 0x13;
    code.writeByte(opcode);
    code.writeShort(idx);

    stack.push(descriptor);
    if (stack.size() > maxStackSize) maxStackSize = stack.size();
  }

  // Loads //
  public void iload(int idx) {
    final int opcode = 0x15;
    code.writeByte(opcode);
    code.writeByte(idx);

    stack.push("I");
    if (stack.size() > maxStackSize) maxStackSize = stack.size();
    
    if (locals.containsKey(idx) && !locals.get(idx).equals("I"))
      throw new IllegalArgumentException("The types for the local variable at " + idx + " don't match!");
    
    locals.put(idx, "I");
    if (idx > maxLocalsSize) maxLocalsSize = idx;
  }

  public void aload(int idx, String descriptor) {
    final int opcode = 0x19;
    code.writeByte(opcode);
    code.writeByte(idx);

    stack.push("I");
    if (stack.size() > maxStackSize) maxStackSize = stack.size();
    
    if (locals.containsKey(idx) && !locals.get(idx).equals(descriptor))
      throw new IllegalArgumentException("The types for the local variable at " + idx + " don't match!");
    
    locals.put(idx, descriptor);
    if (idx > maxLocalsSize) maxLocalsSize = idx;
  }

  public void iload_0() {
    final int opcode = 0x1a;
    code.writeByte(opcode);

    stack.push("I");
    if (stack.size() > maxStackSize) maxStackSize = stack.size();
    if (0 > maxLocalsSize) maxLocalsSize = 0;
  }

  public void iload_1() {
    final int opcode = 0x1b;
    code.writeByte(opcode);

    stack.push("I");
    if (stack.size() > maxStackSize) maxStackSize = stack.size();
    if (1 > maxLocalsSize) maxLocalsSize = 1;
  }

  public void iload_2() {
    final int opcode = 0x1c;
    code.writeByte(opcode);

    stack.push("I");
    if (stack.size() > maxStackSize) maxStackSize = stack.size();
    if (2 > maxLocalsSize) maxLocalsSize = 2;
  }

  public void iload_3() {
    final int opcode = 0x1d;
    code.writeByte(opcode);

    stack.push("I");
    if (stack.size() > maxStackSize) maxStackSize = stack.size();
    if (3 > maxLocalsSize) maxLocalsSize = 3;
  }

  public void aload_0() {
    final int opcode = 0x2a;
    code.writeByte(opcode);

    stack.push(locals.get(0));
    if (stack.size() > maxStackSize) maxStackSize = stack.size();
    if (0 > maxLocalsSize) maxLocalsSize = 0;
  }

  public void aload_1() {
    final int opcode = 0x2b;
    code.writeByte(opcode);

    stack.push(locals.get(1));
    if (stack.size() > maxStackSize) maxStackSize = stack.size();
    if (1 > maxLocalsSize) maxLocalsSize = 1;
  }

  public void aload_2() {
    final int opcode = 0x2c;
    code.writeByte(opcode);

    stack.push(locals.get(2));
    if (stack.size() > maxStackSize) maxStackSize = stack.size();
    if (2 > maxLocalsSize) maxLocalsSize = 2;
  }

  public void aload_3() {
    final int opcode = 0x2d;
    code.writeByte(opcode);

    stack.push(locals.get(3));
    if (stack.size() > maxStackSize) maxStackSize = stack.size();
    if (3 > maxLocalsSize) maxLocalsSize = 3;
  }

  public void iaload() {
    final int opcode = 0x2e;
    code.writeByte(opcode);

    stack.pop();
  }

  public void aaload() {
    final int opcode = 0x32;
    code.writeByte(opcode);

    stack.pop();
  }

  public void baload() {
    final int opcode = 0x33;
    code.writeByte(opcode);

    stack.pop();
  }

  // Stores //
  public void istore(int idx) {
    final int opcode = 0x36;
    code.writeByte(opcode);
    code.writeByte(idx);

    stack.pop();

    if (locals.containsKey(idx) && !locals.get(idx).equals("I"))
      throw new IllegalArgumentException("The types for the local variable at " + idx + " don't match!");
    
    locals.put(idx, "I");
    if (idx > maxLocalsSize) maxLocalsSize = idx;
  }

  public void astore(int idx, String descriptor) {
    final int opcode = 0x3a;
    code.writeByte(opcode);
    code.writeByte(idx);

    stack.pop();

    if (locals.containsKey(idx) && !locals.get(idx).equals(descriptor))
      throw new IllegalArgumentException("The types for the local variable at " + idx + " don't match!");
    
    locals.put(idx, descriptor);
    if (idx > maxLocalsSize) maxLocalsSize = idx;
  }

  public void istore_0() {
    final int opcode = 0x3b;
    code.writeByte(opcode);

    stack.pop();
    if (locals.containsKey(0) && !locals.get(0).equals("I"))
      throw new IllegalArgumentException("The types for the local variable at 0 don't match!");
    
    locals.put(0, "I");
    if (0 > maxLocalsSize) maxLocalsSize = 0;
  }

  public void istore_1() {
    final int opcode = 0x3c;
    code.writeByte(opcode);

    stack.pop();
    if (locals.containsKey(1) && !locals.get(1).equals("I"))
      throw new IllegalArgumentException("The types for the local variable at 1 don't match!");
    
    locals.put(1, "I");
    if (1 > maxLocalsSize) maxLocalsSize = 1;
  }

  public void istore_2() {
    final int opcode = 0x3d;
    code.writeByte(opcode);

    stack.pop();
    if (locals.containsKey(2) && !locals.get(2).equals("I"))
      throw new IllegalArgumentException("The types for the local variable at 2 don't match!");
    
    locals.put(2, "I");
    if (2 > maxLocalsSize) maxLocalsSize = 2;
  }

  public void istore_3() {
    final int opcode = 0x3e;
    code.writeByte(opcode);

    stack.pop();
    if (locals.containsKey(3) && !locals.get(3).equals("I"))
      throw new IllegalArgumentException("The types for the local variable at 3 don't match!");
    
    locals.put(3, "I");
    if (3 > maxLocalsSize) maxLocalsSize = 3;
  }

  public void astore_0(String descriptor) {
    final int opcode = 0x4b;
    code.writeByte(opcode);

    stack.pop();
    if (locals.containsKey(0) && !locals.get(0).equals(descriptor))
      throw new IllegalArgumentException("The types for the local variable at 0 don't match!");
    
    locals.put(0, descriptor);
    if (0 > maxLocalsSize) maxLocalsSize = 0;
  }

  public void astore_1(String descriptor) {
    final int opcode = 0x4c;
    code.writeByte(opcode);

    stack.pop();
    if (locals.containsKey(1) && !locals.get(1).equals(descriptor))
      throw new IllegalArgumentException("The types for the local variable at 1 don't match!");
    
    locals.put(1, descriptor);
    if (1 > maxLocalsSize) maxLocalsSize = 1;
  }

  public void astore_2(String descriptor) {
    final int opcode = 0x4d;
    code.writeByte(opcode);

    stack.pop();
    if (locals.containsKey(2) && !locals.get(2).equals(descriptor))
      throw new IllegalArgumentException("The types for the local variable at 2 don't match!");
    
    locals.put(2, descriptor);
    if (2 > maxLocalsSize) maxLocalsSize = 2;
  }

  public void astore_3(String descriptor) {
    final int opcode = 0x4e;
    code.writeByte(opcode);

    stack.pop();
    if (locals.containsKey(3) && !locals.get(3).equals(descriptor))
      throw new IllegalArgumentException("The types for the local variable at 3 don't match!");
    
    locals.put(3, descriptor);
    if (3 > maxLocalsSize) maxLocalsSize = 3;
  }

  public void iastore() {
    final int opcode = 0x4f;
    code.writeByte(opcode);

    stack.pop();
    stack.pop();
    stack.pop();
  }

  public void aastore() {
    final int opcode = 0x53;
    code.writeByte(opcode);

    stack.pop();
    stack.pop();
    stack.pop();
  }

  public void bastore() {
    final int opcode = 0x54;
    code.writeByte(opcode);

    stack.pop();
    stack.pop();
    stack.pop();
  }

  // Stack //
  public void pop() {
    final int opcode = 0x57;
    code.writeByte(opcode);

    stack.pop();
  }

  public void dup() {
    final int opcode = 0x59;
    code.writeByte(opcode);

    stack.push(stack.peek());
    if (stack.size() > maxStackSize) maxStackSize = stack.size();
  }

  public void swap() {
    final int opcode = 0x5f;
    code.writeByte(opcode);

    String oldTop = stack.pop();
    String newTop = stack.pop();
    stack.push(oldTop);
    stack.push(newTop);
  }

  // Math //
  public void iadd() {
    final int opcode = 0x60;
    code.writeByte(opcode);

    stack.pop();
  }

  public void isub() {
    final int opcode = 0x64;
    code.writeByte(opcode);

    stack.pop();
  }

  public void imul() {
    final int opcode = 0x68;
    code.writeByte(opcode);

    stack.pop();
  }

  public void idiv() {
    final int opcode = 0x6c;
    code.writeByte(opcode);

    stack.pop();
  }

  public void irem() {
    final int opcode = 0x70;
    code.writeByte(opcode);

    stack.pop();
  }

  public void ineg() {
    final int opcode = 0x74;
    code.writeByte(opcode);
  }

  public void ishl() {
    final int opcode = 0x78;
    code.writeByte(opcode);

    stack.pop();
  }

  public void ishr() {
    final int opcode = 0x7a;
    code.writeByte(opcode);

    stack.pop();
  }

  public void iinc(int idx, int c) {
    final int opcode = 0x84;
    code.writeByte(opcode);
  }

  // Comparison //
  public void ifeq(String label) {
    labels.put(code.size(), label);

    final int opcode = 0x99;
    code.writeByte(opcode);
    code.writeShort(branchPlaceholder); 

    stack.pop();
  }

  public void ifne(String label) {
    labels.put(code.size(), label);

    final int opcode = 0x9a;
    code.writeByte(opcode);
    code.writeShort(branchPlaceholder); 

    stack.pop();
  }

  public void iflt(String label) {
    labels.put(code.size(), label);

    final int opcode = 0x9b;
    code.writeByte(opcode);
    code.writeShort(branchPlaceholder); 

    stack.pop();
  }

  public void ifge(String label) {
    labels.put(code.size(), label);

    final int opcode = 0x9c;
    code.writeByte(opcode);
    code.writeShort(branchPlaceholder); 

    stack.pop();
  }

  public void ifgt(String label) {
    labels.put(code.size(), label);

    final int opcode = 0x9d;
    code.writeByte(opcode);
    code.writeShort(branchPlaceholder); 

    stack.pop();
  }

  public void ifle(String label) {
    labels.put(code.size(), label);

    final int opcode = 0x9e;
    code.writeByte(opcode);
    code.writeShort(branchPlaceholder); 

    stack.pop();
  }

  public void if_icmpeq(String label) {
    labels.put(code.size(), label);

    final int opcode = 0x9f;
    code.writeByte(opcode);
    code.writeShort(branchPlaceholder); 

    stack.pop();
    stack.pop();
  }

  public void if_icmpne(String label) {
    labels.put(code.size(), label);

    final int opcode = 0xa0;
    code.writeByte(opcode);
    code.writeShort(branchPlaceholder); 

    stack.pop();
    stack.pop();
  }

  public void if_icmplt(String label) {
    labels.put(code.size(), label);

    final int opcode = 0xa1;
    code.writeByte(opcode);
    code.writeShort(branchPlaceholder); 

    stack.pop();
    stack.pop();
  }

  public void if_icmpge(String label) {
    labels.put(code.size(), label);

    final int opcode = 0xa2;
    code.writeByte(opcode);
    code.writeShort(branchPlaceholder); 

    stack.pop();
    stack.pop();
  }

  public void if_icmpgt(String label) {
    labels.put(code.size(), label);

    final int opcode = 0xa3;
    code.writeByte(opcode);
    code.writeShort(branchPlaceholder); 

    stack.pop();
    stack.pop();
  }

  public void if_icmple(String label) {
    labels.put(code.size(), label);

    final int opcode = 0xa4;
    code.writeByte(opcode);
    code.writeShort(branchPlaceholder); 

    stack.pop();
    stack.pop();
  }

  public void if_acmpeq(String label) {
    labels.put(code.size(), label);

    final int opcode = 0xa5;
    code.writeByte(opcode);
    code.writeShort(branchPlaceholder); 

    stack.pop();
    stack.pop();
  }

  public void if_acmpne(String label) {
    labels.put(code.size(), label);

    final int opcode = 0xa6;
    code.writeByte(opcode);
    code.writeShort(branchPlaceholder); 

    stack.pop();
    stack.pop();
  }

  // Control //
  public void gotoLabel(String label) {
    labels.put(code.size(), label);

    final int opcode = 0xa7;
    code.writeByte(opcode);
    code.writeShort(branchPlaceholder); 
  }

  public void returnVoid() {
    final int opcode = 0xb1;
    code.writeByte(opcode);

    stack.clear();
  }

  public void ireturn() {
    final int opcode = 0xac;
    code.writeByte(opcode);

    stack.clear();
  }

  public void areturn() {
    final int opcode = 0xb0;
    code.writeByte(opcode);

    stack.clear();
  }

  // References //
  public void getStatic(int idx, String descriptor) {
    final int opcode = 0xb2;
    code.writeByte(opcode);
    code.writeShort(idx);

    stack.push(descriptor);
    if (stack.size() > maxStackSize) maxStackSize = stack.size();
  }

  public void putStatic(int idx) {
    final int opcode = 0xb3;
    code.writeByte(opcode);
    code.writeShort(idx);

    stack.pop();
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

    stack.pop();
    stack.pop();
  }

  public void invokeVirtual(int idx, String descriptor) {
    final int opcode = 0xb6;
    code.writeByte(opcode);
    code.writeShort(idx);

    stack.pop();
    updateStackFor(descriptor);
    if (stack.size() > maxStackSize) maxStackSize = stack.size();
  }

  public void invokeStatic(int idx, String descriptor) {
    final int opcode = 0xb8;
    code.writeByte(opcode);
    code.writeShort(idx);

    updateStackFor(descriptor);
    if (stack.size() > maxStackSize) maxStackSize = stack.size();
  }

  public void anew(int idx, String descriptor) {
    final int opcode = 0xbb;
    code.writeByte(opcode);
    code.writeShort(idx);

    stack.push(descriptor);
    if (stack.size() > maxStackSize) maxStackSize = stack.size();
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
  public void wide(String mnemonic, int idx, String descriptor) {
    int opcode;

    switch (mnemonic) {
      case "iload": 
        opcode = 0x15;
        stack.push(descriptor);
        if (stack.size() > maxStackSize) maxStackSize = stack.size();
        break;
      
      case "aload": 
        opcode = 0x19;
        stack.push(descriptor);
        if (stack.size() > maxStackSize) maxStackSize = stack.size();
        break;

      case "istore": 
        opcode = 0x36;
        stack.pop();
        if (locals.containsKey(idx) && !locals.get(idx).equals(descriptor))
          throw new IllegalArgumentException("The types for the local variable at " + idx + " don't match!");
        
        locals.put(idx, descriptor);
        if (0 > maxLocalsSize) maxLocalsSize = 0;
        break;

      case "astore": 
        opcode = 0x3a;
        stack.pop();
        if (locals.containsKey(idx) && !locals.get(idx).equals(descriptor))
          throw new IllegalArgumentException("The types for the local variable at " + idx + " don't match!");
        
        locals.put(idx, descriptor);
        if (0 > maxLocalsSize) maxLocalsSize = 0;
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
  private void updateStackFor(String descriptor) {
    int idx = 1;
    boolean objectType = false;

    while (descriptor.charAt(idx) != ')') {
      char c = descriptor.charAt(idx);

      if (!objectType) {
        stack.pop();
        objectType = (c == 'L' || c == '[');
      }

      objectType = !(objectType && c == ';');
      idx++;

      if (idx > descriptor.length())
        throw new IllegalStateException("End of parameter list not found in the descriptor!");
    }

    String returnDescriptor = descriptor.substring(idx + 1, descriptor.length());

    if (!returnDescriptor.equals("V")) {
      stack.push(returnDescriptor);
    }
  }

  private void updateLocalsFor(String descriptor, int localsStartIdx) {
    int idx = 1;
    int objectStart = 0;
    boolean objectType = false;

    while (descriptor.charAt(idx) != ')') {
      String paramDescriptor = descriptor.substring(idx, idx + 1);

      if (!objectType) {
        if (!paramDescriptor.equals("L") && !paramDescriptor.equals("[")) {
          locals.put(localsStartIdx++, paramDescriptor);
        }
        else {
          objectStart = idx;
          objectType = true;
        }
      }
      else if (paramDescriptor.equals(";")) {
        paramDescriptor = descriptor.substring(objectStart, idx + 1);
        locals.put(localsStartIdx++, paramDescriptor);
        objectType = false;
      }

      idx++;

      if (idx > descriptor.length())
        throw new IllegalStateException("End of parameter list not found in the descriptor!");
    }

    String returnDescriptor = descriptor.substring(idx + 1, descriptor.length());

    if (!returnDescriptor.equals("V")) {
      stack.push(returnDescriptor);
    }
  }

}
