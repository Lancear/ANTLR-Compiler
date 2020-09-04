package jvm_class_generator.specs.attributes;

import jvm_class_generator.specs.InfoStructure;
import jvm_class_generator.specs.class_content.Method;
import jvm_class_generator.specs.data_areas.ConstantPool;

/**
 * The {@code Code} class represents the structure of the JVM {@code Code} attribute.
 * This Attribute holds the instructions of a class method.
 * 
 * @see 
 * <a href="https://docs.oracle.com/javase/specs/jvms/se14/html/jvms-4.html#jvms-4.7.3">
 *  The JVM Specs - Attributes - Code (Java SE14)
 * </a>
 * 
 * @see 
 * <a href="https://docs.oracle.com/javase/specs/jvms/se14/html/jvms-6.html">
 *  The JVM Specs - Instruction Set (Java SE14)
 * </a>
 */
public abstract class Code extends Attribute {

  /**
   * The name of the attribute.
   * 
   * @see 
   * <a href="https://docs.oracle.com/javase/specs/jvms/se14/html/jvms-4.html#jvms-4.7">
   *  The JVM Specs - Attributes (Java SE14)
   * </a>
   */
  public final static String name = "Code";

  /**
   * The {@code constantPool} is a table of structures representing various string constants, 
   * class and interface names, field names, and other constants that are referred to within the 
   * {@code ClassFile} structure and its substructures.
   * 
   * @see jvm_class_generator.specs.ConstantPool ConstantPool
   */
  public ConstantPool constantPool() {
    return parent.constantPool();
  }

  /**
   * Adds a label at the current position in the code.
   * This label can be used to jump to.
   * 
   * @param label ... the name of the label / jump location, <i>must be unique</i>
   */
  public abstract Code addLabel(String label);

  /**
   * Adds a label at the current position in the code.
   * This label can be used to jump to.
   * The {@code Frame} is reset to the {@code Frame} of the parentLabel, 
   * this allows for constructing {@code StackMapTable}s with conditional branches which leave values on the stack
   * 
   * @param label ... the name of the label / jump location, <i>must be unique</i>
   * @param parentLabel ... the name of the parent label to reset the frame to, <i>must be resolved</i>
   * 
   * @see jvm_class_generator.specs.attributes.StackMapTable StackMapTable
   */
  public abstract Code addLabel(String label, String parentLabel);

  /**
   * Adds the {@code StackMapTable} attribute to this {@code Code} attribute.
   * 
   * @see jvm_class_generator.specs.attributes.StackMapTable StackMapTable
   */
  public abstract void addStackMapTableAttribute();


  /**
   * Allocates a local for the code.
   * @param type ... the type of the local
   * @return the index of the local
   */
  public abstract int allocLocal(String type);


  public Code(InfoStructure parent) {
    super(parent);

    if (!(parent instanceof Method))
      throw new IllegalArgumentException("The Code attribute can only be added to methods!");
  }



  // --------------- ---------- ----- --- Instructions --- ----- ---------- --------------- //

  /**
   * Pushes the constant integer -1 onto the {@code operand stack}.
   */
  public abstract Code iconst_m1();

  /**
   * Pushes the constant integer 0 onto the {@code operand stack}.
   */
  public abstract Code iconst_0();

  /**
   * Pushes the constant integer 1 onto the {@code operand stack}.
   */
  public abstract Code iconst_1();

  /**
   * Pushes the constant integer 2 onto the {@code operand stack}.
   */
  public abstract Code iconst_2();

  /**
   * Pushes the constant integer 3 onto the {@code operand stack}.
   */
  public abstract Code iconst_3();

  /**
   * Pushes the constant integer 4 onto the {@code operand stack}.
   */
  public abstract Code iconst_4();

  /**
   * Pushes the constant integer 5 onto the {@code operand stack}.
   */
  public abstract Code iconst_5();

  /**
   * Pushes the constant byte {@code b} onto the {@code operand stack}.
   * @param b ... {@code byte} the constant to be added
   */
  public abstract Code bipush(int b);

  /**
   * Pushes the constant short {@code s} onto the {@code operand stack}.
   * @param b ... {@code short} the constant to be added
   */
  public abstract Code sipush(int s);

  /**
   * Loads a constant from the constant pool onto the {@code operand stack}.
   * @param index ... {@code u1} the index of the constant in the constant pool
   * @return
   */
  public abstract Code ldc(int index);

  /**
   * Loads a constant from the constant pool onto the {@code operand stack}.
   * @param index ... {@code u2} the index of the constant in the constant pool
   * @return
   */
  public abstract Code ldc_w(int index);



