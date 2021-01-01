package information;

import org.antlr.v4.runtime.ParserRuleContext;

public class Expression extends Variable {

  public static String SYMBOL_TYPE = "expression";

  public Expression(ParserRuleContext context, String dataType) {
    super(nameOf(context), dataType);
    this.symbolType = Expression.SYMBOL_TYPE;
  }

  public static String nameOf(ParserRuleContext context) {
    return (context == null) ? YaplConstants.UNDEFINED : String.format("<%d>", context.hashCode());
  }

}
