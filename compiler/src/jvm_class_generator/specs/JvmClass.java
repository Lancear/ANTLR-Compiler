package jvm_class_generator.specs;

import java.util.Collection;
import java.util.Map;


import jvm_class_generator.specs.attributes.Attribute;
import jvm_class_generator.specs.class_content.Field;
import jvm_class_generator.specs.class_content.Method;
import jvm_class_generator.specs.data_areas.ConstantPool;
import jvm_class_generator.specs.helpers.Descriptor;

/**
 * The {@code JvmClass} class represents the structure of a JVM {@code class} file.
 * Each {@code class} file contains the definition of a single class, interface, or module.
 * 
 * @see 
 * <a href="https://docs.oracle.com/javase/specs/jvms/se14/html/jvms-4.html">
 *  The JVM Specs - Class file format (Java SE14)
 * </a>
 */
public abstract class JvmClass implements InfoStructure {
  
  /**
   * The magic number identifying the {@code class} file format.
   */
  public final static int MAGIC_NUMBER = 0xcafebabe;

  /**
   * The minor and major version numbers of this {@code class} file.
   * 
   * @see 
   * <a href="https://docs.oracle.com/javase/specs/jvms/se14/html/jvms-4.html#jvms-4.1-200-B.2">
   *  The JVM Specs - Class file versions (Java SE14)
   * </a>
   */
  public short minorVersion, majorVersion;

  /**
   * The {@code constantPool} is a table of structures representing various string constants, 
   * class and interface names, field names, and other constants that are referred to within the 
   * {@code ClassFile} structure and its substructures.
   * 
   * @see jvm_class_generator.specs.ConstantPool ConstantPool
   */
  protected final ConstantPool constantPool;

  /**
   * The {@code constantPool} is a table of structures representing various string constants, 
   * class and interface names, field names, and other constants that are referred to within the 
   * {@code ClassFile} structure and its substructures.
   * 
   * @see jvm_class_generator.specs.ConstantPool ConstantPool
   */
  public ConstantPool constantPool() {
    return this.constantPool;
  }

  /**
   * A mask of flags used to denote access permissions to and properties of this class or interface.
   * 
   * @see jvm_class_generator.specs.helpers.AccessFlags AccessFlags
   * @see
   * <a href="https://docs.oracle.com/javase/specs/jvms/se14/html/jvms-4.html#jvms-4.1-200-E.1">
   *  The JVM Specs - Class access and property modifiers (Java SE14)
   * </a>
   */
  protected final short accessFlags;

  /**
   * A mask of flags used to denote access permissions to and properties of this class or interface.
   * 
   * @see jvm_class_generator.specs.helpers.AccessFlags AccessFlags
   * @see
   * <a href="https://docs.oracle.com/javase/specs/jvms/se14/html/jvms-4.html#jvms-4.1-200-E.1">
   *  The JVM Specs - Class access and property modifiers (Java SE14)
   * </a>
   */
  public short accessFlags() {
    return this.accessFlags;
  }

  /**
   * The name of the class or interface defined by this {@code class} file,
   * it is used to generate the {@code thisClass} entry in the {@code ConstantPool}.
   */
  protected final String className;

  /**
   * The name of the class or interface defined by this {@code class} file,
   * it is used to generate the {@code thisClass} entry in the {@code ConstantPool}.
   */
  public String name() {
    return this.className;
  }

  /**
   * Returns the descriptor of this class or interface, used to access this class or interface from another class or interface.
   */
  public String descriptor() {
    return Descriptor.REFERENCE(this.className);
  }

  /**
   * The name of the direct superclass of this {@code class} file,
   * it is used to generate the {@code superClass} entry in the {@code ConstantPool}.
   * <br><br>
   * <i>Note: for an Interface, this must be {@code Object}.</i>
   */
  protected final String superName;

  /**
   * A collection of interfaces in which each interface represent a direct superinterface of this class or interface type, 
   * in the left-to-right order given in the source for the type.
   * 
   * @see jvm_class_generator.specs.Interface Interface
   */
  public final Collection<String> interfaces;
  
  /**
   * Adds an interface to the collection of direct superinterfaces of this class or interface type.
   * <br><br>
   * <i>Note: Should be added in the left-to-right order given in the source for the type.</i>
   * 
   * @param interfaceName ... the name of the interface
   */
  public void addInterface(String interfaceName) {
    interfaces.add(interfaceName);
  }

  /**
   * Checks if the given interface exists in the collection of direct superinterfaces of this class or interface type.
   * @param interfaceName ... the name of the interface
   * @return true if the given interface exists in the collection of direct superinterfaces of this class or interface type
   */
  public boolean hasInterface(String interfaceName) {
    return interfaces.contains(interfaceName);
  }


