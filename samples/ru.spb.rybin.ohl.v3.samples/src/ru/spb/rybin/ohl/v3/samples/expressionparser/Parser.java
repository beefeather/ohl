package ru.spb.rybin.ohl.v3.samples.expressionparser;

import ru.spb.rybin.ohl.v3.samples.expressionparser.ast.AstBinaryOperation;
import ru.spb.rybin.ohl.v3.samples.expressionparser.ast.AstConstant;
import ru.spb.rybin.ohl.v3.samples.expressionparser.ast.AstNode;
import ru.spb.rybin.ohl.v3.samples.expressionparser.ast.AstType;
import ru.spb.rybin.ohl.v3.samples.expressionparser.ast.AstVariable;
import ru.spb.rybin.ohl.v3.samples.expressionparser.lexer.Lexer;
import ru.spb.rybin.ohl.v3.samples.expressionparser.lexer.Tokens;
import ru.spb.rybin.ohl.v3.samples.expressionparser.lexer.TokensEx;

public class Parser {
  public Parser(Lexer lexer) {
    this.lexer = lexer;
  }
  
  AstNode parse() throws ParserException {
    AstNode result = parseExpression(); 
    if (lexer.peek() != TokensEx.eos) {
      throw new ParserException("EOS expected");
    }
    return result;
  }
  private AstNode parseExpression() throws ParserException {
    final AstNode left = parseTerminal();
    final AstBinaryOperation.Operation.case operation;

    switch (lexer.peek()) {
      case * plus() {
        operation = AstBinaryOperation.Operation.plus;
      }
      case * minus() {
        operation = AstBinaryOperation.Operation.minus;
      }
      default * {
          return left;
      }
    }
    lexer.consume();
    final AstNode right = parseExpression();

    return new AstBinaryOperation() {
      public AstNode getLeft() {
        return left;
      }
      public AstNode getRight() {
        return right;
      }         
      @Override
      public AstType.case getSubtype() {
        return this;
      }
      public Operation.case getOperation() {
        return operation;
      }
    };
  }

  private AstNode parseTerminal() throws ParserException {
    switch (lexer.peek()) {
      case * paren_open() {
        lexer.consume();
        AstNode inner = parseExpression();
        if (lexer.peek() != Tokens.paren_close) {
          throw new ParserException("')' expected");
        }
        lexer.consume();
        return inner;
      }
      case * literal(final int value) {
        lexer.consume();
        return new AstConstant() {
          public int getValue() {
            return value;
          }
          public AstType.case getSubtype() {
            return this;
          }
        };
      }
      case * identifier(final String name) {
        lexer.consume();
        return new AstVariable() {
          public String getName() {
            return name;
          }
          public AstType.case getSubtype() {
            return this;
          }
        };
      }
      default * {
        throw new ParserException("integer or identifier or parenthized expression expected");
      }
    }
  }
  
  private final Lexer lexer;
  
  public static class ParserException extends Exception {
    public ParserException() {
        super();
    }
    public ParserException(String message, Throwable cause) {
        super(message, cause);
    }
    public ParserException(String message) {
        super(message);
    }
    public ParserException(Throwable cause) {
        super(cause);
    }
  }
} 