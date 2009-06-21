package ru.spb.rybin.ohl.statemachiner.generator;

import ru.spb.rybin.ohl.statemachiner.parser.ast.Transition;

public interface Edge {
  StateOnSide getDestination();
  Transition getTransition();
}
