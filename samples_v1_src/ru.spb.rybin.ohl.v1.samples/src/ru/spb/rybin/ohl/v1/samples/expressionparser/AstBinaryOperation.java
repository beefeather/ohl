package ru.spb.rybin.ohl.v1.samples.expressionparser;

public interface AstBinaryOperation extends AstNode {
  AstNode getLeft();

  AstNode getRight();
}
