package symboltable.symbols;

import java.util.Collections;
import java.util.Map;

import org.antlr.v4.runtime.Token;

public class RecordSymbol extends Symbol {

  protected Map<String, VariableSymbol> fields;

  public RecordSymbol(String name, Token token) {
    super("record", name, token);
    this.fields = null;
  }
  
  public void setFields(Map<String, VariableSymbol> fields) {
    if (this.fields != null) 
      throw new IllegalStateException("Fields have already been set!");

    this.fields = Collections.unmodifiableMap(fields);
  }

  public Map<String, VariableSymbol> getFields() {
    return this.fields;
  }

  public VariableSymbol getField(String fieldName) {
    return this.fields.get(fieldName);
  }
  
}
