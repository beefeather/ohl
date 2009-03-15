package ru.spb.rybin.ohl.v1.samples.expressionparser;

public class Parser {
  public Parser(Lexer lexer) {
	  this.lexer = lexer;
  }
  
  AstNode parse() throws ParserException {
	  return parseExpression();
  }
  private AstNode parseExpression() throws ParserException {
	  final AstNode left = parseTerminal();
	  final boolean plusNotMinus;

	  switch (lexer.peek()) {
  	  case * plus() {
  		  plusNotMinus = true;
  	  }
  	  case * minus() {
  		  plusNotMinus = false;
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
      public AstType.case getSubtype() {
      	if (plusNotMinus) {
      		return AstType.addition(this);
      	} else {
      		return AstType.subtraction(this);
      	}
      }
	  };
  }

  private AstNode parseTerminal() throws ParserException {
	  switch (lexer.peek()) {
  	  case * paren_open() {
  		 lexer.consume();
  		 AstNode inner = parseExpression();
  		 if (lexer.peek() != Lexer.Tokens.paren_close()) {
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
    		    return AstType.constant(this);
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
    		    return AstType.variable(this);
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
