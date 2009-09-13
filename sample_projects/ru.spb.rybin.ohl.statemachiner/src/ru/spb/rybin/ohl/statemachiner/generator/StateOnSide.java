package ru.spb.rybin.ohl.statemachiner.generator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.spb.rybin.ohl.statemachiner.parser.ast.FormalParameter;
import ru.spb.rybin.ohl.statemachiner.parser.ast.StateDefinition;
import ru.spb.rybin.ohl.statemachiner.parser.ast.Transition;
import ru.spb.rybin.ohl.statemachiner.parser.ast.TransitionData;

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
  
  public StateOnSide getFinalImmegiate() {
    if (immediate == null) {
      return this;
    } else {
      return immediate.getFinalImmegiate();
    }
  }
  
  void analyze() {
    if (getImmediateNext() != null && !getOutgoing().isEmpty()) {
      throw new RuntimeException("Both outgoing transitions and immediate next");
    }
    
    Map<StateOnSide, Edge> destination2Edge = new HashMap<StateOnSide, Edge>(5);
    for (Edge edge : outgoing) {
      Transition tr = edge.getTransition();
      TransitionData data = tr.getTransitionData();
      String methodName;
      if (outgoing.size() == 1) {
        methodName = "next";
      } else {
        if (!tr.isFork()) {
          throw new RuntimeException("Transition not declared as fork");
        }
        if (tr.getTransitionData() == null) {
          throw new RuntimeException("Transition without data");
        }
        if (data.getMethodName() == null) {
          throw new RuntimeException("Transition with default name");
        }
        methodName = data.getMethodName();
      }
      edge.setResolvedName(methodName);
      
      Edge sameDestination = destination2Edge.get(edge.getDestination());
      if (sameDestination == null) {
        destination2Edge.put(edge.getDestination(), edge);
      } else {
        edge.setSameDestinationEdge(sameDestination);
      }

    
      Collection<Edge> comingBack = Analyze.findReturningEdges(edge, getSide());
      edge.setComingBack(comingBack);
      EdgeReturn.Type.case returnType;
      if (comingBack.isEmpty()) {
        returnType = EdgeReturn.Type.void_type;
      } else if (comingBack.size() == 1) {
        final Transition transition = comingBack.iterator().next().getTransition();
        TransitionData transitionData = transition.getTransitionData();
        if (transitionData == null) {
          returnType = new EdgeReturn.Direct();
        } else if (transitionData.getParamters().isEmpty()) {
          returnType = new EdgeReturn.Direct();
        } else {
          List<FormalParameter> fields = new ArrayList<FormalParameter>();
          fields.addAll(transitionData.getParamters());
          fields.add(new FormalParameter() {
            @Override
            public String getName() {
              return "nextStep";
            }
            @Override
            public String getTypeName() {
              return transition.getTo().getState().getName();
            }
          });
          returnType = new EdgeReturn.Struct(fields);
        }
      } else {
        returnType = new EdgeReturn.EnumCase();
      }
      edge.setEdgeReturn(returnType);
    }
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
