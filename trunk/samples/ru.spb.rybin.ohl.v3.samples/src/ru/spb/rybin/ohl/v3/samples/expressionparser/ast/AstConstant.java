package ru.spb.rybin.ohl.v3.samples.expressionparser.ast;

public interface AstConstant extends AstNode, case {
  int getValue();
}
