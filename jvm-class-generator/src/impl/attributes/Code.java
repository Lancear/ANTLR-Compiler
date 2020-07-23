package impl.attributes;

import java.util.Collection;
import java.util.HashMap;
import java.util.Stack;

import impl.helpers.DynamicByteBuffer;
import specs.helpers.Descriptor;
import specs.data_areas.Frame;
import specs.BytecodeStructure;
import specs.InfoStructure;
import specs.attributes.Attribute;
import specs.attributes.StackMapTable;
import specs.class_content.Method;

public class Code extends specs.attributes.Code {

  private final Frame currFrame;
  private final Stack<Frame> frames;

  private final HashMap<String, Integer> labels;
  private final HashMap<Integer, String> jumps;
  private final static short BRANCH_PLACEHOLDER = (short)0xdead;

  private final static int WIDE_OPCODE = 0xc4;
  private final DynamicByteBuffer code;
  private int maxStackSize;
  private int maxLocalsIdx;

  private HashMap<String, Attribute> attributes;

  public Code(InfoStructure parent) {
    super(parent);
    Frame frame = new impl.data_areas.Frame((Method)parent);
    this.currFrame = frame;
    this.maxStackSize = currFrame.stack.size();
    this.maxLocalsIdx = currFrame.locals.size() - 1;
    this.frames = new Stack<>();

    this.labels = new HashMap<>();
    this.jumps = new HashMap<>();

    
    this.code = new DynamicByteBuffer();

    this.attributes = new HashMap<>();
  }


  public byte[] generate() {
    short nameId = (short)parent.constantPool().addUtf8(name);
    byte[] attributes = generateCollection( this.attributes.values() );

    DynamicByteBuffer bytecode = new DynamicByteBuffer();
    bytecode.writeShort(nameId);
    // code + 2B stack size + 2B local size + 4B code size 
    // + 2B exception table size + attributes (inlc. 2B attribute size)
    bytecode.writeInt(code.size() + 10 + attributes.length);
    bytecode.writeShort(maxStackSize);
    bytecode.writeShort(maxLocalsIdx + 1);
    bytecode.writeInt(code.size());
    bytecode.write( backpatch( code.toByteArray() ) );
    bytecode.writeShort(0); // exception table size
    bytecode.write( attributes );

    return bytecode.toByteArray();
  }

  protected byte[] generateCollection(Collection<? extends BytecodeStructure> coll) {
    DynamicByteBuffer bytecode = new DynamicByteBuffer();
    
    bytecode.writeShort( coll.size() );
    for (BytecodeStructure structure : coll) {
      bytecode.write( structure.generate() );
    }
    
    return bytecode.toByteArray();
  }

  public byte[] backpatch(byte[] bytecode) {
    for (int jump : jumps.keySet()) {
      String label = jumps.get(jump);

      if (!labels.containsKey(label)) 
        throw new IllegalStateException("Unresolved label: '" + label + "'");

      int address = labels.get(label) - jump;
      bytecode[jump + 1] = (byte)(address >> 8 & 0xff);
      bytecode[jump + 2] = (byte)(address & 0xff);
    }

    return bytecode;
  }

  public specs.attributes.Code addLabel(String label) {
    labels.put(label, code.size());
    frames.push(new impl.data_areas.Frame(currFrame, code.size()));
    return this;
  }

  public void addStackMapTableAttribute() {
    StackMapTable stackMapTable =  new impl.attributes.StackMapTable(parent);
    stackMapTable.setFrames(frames);
    attributes.put(StackMapTable.name, stackMapTable);
  }


  // Instructions //
  // Constants //
  public specs.attributes.Code iconst_m1() {
    final int opcode = 0x02;
    code.writeByte(opcode);

    currFrame.stack.push(Descriptor.INT);
    if (currFrame.stack.size() > maxStackSize) maxStackSize = currFrame.stack.size();
    return this;
  }

  public specs.attributes.Code iconst_0() {
    final int opcode = 0x03;
    code.writeByte(opcode);

    currFrame.stack.push(Descriptor.INT);
    if (currFrame.stack.size() > maxStackSize) maxStackSize = currFrame.stack.size();
    return this;
  }

