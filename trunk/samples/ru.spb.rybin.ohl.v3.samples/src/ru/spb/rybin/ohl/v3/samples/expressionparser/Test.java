package ru.spb.rybin.ohl.v3.samples.expressionparser;

import java.util.ArrayList;
import java.util.List;

import ru.spb.rybin.ohl.v3.samples.expressionparser.Parser.ParserException;
import ru.spb.rybin.ohl.v3.samples.expressionparser.ast.AstNode;
import ru.spb.rybin.ohl.v3.samples.expressionparser.lexer.Lexer;
import ru.spb.rybin.ohl.v3.samples.expressionparser.lexer.LexerImpl;
import ru.spb.rybin.ohl.v3.samples.expressionparser.lexer.Tokens;

public class Test {
  public static void main(String[] args) throws ParserException {
    
    final List<Tokens.case> tokens = new ArrayList<Tokens.case>();
    
    tokens.add(Tokens.paren_open);           // (
    tokens.add(new Tokens.literal(5));       // 5
    tokens.add(Tokens.plus);                 // +
    tokens.add(Tokens.paren_open);           // (
    tokens.add(new Tokens.identifier("a"));  // a
    tokens.add(Tokens.minus);                // -
    tokens.add(new Tokens.literal(1));       // 1
    tokens.add(Tokens.paren_close);          // )
    tokens.add(Tokens.paren_close);          // )
    
    Lexer lexer = new LexerImpl(tokens);
    
    Parser parser = new Parser(lexer);
    
    AstNode root = parser.parse();
    
    StringBuilder builder = new StringBuilder();
    PerlExpressionGenerator.generate(root, builder);
    System.out.println(builder);
  }
}
