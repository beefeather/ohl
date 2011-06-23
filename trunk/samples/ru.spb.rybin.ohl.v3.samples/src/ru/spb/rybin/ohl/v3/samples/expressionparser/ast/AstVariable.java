package ru.spb.rybin.ohl.v3.samples.expressionparser.ast;

public interface AstVariable extends AstNode, case {
  String getName();
}