  /**
   * Loads the integer stored at {@code index} in the {@code local variables} onto the {@code operand stack}.
   * @param index ... {@code u1} the index of the local variable
   * @param wide ... if true the {@code index} is extended by an extra byte, {@code u2}
   */
  public abstract Code iload(int index, boolean wide);

  /**
   * Loads the reference stored at {@code index} in the {@code local variables} onto the {@code operand stack}.
   * @param index ... {@code u1} the index of the local variable
   * @param wide ... if true the {@code index} is extended by an extra byte, {@code u2}
   */
  public abstract Code aload(int index, boolean wide);

  /**
   * Loads the integer stored at index 0 in the {@code local variables} onto the {@code operand stack}.
   */
  public abstract Code iload_0();

  /**
   * Loads the integer stored at index 1 in the {@code local variables} onto the {@code operand stack}.
   */
  public abstract Code iload_1();

  /**
   * Loads the integer stored at index 2 in the {@code local variables} onto the {@code operand stack}.
   */
  public abstract Code iload_2();

  /**
   * Loads the integer stored at index 3 in the {@code local variables} onto the {@code operand stack}.
   */
  public abstract Code iload_3();

  /**
   * Loads the reference stored at index 0 in the {@code local variables} onto the {@code operand stack}.
   */
  public abstract Code aload_0();

  /**
   * Loads the reference stored at index 1 in the {@code local variables} onto the {@code operand stack}.
   */
  public abstract Code aload_1();

  /**
   * Loads the reference stored at index 2 in the {@code local variables} onto the {@code operand stack}.
   */
  public abstract Code aload_2();

  /**
   * Loads the reference stored at index 3 in the {@code local variables} onto the {@code operand stack}.
   */
  public abstract Code aload_3();

  /**
   * Loads an integer from an array.
   * <br><br>
   * <i>{@code Operand Stack}: arrayref, index -> value</i>
   */
  public abstract Code iaload();

  /**
   * Loads a reference from an array.
   * <br><br>
   * <i>{@code Operand Stack}: arrayref, index -> value</i>
   */
  public abstract Code aaload();

  /**
   * Loads a byte / boolean from an array.
   * <br><br>
   * <i>{@code Operand Stack}: arrayref, index -> value</i>
   */
  public abstract Code baload();



  /**
   * Stores the integer at the top of the {@code operand stack} to the {@code local variables} at {@code index}.
   * @param index ... {@code u1} the index of the local variable
   * @param wide ... if true the {@code index} is extended by an extra byte, {@code u2}
   */
  public abstract Code istore(int index, boolean wide);

  /**
   * Stores the reference at the top of the {@code operand stack} to the {@code local variables} at {@code index}.
   * @param index ... {@code u1} the index of the local variable
   * @param wide ... if true the {@code index} is extended by an extra byte, {@code u2}
   */
  public abstract Code astore(int index, boolean wide);

  /**
   * Stores the integer at the top of the {@code operand stack} to the {@code local variables} at index 0.
   */
  public abstract Code istore_0();

  /**
   * Stores the integer at the top of the {@code operand stack} to the {@code local variables} at index 1.
   */
  public abstract Code istore_1();

  /**
   * Stores the integer at the top of the {@code operand stack} to the {@code local variables} at index 2.
   */
  public abstract Code istore_2();

  /**
   * Stores the integer at the top of the {@code operand stack} to the {@code local variables} at index 3.
   */
  public abstract Code istore_3();

  /**
   * Stores the reference at the top of the {@code operand stack} to the {@code local variables} at index 0.
   */
  public abstract Code astore_0();

  /**
   * Stores the reference at the top of the {@code operand stack} to the {@code local variables} at index 1.
   */
  public abstract Code astore_1();

  /**
   * Stores the reference at the top of the {@code operand stack} to the {@code local variables} at index 2.
   */
  public abstract Code astore_2();

  /**
   * Stores the reference at the top of the {@code operand stack} to the {@code local variables} at index 3.
   */
  public abstract Code astore_3();

  /**
   * Stores an integer to an array.
   * <br><br>
   * <i>{@code Operand Stack}: arrayref, index, value -> ...</i>
   */
  public abstract Code iastore();

  /**
   * Stores a reference to an array.
   * <br><br>
   * <i>{@code Operand Stack}: arrayref, index, value -> ...</i>
   */
  public abstract Code aastore();

  /**
   * Stores a byte / boolean to an array.
   * <br><br>
   * <i>{@code Operand Stack}: arrayref, index, value -> ...</i>
   */
  public abstract Code bastore();



  /**
   * Pops the value at the top of the {@code operand stack}.
   */
  public abstract Code pop();

  /**
   * Duplicates the value at the top of the {@code operand stack}.
   */
  public abstract Code dup();

  /**
   * Swaps the top 2 values of the {@code operand stack}. 
   */
  public abstract Code swap();

  