  /**
   * A collection of fields in which each entry gives a complete description of a field in this class or interface.
   * <br><br>
   * <i>
   *  Note: The {@code fields} table only contains fields that are declared in this class or interface, 
   *  inherited fields are not included.
   * </i>
   * <br><br>
   * <i>Note: No two fields in one {@code class} file may have the same name and descriptor.</i>
   * 
   * @see jvm_class_generator.specs.Field Field
   */
  public final Map<String, Field> fields;

  /** 
   * Creates and adds a field to the collection of fields.
   * 
   * @param accessFlags ... the accessFlags for the field
   * @param name ... the name of the field
   * @param descriptor ... the descriptor/type of the field
   * @return the created field
   * 
   * @see jvm_class_generator.specs.class_content.Field Field
   * @see jvm_class_generator.specs.helpers.AccessFlags AccessFlags
   * @see
   * <a href="https://docs.oracle.com/javase/specs/jvms/se14/html/jvms-4.html#jvms-4.5-200-A.1">
   *  The JVM Specs - Field access and property modifiers (Java SE14)
   * </a>
   */
  public abstract Field addField(String name, String descriptor, int accessFlags);

  /**
   * Checks if a field with the given {@code name} and {@code descriptor} already exists in the collection of fields.
   * 
   * @param name ... the name of the field
   * @param descriptor ... the descriptor/type of the field
   * @return true if a field with the given {@code name} and {@code descriptor} already exists in the collection of fields
   * 
   * @see jvm_class_generator.specs.class_content.Field Field
   */
  public abstract boolean hasField(String name, String descriptor);

  /**
   * Gets the field with the given {@code name} and {@code descriptor} from the collection of fields.
   * 
   * @param name ... the name of the field
   * @param descriptor ... the descriptor/type of the field
   * @return the field from the collection of fields, or {@code null} if the field does not exist
   * 
   * @see jvm_class_generator.specs.class_content.Field Field
   */
  public abstract Field getField(String name, String descriptor);

  /**
   * A collection of methods in which each entry gives a complete description of a method in this class or interface.
   * <br><br>
   * <i>
   *  Note: The {@code methods} table only contains methods that are declared in this class or interface 
   *  (including initialization methods), inherited methods are not included.
   * </i>
   * <br><br>
   * <i>Note: No two methods in one {@code class} file may have the same name and descriptor.</i>
   * 
   * @see jvm_class_generator.specs.Method Method
   */
  public final Map<String, Method> methods;

  /** 
   * Creates and adds a method to the collection of methods.
   * 
   * @param accessFlags ... the accessFlags for the method
   * @param name ... the name of the method
   * @param descriptor ... the descriptor/type of the method
   * @return the created method
   * 
   * @see jvm_class_generator.specs.class_content.Method Method
   * @see jvm_class_generator.specs.helpers.AccessFlags AccessFlags
   * @see
   * <a href="https://docs.oracle.com/javase/specs/jvms/se14/html/jvms-4.html#jvms-4.6-200-A.1">
   *  The JVM Specs - Method access and property modifiers (Java SE14)
   * </a>
   */
  public abstract Method addMethod(String name, String descriptor, int accessFlags);

  /**
   * Checks if a method with the given {@code name} and {@code descriptor} already exists in the collection of methods.
   * 
   * @param name ... the name of the method
   * @param descriptor ... the descriptor/type of the method
   * @return true if a method with the given {@code name} and {@code descriptor} already exists in the collection of methods
   * 
   * @see jvm_class_generator.specs.class_content.Method Method
   */
  public abstract boolean hasMethod(String name, String descriptor);

  /**
   * Gets the method with the given {@code name} and {@code descriptor} from the collection of methods.
   * 
   * @param name ... the name of the method
   * @param descriptor ... the descriptor/type of the method
   * @return the method from the collection of methods, or {@code null} if the method does not exist
   * 
   * @see jvm_class_generator.specs.class_content.Method Method
   */
  public abstract Method getMethod(String name, String descriptor);

  /**
   * A collection of {@code ClassFile} attributes.
   * 
   * @see jvm_class_generator.specs.attributes.Attribute Attribute
   * @see
   * <a href="https://docs.oracle.com/javase/specs/jvms/se14/html/jvms-4.html#jvms-4.7-320">
   *  The JVM Specs - Class file attributes (Java SE14)
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
   *  The JVM Specs - Class file attributes (Java SE14)
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
   *  The JVM Specs - Class file attributes (Java SE14)
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
   *  The JVM Specs - Class file attributes (Java SE14)
   * </a>
   */
  public abstract Attribute getAttribute(String name);
  
  public JvmClass(
    short accessFlags,
    String className,
    String superName,
    ConstantPool constantPool, 
    Collection<String> interfaces, 
    Map<String, Field> fields, 
    Map<String, Method> methods, 
    Map<String, Attribute> attributes
  ) {
    this.accessFlags = accessFlags;
    this.className = className;
    this.superName = superName;
    this.constantPool = constantPool;
    this.interfaces = interfaces;
    this.fields = fields;
    this.methods = methods;
    this.attributes = attributes;
  }

}
