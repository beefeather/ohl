# Extensions between enum-cases #

One [enum-case](LangEnumCase.md) as a set of cases may extend several other enum-cases.

```
enum-case LiteralTokens {
  case identifier(String value),
  case intLiteral(int number)
}
enum-case OperatorTokens {
  case plus(),
  case minus()
}

enum-case Tokens extends LiteralTokens, OperatorTokens {
  case parenOpen(),
  case parenClose()
}
```

This makes types _LiteralTokens.case_ and _Tokens.case_ related: a subtype and a supertype:
```
Tokens.case token;
LiteralTokens.case literal = new LiteralTokens.identifier("foo");
token = literal; // implicit cast
```