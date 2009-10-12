package ru.spb.rybin.ohl.v3.samples.expressionparser;

public class PerlExpressionGenerator {
  public static void generate(AstNode node, StringBuilder output) {
      output.append('(');
      switch (node.getSubtype()) {
      case instanceof AstBinaryOperation binary {
        generate(binary.getLeft(), output);
        switch (binary.getOperation()) {
          case * plus() {
            output.append(" + ");
          }
          case * minus() {
            output.append(" - ");
          }
        }
          generate(binary.getRight(), output);
      }
      case instanceof AstConstant constant {
          output.append(Integer.toString(constant.getValue()));
      }
      case instanceof AstVariable variable {
          output.append("$" + variable.getName());  
      }
      }
      output.append(')');
  }
}
