package semantic;

import java.util.List;

public class Attributes {
  public String type;
  public String name;
  public List<String> names;
  
  public Attributes(String type) {
    this.type = type;
    this.name = null;
    this.names = null;
  }

  public Attributes(String type, String name) {
    this.type = type;
    this.name = name;
    this.names = null;
  }

  public Attributes(String type, List<String> names) {
    this.type = type;
    this.names = names;
    this.name = null;
  }
}
