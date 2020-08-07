package jvm_class_generator.impl.attributes;

import jvm_class_generator.specs.InfoStructure;

public abstract class Attribute {
  
  public static jvm_class_generator.specs.attributes.Attribute create(String name, InfoStructure parent) {
    switch(name) {
      case Code.name:
        return new Code(parent);

      case InnerClasses.name:
        return new InnerClasses(parent);
        
      case StackMapTable.name:
        return new StackMapTable(parent);
    }

    throw new IllegalArgumentException("Unknown attribute " + name + "!");
  }

}
