package jvm_class_generator.specs.data_areas;

import jvm_class_generator.specs.BytecodeStructure;

/**
 * The {@code ConstantPool} is a table of structures representing various string constants, 
 * class and interface names, field names, and other constants that are referred to within the 
 * {@code ClassFile} structure and its substructures.
 * 
 * @see 
 * <a href="https://docs.oracle.com/javase/specs/jvms/se14/html/jvms-4.html#jvms-4.4">
 *  The JVM Specs - Constant pool (Java SE14)
 * </a>
 */
public abstract class ConstantPool implements BytecodeStructure {
  
  /**
   * All 1-byte tags indicating the kind of constant denoted by an entry.
   * 
   * @see 
   * <a href="https://docs.oracle.com/javase/specs/jvms/se14/html/jvms-4.html#jvms-4.4-140">
   *  The JVM Specs - Constant pool tags (Java SE14)
   * </a> 
   */
  public static abstract class Tags {
    public static final byte UTF8                =  1;
    public static final byte INTEGER             =  3;
    public static final byte FLOAT               =  4;
    public static final byte LONG                =  5;
    public static final byte DOUBLE              =  6;
    public static final byte CLASS               =  7;
    public static final byte STRING              =  8;
    public static final byte FIELDREF            =  9;
    public static final byte METHODREF           = 10;
    public static final byte INTERFACE_METHODREF = 11;
    public static final byte NAME_AND_TYPE       = 12;
    public static final byte METHOD_HANDLE       = 15;
    public static final byte METHOD_TYPE         = 16;
    public static final byte DYNAMIC             = 17;
    public static final byte INVOKE_DYNAMIC      = 18;
    public static final byte MODULE              = 19;
    public static final byte PACKAGE             = 20;
  }

  
  /**
   * Adds a {@code ConstUtf8Info} structure to the {@code ConstantPool}.
   * The structure is used to represent constant string values.
   * <br><br>
   * <i>Note: If the entry already exists, the index of the existing entry is returned.</i>
   * 
   * @param value ... the value of this {@code ConstUtf8Info} structure
   * 
   * @return the index of the {@code ConstUtf8Info} structure in the {@code ConstantPool}
   * 
   * @see 
   * <a href="https://docs.oracle.com/javase/specs/jvms/se14/html/jvms-4.html#jvms-4.4.7">
   *  The JVM Specs - Constant pool - Utf8 info structure (Java SE14)
   * </a> 
   */
  public abstract int addUtf8(String value);

  /**
   * Adds a {@code ConstIntegerInfo} structure to the {@code ConstantPool}.
   * The structure is used to represent 4-byte numeric int constants.
   * <br><br>
   * <i>Note: If the entry already exists, the index of the existing entry is returned.</i>
   * 
   * @param value ... the value of this {@code ConstIntegerInfo} structure
   * 
   * @return the index of the {@code ConstIntegerInfo} structure in the {@code ConstantPool}
   * 
   * @see 
   * <a href="https://docs.oracle.com/javase/specs/jvms/se14/html/jvms-4.html#jvms-4.4.4">
   *  The JVM Specs - Constant pool - Integer info structure (Java SE14)
   * </a> 
   */
  public abstract int addInteger(int value);

  /**
   * Adds a {@code ConstClassInfo} structure to the {@code ConstantPool}.
   * The structure is used to represent a class or an interface.
   * <br><br>
   * <i>Note: If the entry already exists, the index of the existing entry is returned.</i>
   * 
   * @param className ... the name of the class, 
   *  <i>a {@code Utf8Index} is automatically resolved or added for this {@code className}</i>
   * 
   * @return the index of the {@code ConstClassInfo} structure in the {@code ConstantPool}
   * 
   * @see 
   * <a href="https://docs.oracle.com/javase/specs/jvms/se14/html/jvms-4.html#jvms-4.4.1">
   *  The JVM Specs - Constant pool - Class info structure (Java SE14)
   * </a> 
   */
  public abstract int addClass(String className);

