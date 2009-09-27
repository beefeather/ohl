package ru.spb.rybin.ohl.v3.samples.expressionparser;

public interface AstVariable extends AstNode, case {
  String getName();
}
