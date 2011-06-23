package ru.spb.rybin.ohl.v3.samples.expressionparser.lexer;

public interface Lexer {
  TokensEx.case peek();
  void consume();
}