  /**
   * Adds integers.
   * <br><br>
   * <i>{@code Operand Stack}: value1, value2 -> result</i>
   */
  public abstract Code iadd();

  /**
   * Subtracts integers.
   * <br><br>
   * <i>{@code Operand Stack}: value1, value2 -> result</i>
   */
  public abstract Code isub();

  /**
   * Multiplies integers.
   * <br><br>
   * <i>{@code Operand Stack}: value1, value2 -> result</i>
   */
  public abstract Code imul();

  /**
   * Divides integers.
   * <br><br>
   * <i>{@code Operand Stack}: value1, value2 -> result</i>
   */
  public abstract Code idiv();

  /**
   * Calculates the remainder of an integer division.
   * <br><br>
   * <i>{@code Operand Stack}: value1, value2 -> result</i>
   */
  public abstract Code irem();

  /**
   * Negates the integer at the top of the {@code operand stack}.
   */
  public abstract Code ineg();

  /**
   * Shifts integer left.
   * <br><br>
   * <i>{@code Operand Stack}: value1, value2 -> result</i>
   */
  public abstract Code ishl();

  /**
   * Shifts integer right.
   * <br><br>
   * <i>{@code Operand Stack}: value1, value2 -> result</i>
   */
  public abstract Code ishr();

  /**
   * Increments the integer at {@code index} in the {@code local variables} by {@code c}.
   * @param index ... {@code u1} the index of the local variable
   * @param c ... {@code u1} the constant value, by which the variable should be incremented
   * @param wide ... if true the {@code index} and {@code c} are extended by an extra byte, {@code u2}
   */
  public abstract Code iinc(int index, int c, boolean wide);



  /**
   * Branches if the integer at the top of the {@code operand stack} == 0.
   * <br><br>
   * <i>{@code Operand Stack}: value -> ...</i>
   * 
   * @param label ... the location to jump to
   */
  public abstract Code ifeq(String label);

  /**
   * Branches if the integer at the top of the {@code operand stack} != 0.
   * <br><br>
   * <i>{@code Operand Stack}: value -> ...</i>
   * 
   * @param label ... the location to jump to
   */
  public abstract Code ifne(String label);

  /**
   * Branches if the integer at the top of the {@code operand stack} < 0.
   * <br><br>
   * <i>{@code Operand Stack}: value -> ...</i>
   * 
   * @param label ... the location to jump to
   */
  public abstract Code iflt(String label);

  /**
   * Branches if the integer at the top of the {@code operand stack} >= 0.
   * <br><br>
   * <i>{@code Operand Stack}: value -> ...</i>
   * 
   * @param label ... the location to jump to
   */
  public abstract Code ifge(String label);

  /**
   * Branches if the integer at the top of the {@code operand stack} > 0.
   * <br><br>
   * <i>{@code Operand Stack}: value -> ...</i>
   * 
   * @param label ... the location to jump to
   */
  public abstract Code ifgt(String label);

  /**
   * Branches if the integer at the top of the {@code operand stack} <= 0.
   * <br><br>
   * <i>{@code Operand Stack}: value -> ...</i>
   * 
   * @param label ... the location to jump to
   */
  public abstract Code ifle(String label);

  /**
   * Branches if the integers {@code value1} == {@code value2}.
   * <br><br>
   * <i>{@code Operand Stack}: value1, value2 -> ...</i>
   * 
   * @param label ... the location to jump to
   */
  public abstract Code if_icmpeq(String label);

  /**
   * Branches if the integers {@code value1} != {@code value2}.
   * <br><br>
   * <i>{@code Operand Stack}: value1, value2 -> ...</i>
   * 
   * @param label ... the location to jump to
   */
  public abstract Code if_icmpne(String label);

  /**
   * Branches if the integers {@code value1} < {@code value2}.
   * <br><br>
   * <i>{@code Operand Stack}: value1, value2 -> ...</i>
   * 
   * @param label ... the location to jump to
   */
  public abstract Code if_icmplt(String label);

  /**
   * Branches if the integers {@code value1} >= {@code value2}.
   * <br><br>
   * <i>{@code Operand Stack}: value1, value2 -> ...</i>
   * 
   * @param label ... the location to jump to
   */
  public abstract Code if_icmpge(String label);

  /**
   * Branches if the integers {@code value1} < {@code value2}.
   * <br><br>
   * <i>{@code Operand Stack}: value1, value2 -> ...</i>
   * 
   * @param label ... the location to jump to
   */
  public abstract Code if_icmpgt(String label);

  /**
   * Branches if the integers {@code value1} >= {@code value2}.
   * <br><br>
   * <i>{@code Operand Stack}: value1, value2 -> ...</i>
   * 
   * @param label ... the location to jump to
   */
  public abstract Code if_icmple(String label);

