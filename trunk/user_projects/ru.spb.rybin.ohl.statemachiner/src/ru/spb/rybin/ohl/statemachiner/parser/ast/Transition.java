package ru.spb.rybin.ohl.statemachiner.parser.ast;

import ru.spb.rybin.ohl.statemachiner.parser.Position;



public interface Transition {
  StateOnSideReference getFrom();
  StateOnSideReference getTo();
  boolean isFork();
  TransitionData getTransitionData();
  Position getPosition();
}