  public specs.attributes.Code iconst_1() {
    final int opcode = 0x04;
    code.writeByte(opcode);

    currFrame.stack.push(Descriptor.INT);
    if (currFrame.stack.size() > maxStackSize) maxStackSize = currFrame.stack.size();
    return this;
  }

  public specs.attributes.Code iconst_2() {
    final int opcode = 0x05;
    code.writeByte(opcode);

    currFrame.stack.push(Descriptor.INT);
    if (currFrame.stack.size() > maxStackSize) maxStackSize = currFrame.stack.size();
    return this;
  }

  public specs.attributes.Code iconst_3() {
    final int opcode = 0x06;
    code.writeByte(opcode);

    currFrame.stack.push(Descriptor.INT);
    if (currFrame.stack.size() > maxStackSize) maxStackSize = currFrame.stack.size();
    return this;
  }

  public specs.attributes.Code iconst_4() {
    final int opcode = 0x07;
    code.writeByte(opcode);

    currFrame.stack.push(Descriptor.INT);
    if (currFrame.stack.size() > maxStackSize) maxStackSize = currFrame.stack.size();
    return this;
  }

  public specs.attributes.Code iconst_5() {
    final int opcode = 0x08;
    code.writeByte(opcode);

    currFrame.stack.push(Descriptor.INT);
    if (currFrame.stack.size() > maxStackSize) maxStackSize = currFrame.stack.size();
    return this;
  }

  public specs.attributes.Code bipush(int b) {
    final int opcode = 0x10;
    code.writeByte(opcode);
    code.writeByte(b);

    currFrame.stack.push(Descriptor.INT);
    if (currFrame.stack.size() > maxStackSize) maxStackSize = currFrame.stack.size();
    return this;
  }

  public specs.attributes.Code sipush(int s) {
    final int opcode = 0x11;
    code.writeByte(opcode);
    code.writeShort(s);

    currFrame.stack.push(Descriptor.INT);
    if (currFrame.stack.size() > maxStackSize) maxStackSize = currFrame.stack.size();
    return this;
  }

  public specs.attributes.Code ldc(int id) {
    final int opcode = 0x12;
    code.writeByte(opcode);
    code.writeByte(id);

    currFrame.stack.push( parent.constantPool().findDescriptorByIndex(id) );
    if (currFrame.stack.size() > maxStackSize) maxStackSize = currFrame.stack.size();
    return this;
  }

  public specs.attributes.Code ldc_w(int id) {
    final int opcode = 0x13;
    code.writeByte(opcode);
    code.writeShort(id);

    currFrame.stack.push( parent.constantPool().findDescriptorByIndex(id) );
    if (currFrame.stack.size() > maxStackSize) maxStackSize = currFrame.stack.size();
    return this;
  }

  // Loads //
  public specs.attributes.Code iload(int idx, boolean wide) {
    final int opcode = 0x15;

    if (!wide) {
      code.writeByte(opcode);
      code.writeByte(idx);
    }
    else {
      code.writeByte(WIDE_OPCODE);
      code.writeByte(opcode);
      code.writeShort(idx);
    }

    currFrame.stack.push( currFrame.locals.get(idx) );
    if (currFrame.stack.size() > maxStackSize) maxStackSize = currFrame.stack.size();
    return this;
  }

  public specs.attributes.Code aload(int idx, boolean wide) {
    final int opcode = 0x19;

    if (!wide) {
      code.writeByte(opcode);
      code.writeByte(idx);
    }
    else {
      code.writeByte(WIDE_OPCODE);
      code.writeByte(opcode);
      code.writeShort(idx);
    }

    currFrame.stack.push( currFrame.locals.get(idx) );
    if (currFrame.stack.size() > maxStackSize) maxStackSize = currFrame.stack.size();
    return this;
  }

  public specs.attributes.Code iload_0() {
    final int opcode = 0x1a;
    code.writeByte(opcode);

    currFrame.stack.push( currFrame.locals.get(0) );
    if (currFrame.stack.size() > maxStackSize) maxStackSize = currFrame.stack.size();
    return this;
  }

  public specs.attributes.Code iload_1() {
    final int opcode = 0x1b;
    code.writeByte(opcode);

    currFrame.stack.push( currFrame.locals.get(1) );
    if (currFrame.stack.size() > maxStackSize) maxStackSize = currFrame.stack.size();
    return this;
  }

