package ru.spb.rybin.ohl.v2.samples.expressionparser;

public interface AstBinaryOperation extends AstNode, case {
  AstNode getLeft();

  AstNode getRight();
  
  Operation.case getOperation();
  
  enum-case Operation {
    case plus(),
    case minus()
  }
}
