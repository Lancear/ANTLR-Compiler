package jvm_class_generator.specs.helpers;

/**
 * All flag masks used to denote access permissions and properties.
 * 
 * @see
 * <a href="https://docs.oracle.com/javase/specs/jvms/se14/html/jvms-4.html#jvms-4.1-200-E.1">
 *  The JVM Specs - Class access and property modifiers (Java SE14)
 * </a>
 * 
 * @see
 * <a href="https://docs.oracle.com/javase/specs/jvms/se14/html/jvms-4.html#jvms-4.5-200-A.1">
 *  The JVM Specs - Field access and property modifiers (Java SE14)
 * </a>
 * 
 * @see
 * <a href="https://docs.oracle.com/javase/specs/jvms/se14/html/jvms-4.html#jvms-4.6-200-A.1">
 *  The JVM Specs - Method access and property modifiers (Java SE14)
 * </a>
 */
public abstract class AccessFlags {
  public static final short PUBLIC        = 0x0001;
  public static final short PRIVATE       = 0x0002;
  public static final short PROTECTED     = 0x0004;
  public static final short STATIC        = 0x0008;
  public static final short FINAL         = 0x0010;
  public static final short SUPER         = 0x0020;
  public static final short SYNCHRONIZED  = 0x0020;
  public static final short VOLATILE      = 0x0040;
  public static final short BRIDGE        = 0x0040;
  public static final short TRANSIENT     = 0x0080;
  public static final short VARARGS       = 0x0080;
  public static final short NATIVE        = 0x0100;
  public static final short INTERFACE     = 0x0200;
  public static final short ABSTRACT      = 0x0400;
  public static final short STRICT        = 0x0800;
  public static final short SYNTHETIC     = 0x1000;
  public static final short ANNOTATION    = 0x2000;
  public static final short ENUM          = 0x4000;
  public static final short MODULE        = (short)0x8000;
}
