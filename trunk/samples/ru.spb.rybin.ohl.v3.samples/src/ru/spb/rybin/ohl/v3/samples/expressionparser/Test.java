package ru.spb.rybin.ohl.v3.samples.expressionparser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ru.spb.rybin.ohl.v3.samples.expressionparser.Parser.ParserException;

public class Test {
	public static void main(String[] args) throws ParserException {
		final List<Lexer.Tokens.case> tokens = new ArrayList<Lexer.Tokens.case>();
		tokens.add(Lexer.Tokens.paren_open);
		tokens.add(new Lexer.Tokens.literal(5));
		tokens.add(Lexer.Tokens.plus);
		tokens.add(Lexer.Tokens.paren_open);
		tokens.add(new Lexer.Tokens.identifier("a"));
		tokens.add(Lexer.Tokens.minus);
		tokens.add(new Lexer.Tokens.literal(1));
		tokens.add(Lexer.Tokens.paren_close);
		tokens.add(Lexer.Tokens.paren_close);
		
		Lexer lexer = new Lexer() {
			private final Iterator<Lexer.Tokens.case> it = tokens.iterator();
			private Lexer.TokensEx.case current;
			{
				advance();
			}
			@Override
			public void consume() {
				if (current == Lexer.TokensEx.eos) {
					throw new RuntimeException("Already eos");
				}
				advance();
			}
			private void advance() {
				if (it.hasNext()) {
					current = it.next();
				} else {
					current = Lexer.TokensEx.eos;
				}
			}
				
			@Override
			public TokensEx.case peek() {
				return current;
			}
		};
		
		Parser parser = new Parser(lexer);
		AstNode root = parser.parse();
		
		StringBuilder builder = new StringBuilder();
		PerlExpressionGenerator.generate(root, builder);
		System.out.println(builder);
	}
}
