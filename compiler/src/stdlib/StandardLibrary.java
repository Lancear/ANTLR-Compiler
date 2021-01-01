package stdlib;

import information.Symbol;

public interface StandardLibrary {

  String getName();

  /**
   * @return all predefined symbols which are implemented by this standard library.
   */
  Symbol[] getPredefinedSymbols();

  /**
   * @return the generated code implementing all predefined symbols of this standard library.
   */
  byte[] generate();

}
