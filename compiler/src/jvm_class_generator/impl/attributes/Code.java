package jvm_class_generator.impl.attributes;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Stack;
import java.util.stream.Stream;

import jvm_class_generator.impl.helpers.DynamicByteBuffer;
import jvm_class_generator.specs.helpers.Descriptor;
import jvm_class_generator.specs.data_areas.Frame;
import jvm_class_generator.specs.BytecodeStructure;
import jvm_class_generator.specs.InfoStructure;
import jvm_class_generator.specs.attributes.Attribute;
import jvm_class_generator.specs.attributes.StackMapTable;
import jvm_class_generator.specs.class_content.Method;

public class Code extends jvm_class_generator.specs.attributes.Code {

  public Frame currFrame;
  private final Stack<Frame> frames;

  private final LinkedHashMap<String, Integer> labels;
  private final LinkedHashMap<Integer, String> jumps;
  private final static short BRANCH_PLACEHOLDER = (short)0xdead;

  private final static int WIDE_OPCODE = 0xc4;
  private final DynamicByteBuffer code;
  private int maxStackSize;
  private int maxLocalsIdx;

  private HashMap<String, Attribute> attributes;

  public Code(InfoStructure parent) {
    super(parent);
    Frame frame = new jvm_class_generator.impl.data_areas.Frame((Method)parent);
    this.currFrame = frame;
    this.maxStackSize = currFrame.stack.size();
    this.maxLocalsIdx = currFrame.locals.size() - 1;
    this.frames = new Stack<>();

    this.labels = new LinkedHashMap<>();
    this.jumps = new LinkedHashMap<>();

    
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

  public jvm_class_generator.specs.attributes.Code addLabel(String label) {
    labels.put(label, code.size());
    frames.push(new jvm_class_generator.impl.data_areas.Frame(currFrame, code.size()));
    return this;
  }

  public jvm_class_generator.specs.attributes.Code addLabel(String label, String parentLabel) {
    labels.put(label, code.size());

    if (!labels.containsKey(parentLabel)) 
        throw new IllegalStateException("Unresolved label: '" + parentLabel + "'");

    int parentCodeOffset = labels.get(parentLabel);
    Frame parent = Stream.of(frames.toArray(new Frame[frames.size()])).filter(parentFrame -> parentFrame.codeOffset == parentCodeOffset).findFirst().get();

    currFrame = new jvm_class_generator.impl.data_areas.Frame(parent, code.size());
    frames.push(new jvm_class_generator.impl.data_areas.Frame(parent, code.size()));
    return this;
  }

  public void addStackMapTableAttribute() {
    StackMapTable stackMapTable =  new jvm_class_generator.impl.attributes.StackMapTable(parent);
    stackMapTable.setFrames(frames);
    attributes.put(StackMapTable.name, stackMapTable);
  }


  public int allocLocal(String type) {
    final int idx = currFrame.locals.size();
    currFrame.locals.put(currFrame.locals.size(), type);
    if (idx > maxLocalsIdx) maxLocalsIdx = idx;
    return idx;
  }


  // Instructions //
  // Constants //
  public jvm_class_generator.specs.attributes.Code iconst_m1() {
    final int opcode = 0x02;
    code.writeByte(opcode);

    currFrame.stack.push(Descriptor.INT);
    if (currFrame.stack.size() > maxStackSize) maxStackSize = currFrame.stack.size();
    return this;
  }

  public jvm_class_generator.specs.attributes.Code iconst_0() {
    final int opcode = 0x03;
    code.writeByte(opcode);

    currFrame.stack.push(Descriptor.INT);
    if (currFrame.stack.size() > maxStackSize) maxStackSize = currFrame.stack.size();
    return this;
  }

  public jvm_class_generator.specs.attributes.Code iconst_1() {
    final int opcode = 0x04;
    code.writeByte(opcode);

    currFrame.stack.push(Descriptor.INT);
    if (currFrame.stack.size() > maxStackSize) maxStackSize = currFrame.stack.size();
    return this;
  }

  public jvm_class_generator.specs.attributes.Code iconst_2() {
    final int opcode = 0x05;
    code.writeByte(opcode);

    currFrame.stack.push(Descriptor.INT);
    if (currFrame.stack.size() > maxStackSize) maxStackSize = currFrame.stack.size();
    return this;
  }

  public jvm_class_generator.specs.attributes.Code iconst_3() {
    final int opcode = 0x06;
    code.writeByte(opcode);

    currFrame.stack.push(Descriptor.INT);
    if (currFrame.stack.size() > maxStackSize) maxStackSize = currFrame.stack.size();
    return this;
  }

  public jvm_class_generator.specs.attributes.Code iconst_4() {
    final int opcode = 0x07;
    code.writeByte(opcode);

    currFrame.stack.push(Descriptor.INT);
    if (currFrame.stack.size() > maxStackSize) maxStackSize = currFrame.stack.size();
    return this;
  }

  public jvm_class_generator.specs.attributes.Code iconst_5() {
    final int opcode = 0x08;
    code.writeByte(opcode);

    currFrame.stack.push(Descriptor.INT);
    if (currFrame.stack.size() > maxStackSize) maxStackSize = currFrame.stack.size();
    return this;
  }

  public jvm_class_generator.specs.attributes.Code bipush(int b) {
    final int opcode = 0x10;
    code.writeByte(opcode);
    code.writeByte(b);

    currFrame.stack.push(Descriptor.INT);
    if (currFrame.stack.size() > maxStackSize) maxStackSize = currFrame.stack.size();
    return this;
  }

  public jvm_class_generator.specs.attributes.Code sipush(int s) {
    final int opcode = 0x11;
    code.writeByte(opcode);
    code.writeShort(s);

    currFrame.stack.push(Descriptor.INT);
    if (currFrame.stack.size() > maxStackSize) maxStackSize = currFrame.stack.size();
    return this;
  }

  public jvm_class_generator.specs.attributes.Code ldc(int id) {
    final int opcode = 0x12;
    code.writeByte(opcode);
    code.writeByte(id);

    currFrame.stack.push( parent.constantPool().findDescriptorByIndex(id) );
    if (currFrame.stack.size() > maxStackSize) maxStackSize = currFrame.stack.size();
    return this;
  }

  public jvm_class_generator.specs.attributes.Code ldc_w(int id) {
    final int opcode = 0x13;
    code.writeByte(opcode);
    code.writeShort(id);

    currFrame.stack.push( parent.constantPool().findDescriptorByIndex(id) );
    if (currFrame.stack.size() > maxStackSize) maxStackSize = currFrame.stack.size();
    return this;
  }

  // Loads //
  public jvm_class_generator.specs.attributes.Code iload(int idx, boolean wide) {
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

  public jvm_class_generator.specs.attributes.Code aload(int idx, boolean wide) {
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

  public jvm_class_generator.specs.attributes.Code iload_0() {
    final int opcode = 0x1a;
    code.writeByte(opcode);

    currFrame.stack.push( currFrame.locals.get(0) );
    if (currFrame.stack.size() > maxStackSize) maxStackSize = currFrame.stack.size();
    return this;
  }

  public jvm_class_generator.specs.attributes.Code iload_1() {
    final int opcode = 0x1b;
    code.writeByte(opcode);

    currFrame.stack.push( currFrame.locals.get(1) );
    if (currFrame.stack.size() > maxStackSize) maxStackSize = currFrame.stack.size();
    return this;
  }

  public jvm_class_generator.specs.attributes.Code iload_2() {
    final int opcode = 0x1c;
    code.writeByte(opcode);

    currFrame.stack.push( currFrame.locals.get(2) );
    if (currFrame.stack.size() > maxStackSize) maxStackSize = currFrame.stack.size();
    return this;
  }

  public jvm_class_generator.specs.attributes.Code iload_3() {
    final int opcode = 0x1d;
    code.writeByte(opcode);

    currFrame.stack.push( currFrame.locals.get(3) );
    if (currFrame.stack.size() > maxStackSize) maxStackSize = currFrame.stack.size();
    return this;
  }

  public jvm_class_generator.specs.attributes.Code aload_0() {
    final int opcode = 0x2a;
    code.writeByte(opcode);

    currFrame.stack.push( currFrame.locals.get(0) );
    if (currFrame.stack.size() > maxStackSize) maxStackSize = currFrame.stack.size();
    return this;
  }

  public jvm_class_generator.specs.attributes.Code aload_1() {
    final int opcode = 0x2b;
    code.writeByte(opcode);

    currFrame.stack.push( currFrame.locals.get(1) );
    if (currFrame.stack.size() > maxStackSize) maxStackSize = currFrame.stack.size();
    return this;
  }

  public jvm_class_generator.specs.attributes.Code aload_2() {
    final int opcode = 0x2c;
    code.writeByte(opcode);

    currFrame.stack.push(currFrame.locals.get(2));
    if (currFrame.stack.size() > maxStackSize) maxStackSize = currFrame.stack.size();
    return this;
  }

  public jvm_class_generator.specs.attributes.Code aload_3() {
    final int opcode = 0x2d;
    code.writeByte(opcode);

    currFrame.stack.push(currFrame.locals.get(3));
    if (currFrame.stack.size() > maxStackSize) maxStackSize = currFrame.stack.size();
    return this;
  }

  public jvm_class_generator.specs.attributes.Code iaload() {
    final int opcode = 0x2e;
    code.writeByte(opcode);

    currFrame.stack.pop();
    currFrame.stack.push( Descriptor.ARRAY_BASE_DESCRIPTOR( currFrame.stack.pop() ) );
    return this;
  }

  public jvm_class_generator.specs.attributes.Code aaload() {
    final int opcode = 0x32;
    code.writeByte(opcode);

    currFrame.stack.pop();
    currFrame.stack.push( Descriptor.ARRAY_BASE_DESCRIPTOR( currFrame.stack.pop() ) );
    return this;
  }

  public jvm_class_generator.specs.attributes.Code baload() {
    final int opcode = 0x33;
    code.writeByte(opcode);

    currFrame.stack.pop();
    currFrame.stack.push( Descriptor.ARRAY_BASE_DESCRIPTOR( currFrame.stack.pop() ) );
    return this;
  }

  // Stores //
  public jvm_class_generator.specs.attributes.Code istore(int idx, boolean wide) {
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

    currFrame.stack.pop();
    return this;
  }

  public jvm_class_generator.specs.attributes.Code astore(int idx, boolean wide) {
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

    currFrame.stack.pop();
    return this;
  }

  public jvm_class_generator.specs.attributes.Code istore_0() {
    final int opcode = 0x3b;
    code.writeByte(opcode);
    
    currFrame.stack.pop();
    return this;
  }

  public jvm_class_generator.specs.attributes.Code istore_1() {
    final int opcode = 0x3c;
    code.writeByte(opcode);
    
    currFrame.stack.pop();
    return this;
  }

  public jvm_class_generator.specs.attributes.Code istore_2() {
    final int opcode = 0x3d;
    code.writeByte(opcode);
    
    currFrame.stack.pop();
    return this;
  }

  public jvm_class_generator.specs.attributes.Code istore_3() {
    final int opcode = 0x3e;
    code.writeByte(opcode);
    
    currFrame.stack.pop();
    return this;
  }

  public jvm_class_generator.specs.attributes.Code astore_0() {
    final int opcode = 0x4b;
    code.writeByte(opcode);
    
    currFrame.stack.pop();
    return this;
  }

  public jvm_class_generator.specs.attributes.Code astore_1() {
    final int opcode = 0x4c;
    code.writeByte(opcode);
    
    currFrame.stack.pop();
    return this;
  }

  public jvm_class_generator.specs.attributes.Code astore_2() {
    final int opcode = 0x4d;
    code.writeByte(opcode);
    
    currFrame.stack.pop();
    return this;
  }

  public jvm_class_generator.specs.attributes.Code astore_3() {
    final int opcode = 0x4e;
    code.writeByte(opcode);
    
    currFrame.stack.pop();
    return this;
  }

  public jvm_class_generator.specs.attributes.Code iastore() {
    final int opcode = 0x4f;
    code.writeByte(opcode);

    currFrame.stack.pop();
    currFrame.stack.pop();
    currFrame.stack.pop();
    return this;
  }

  public jvm_class_generator.specs.attributes.Code aastore() {
    final int opcode = 0x53;
    code.writeByte(opcode);

    currFrame.stack.pop();
    currFrame.stack.pop();
    currFrame.stack.pop();
    return this;
  }

  public jvm_class_generator.specs.attributes.Code bastore() {
    final int opcode = 0x54;
    code.writeByte(opcode);

    currFrame.stack.pop();
    currFrame.stack.pop();
    currFrame.stack.pop();
    return this;
  }

  // Stack //
  public jvm_class_generator.specs.attributes.Code pop() {
    final int opcode = 0x57;
    code.writeByte(opcode);

    currFrame.stack.pop();
    return this;
  }

  public jvm_class_generator.specs.attributes.Code dup() {
    final int opcode = 0x59;
    code.writeByte(opcode);

    currFrame.stack.push(currFrame.stack.peek());
    if (currFrame.stack.size() > maxStackSize) maxStackSize = currFrame.stack.size();
    return this;
  }

  public jvm_class_generator.specs.attributes.Code swap() {
    final int opcode = 0x5f;
    code.writeByte(opcode);

    String oldTop = currFrame.stack.pop();
    String newTop = currFrame.stack.pop();
    currFrame.stack.push(oldTop);
    currFrame.stack.push(newTop);
    return this;
  }

  // Math //
  public jvm_class_generator.specs.attributes.Code iadd() {
    final int opcode = 0x60;
    code.writeByte(opcode);

    currFrame.stack.pop();
    return this;
  }

  public jvm_class_generator.specs.attributes.Code isub() {
    final int opcode = 0x64;
    code.writeByte(opcode);

    currFrame.stack.pop();
    return this;
  }

  public jvm_class_generator.specs.attributes.Code imul() {
    final int opcode = 0x68;
    code.writeByte(opcode);

    currFrame.stack.pop();
    return this;
  }

  public jvm_class_generator.specs.attributes.Code idiv() {
    final int opcode = 0x6c;
    code.writeByte(opcode);

    currFrame.stack.pop();
    return this;
  }

  public jvm_class_generator.specs.attributes.Code irem() {
    final int opcode = 0x70;
    code.writeByte(opcode);

    currFrame.stack.pop();
    return this;
  }

  public jvm_class_generator.specs.attributes.Code ineg() {
    final int opcode = 0x74;
    code.writeByte(opcode);
    return this;
  }

  public jvm_class_generator.specs.attributes.Code ishl() {
    final int opcode = 0x78;
    code.writeByte(opcode);

    currFrame.stack.pop();
    return this;
  }

  public jvm_class_generator.specs.attributes.Code ishr() {
    final int opcode = 0x7a;
    code.writeByte(opcode);

    currFrame.stack.pop();
    return this;
  }

  public jvm_class_generator.specs.attributes.Code iinc(int idx, int c, boolean wide) {
    final int opcode = 0x84;

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
  public jvm_class_generator.specs.attributes.Code ifeq(String label) {
    jumps.put(code.size(), label);

    final int opcode = 0x99;
    code.writeByte(opcode);
    code.writeShort(BRANCH_PLACEHOLDER); 

    currFrame.stack.pop();
    return this;
  }

  public jvm_class_generator.specs.attributes.Code ifne(String label) {
    jumps.put(code.size(), label);

    final int opcode = 0x9a;
    code.writeByte(opcode);
    code.writeShort(BRANCH_PLACEHOLDER); 

    currFrame.stack.pop();
    return this;
  }

  public jvm_class_generator.specs.attributes.Code iflt(String label) {
    jumps.put(code.size(), label);

    final int opcode = 0x9b;
    code.writeByte(opcode);
    code.writeShort(BRANCH_PLACEHOLDER); 

    currFrame.stack.pop();
    return this;
  }

  public jvm_class_generator.specs.attributes.Code ifge(String label) {
    jumps.put(code.size(), label);

    final int opcode = 0x9c;
    code.writeByte(opcode);
    code.writeShort(BRANCH_PLACEHOLDER); 

    currFrame.stack.pop();
    return this;
  }

  public jvm_class_generator.specs.attributes.Code ifgt(String label) {
    jumps.put(code.size(), label);

    final int opcode = 0x9d;
    code.writeByte(opcode);
    code.writeShort(BRANCH_PLACEHOLDER); 

    currFrame.stack.pop();
    return this;
  }

  public jvm_class_generator.specs.attributes.Code ifle(String label) {
    jumps.put(code.size(), label);

    final int opcode = 0x9e;
    code.writeByte(opcode);
    code.writeShort(BRANCH_PLACEHOLDER); 

    currFrame.stack.pop();
    return this;
  }

  public jvm_class_generator.specs.attributes.Code if_icmpeq(String label) {
    jumps.put(code.size(), label);

    final int opcode = 0x9f;
    code.writeByte(opcode);
    code.writeShort(BRANCH_PLACEHOLDER); 

    currFrame.stack.pop();
    currFrame.stack.pop();
    return this;
  }

  public jvm_class_generator.specs.attributes.Code if_icmpne(String label) {
    jumps.put(code.size(), label);

    final int opcode = 0xa0;
    code.writeByte(opcode);
    code.writeShort(BRANCH_PLACEHOLDER); 

    currFrame.stack.pop();
    currFrame.stack.pop();
    return this;
  }

  public jvm_class_generator.specs.attributes.Code if_icmplt(String label) {
    jumps.put(code.size(), label);

    final int opcode = 0xa1;
    code.writeByte(opcode);
    code.writeShort(BRANCH_PLACEHOLDER); 

    currFrame.stack.pop();
    currFrame.stack.pop();
    return this;
  }

  public jvm_class_generator.specs.attributes.Code if_icmpge(String label) {
    jumps.put(code.size(), label);

    final int opcode = 0xa2;
    code.writeByte(opcode);
    code.writeShort(BRANCH_PLACEHOLDER); 

    currFrame.stack.pop();
    currFrame.stack.pop();
    return this;
  }

  public jvm_class_generator.specs.attributes.Code if_icmpgt(String label) {
    jumps.put(code.size(), label);

    final int opcode = 0xa3;
    code.writeByte(opcode);
    code.writeShort(BRANCH_PLACEHOLDER); 

    currFrame.stack.pop();
    currFrame.stack.pop();
    return this;
  }
  

  public jvm_class_generator.specs.attributes.Code if_icmple(String label) {
    jumps.put(code.size(), label);

    final int opcode = 0xa4;
    code.writeByte(opcode);
    code.writeShort(BRANCH_PLACEHOLDER); 

    currFrame.stack.pop();
    currFrame.stack.pop();
    return this;
  }

  public jvm_class_generator.specs.attributes.Code if_acmpeq(String label) {
    jumps.put(code.size(), label);

    final int opcode = 0xa5;
    code.writeByte(opcode);
    code.writeShort(BRANCH_PLACEHOLDER); 

    currFrame.stack.pop();
    currFrame.stack.pop();
    return this;
  }

  public jvm_class_generator.specs.attributes.Code if_acmpne(String label) {
    jumps.put(code.size(), label);

    final int opcode = 0xa6;
    code.writeByte(opcode);
    code.writeShort(BRANCH_PLACEHOLDER); 

    currFrame.stack.pop();
    currFrame.stack.pop();
    return this;
  }

  // Control //
  public jvm_class_generator.specs.attributes.Code gotoLabel(String label) {
    jumps.put(code.size(), label);

    final int opcode = 0xa7;
    code.writeByte(opcode);
    code.writeShort(BRANCH_PLACEHOLDER); 

    frames.push(new jvm_class_generator.impl.data_areas.Frame(currFrame, code.size()));
    return this;
  }

  public jvm_class_generator.specs.attributes.Code vreturn() {
    final int opcode = 0xb1;
    code.writeByte(opcode);

    currFrame.stack.clear();
    return this;
  }

  public jvm_class_generator.specs.attributes.Code ireturn() {
    final int opcode = 0xac;
    code.writeByte(opcode);

    currFrame.stack.clear();
    return this;
  }

  public jvm_class_generator.specs.attributes.Code areturn() {
    final int opcode = 0xb0;
    code.writeByte(opcode);

    currFrame.stack.clear();
    return this;
  }

  // References //
  public jvm_class_generator.specs.attributes.Code getStatic(int id) {
    final int opcode = 0xb2;
    code.writeByte(opcode);
    code.writeShort(id);

    currFrame.stack.push( parent.constantPool().findDescriptorByIndex(id) );
    if (currFrame.stack.size() > maxStackSize) maxStackSize = currFrame.stack.size();
    return this;
  }

  public jvm_class_generator.specs.attributes.Code putStatic(int id) {
    final int opcode = 0xb3;
    code.writeByte(opcode);
    code.writeShort(id);

    currFrame.stack.pop();
    return this;
  }

  public jvm_class_generator.specs.attributes.Code getField(int id) {
    final int opcode = 0xb4;
    code.writeByte(opcode);
    code.writeShort(id);

    currFrame.stack.pop();
    currFrame.stack.push( parent.constantPool().findDescriptorByIndex(id) );
    return this;
  }

  public jvm_class_generator.specs.attributes.Code putField(int id) {
    final int opcode = 0xb5;
    code.writeByte(opcode);
    code.writeShort(id);

    currFrame.stack.pop();
    currFrame.stack.pop();
    return this;
  }

  public jvm_class_generator.specs.attributes.Code invokeVirtual(int id) {
    final int opcode = 0xb6;
    code.writeByte(opcode);
    code.writeShort(id);
    
    String methodDescriptor = parent.constantPool().findDescriptorByIndex(id);

    for (int counter = 0; counter < Descriptor.METHOD_PARAM_DESCRIPTORS(methodDescriptor).size(); counter++)
      currFrame.stack.pop();

    // objectref
    currFrame.stack.pop();

    String returnDescriptor = Descriptor.METHOD_RETURN_DESCRIPTOR(methodDescriptor);
    if (!returnDescriptor.equals(Descriptor.VOID))
      currFrame.stack.push(returnDescriptor);

    if (currFrame.stack.size() > maxStackSize) maxStackSize = currFrame.stack.size();
    return this;
  }

  public jvm_class_generator.specs.attributes.Code invokeSpecial(int id) {
    final int opcode = 0xb7;
    code.writeByte(opcode);
    code.writeShort(id);
    
    String methodDescriptor = parent.constantPool().findDescriptorByIndex(id);

    for (int counter = 0; counter < Descriptor.METHOD_PARAM_DESCRIPTORS(methodDescriptor).size(); counter++)
      currFrame.stack.pop();

    // objectref
    currFrame.stack.pop();

    String returnDescriptor = Descriptor.METHOD_RETURN_DESCRIPTOR(methodDescriptor);
    if (!returnDescriptor.equals(Descriptor.VOID))
      currFrame.stack.push(returnDescriptor);

    if (currFrame.stack.size() > maxStackSize) maxStackSize = currFrame.stack.size();
    return this;
  }

  public jvm_class_generator.specs.attributes.Code invokeStatic(int id) {
    final int opcode = 0xb8;
    code.writeByte(opcode);
    code.writeShort(id);

    String methodDescriptor = parent.constantPool().findDescriptorByIndex(id);

    for (int counter = 0; counter < Descriptor.METHOD_PARAM_DESCRIPTORS(methodDescriptor).size(); counter++)
      currFrame.stack.pop();

    String returnDescriptor = Descriptor.METHOD_RETURN_DESCRIPTOR(methodDescriptor);
    if (!returnDescriptor.equals(Descriptor.VOID))
      currFrame.stack.push(returnDescriptor);
    if (currFrame.stack.size() > maxStackSize) maxStackSize = currFrame.stack.size();
    return this;
  }

  public jvm_class_generator.specs.attributes.Code anew(int id) {
    final int opcode = 0xbb;
    code.writeByte(opcode);
    code.writeShort(id);

    currFrame.stack.push( parent.constantPool().findDescriptorByIndex(id) );
    if (currFrame.stack.size() > maxStackSize) maxStackSize = currFrame.stack.size();
    return this;
  }

  public jvm_class_generator.specs.attributes.Code newArray(int aType) {
    final int opcode = 0xbc;
    code.writeByte(opcode);
    code.writeByte(aType);

    currFrame.stack.pop();
    currFrame.stack.push( Descriptor.ARRAY(aType) );
    return this;
  }

  public jvm_class_generator.specs.attributes.Code anewArray(int id) {
    final int opcode = 0xbd;
    code.writeByte(opcode);
    code.writeShort(id);

    currFrame.stack.pop();
    currFrame.stack.push( Descriptor.ARRAY( parent.constantPool().findDescriptorByIndex(id) ) );
    return this;
  }

  public jvm_class_generator.specs.attributes.Code multianewArray(int id, int dims) {
    final int opcode = 0xc5;
    code.writeByte(opcode);
    code.writeShort(id);
    code.writeByte(dims);

    for (int i = 0; i < dims; i++)
      currFrame.stack.pop();
      
    currFrame.stack.push( parent.constantPool().findDescriptorByIndex(id) );
    return this;
  }

  public jvm_class_generator.specs.attributes.Code arraylength() {
    final int opcode = 0xbe;
    code.writeByte(opcode);

    currFrame.stack.pop();
    currFrame.stack.push(Descriptor.INT);
    return this;
  }

}
