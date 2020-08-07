package jvm_class_generator.specs.helpers;

/**
 * All codes used to indicate the type of an array.
 * 
 * @see
 * <a href="https://docs.oracle.com/javase/specs/jvms/se14/html/jvms-6.html#jvms-6.5.newarray.desc-120">
 *  The JVM Specs - Array type codes (Java SE14)
 * </a>
 */
public abstract class ArrayType {
  public static final byte BOOLEAN =  4;
  public static final byte CHAR    =  5;
  public static final byte FLOAT   =  6;
  public static final byte DOUBLE  =  7;
  public static final byte BYTE    =  8;
  public static final byte SHORT   =  9;
  public static final byte INT     = 10;
  public static final byte LONG    = 11;
}
