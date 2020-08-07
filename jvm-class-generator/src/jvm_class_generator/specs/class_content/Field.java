package jvm_class_generator.specs.class_content;

import java.util.Map;

import jvm_class_generator.specs.JvmClass;
import jvm_class_generator.specs.InfoStructure;
import jvm_class_generator.specs.attributes.Attribute;
import jvm_class_generator.specs.data_areas.ConstantPool;

/**
 * The {@code Field} class represents the structure of a JVM {@code FieldInfo} structure.
 * <br><br>
 * <i>Note: No two fields in one {@code class} file may have the same name and descriptor.</i>
 * 
 * @see 
 * <a href="https://docs.oracle.com/javase/specs/jvms/se14/html/jvms-4.html#jvms-4.5">
 *  The JVM Specs - Fields (Java SE14)
 * </a>
 */
public abstract class Field implements InfoStructure {

  /**
   * A mask of flags used to denote access permissions to and properties of this field.
   * 
   * @see jvm_class_generator.specs.helpers.AccessFlags AccessFlags
   * @see
   * <a href="https://docs.oracle.com/javase/specs/jvms/se14/html/jvms-4.html#jvms-4.5-200-A.1">
   *  The JVM Specs - Field access and property modifiers (Java SE14)
   * </a>
   */
  protected final short accessFlags;

  /**
   * The name of the field defined by this {@code FieldInfo} structure,
   * it is used to generate the {@code nameIndex} entry in the {@code ConstantPool}.
   */
  protected final String name;
  
  /**
   * The name of the field defined by this {@code FieldInfo} structure,
   * it is used to generate the {@code nameIndex} entry in the {@code ConstantPool}.
   */
  public String name() { 
    return this.name; 
  }

  /**
   * The descriptor of the field defined by this {@code FieldInfo} structure,
   * it is used to generate the {@code descriptorIndex} entry in the {@code ConstantPool}.
   */
  protected final String descriptor;

  /**
   * The descriptor of the field defined by this {@code FieldInfo} structure,
   * it is used to generate the {@code descriptorIndex} entry in the {@code ConstantPool}.
   */
  public String descriptor() {
    return this.descriptor;
  }

  /**
   * A collection of {@code FieldInfo} attributes.
   * 
   * @see jvm_class_generator.specs.attributes.Attribute Attribute
   * @see
   * <a href="https://docs.oracle.com/javase/specs/jvms/se14/html/jvms-4.html#jvms-4.7-320">
   *  The JVM Specs - FieldInfo attributes (Java SE14)
   * </a>
   */
  protected final Map<String, Attribute> attributes;

  /** 
   * Creates and adds an attribute to the collection of attributes.
   * 
   * @param name ... the name of the attribute
   * @return the created attribute
   * 
   * @see jvm_class_generator.specs.attributes.Attribute Attribute
   * @see
   * <a href="https://docs.oracle.com/javase/specs/jvms/se14/html/jvms-4.html#jvms-4.7-320">
   *  The JVM Specs - FieldInfo attributes (Java SE14)
   * </a>
   */
  public abstract Attribute addAttribute(String name);

  /**
   * Checks if an attribute with the given {@code name} already exists in the collection of attributes.
   * 
   * @param name ... the name of the attribute
   * @return true if an attribute with the given {@code name} already exists in the collection of attributes
   * 
   * @see jvm_class_generator.specs.attributes.Attribute Attribute
   * @see
   * <a href="https://docs.oracle.com/javase/specs/jvms/se14/html/jvms-4.html#jvms-4.7-320">
   *  The JVM Specs - FieldInfo attributes (Java SE14)
   * </a>
   */
  public abstract boolean hasAttribute(String name);

  /**
   * Gets the attribute with the given {@code name} from the collection of attributes.
   * 
   * @param name ... the name of the attribute
   * @return the attribute from the collection of attributes, or {@code null} if the attribute does not exist
   * 
   * @see jvm_class_generator.specs.attributes.Attribute Attribute
   * @see
   * <a href="https://docs.oracle.com/javase/specs/jvms/se14/html/jvms-4.html#jvms-4.7-320">
   *  The JVM Specs - FieldInfo attributes (Java SE14)
   * </a>
   */
  public abstract Attribute getAttribute(String name);

  /**
   * The {@code class} this {@code Field} belongs to.
   */
  protected final JvmClass clazz;

  /**
   * The {@code constantPool} is a table of structures representing various string constants, 
   * class and interface names, field names, and other constants that are referred to within the 
   * {@code ClassFile} structure and its substructures.
   * 
   * @see jvm_class_generator.specs.ConstantPool ConstantPool
   */
  public ConstantPool constantPool() {
    return clazz.constantPool();
  }


  public Field(
    JvmClass clazz, 
    short accessFlags, 
    String name, 
    String descriptor, 
    Map<String, Attribute> attributes
  ) {
    this.clazz = clazz;
    this.accessFlags = accessFlags;
    this.name = name;
    this.descriptor = descriptor;
    this.attributes = attributes;
  }

}
