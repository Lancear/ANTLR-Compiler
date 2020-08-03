package semantic;

import java.util.List;

public class Attributes {
  public String type;
  public String name;
  public List<String> names;
  public boolean hasReturn;
  
  public Attributes(String type) {
    this.type = type;
    this.name = null;
    this.names = null;
    this.hasReturn = false;
  }

  public Attributes(boolean hasReturn) {
    this.type = null;
    this.name = null;
    this.names = null;
    this.hasReturn = hasReturn;
  }

  public Attributes(String type, String name) {
    this.type = type;
    this.name = name;
    this.names = null;
    this.hasReturn = false;
  }

  public Attributes(String type, List<String> names) {
    this.type = type;
    this.name = null;
    this.names = names;
    this.hasReturn = false;
  }
}
