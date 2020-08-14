package jvm_class_generator.specs.attributes;

import java.util.Stack;

import jvm_class_generator.specs.InfoStructure;
import jvm_class_generator.specs.class_content.Method;
import jvm_class_generator.specs.data_areas.Frame;

/**
 * The {@code StackMapTable} class represents the structure of the JVM {@code StackMapTable} attribute.
 * 
 * @see 
 * <a href="https://docs.oracle.com/javase/specs/jvms/se14/html/jvms-4.html#jvms-4.7.4">
 *  The JVM Specs - Attributes - StackMapTable (Java SE14)
 * </a>
 */
public abstract class StackMapTable extends Attribute {
  
  /**
   * The name of the attribute.
   * 
   * @see 
   * <a href="https://docs.oracle.com/javase/specs/jvms/se14/html/jvms-4.html#jvms-4.7">
   *  The JVM Specs - Attributes (Java SE14)
   * </a>
   */
  public final static String name = "StackMapTable";

  /**
   * Sets the {@code frames} to use for generating the StackMapTable.
   * @param frames ... the frame stack of the {@code Code} attribute
   * 
   * @see jvm_class_generator.specs.data_areas.Frame Frame
   */
  public abstract void setFrames(Stack<Frame> frames);

  public StackMapTable(InfoStructure parent) {
    super(parent);

    if (!(parent instanceof Method))
      throw new IllegalArgumentException("The StackMapTable attribute can only be added to the code attribute of methods!");
  }

}
