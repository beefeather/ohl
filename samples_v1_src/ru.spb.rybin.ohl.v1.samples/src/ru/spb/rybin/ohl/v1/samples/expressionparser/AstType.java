package ru.spb.rybin.ohl.v1.samples.expressionparser;

public enum-case AstType {
  addition(AstBinaryOperation addition),
  subtraction(AstBinaryOperation subtration),
  variable(AstVariable variable),
  constant(AstConstant constant)
}
