package jvm_class_generator.impl.helpers;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Combines the DataOutput interface with the ByteArrayOutputStream.
 */
public class DynamicByteBuffer implements DataOutput {
  
  private final static String IOExceptionMessage = "\n" +
    "  ByteArrayOutputStream should never throw an IOException,\n" +
    "  this DataOutputStream uses the ByteArrayOutputStream for IO,\n" + 
    "  therefore this DataOutputStream should never throw an IOException!\n";

  private ByteArrayOutputStream out;
  private DataOutputStream data;


  public DynamicByteBuffer() {
    this.out = new ByteArrayOutputStream();
    this.data = new DataOutputStream(out);
  }

  /**
   * Writes {@code b.length} bytes to this output stream.
   * <p>
   * The {@code write} method of {@code FilterOutputStream}
   * calls its {@code write} method of three arguments with the
   * arguments {@code b}, {@code 0}, and
   * {@code b.length}.
   * <p>
   * Note that this method does not call the one-argument
   * {@code write} method of its underlying output stream with
   * the single argument {@code b}.
   *
   * @param      b   the data to be written.
   * @see        java.io.FilterOutputStream#write(byte[], int, int)
   */
  public synchronized void write(byte[] b) {
    try {
      data.write(b);
    }
    catch (IOException ex) {
      throw new IllegalStateException(IOExceptionMessage, ex);
    }
  }

  /**
   * Writes {@code len} bytes from the specified byte array
   * starting at offset {@code off} to the underlying output stream.
   * If no exception is thrown, the counter {@code written} is
   * incremented by {@code len}.
   *
   * @param      b     the data.
   * @param      off   the start offset in the data.
   * @param      len   the number of bytes to write.
   * @see        java.io.FilterOutputStream#out
   */
  public synchronized void write(byte[] b, int off, int len) {
    try {
      data.write(b, off, len);
    }
    catch (IOException ex) {
      throw new IllegalStateException(IOExceptionMessage, ex);
    }
  }

  /**
   * Writes the specified byte (the low eight bits of the argument
   * {@code b}) to the underlying output stream. If no exception
   * is thrown, the counter {@code written} is incremented by
   * {@code 1}.
   * <p>
   * Implements the {@code write} method of {@code OutputStream}.
   *
   * @param      b   the {@code byte} to be written.
   * @see        java.io.FilterOutputStream#out
   */
  public synchronized void write(int b) {
    try {
      data.write(b);
    }
    catch (IOException ex) {
      throw new IllegalStateException(IOExceptionMessage, ex);
    }
  }

  /**
   * Writes a {@code boolean} to the underlying output stream as
   * a 1-byte value. The value {@code true} is written out as the
   * value {@code (byte)1}; the value {@code false} is
   * written out as the value {@code (byte)0}. If no exception is
   * thrown, the counter {@code written} is incremented by
   * {@code 1}.
   *
   * @param      v   a {@code boolean} value to be written.
   * @see        java.io.FilterOutputStream#out
   */
  public void writeBoolean(boolean v) {
    try {
      data.writeBoolean(v);
    }
    catch (IOException ex) {
      throw new IllegalStateException(IOExceptionMessage, ex);
    }
  }

  /**
   * Writes out a {@code byte} to the underlying output stream as
   * a 1-byte value. If no exception is thrown, the counter
   * {@code written} is incremented by {@code 1}.
   *
   * @param      v   a {@code byte} value to be written.
   * @see        java.io.FilterOutputStream#out
   */
  public void writeByte(int v) {
    try {
      data.writeByte(v);
    }
    catch (IOException ex) {
      throw new IllegalStateException(IOExceptionMessage, ex);
    }
  }

  /**
   * Writes out the string to the underlying output stream as a
   * sequence of bytes. Each character in the string is written out, in
   * sequence, by discarding its high eight bits. If no exception is
   * thrown, the counter {@code written} is incremented by the
   * length of {@code s}.
   *
   * @param      s   a string of bytes to be written.
   * @see        java.io.FilterOutputStream#out
   */
  public void writeBytes(String s) {
    try {
      data.writeBytes(s);
    }
    catch (IOException ex) {
      throw new IllegalStateException(IOExceptionMessage, ex);
    }
  }

  /**
   * Writes a {@code char} to the underlying output stream as a
   * 2-byte value, high byte first. If no exception is thrown, the
   * counter {@code written} is incremented by {@code 2}.
   *
   * @param      v   a {@code char} value to be written.
   * @see        java.io.FilterOutputStream#out
   */
  public void writeChar(int v) {
    try {
      data.writeChar(v);
    }
    catch (IOException ex) {
      throw new IllegalStateException(IOExceptionMessage, ex);
    }
  }

  /**
   * Writes a string to the underlying output stream as a sequence of
   * characters. Each character is written to the data output stream as
   * if by the {@code writeChar} method. If no exception is
   * thrown, the counter {@code written} is incremented by twice
   * the length of {@code s}.
   *
   * @param      s   a {@code String} value to be written.
   * @see        java.io.DataOutputStream#writeChar(int)
   * @see        java.io.FilterOutputStream#out
   */
  public void writeChars(String s) {
    try {
      data.writeChars(s);
    }
    catch (IOException ex) {
      throw new IllegalStateException(IOExceptionMessage, ex);
    }
  }

