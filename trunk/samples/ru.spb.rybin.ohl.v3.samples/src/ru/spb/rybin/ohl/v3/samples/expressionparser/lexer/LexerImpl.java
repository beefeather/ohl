package ru.spb.rybin.ohl.v3.samples.expressionparser.lexer;

import java.util.Iterator;
import java.util.List;

public class LexerImpl implements Lexer {
  private final Iterator<Tokens.case> it;
  private TokensEx.case current;
  
  public LexerImpl(List<Tokens.case> tokens) {
    it = tokens.iterator();
    advance();
  }
  
  @Override
  public void consume() {
    if (current == TokensEx.eos) {
      throw new RuntimeException("Already eos");
    }
    advance();
  }
  private void advance() {
    if (it.hasNext()) {
        current = it.next();
    } else {
        current = TokensEx.eos;
    }
  }
      
  @Override
  public TokensEx.case peek() {
    return current;
  }
}
