package ru.spb.rybin.ohl.v2.samples.expressionparser;

public interface AstConstant extends AstNode, case {
  int getValue();
}
