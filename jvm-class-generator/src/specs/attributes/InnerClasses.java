package specs.attributes;

import specs.InfoStructure;
import specs.JvmClass;

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

  public InnerClasses(InfoStructure parent) {
    super(parent);

    if (!(parent instanceof JvmClass))
      throw new IllegalArgumentException("The InnerClass attribute can only be added to classes!");
  }
  
}
