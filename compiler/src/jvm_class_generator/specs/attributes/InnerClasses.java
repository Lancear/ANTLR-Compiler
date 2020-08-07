package jvm_class_generator.specs.attributes;

import jvm_class_generator.specs.InfoStructure;
import jvm_class_generator.specs.JvmClass;

/**
 * The {@code InnerClasses} class represents the structure of the JVM {@code InnerClasses} attribute.
 * 
 * @see 
 * <a href="https://docs.oracle.com/javase/specs/jvms/se14/html/jvms-4.html#jvms-4.7.6">
 *  The JVM Specs - Attributes - InnerClasses (Java SE14)
 * </a>
 */
public abstract class InnerClasses extends Attribute {

  /**
   * The name of the attribute.
   * 
   * @see 
   * <a href="https://docs.oracle.com/javase/specs/jvms/se14/html/jvms-4.html#jvms-4.7">
   *  The JVM Specs - Attributes (Java SE14)
   * </a>
   */
  public final static String name = "InnerClasses";

  /**
   * Adds an inner class to the inner classes attribute.
   * @param innerClass ... the inner class
   * @param fileName ... the {@code class} file name of the inner class
   */
  public abstract InnerClasses add(JvmClass innerClass, String fileName);

  public InnerClasses(InfoStructure parent) {
    super(parent);

    if (!(parent instanceof JvmClass))
      throw new IllegalArgumentException("The InnerClass attribute can only be added to classes!");
  }
  
}
