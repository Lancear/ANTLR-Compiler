package impl.helpers;

public class TypeChecker {
  
  public static void checkU1(String argName, int value) throws IllegalArgumentException {
    if ((value >> 8) > 0 || value < 0)
      throw new IllegalArgumentException(argName + " must be of type u1 (unsigned byte)");
  }

  public static void checkByte(String argName, int value) throws IllegalArgumentException {
    if ((value >> 8) > 0)
      throw new IllegalArgumentException(argName + " must be of type byte");
  }

  public static void checkU2(String argName, int value) throws IllegalArgumentException {
    if ((value >> 16) > 0 || value < 0)
      throw new IllegalArgumentException(argName + " must be of type u2 (unsigned short)");
  }

  public static void checkShort(String argName, int value) throws IllegalArgumentException {
    if ((value >> 16) > 0)
      throw new IllegalArgumentException(argName + " must be of type short");
  }
  
}
