package ru.spb.rybin.ohl.v2.samples.expressionparser;

public interface Lexer {
	
  Tokens.case peek();
  void consume();
  
  public enum-case Tokens {
    case plus(),
    case minus(),
    case paren_open(),
    case paren_close(),
    case literal(int num),
    case identifier(String name),
    case eos()
  }
}
