package impl.attributes;

import impl.helpers.DynamicByteBuffer;
import specs.InfoStructure;
import specs.JvmClass;

public class InnerClasses extends specs.attributes.InnerClasses {

  private DynamicByteBuffer innerClasses;
  private int nrOfInnerClasses;

  public InnerClasses(InfoStructure parent) {
    super(parent);

    this.innerClasses = new DynamicByteBuffer();
    this.nrOfInnerClasses = 0;
  }

  public InnerClasses add(JvmClass innerClass, String fileName) {
    innerClasses.writeShort( parent.constantPool().addClass(fileName) );
    innerClasses.writeShort( parent.constantPool().addClass( parent.name() ) );
    innerClasses.writeShort( parent.constantPool().addUtf8( innerClass.name() ) );
    innerClasses.writeShort( innerClass.accessFlags() );
    nrOfInnerClasses++;
    return this;
  }

  public byte[] generate() {
    short nameId = (short)parent.constantPool().addUtf8(name);

    DynamicByteBuffer bytecode = new DynamicByteBuffer();
    bytecode.writeShort(nameId);
    bytecode.writeInt(2 + innerClasses.size());
    bytecode.writeShort(nrOfInnerClasses);
    bytecode.write( innerClasses.toByteArray() );

    return bytecode.toByteArray();
  }

}
