package jvm_class_generator.impl.attributes;

import java.util.Stack;

import jvm_class_generator.impl.helpers.DynamicByteBuffer;
import jvm_class_generator.specs.InfoStructure;
import jvm_class_generator.specs.data_areas.Frame;

public class StackMapTable extends jvm_class_generator.specs.attributes.StackMapTable {

  private Stack<Frame> frames;
  private int nrOfStackMapFrames = 0;

  public StackMapTable(InfoStructure parent) {
    super(parent);
  }

  public void setFrames(Stack<Frame> frames) {
    this.frames = frames;
  }
 
  public byte[] generate() {
    short nameId = (short)parent.constantPool().addUtf8(name);
    byte[] stackMapTable = generateStackMapTable();

    DynamicByteBuffer bytecode = new DynamicByteBuffer();
    bytecode.writeShort( nameId );
    // stackMapTable + 2B number_of_entries
    bytecode.writeInt( stackMapTable.length + 2 );
    bytecode.writeShort(nrOfStackMapFrames);
    bytecode.write(stackMapTable);
    return bytecode.toByteArray();
  }

  protected byte[] generateStackMapTable() {
    DynamicByteBuffer stackMapTable = new DynamicByteBuffer();
    int currOffset = 0;
    
    for (Frame frame : frames) {
      int offsetDelta = frame.codeOffset - currOffset;
      if (offsetDelta < 0)
        throw new IllegalStateException("Labels should be added in order, so the offsets should therefore be ascending!");

      if (offsetDelta == 0)
        continue;

      nrOfStackMapFrames++;
      if (currOffset != 0) offsetDelta--;

      // generate only full frames for simplicity
      stackMapTable.write( frame.generateFullFrame(offsetDelta) );
      
      currOffset = frame.codeOffset;
    }

    return stackMapTable.toByteArray();
  }

}
