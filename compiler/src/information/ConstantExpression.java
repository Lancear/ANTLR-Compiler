package information;

import org.antlr.v4.runtime.ParserRuleContext;

/**
 * Used for constant folding
 */
public class ConstantExpression extends Constant {

  public static String SYMBOL_TYPE = "constant expression";
  public ParserRuleContext context;

  public ConstantExpression(ParserRuleContext context, String dataType, String value) {
    super(nameOf(context), dataType, value);
    this.context = context;
    this.symbolType = ConstantExpression.SYMBOL_TYPE;
  }

  public static String nameOf(ParserRuleContext context) {
    return (context == null) ? YaplConstants.UNDEFINED : String.format("<%d>", context.hashCode());
  }

  @Override
  public String toString() {
    String text = (context == null) ? YaplConstants.UNDEFINED : CompilerContext.getText(context);
    String pos = (context == null) ? "" : " // line: " + CompilerContext.getLine(context) + ", column: " + CompilerContext.getColumn(context);
    return symbolType.toUpperCase() + " " + dataType + " " + text + " = " + value + pos;
  }

}
