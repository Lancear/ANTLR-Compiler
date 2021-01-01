package information;

public class YaplConstants {
  // types
  public static final String INT = "int";
  public static final String BOOL = "bool";
  public static final String VOID = "void";
  public static final String STRING = "string";

  // values
  public static final String TRUE = "True";
  public static final String FALSE = "False";

  /**
   * null/undefined value for names/types to simplify {@code .equals()} checks since it removes the chance of {@code NullReferenceException}s.
   */
  public static final String UNDEFINED = "<?>";
}
