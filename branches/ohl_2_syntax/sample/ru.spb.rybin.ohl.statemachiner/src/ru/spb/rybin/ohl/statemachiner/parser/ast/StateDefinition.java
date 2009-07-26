package ru.spb.rybin.ohl.statemachiner.parser.ast;

import java.util.List;

public interface StateDefinition {
  String getName();
  List<FormalParameter> getFields();
  StateReference getNextImmediate();
}
