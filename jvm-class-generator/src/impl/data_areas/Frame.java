package impl.data_areas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import impl.helpers.DynamicByteBuffer;
import specs.class_content.Method;
import specs.helpers.Descriptor;

public class Frame extends specs.data_areas.Frame {

  public Frame(Method method) {
    super(method, 0, new HashMap<>(), new Stack<>());

    ArrayList<String> params = Descriptor.METHOD_PARAM_DESCRIPTORS(method.descriptor());
    int idx = 0; // always static methods :/
    
    for (String paramDescriptor : params) {
      locals.put(idx++, paramDescriptor);
    }
  }

  public Frame(specs.data_areas.Frame frame, int codeOffset) {
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
        bytecode.writeShort( method.constantPool().addClass( locals.get(idx) ) );
      }
    }

    bytecode.writeShort(stack.size());
    for (int idx = 0; idx < stack.size(); idx++) {
      if (stack.get(idx).equals(Descriptor.INT)) {
        bytecode.writeByte(VerificationTypeInfo.INTEGER);
      }
      else {
        bytecode.writeByte(VerificationTypeInfo.OBJECT);
        bytecode.writeShort( method.constantPool().addClass( stack.get(idx) ) );
      }
    }

    return bytecode.toByteArray();
  }
}