  /**
   * Branches if the references {@code value1} == {@code value2}.
   * <br><br>
   * <i>{@code Operand Stack}: value1, value2 -> ...</i>
   * 
   * @param label ... the location to jump to
   */
  public abstract Code if_acmpeq(String label);

  /**
   * Branches if the references {@code value1} != {@code value2}.
   * <br><br>
   * <i>{@code Operand Stack}: value1, value2 -> ...</i>
   * 
   * @param label ... the location to jump to
   */
  public abstract Code if_acmpne(String label);

  

  /**
   * Jumps to the given {@code label}.
   * @param label ... the location to jump to
   */
  public abstract Code gotoLabel(String label);

  /**
   * Returns from the method.
   */
  public abstract Code vreturn();

  /**
   * Returns from the method with the integer at the top of the {@code operand stack} as the return value.
   */
  public abstract Code ireturn();

  /**
   * Returns from the method with the reference at the top of the {@code operand stack} as the return value.
   */
  public abstract Code areturn();
  


  /**
   * Fetches the value of a static field from a class, and pushes it onto the {@code operand stack}.
   * @param index ... {@code u2} the index of the {@code ConstFieldrefInfo}
   */
  public abstract Code getStatic(int index);

  /**
   * Stores the value at the top of the {@code operand stack} into static field of a class.
   * @param index ... {@code u2} the index of the {@code ConstFieldrefInfo}
   */
  public abstract Code putStatic(int index);

  /**
   * Fetsches the value of a field from {@code objectref}, and pushes it onto the {@code operand stack}.
   * <br><br>
   * <i>{@code Operand Stack}: objectref -> value</i>
   * 
   * @param index ... {@code u2} the index of the {@code ConstFieldrefInfo}
   */
  public abstract Code getField(int index);

  /**
   * Stors the {@code value} into field of a {@code objectref}.
   * <br><br>
   * <i>{@code Operand Stack}: objectref, value -> ...</i>
   * 
   * @param index ... {@code u2} the index of the {@code ConstFieldrefInfo}
   */
  public abstract Code putField(int index);

  /**
   * Invokes an instance (object) method of {@code objectref}.
   * <br><br>
   * <i>{@code Operand Stack}: objectref, [...args] -> [returnValue]</i>
   * 
   * @param index ... {@code u2} the index of the {@code ConstMethodrefInfo}
   */
  public abstract Code invokeVirtual(int index);

  /**
   * Invoke instance method; direct invocation of instance initialization methods and methods of the current class and its supertypes.
   * <br><br>
   * <i>{@code Operand Stack}: objectref, [...args] -> [returnValue]</i>
   * 
   * @param index ... {@code u2} the index of the {@code ConstMethodrefInfo}
   */
  public abstract Code invokeSpecial(int index);

  /**
   * Invokes a class (static) method.
   * <br><br>
   * <i>{@code Operand Stack}: [...args] -> [returnValue]</i>
   * 
   * @param index ... {@code u2} the index of the {@code ConstMethodrefInfo}
   */
  public abstract Code invokeStatic(int index);

  /**
   * Creates a new object of the given {@code ConstClass} type, and pushes it onto the {@code operand stack}.
   * @param index ... {@code u2} the index of the {@code ConstClassInfo}
   */
  public abstract Code anew(int index);

  /**
   * Creates a new array of the given {@code aType} with the given {@code count}, and pushes it onto the {@code operand stack}.
   * <br><br>
   * <i>{@code Operand Stack}: count -> arrayref</i>
   * 
   * @param aType ... {@code u1} the type of the array
   * 
   * @see jvm_class_generator.specs.helpers.ArrayType ArrayType
   */
  public abstract Code newArray(int aType);

  /**
   * Creates a new array of references with the given {@code count}, and pushes it onto the {@code operand stack}.
   * <br><br>
   * <i>{@code Operand Stack}: count -> arrayref</i>
   * 
   * @param index ... {@code u2} the index of the {@code ConstClassInfo}
   */
  public abstract Code anewArray(int index);

  /**
   * Creates a new multidimensional array of references with the given {@code dimensionSizes}, and pushes it onto the {@code operand stack}.
   * <br><br>
   * <i>{@code Operand Stack}: ...dimensionSizes -> arrayref</i>
   * 
   * @param index ... {@code u2} the index of the {@code ConstClassInfo}, must be an array class type of dimensionality greater than or equal to dimensions
   * @param dimensions ... {@code u1} the number of dimensions
   */
  public abstract Code multianewArray(int index, int dimensions);

  /**
   * Gets the length of the given {@code arrayref}.
   * <br><br>
   * <i>{@code Operand Stack}: arrayref -> length</i>
   */
  public abstract Code arraylength();

}
