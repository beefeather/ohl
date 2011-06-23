package ru.spb.rybin.ohl.v3.samples.expressionparser.ast;

public interface AstBinaryOperation extends AstNode, case {
  AstNode getLeft();

  AstNode getRight();
  
  Operation.case getOperation();
  
  enum-case Operation {
    case plus(),
    case minus()
  }
}
