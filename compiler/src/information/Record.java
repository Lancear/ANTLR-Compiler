package information;

import java.util.LinkedHashMap;

public class Record extends Symbol {

  public static String SYMBOL_TYPE = "record";

  public LinkedHashMap<String, Variable> fields;

  public Record(String name) {
    this(name, new LinkedHashMap<>());
  }

  public Record(String name, LinkedHashMap<String, Variable> fields) {
    super(name, Record.SYMBOL_TYPE);
    this.fields = fields;
  }

}
