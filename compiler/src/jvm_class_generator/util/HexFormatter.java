package jvm_class_generator.util;

public class HexFormatter {

  public static String toHexString(byte[] bytes) {
    char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
    StringBuilder hexString = new StringBuilder();

    for (int idx = 0; idx < bytes.length; idx++) {
      int value = bytes[idx] & 0x000000FF;    // empty int, with 1 byte set to the value
      
      hexString
       .append(hexArray[value / 16])
       .append(hexArray[value % 16]);

      if ((idx + 1) % 16 == 0) {              // lines of 8 blocks
        hexString.append(System.lineSeparator());
      }
      else if ((idx + 1) % 2 == 0) {          // blocks of 4 hex-digits
        hexString.append(" ");
      }
    }

    return hexString.toString();
  }
  
}