  public specs.attributes.Code iload_2() {
    final int opcode = 0x1c;
    code.writeByte(opcode);

    currFrame.stack.push( currFrame.locals.get(2) );
    if (currFrame.stack.size() > maxStackSize) maxStackSize = currFrame.stack.size();
    return this;
  }

  public specs.attributes.Code iload_3() {
    final int opcode = 0x1d;
    code.writeByte(opcode);

    currFrame.stack.push( currFrame.locals.get(3) );
    if (currFrame.stack.size() > maxStackSize) maxStackSize = currFrame.stack.size();
    return this;
  }

  public specs.attributes.Code aload_0() {
    final int opcode = 0x2a;
    code.writeByte(opcode);

    currFrame.stack.push( currFrame.locals.get(0) );
    if (currFrame.stack.size() > maxStackSize) maxStackSize = currFrame.stack.size();
    return this;
  }

  public specs.attributes.Code aload_1() {
    final int opcode = 0x2b;
    code.writeByte(opcode);

    currFrame.stack.push( currFrame.locals.get(1) );
    if (currFrame.stack.size() > maxStackSize) maxStackSize = currFrame.stack.size();
    return this;
  }

  public specs.attributes.Code aload_2() {
    final int opcode = 0x2c;
    code.writeByte(opcode);

    currFrame.stack.push(currFrame.locals.get(2));
    if (currFrame.stack.size() > maxStackSize) maxStackSize = currFrame.stack.size();
    return this;
  }

  public specs.attributes.Code aload_3() {
    final int opcode = 0x2d;
    code.writeByte(opcode);

    currFrame.stack.push(currFrame.locals.get(3));
    if (currFrame.stack.size() > maxStackSize) maxStackSize = currFrame.stack.size();
    return this;
  }

  public specs.attributes.Code iaload() {
    final int opcode = 0x2e;
    code.writeByte(opcode);

    currFrame.stack.pop();
    return this;
  }

  public specs.attributes.Code aaload() {
    final int opcode = 0x32;
    code.writeByte(opcode);

    currFrame.stack.pop();
    return this;
  }

  public specs.attributes.Code baload() {
    final int opcode = 0x33;
    code.writeByte(opcode);

    currFrame.stack.pop();
    return this;
  }

  // Stores //
  public specs.attributes.Code istore(int idx, boolean wide) {
    final int opcode = 0x36;

    if (!wide) {
      code.writeByte(opcode);
      code.writeByte(idx);
    }
    else {
      code.writeByte(WIDE_OPCODE);
      code.writeByte(opcode);
      code.writeShort(idx);
    }

    currFrame.locals.put(idx, currFrame.stack.pop());
    if (idx > currFrame.locals.size()) maxLocalsIdx = idx;
    return this;
  }

  public specs.attributes.Code astore(int idx, boolean wide) {
    final int opcode = 0x3a;

    if (!wide) {
      code.writeByte(opcode);
      code.writeByte(idx);
    }
    else {
      code.writeByte(WIDE_OPCODE);
      code.writeByte(opcode);
      code.writeShort(idx);
    }

    currFrame.locals.put(idx, currFrame.stack.pop());
    if (idx > currFrame.locals.size()) maxLocalsIdx = idx;
    return this;
  }

  public specs.attributes.Code istore_0() {
    final int opcode = 0x3b;
    code.writeByte(opcode);
    
    currFrame.locals.put(0, currFrame.stack.pop());
    if (0 > maxLocalsIdx) maxLocalsIdx = 0;
    return this;
  }

  public specs.attributes.Code istore_1() {
    final int opcode = 0x3c;
    code.writeByte(opcode);
    
    currFrame.locals.put(1, currFrame.stack.pop());
    if (1 > maxLocalsIdx) maxLocalsIdx = 1;
    return this;
  }

  public specs.attributes.Code istore_2() {
    final int opcode = 0x3d;
    code.writeByte(opcode);
    
    currFrame.locals.put(2, currFrame.stack.pop());
    if (2 > maxLocalsIdx) maxLocalsIdx = 2;
    return this;
  }

  public specs.attributes.Code istore_3() {
    final int opcode = 0x3e;
    code.writeByte(opcode);
    
    currFrame.locals.put(3, currFrame.stack.pop());
    if (3 > maxLocalsIdx) maxLocalsIdx = 3;
    return this;
  }

