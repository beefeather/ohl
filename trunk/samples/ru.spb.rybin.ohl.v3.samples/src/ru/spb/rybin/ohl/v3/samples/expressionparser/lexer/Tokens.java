package ru.spb.rybin.ohl.v3.samples.expressionparser.lexer;

public enum-case Tokens {
	case plus(),
	case minus(),
	case paren_open(),
	case paren_close(),
	case literal(int num),
	case identifier(String name)
}
