package impl.class_content;

import java.util.Collection;
import java.util.HashMap;

import impl.helpers.DynamicByteBuffer;
import specs.BytecodeStructure;
import specs.JvmClass;
import specs.attributes.Attribute;

public class Method extends specs.class_content.Method {

  public Method(JvmClass clazz, String name, String descriptor, int accessFlags) {
    super(clazz, (short)accessFlags, name, descriptor, new HashMap<>());
  }

  public Attribute addAttribute(String name) {
    Attribute attribute = impl.attributes.Attribute.create(name, this);
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
    short nameId = (short)clazz.constantPool().addUtf8(name);
    short descriptorId = (short)clazz.constantPool().addUtf8(descriptor);
    byte[] attributes = generateCollection( this.attributes.values() );

    DynamicByteBuffer bytecode = new DynamicByteBuffer();
    bytecode.writeShort(accessFlags);
    bytecode.writeShort(nameId);
    bytecode.writeShort(descriptorId);
    bytecode.write(attributes);
    
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