  public specs.attributes.Code astore_0() {
    final int opcode = 0x4b;
    code.writeByte(opcode);
    
    currFrame.locals.put(0, currFrame.stack.pop());
    if (0 > maxLocalsIdx) maxLocalsIdx = 0;
    return this;
  }

  public specs.attributes.Code astore_1() {
    final int opcode = 0x4c;
    code.writeByte(opcode);
    
    currFrame.locals.put(1, currFrame.stack.pop());
    if (1 > maxLocalsIdx) maxLocalsIdx = 1;
    return this;
  }

  public specs.attributes.Code astore_2() {
    final int opcode = 0x4d;
    code.writeByte(opcode);
    
    currFrame.locals.put(2, currFrame.stack.pop());
    if (2 > maxLocalsIdx) maxLocalsIdx = 2;
    return this;
  }

  public specs.attributes.Code astore_3() {
    final int opcode = 0x4e;
    code.writeByte(opcode);
    
    currFrame.locals.put(3, currFrame.stack.pop());
    if (3 > maxLocalsIdx) maxLocalsIdx = 3;
    return this;
  }

  public specs.attributes.Code iastore() {
    final int opcode = 0x4f;
    code.writeByte(opcode);

    currFrame.stack.pop();
    currFrame.stack.pop();
    currFrame.stack.pop();
    return this;
  }

  public specs.attributes.Code aastore() {
    final int opcode = 0x53;
    code.writeByte(opcode);

    currFrame.stack.pop();
    currFrame.stack.pop();
    currFrame.stack.pop();
    return this;
  }

  public specs.attributes.Code bastore() {
    final int opcode = 0x54;
    code.writeByte(opcode);

    currFrame.stack.pop();
    currFrame.stack.pop();
    currFrame.stack.pop();
    return this;
  }

  // Stack //
  public specs.attributes.Code pop() {
    final int opcode = 0x57;
    code.writeByte(opcode);

    currFrame.stack.pop();
    return this;
  }

  public specs.attributes.Code dup() {
    final int opcode = 0x59;
    code.writeByte(opcode);

    currFrame.stack.push(currFrame.stack.peek());
    if (currFrame.stack.size() > maxStackSize) maxStackSize = currFrame.stack.size();
    return this;
  }

  public specs.attributes.Code swap() {
    final int opcode = 0x5f;
    code.writeByte(opcode);

    String oldTop = currFrame.stack.pop();
    String newTop = currFrame.stack.pop();
    currFrame.stack.push(oldTop);
    currFrame.stack.push(newTop);
    return this;
  }

  // Math //
  public specs.attributes.Code iadd() {
    final int opcode = 0x60;
    code.writeByte(opcode);

    currFrame.stack.pop();
    return this;
  }

  public specs.attributes.Code isub() {
    final int opcode = 0x64;
    code.writeByte(opcode);

    currFrame.stack.pop();
    return this;
  }

  public specs.attributes.Code imul() {
    final int opcode = 0x68;
    code.writeByte(opcode);

    currFrame.stack.pop();
    return this;
  }

  public specs.attributes.Code idiv() {
    final int opcode = 0x6c;
    code.writeByte(opcode);

    currFrame.stack.pop();
    return this;
  }

  public specs.attributes.Code irem() {
    final int opcode = 0x70;
    code.writeByte(opcode);

    currFrame.stack.pop();
    return this;
  }

  public specs.attributes.Code ineg() {
    final int opcode = 0x74;
    code.writeByte(opcode);
    return this;
  }

  public specs.attributes.Code ishl() {
    final int opcode = 0x78;
    code.writeByte(opcode);

    currFrame.stack.pop();
    return this;
  }

  public specs.attributes.Code ishr() {
    final int opcode = 0x7a;
    code.writeByte(opcode);

    currFrame.stack.pop();
    return this;
  }

  public specs.attributes.Code iinc(int idx, int c, boolean wide) {
    final int opcode = 0x84;
    code.writeByte(opcode);

    if (!wide) {
      code.writeByte(opcode);
      code.writeByte(idx);
      code.writeByte(c);
    }
    else {
      code.writeByte(WIDE_OPCODE);
      code.writeByte(opcode);
      code.writeShort(idx);
      code.writeShort(c);
    }

    return this;
  }

