package jvm_class_generator.specs.helpers;

import java.util.ArrayList;

/**
 * All type descriptors, and some handy descriptor functions.
 * 
 * @see
 * <a href="https://docs.oracle.com/javase/specs/jvms/se14/html/jvms-4.html#jvms-4.3.2-200">
 *  The JVM Specs - Field and method descriptors (Java SE14)
 * </a>
 */
public abstract class Descriptor {
  public final static String BYTE = "B";
  public final static String CHAR = "C";
  public final static String DOUBLE = "D";
  public final static String FLOAT = "F";
  public final static String INT = "I";
  public final static String LONG = "J";
  public final static String SHORT = "S";
  public final static String BOOLEAN = "Z";
  public final static String VOID = "V";
  public final static String STRING = REFERENCE( NAME_OF(String.class) );
  public final static String OBJECT = REFERENCE( NAME_OF(Object.class) );
  public final static String MAIN = METHOD(VOID, ARRAY(STRING));

  /**
   * Returns the internal form of the class-name
   */
  public static String NAME_OF(Class<?> clazz) {
    return clazz.getCanonicalName().replaceAll("[.]", "/");
  }

  /**
   * Returns a reference descriptor for the given class-name.
   * @param className ... class-name in internal form, to get the internal form use: {@link #NAME_OF NAME_OF}
   */
  public static String REFERENCE(String className) {
    return "L" + className + ";";
  }

  /**
   * Returns an array descriptor of the given type-descriptor.
   */
  public static String ARRAY(String type) {
    return "[" + type;
  }

  /**
   * Returns an array descriptor of the given arraytype.
   */
  public static String ARRAY(int atype) {
    switch (atype) {
      case ArrayType.BOOLEAN:
        return ARRAY(BOOLEAN);
      
      case ArrayType.CHAR:
        return ARRAY(CHAR);

      case ArrayType.FLOAT:
        return ARRAY(FLOAT);

      case ArrayType.DOUBLE:
        return ARRAY(DOUBLE);

      case ArrayType.BYTE:
        return ARRAY(BYTE);
      
      case ArrayType.SHORT:
        return ARRAY(SHORT);

      case ArrayType.INT:
        return ARRAY(INT);

      case ArrayType.LONG:
        return ARRAY(LONG);
    }

    throw new IllegalArgumentException("Invalid atype, atype must be a vaild ArrayType!");
  }

  /**
   * Returns an array descriptor of the given type-descriptor.
   */
  public static String ARRAY_BASE_DESCRIPTOR(String arrayDescriptor) {
    if (!arrayDescriptor.startsWith("["))
      throw new IllegalArgumentException("The given arrayDescriptor is invalid! No dimensions found!");

    return arrayDescriptor.substring(1);
  }


  /**
   * Returns a method descriptor for the given return and param types.
   */
  public static String METHOD(String returnType, String ...paramTypes) {
    String descriptor = "(";

    for (Object param : paramTypes) {
      descriptor += param;
    }

    return descriptor + ")" + returnType;
  }

  /**
   * Returns a method descriptor for the given return and param types.
   */
  public static String METHOD(String returnType, String paramTypes) {
    return "(" + paramTypes + ")" + returnType;
  }
  
  /**
   * Returns a list with the param descriptors of a method descriptor.
   */
  public static ArrayList<String> METHOD_PARAM_DESCRIPTORS(String methodDescriptor) {
    if (!methodDescriptor.startsWith("(") || !methodDescriptor.contains(")"))
      throw new IllegalArgumentException("The given methodDescriptor is invalid! Invalid parameter list!");
    
    ArrayList<String> paramDescriptors = new ArrayList<>(); 

    boolean objectType = false;
    int descriptorStart = -1;

    int idx = 1;
    while (methodDescriptor.charAt(idx) != ')') {
      if (!objectType && methodDescriptor.charAt(idx) == '[') {
        if (descriptorStart < 0) 
          descriptorStart = idx;
      }
      else if (!objectType && methodDescriptor.charAt(idx) == 'L') {
        objectType = true;
        
        if (descriptorStart < 0) 
          descriptorStart = idx;
      }
      else if (!objectType || objectType && methodDescriptor.charAt(idx) == ';') {
        if (descriptorStart < 0) 
          descriptorStart = idx; 

        paramDescriptors.add(methodDescriptor.substring(descriptorStart, idx + 1));
        descriptorStart = -1;
        objectType = false;
      }

      idx++;
    }

    return paramDescriptors;
  }

  /**
   * Returns the return-value descriptor of a method descriptor.
   */
  public static String METHOD_RETURN_DESCRIPTOR(String methodDescriptor) {
    if (!methodDescriptor.startsWith("(") || !methodDescriptor.contains(")"))
      throw new IllegalArgumentException("The given methodDescriptor is invalid! Invalid parameter list!");

    return methodDescriptor.substring( methodDescriptor.indexOf(")") + 1 );
  }

}