  /**
   * Converts the double argument to a {@code long} using the
   * {@code doubleToLongBits} method in class {@code Double},
   * and then writes that {@code long} value to the underlying
   * output stream as an 8-byte quantity, high byte first. If no
   * exception is thrown, the counter {@code written} is
   * incremented by {@code 8}.
   *
   * @param      v   a {@code double} value to be written.
   * @see        java.io.FilterOutputStream#out
   * @see        java.lang.Double#doubleToLongBits(double)
   */
  public void writeDouble(double v) {
    try {
      data.writeDouble(v);
    }
    catch (IOException ex) {
      throw new IllegalStateException(IOExceptionMessage, ex);
    }
  }

  /**
   * Converts the float argument to an {@code int} using the
   * {@code floatToIntBits} method in class {@code Float},
   * and then writes that {@code int} value to the underlying
   * output stream as a 4-byte quantity, high byte first. If no
   * exception is thrown, the counter {@code written} is
   * incremented by {@code 4}.
   *
   * @param      v   a {@code float} value to be written.
   * @see        java.io.FilterOutputStream#out
   * @see        java.lang.Float#floatToIntBits(float)
   */
  public void writeFloat(float v) {
    try {
      data.writeFloat(v);
    }
    catch (IOException ex) {
      throw new IllegalStateException(IOExceptionMessage, ex);
    }
  }

  /**
   * Writes an {@code int} to the underlying output stream as four
   * bytes, high byte first. If no exception is thrown, the counter
   * {@code written} is incremented by {@code 4}.
   *
   * @param      v   an {@code int} to be written.
   * @see        java.io.FilterOutputStream#out
   */
  public void writeInt(int v) {
    try {
      data.writeInt(v);
    }
    catch (IOException ex) {
      throw new IllegalStateException(IOExceptionMessage, ex);
    }
  }

  /**
   * Writes a {@code long} to the underlying output stream as eight
   * bytes, high byte first. In no exception is thrown, the counter
   * {@code written} is incremented by {@code 8}.
   *
   * @param      v   a {@code long} to be written.
   * @see        java.io.FilterOutputStream#out
   */
  public void writeLong(long v) {
    try {
      data.writeLong(v);
    }
    catch (IOException ex) {
      throw new IllegalStateException(IOExceptionMessage, ex);
    }
  }

  /**
   * Writes a {@code short} to the underlying output stream as two
   * bytes, high byte first. If no exception is thrown, the counter
   * {@code written} is incremented by {@code 2}.
   *
   * @param      v   a {@code short} to be written.
   * @see        java.io.FilterOutputStream#out
   */
  public void writeShort(int v) {
    try {
      data.writeShort(v);
    }
    catch (IOException ex) {
      throw new IllegalStateException(IOExceptionMessage, ex);
    }
  }

  /**
   * Writes a string to the underlying output stream using
   * <a href="DataInput.html#modified-utf-8">modified UTF-8</a>
   * encoding in a machine-independent manner.
   * <p>
   * First, two bytes are written to the output stream as if by the
   * {@code writeShort} method giving the number of bytes to
   * follow. This value is the number of bytes actually written out,
   * not the length of the string. Following the length, each character
   * of the string is output, in sequence, using the modified UTF-8 encoding
   * for the character. If no exception is thrown, the counter
   * {@code written} is incremented by the total number of
   * bytes written to the output stream. This will be at least two
   * plus the length of {@code str}, and at most two plus
   * thrice the length of {@code str}.
   *
   * @param      str   a string to be written.
   * @throws     UTFDataFormatException  if the modified UTF-8 encoding of
   *             {@code str} would exceed 65535 bytes in length
   * @see        #writeChars(String)
   */
  public void writeUTF(String s) {
    try {
      data.writeUTF(s);
    }
    catch (IOException ex) {
      throw new IllegalStateException(IOExceptionMessage, ex);
    }
  }

  /**
   * Creates a newly allocated byte array. Its size is the current
   * size of this output stream and the valid contents of the buffer
   * have been copied into it.
   *
   * @return  the current contents of this output stream, as a byte array.
   * @see     java.io.ByteArrayOutputStream#size()
   */
  public byte[] toByteArray() {
    try {
      data.flush();
    }
    catch (IOException ex) {
      throw new IllegalStateException(IOExceptionMessage, ex);
    }

    return out.toByteArray();
  }

  /**
   * Returns the current size of the buffer.
   *
   * @return  the value of the {@code count} field, which is the number
   *          of valid bytes in this output stream.
   * @see     java.io.ByteArrayOutputStream#count
   */
  public synchronized int size() {
    try {
      data.flush();
    }
    catch (IOException ex) {
      throw new IllegalStateException(IOExceptionMessage, ex);
    }
    
    return out.size();
  }

  /**
   * Converts the buffer's contents into a string decoding bytes using the
   * platform's default character set. The length of the new {@code String}
   * is a function of the character set, and hence may not be equal to the
   * size of the buffer.
   *
   * <p> This method always replaces malformed-input and unmappable-character
   * sequences with the default replacement string for the platform's
   * default character set. The {@linkplain java.nio.charset.CharsetDecoder}
   * class should be used when more control over the decoding process is
   * required.
   *
   * @return String decoded from the buffer's contents.
   * @since  1.1
   */
  public String toString() {
    try {
      data.flush();
    }
    catch (IOException ex) {
      throw new IllegalStateException(IOExceptionMessage, ex);
    }

    return out.toString();
  }

}
