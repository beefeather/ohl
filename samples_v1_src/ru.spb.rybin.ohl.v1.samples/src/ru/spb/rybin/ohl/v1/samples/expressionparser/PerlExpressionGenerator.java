package ru.spb.rybin.ohl.v1.samples.expressionparser;

public class PerlExpressionGenerator {
  public static void generate(AstNode node, StringBuilder output) {
	  output.append('(');
	  switch (node.getSubtype()) {
	  case * addition(AstBinaryOperation addition) {
		  generate(addition.getLeft(), output);
		  output.append(" + ");
		  generate(addition.getRight(), output);
	  }
	  case * subtraction(AstBinaryOperation subtration) {
		  generate(subtration.getLeft(), output);
		  output.append(" - ");
		  generate(subtration.getRight(), output);
	  }
	  case * constant(AstConstant constant) {
		  output.append(Integer.toString(constant.getValue()));
	  }
	  case * variable(AstVariable variable) {
		  output.append("$" + variable.getName());  
	  }
	  }
	  output.append(')');
  }
}
