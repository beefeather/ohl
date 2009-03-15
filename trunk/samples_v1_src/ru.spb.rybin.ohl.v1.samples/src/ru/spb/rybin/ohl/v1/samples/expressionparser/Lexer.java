package ru.spb.rybin.ohl.v1.samples.expressionparser;

public interface Lexer {
	
  Tokens.case peek();
  void consume();
  
  public enum-case Tokens {
	plus(),
	minus(),
	paren_open(),
	paren_close(),
	literal(int num),
	identifier(String name),
	eos()
  }
}
