package jvm_class_generator.specs.data_areas;

import java.util.Map;
import java.util.Stack;

import jvm_class_generator.specs.class_content.Method;

/**
 * The {@code Frame} class represents the structure of a JVM Stack {@code Frame}.
 * 
 * @see 
 * <a href="https://docs.oracle.com/javase/specs/jvms/se14/html/jvms-2.html#jvms-2.6">
 *  The JVM Specs - Frames (Java SE14)
 * </a>
 * 
 * @see 
 * <a href="https://docs.oracle.com/javase/specs/jvms/se14/html/jvms-4.html#jvms-4.7.4-610">
 *  The JVM Specs - Frame structure (Java SE14)
 * </a>
 */
public abstract class Frame {

  // Frame Types with a constant value //
  public final static int SAME_LOCALS_1_STACK_ITEM_EXTENDED = 247;
  public final static int SAME_FRAME_EXTENDED = 251;
  public final static int FULL_FRAME = 255;

  /**
   * All 1-byte tags indicating the type of a single variable.
   * 
   * @see 
   * <a href="https://docs.oracle.com/javase/specs/jvms/se14/html/jvms-4.html#jvms-4.7.4-510">
   *  The JVM Specs - Verification types (Java SE14)
   * </a>
   */
  public final static class VerificationTypeInfo {
    public final static byte TOP                = 0;
    public final static byte INTEGER            = 1;
    public final static byte FLOAT              = 2;
    public final static byte DOUBLE             = 3;
    public final static byte LONG               = 4;
    public final static byte NULL               = 5;
    public final static byte UNINITIALIZED_THIS = 6;
    public final static byte OBJECT             = 7;
    public final static byte UNINITIALIZED      = 8;
  }

  /**
   * The code offset of this frame.
   */
  public final int codeOffset;

  /**
   * A map containing the descriptors of all local variables in this frame.
   */
  public final Map<Integer, String> locals;

  /**
   * A stack containing the descriptors of all stack operands in this frame.
   */
  public final Stack<String> stack;

  /**
   * Generates bytecode for the {@code FullFrame} structure.
   * @param offsetDelta ... the offset delta between this and the previous frame
   * @return the bytecode for the {@code FullFrame} structure
   * 
   * @see
   * <a href="https://docs.oracle.com/javase/specs/jvms/se14/html/jvms-4.html#jvms-4.7.4-620-H.1">
   *  The JVM Specs - Full frame structure (Java SE14)
   * </a>
   */
  public abstract byte[] generateFullFrame(int offsetDelta);

  /**
   * The parent method of this attribute.
   * 
   * @see jvm_class_generator.specs.class_content.Method Method
   */
  protected final Method method;

  /**
   * The parent method of this attribute.
   * 
   * @see jvm_class_generator.specs.class_content.Method Method
   */
  public Method method() {
    return this.method;
  }


  public Frame(Method method, int codeOffset, Map<Integer, String> localVariables, Stack<String> operandStack) {
    this.method = method;
    this.codeOffset = codeOffset;
    this.locals = localVariables;
    this.stack = operandStack;
  }
  
}
