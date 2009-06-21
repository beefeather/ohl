package ru.spb.rybin.ohl.statemachiner.generator;

import java.util.ArrayList;
import java.util.List;

import ru.spb.rybin.ohl.statemachiner.parser.ast.StateDefinition;
import ru.spb.rybin.ohl.statemachiner.parser.ast.Transition;

public class StateOnSide {
  public StateOnSide(String name, Side side, StateDefinition definition) {
    this.name = name;
    this.side = side;
    this.ast = definition;
  }
  
  void init() {
    if (ast != null) {
      if (ast.getNextImmediate() != null) {
        immediate = side.getState(ast.getNextImmediate().getName());
      }
    }
  }

  public Side getSide() {
    return side;
  }

  public void addOutgoing(Edge tr) {
    outgoing.add(tr);
  }

  public void addIncoming(Transition tr) {
  }

  public String getName() {
    return name;
  }

  public void addImmediateNext(StateOnSide to) {
    if (immediate != null) {
      throw new RuntimeException("Immediate next is already defined");
    }
    immediate = to;
  } 
  
  public StateOnSide getImmediateNext() {
    return immediate;
  }
  public List<Edge> getOutgoing() {
    return outgoing;
  }
  public StateDefinition getAst() {
    return ast;
  }
  
  private final String name;
  private final Side side;
  private final StateDefinition ast;

  private List<Edge> outgoing = new ArrayList<Edge>(3);
  private StateOnSide immediate = null;
}
