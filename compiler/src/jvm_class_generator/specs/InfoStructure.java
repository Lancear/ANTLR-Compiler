package jvm_class_generator.specs;

import jvm_class_generator.specs.attributes.Attribute;
import jvm_class_generator.specs.data_areas.ConstantPool;

/**
 * Provides a common interface for attributes to access the most important fields of their parents.
 */
public interface InfoStructure extends BytecodeStructure {
  
  public abstract ConstantPool constantPool();
  public abstract String name();
  public abstract String descriptor();

  public Attribute addAttribute(String name);
  public boolean hasAttribute(String name);
  public Attribute getAttribute(String name);

}
