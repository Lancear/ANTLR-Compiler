package jvm_class_generator.specs.class_content;

import java.util.Map;

import jvm_class_generator.specs.InfoStructure;
import jvm_class_generator.specs.JvmClass;
import jvm_class_generator.specs.attributes.Attribute;
import jvm_class_generator.specs.data_areas.ConstantPool;

/**
 * The {@code Method} class represents the structure of a JVM {@code MethodInfo} structure.
 * <br><br>
 * <i>Note: No two methods in one {@code class} file may have the same name and descriptor.</i>
 * 
 * @see 
 * <a href="https://docs.oracle.com/javase/specs/jvms/se14/html/jvms-4.html#jvms-4.6">
 *  The JVM Specs - Methods (Java SE14)
 * </a>
 */
public abstract class Method implements InfoStructure {

  /**
   * A mask of flags used to denote access permissions to and properties of this method.
   * 
   * @see jvm_class_generator.specs.helpers.AccessFlags AccessFlags
   * @see
   * <a href="https://docs.oracle.com/javase/specs/jvms/se14/html/jvms-4.html#jvms-4.6-200-A.1">
   *  The JVM Specs - Method access and property modifiers (Java SE14)
   * </a>
   */
  protected final short accessFlags;

  /**
   * A mask of flags used to denote access permissions to and properties of this method.
   * 
   * @see jvm_class_generator.specs.helpers.AccessFlags AccessFlags
   * @see
   * <a href="https://docs.oracle.com/javase/specs/jvms/se14/html/jvms-4.html#jvms-4.6-200-A.1">
   *  The JVM Specs - Method access and property modifiers (Java SE14)
   * </a>
   */
  public short accessFlags() {
    return this.accessFlags;
  }

  /**
   * The name of the method defined by this {@code MethodInfo} structure,
   * it is used to generate the {@code nameIndex} entry in the {@code ConstantPool}.
   */
  protected final String name;
  
  /**
   * The name of the method defined by this {@code MethodInfo} structure,
   * it is used to generate the {@code nameIndex} entry in the {@code ConstantPool}.
   */
  public String name() {
    return this.name;
  }

  /**
   * The descriptor of the method defined by this {@code MethodInfo} structure,
   * it is used to generate the {@code descriptorIndex} entry in the {@code ConstantPool}.
   */
  protected final String descriptor;

  /**
   * The descriptor of the method defined by this {@code MethodInfo} structure,
   * it is used to generate the {@code descriptorIndex} entry in the {@code ConstantPool}.
   */
  public String descriptor() {
    return this.descriptor;
  }

  /**
   * A collection of {@code MethodInfo} attributes.
   * 
   * @see jvm_class_generator.specs.attributes.Attribute Attribute
   * @see
   * <a href="https://docs.oracle.com/javase/specs/jvms/se14/html/jvms-4.html#jvms-4.7-320">
   *  The JVM Specs - MethodInfo attributes (Java SE14)
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
   *  The JVM Specs - MethodInfo attributes (Java SE14)
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
   *  The JVM Specs - MethodInfo attributes (Java SE14)
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
   *  The JVM Specs - MethodInfo attributes (Java SE14)
   * </a>
   */
  public abstract Attribute getAttribute(String name);

  /**
   * The class this method belongs to.
   */
  protected final JvmClass clazz;

  /**
   * The class this method belongs to.
   */
  public JvmClass clazz() {
    return this.clazz;
  }

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


  public Method(
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