  /**
   * Adds a {@code ConstStringInfo} structure to the {@code ConstantPool}.
   * The structure is used to represent constant objects of type {@code String}.
   * <br><br>
   * <i>Note: If the entry already exists, the index of the existing entry is returned.</i>
   * 
   * @param value ... the value of this {@code ConstStringInfo} structure, 
   *  <i>a {@code Utf8Index} is automatically resolved or added for this {@code value}</i>
   * 
   * @return the index of the {@code ConstStringInfo} structure in the {@code ConstantPool}
   * 
   * @see 
   * <a href="https://docs.oracle.com/javase/specs/jvms/se14/html/jvms-4.html#jvms-4.4.3">
   *  The JVM Specs - Constant pool - String info structure (Java SE14)
   * </a> 
   */
  public abstract int addString(String value);

  /**
   * Adds a {@code ConstNameAndTypeInfo} structure to the {@code ConstantPool}.
   * The structure is used to represent a field or method, without indicating which class or interface type it belongs to.
   * <br><br>
   * <i>Note: If the entry already exists, the index of the existing entry is returned.</i>
   * 
   * @param name ... the name of this {@code ConstNameAndTypeInfo} structure
   * @param type ... the type of this {@code ConstNameAndTypeInfo} structure
   * 
   * @return the index of the {@code ConstNameAndTypeInfo} structure in the {@code ConstantPool}
   * 
   * @see 
   * <a href="https://docs.oracle.com/javase/specs/jvms/se14/html/jvms-4.html#jvms-4.4.6">
   *  The JVM Specs - Constant pool - NameAndType info structure (Java SE14)
   * </a> 
   */
  public abstract int addNameAndType(String name, String type);

  /**
   * Adds a {@code ConstFieldrefInfo} structure to the {@code ConstantPool}.
   * The structure is used to represent a field reference, which can be used to get the value of that field.
   * <br><br>
   * <i>Note: If the entry already exists, the index of the existing entry is returned.</i>
   * 
   * @param className ... the name of the class, 
   *  <i>a {@code classIndex} is automatically resolved or added for this {@code className}</i>
   * @param fieldName ... the name of the field, 
   *  <i>a {@code nameAndTypeIndex} is automatically resolved or added for this {@code fieldName} and {@code fieldType}</i>
   * @param fieldType ... the type of the field, 
   *  <i>a {@code nameAndTypeIndex} is automatically resolved or added for this {@code fieldName} and {@code fieldType}</i>
   * 
   * @return the index of the {@code ConstFieldrefInfo} structure in the {@code ConstantPool}
   * 
   * @see 
   * <a href="https://docs.oracle.com/javase/specs/jvms/se14/html/jvms-4.html#jvms-4.4.2">
   *  The JVM Specs - Constant pool - Fieldref info structure (Java SE14)
   * </a> 
   */
  public abstract int addFieldref(String className, String fieldName, String fieldType);

  /**
   * Adds a {@code ConstMethodrefInfo} structure to the {@code ConstantPool}.
   * The structure is used to represent a method reference, which can be used to invoke that method.
   * <br><br>
   * <i>Note: If the entry already exists, the index of the existing entry is returned.</i>
   * 
   * @param className ... the name of the class, 
   *  <i>a {@code classIndex} is automatically resolved or added for this {@code className}</i>
   * @param methodName ... the name of the method, 
   *  <i>a {@code nameAndTypeIndex} is automatically resolved or added for this {@code methodName} and {@code methodType}</i>
   * @param methodType ... the type of the method, 
   *  <i>a {@code nameAndTypeIndex} is automatically resolved or added for this {@code methodName} and {@code methodType}</i>
   * 
   * @return the index of the {@code ConstMethodrefInfo} structure in the {@code ConstantPool}
   * 
   * @see 
   * <a href="https://docs.oracle.com/javase/specs/jvms/se14/html/jvms-4.html#jvms-4.4.2">
   *  The JVM Specs - Constant pool - Methodref info structure (Java SE14)
   * </a> 
   */
  public abstract int addMethodref(String className, String methodName, String methodType);


  /**
   * Searches the descriptor of the given {@code index} in the {@code ConstantPool}.
   * 
   * @param index ... the index in the {@code ConstantPool}
   *
   * @return the descriptor of the entry at the given {@code index} of the {@code ConstantPool}
   */
  public abstract String findDescriptorByIndex(int index);


  /**
   * Returns the number of {@code ConstantPool} entries.
   * 
   * @return the number of {@code ConstantPool} entries
   */
  public abstract int size();

}
