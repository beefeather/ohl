package ru.spb.rybin.ohl.statemachiner.parser.ast;

import ru.spb.rybin.ohl.statemachiner.parser.StateQualifier;

public interface StateOnSideReference {
  StateReference getState();
  StateQualifier.case getQualifier();
}
