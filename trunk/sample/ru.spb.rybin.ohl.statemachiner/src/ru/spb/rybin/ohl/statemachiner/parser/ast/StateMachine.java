package ru.spb.rybin.ohl.statemachiner.parser.ast;

import java.util.List;

public interface StateMachine {
  List<StateDefinition> getStates();
  List<Transition> getTransitions();
}
