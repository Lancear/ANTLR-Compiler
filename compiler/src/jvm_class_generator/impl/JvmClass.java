package jvm_class_generator.impl;

import java.util.HashSet;

import jvm_class_generator.impl.helpers.DynamicByteBuffer;
import jvm_class_generator.specs.BytecodeStructure;
import jvm_class_generator.specs.attributes.Attribute;
import jvm_class_generator.specs.class_content.Field;
import jvm_class_generator.specs.class_content.Method;

import java.util.Collection;
import java.util.HashMap;

public class JvmClass extends jvm_class_generator.specs.JvmClass {

  public JvmClass(String className, String superName, int accessFlags) {
    super(
      (short)accessFlags,
      className,
      superName,
      new jvm_class_generator.impl.data_areas.ConstantPool(),
      new HashSet<>(),
      new HashMap<>(),
      new HashMap<>(),
      new HashMap<>()
    );

    majorVersion = 58;
    minorVersion = 0;
  }

  public Field addField(String name, String descriptor, int accessFlags) {
    Field field = new jvm_class_generator.impl.class_content.Field(this, name, descriptor, accessFlags);
    fields.put(name + ":" + descriptor, field);
    return field;
  }

  public boolean hasField(String name, String descriptor) {
    return fields.containsKey(name + ":" + descriptor);
  }

  public Field getField(String name, String descriptor) {
    return fields.get(name + ":" + descriptor);
  }

  public Method addMethod(String name, String descriptor, int accessFlags) {
    Method method = new jvm_class_generator.impl.class_content.Method(this, name, descriptor, accessFlags);
    methods.put(name + ":" + descriptor, method);
    return method;
  }

  public boolean hasMethod(String name, String descriptor) {
    return methods.containsKey(name + ":" + descriptor);
  }

  public Method getMethod(String name, String descriptor) {
    return methods.get(name + ":" + descriptor);
  }

  public Attribute addAttribute(String name) {
    Attribute attribute = jvm_class_generator.impl.attributes.Attribute.create(name, this);
    attributes.put(name, attribute);
    return attribute;
  }

  public boolean hasAttribute(String name) {
    return attributes.containsKey(name);
  }

  public Attribute getAttribute(String name) {
    return attributes.get(name);
  }

  public byte[] generate() {
    short thisClass = (short)constantPool.addClass(this.className);
    short superClass = (short)constantPool.addClass(this.superName);
    byte[] interfaces = generateInterfaces();
    byte[] fields = generateCollection( this.fields.values() );
    byte[] methods = generateCollection( this.methods.values() );
    byte[] attributes = generateCollection( this.attributes.values() );

    DynamicByteBuffer bytecode = new DynamicByteBuffer();
    bytecode.writeInt(MAGIC_NUMBER);
    bytecode.writeShort(minorVersion);
    bytecode.writeShort(majorVersion);

    // the constant_pool_count item is equal to the number of entries in the constant_pool table plus one
    bytecode.writeShort( constantPool.size() + 1 );
    bytecode.write( constantPool.generate() );

    bytecode.writeShort(accessFlags);
    bytecode.writeShort(thisClass);
    bytecode.writeShort(superClass);

    bytecode.write(interfaces);
    bytecode.write(fields);
    bytecode.write(methods);
    bytecode.write(attributes);

    return bytecode.toByteArray();
  }

  protected byte[] generateInterfaces() {
    DynamicByteBuffer bytecode = new DynamicByteBuffer();
    
    bytecode.writeShort( interfaces.size() );
    for (String interfaze : interfaces) {
      bytecode.writeShort( constantPool.addClass(interfaze) );
    }

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

}
