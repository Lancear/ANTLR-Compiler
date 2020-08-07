package jvm_class_generator.specs.attributes;

import jvm_class_generator.specs.BytecodeStructure;
import jvm_class_generator.specs.InfoStructure;

/**
 * The {@code Attribute} class represents the baseclass of all JVM {@code Attributes}.
 * 
 * @see 
 * <a href="https://docs.oracle.com/javase/specs/jvms/se14/html/jvms-4.html#jvms-4.7">
 *  The JVM Specs - Attributes (Java SE14)
 * </a>
 */
public abstract class Attribute implements BytecodeStructure {
  
  /**
   * The name of the attribute.
   * 
   * @see 
   * <a href="https://docs.oracle.com/javase/specs/jvms/se14/html/jvms-4.html#jvms-4.7">
   *  The JVM Specs - Attributes (Java SE14)
   * </a>
   */
  public static String name;

  /**
   * The parent (class / method / field) of this attribute.
   * <br><br>
   * <i>Note: For nested attributes, the parent is the parent of the outer attribute.</i>
   * 
   * @see jvm_class_generator.specs.InfoStructure InfoStructure
   */
  protected final InfoStructure parent;

  public Attribute(InfoStructure parent) {
    this.parent = parent;
  }

}
