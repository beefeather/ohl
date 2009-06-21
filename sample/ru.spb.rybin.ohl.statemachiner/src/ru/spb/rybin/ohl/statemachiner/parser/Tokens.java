package ru.spb.rybin.ohl.statemachiner.parser;

public enum-case Tokens extends ConnectorTokens, StateQualifier {
  case state(),
  case openParen(),
  case closeParen(),
  case openBrace(),
  case closeBrace(),
  case comma(),
  case semicolon(),
  case identifier(String id)
}