  // Comparison //
  public specs.attributes.Code ifeq(String label) {
    jumps.put(code.size(), label);

    final int opcode = 0x99;
    code.writeByte(opcode);
    code.writeShort(BRANCH_PLACEHOLDER); 

    currFrame.stack.pop();
    return this;
  }

  public specs.attributes.Code ifne(String label) {
    jumps.put(code.size(), label);

    final int opcode = 0x9a;
    code.writeByte(opcode);
    code.writeShort(BRANCH_PLACEHOLDER); 

    currFrame.stack.pop();
    return this;
  }

  public specs.attributes.Code iflt(String label) {
    jumps.put(code.size(), label);

    final int opcode = 0x9b;
    code.writeByte(opcode);
    code.writeShort(BRANCH_PLACEHOLDER); 

    currFrame.stack.pop();
    return this;
  }

  public specs.attributes.Code ifge(String label) {
    jumps.put(code.size(), label);

    final int opcode = 0x9c;
    code.writeByte(opcode);
    code.writeShort(BRANCH_PLACEHOLDER); 

    currFrame.stack.pop();
    return this;
  }

  public specs.attributes.Code ifgt(String label) {
    jumps.put(code.size(), label);

    final int opcode = 0x9d;
    code.writeByte(opcode);
    code.writeShort(BRANCH_PLACEHOLDER); 

    currFrame.stack.pop();
    return this;
  }

  public specs.attributes.Code ifle(String label) {
    jumps.put(code.size(), label);

    final int opcode = 0x9e;
    code.writeByte(opcode);
    code.writeShort(BRANCH_PLACEHOLDER); 

    currFrame.stack.pop();
    return this;
  }

  public specs.attributes.Code if_icmpeq(String label) {
    jumps.put(code.size(), label);

    final int opcode = 0x9f;
    code.writeByte(opcode);
    code.writeShort(BRANCH_PLACEHOLDER); 

    currFrame.stack.pop();
    currFrame.stack.pop();
    return this;
  }

  public specs.attributes.Code if_icmpne(String label) {
    jumps.put(code.size(), label);

    final int opcode = 0xa0;
    code.writeByte(opcode);
    code.writeShort(BRANCH_PLACEHOLDER); 

    currFrame.stack.pop();
    currFrame.stack.pop();
    return this;
  }

  public specs.attributes.Code if_icmplt(String label) {
    jumps.put(code.size(), label);

    final int opcode = 0xa1;
    code.writeByte(opcode);
    code.writeShort(BRANCH_PLACEHOLDER); 

    currFrame.stack.pop();
    currFrame.stack.pop();
    return this;
  }

  public specs.attributes.Code if_icmpge(String label) {
    jumps.put(code.size(), label);

    final int opcode = 0xa2;
    code.writeByte(opcode);
    code.writeShort(BRANCH_PLACEHOLDER); 

    currFrame.stack.pop();
    currFrame.stack.pop();
    return this;
  }

  public specs.attributes.Code if_icmpgt(String label) {
    jumps.put(code.size(), label);

    final int opcode = 0xa3;
    code.writeByte(opcode);
    code.writeShort(BRANCH_PLACEHOLDER); 

    currFrame.stack.pop();
    currFrame.stack.pop();
    return this;
  }
  

  public specs.attributes.Code if_icmple(String label) {
    jumps.put(code.size(), label);

    final int opcode = 0xa4;
    code.writeByte(opcode);
    code.writeShort(BRANCH_PLACEHOLDER); 

    currFrame.stack.pop();
    currFrame.stack.pop();
    return this;
  }

  public specs.attributes.Code if_acmpeq(String label) {
    jumps.put(code.size(), label);

    final int opcode = 0xa5;
    code.writeByte(opcode);
    code.writeShort(BRANCH_PLACEHOLDER); 

    currFrame.stack.pop();
    currFrame.stack.pop();
    return this;
  }

  public specs.attributes.Code if_acmpne(String label) {
    jumps.put(code.size(), label);

    final int opcode = 0xa6;
    code.writeByte(opcode);
    code.writeShort(BRANCH_PLACEHOLDER); 

    currFrame.stack.pop();
    currFrame.stack.pop();
    return this;
  }

