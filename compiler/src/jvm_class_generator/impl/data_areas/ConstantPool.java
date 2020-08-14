package jvm_class_generator.impl.data_areas;

import java.util.HashMap;
import java.util.Map;

import jvm_class_generator.impl.helpers.DynamicByteBuffer;
import jvm_class_generator.specs.helpers.Descriptor;

public class ConstantPool extends jvm_class_generator.specs.data_areas.ConstantPool {
  
  private final Map<String, Integer> idLookup;
  private final Map<Integer, String> valueLookup;
  private final DynamicByteBuffer bytecode;

  
  public ConstantPool() {
    this.idLookup = new HashMap<>();
    this.valueLookup = new HashMap<>();
    this.bytecode = new DynamicByteBuffer();
  }


  public int addUtf8(String utf8) {
    String entry = "<Utf8>" + utf8;

    if (idLookup.containsKey(entry))
      return idLookup.get(entry);

    int id = getNextId();
    addToLookups(entry, id);

    bytecode.writeByte(Tags.UTF8);
    bytecode.writeUTF(utf8);
    return id;
  }

  public int addInteger(int value) {
    String entry = "<Integer>" + value;

    if (idLookup.containsKey(entry))
      return idLookup.get(entry);

    int id = getNextId();
    addToLookups(entry, id);

    bytecode.writeByte(Tags.INTEGER);
    bytecode.writeInt(value);
    return id;
  }

  public int addClass(String className) {
    String entry = "<Class>" + className;

    if (idLookup.containsKey(entry))
      return idLookup.get(entry);

    int nameId = addUtf8(className);
    int id = getNextId();
    addToLookups(entry, id);

    bytecode.writeByte(Tags.CLASS);
    bytecode.writeShort(nameId);
    return id;
  }

  public int addString(String value) {
    String entry = "<String>" + value;

    if (idLookup.containsKey(entry))
      return idLookup.get(entry);

    int valueId = addUtf8(value);
    int id = getNextId();
    addToLookups(entry, id);

    bytecode.writeByte(Tags.STRING);
    bytecode.writeShort(valueId);
    return id;
  }

  public int addNameAndType(String name, String type) {
    String entry = "<NameAndType>" + name + ":" + type;

    if (idLookup.containsKey(entry))
      return idLookup.get(entry);

    int nameId = addUtf8(name);
    int typeId = addUtf8(type);
    int id = getNextId();
    addToLookups(entry, id);

    bytecode.writeByte(Tags.NAME_AND_TYPE);
    bytecode.writeShort(nameId);
    bytecode.writeShort(typeId);
    return id;
  }

  public int addFieldref(String className, String fieldName, String fieldType) {
    String entry = "<Fieldref>" + className + "." + fieldName + ":" + fieldType;
    
    if (idLookup.containsKey(entry))
      return idLookup.get(entry);

    int classId = addClass(className);
    int nameAndTypeId = addNameAndType(fieldName, fieldType);
    int id = getNextId();
    addToLookups(entry, id);

    bytecode.writeByte(Tags.FIELDREF);
    bytecode.writeShort(classId);
    bytecode.writeShort(nameAndTypeId);
    return id;
  }

  public int addMethodref(String className, String methodName, String methodType) {
    String entry = "<Methodref>" + className + "." + methodName + ":" + methodType;
    
    if (idLookup.containsKey(entry))
      return idLookup.get(entry);

    int classId = addClass(className);
    int nameAndTypeId = addNameAndType(methodName, methodType);
    int id = getNextId();
    addToLookups(entry, id);

    bytecode.writeByte(Tags.METHODREF);
    bytecode.writeShort(classId);
    bytecode.writeShort(nameAndTypeId);
    return id;
  }
  

  public String findDescriptorByIndex(int index) {
    String entry = valueLookup.get(index);

    if (entry.startsWith("<String>")) {
      return Descriptor.STRING;
    }
    else if (entry.startsWith("<Integer>")) {
      return Descriptor.INT;
    }
    else if (entry.startsWith("<Class>")) {
      return (entry.contains("[")) 
        ? entry.replace("<Class>", "")
        : Descriptor.REFERENCE( entry.replace("<Class>", "") );
    }
    else if (entry.startsWith("<NameAndType>") || entry.startsWith("<Fieldref>") || entry.startsWith("<Methodref>")) {
      return entry.substring( entry.indexOf(":") + 1 );
    }

    throw new IllegalArgumentException("The given argument has no descriptor! Valid entry types are: String, Integer, Class, Fieldref, Methodref, and NameAndType!");
  }


  public int size() {
    return idLookup.size();
  }

  public byte[] generate() {
    return bytecode.toByteArray();
  }


  protected void addToLookups(String entry, int id) {
    idLookup.put(entry, id);
    valueLookup.put(id, entry);
  }

  protected int getNextId() throws IllegalStateException {
    if (idLookup.size() + 1 >= 0xffff)
      throw new IllegalStateException("Constant Pool overflow, max Constant Pool size reached!");

    return idLookup.size() + 1;
  }

}
