package ru.spb.rybin.ohl.v2.samples.expressionparser;

public interface AstVariable extends AstNode, case {
  String getName();
}