  // Control //
  public specs.attributes.Code gotoLabel(String label) {
    jumps.put(code.size(), label);

    final int opcode = 0xa7;
    code.writeByte(opcode);
    code.writeShort(BRANCH_PLACEHOLDER); 
    return this;
  }

  public specs.attributes.Code vreturn() {
    final int opcode = 0xb1;
    code.writeByte(opcode);

    currFrame.stack.clear();
    return this;
  }

  public specs.attributes.Code ireturn() {
    final int opcode = 0xac;
    code.writeByte(opcode);

    currFrame.stack.clear();
    return this;
  }

  public specs.attributes.Code areturn() {
    final int opcode = 0xb0;
    code.writeByte(opcode);

    currFrame.stack.clear();
    return this;
  }

  // References //
  public specs.attributes.Code getStatic(int id) {
    final int opcode = 0xb2;
    code.writeByte(opcode);
    code.writeShort(id);

    currFrame.stack.push( parent.constantPool().findDescriptorByIndex(id) );
    if (currFrame.stack.size() > maxStackSize) maxStackSize = currFrame.stack.size();
    return this;
  }

  public specs.attributes.Code putStatic(int id) {
    final int opcode = 0xb3;
    code.writeByte(opcode);
    code.writeShort(id);

    currFrame.stack.pop();
    return this;
  }

  public specs.attributes.Code getField(int id) {
    final int opcode = 0xb4;
    code.writeByte(opcode);
    code.writeShort(id);

    currFrame.stack.pop();
    currFrame.stack.push( parent.constantPool().findDescriptorByIndex(id) );
    return this;
  }

  public specs.attributes.Code putField(int id) {
    final int opcode = 0xb5;
    code.writeByte(opcode);
    code.writeShort(id);

    currFrame.stack.pop();
    currFrame.stack.pop();
    return this;
  }

  public specs.attributes.Code invokeVirtual(int id) {
    final int opcode = 0xb6;
    code.writeByte(opcode);
    code.writeShort(id);
    
    String methodDescriptor = parent.constantPool().findDescriptorByIndex(id);

    for (int counter = 0; counter < Descriptor.METHOD_PARAM_DESCRIPTORS(methodDescriptor).size(); counter++)
      currFrame.stack.pop();

    // objectref
    currFrame.stack.pop();

    String returnDescriptor = Descriptor.METHOD_RETURN_DESCRIPTORS(methodDescriptor);
    if (!returnDescriptor.equals(Descriptor.VOID))
      currFrame.stack.push(returnDescriptor);

    if (currFrame.stack.size() > maxStackSize) maxStackSize = currFrame.stack.size();
    return this;
  }

  public specs.attributes.Code invokeStatic(int id) {
    final int opcode = 0xb8;
    code.writeByte(opcode);
    code.writeShort(id);

    String methodDescriptor = parent.constantPool().findDescriptorByIndex(id);

    currFrame.stack.pop();
    for (int counter = 0; counter < Descriptor.METHOD_PARAM_DESCRIPTORS(methodDescriptor).size(); counter++)
      currFrame.stack.pop();

    String returnDescriptor = Descriptor.METHOD_RETURN_DESCRIPTORS(methodDescriptor);
    if (!returnDescriptor.equals(Descriptor.VOID))
      currFrame.stack.push(returnDescriptor);
    if (currFrame.stack.size() > maxStackSize) maxStackSize = currFrame.stack.size();
    return this;
  }

  public specs.attributes.Code anew(int id) {
    final int opcode = 0xbb;
    code.writeByte(opcode);
    code.writeShort(id);

    currFrame.stack.push( parent.constantPool().findDescriptorByIndex(id) );
    if (currFrame.stack.size() > maxStackSize) maxStackSize = currFrame.stack.size();
    return this;
  }

  public specs.attributes.Code newArray(int aType) {
    final int opcode = 0xbc;
    code.writeByte(opcode);
    code.writeByte(aType);
    return this;
  }

  public specs.attributes.Code anewArray(int idx) {
    final int opcode = 0xbd;
    code.writeByte(opcode);
    code.writeShort(idx);
    return this;
  }

  public specs.attributes.Code arraylength() {
    final int opcode = 0xbe;
    code.writeByte(opcode);
    return this;
  }

}
