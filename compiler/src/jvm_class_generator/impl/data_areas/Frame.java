package jvm_class_generator.impl.data_areas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import jvm_class_generator.impl.helpers.DynamicByteBuffer;
import jvm_class_generator.specs.class_content.Method;
import jvm_class_generator.specs.helpers.AccessFlags;
import jvm_class_generator.specs.helpers.Descriptor;

public class Frame extends jvm_class_generator.specs.data_areas.Frame {

  public Frame(Method method) {
    super(method, 0, new HashMap<>(), new Stack<>());

    ArrayList<String> params = Descriptor.METHOD_PARAM_DESCRIPTORS(method.descriptor());
    int idx = 0;
    
    if ((method.accessFlags() & AccessFlags.STATIC) != AccessFlags.STATIC) {
      locals.put(idx++, method.clazz().descriptor());
    }
    
    for (String paramDescriptor : params) {
      locals.put(idx++, paramDescriptor);
    }
  }

  public Frame(jvm_class_generator.specs.data_areas.Frame frame, int codeOffset) {
    super(frame.method(), codeOffset, new HashMap<>(), new Stack<>());

    for (String operand : frame.stack) {
      stack.push(operand);
    }

    for (int variableIdx : frame.locals.keySet()) {
      locals.put(variableIdx, frame.locals.get(variableIdx));
    }
  }

  public byte[] generateFullFrame(int offsetDelta) {
    DynamicByteBuffer bytecode = new DynamicByteBuffer();
    bytecode.writeByte(Frame.FULL_FRAME);
    bytecode.writeShort(offsetDelta);

    bytecode.writeShort(locals.size());
    for (int idx = 0; idx < locals.size(); idx++) {
      if (locals.get(idx).equals(Descriptor.INT)) {
        bytecode.writeByte(VerificationTypeInfo.INTEGER);
      }
      else {
        bytecode.writeByte(VerificationTypeInfo.OBJECT);
        String type = locals.get(idx);
        if (type.startsWith("L")) {
          // substring -> remove L; from the descriptor, only the internal name is needed
          type = type.substring( 1, type.length() - 1 );
        }

        bytecode.writeShort( method.constantPool().addClass(type) );
      }
    }

    bytecode.writeShort(stack.size());
    for (int idx = 0; idx < stack.size(); idx++) {
      if (stack.get(idx).equals(Descriptor.INT)) {
        bytecode.writeByte(VerificationTypeInfo.INTEGER);
      }
      else {
        bytecode.writeByte(VerificationTypeInfo.OBJECT);
        String type = stack.get(idx);
        if (type.startsWith("L")) {
          // substring -> remove L; from the descriptor, only the internal name is needed
          type = type.substring( 1, type.length() - 1 );
        }

        bytecode.writeShort( method.constantPool().addClass(type) );
      }
    }

    return bytecode.toByteArray();
  }
}
