package ru.spb.rybin.ohl.statemachiner.generator;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.spb.rybin.ohl.statemachiner.parser.StateQualifier;
import ru.spb.rybin.ohl.statemachiner.parser.ast.StateMachine;
import ru.spb.rybin.ohl.statemachiner.parser.ast.StateOnSideReference;
import ru.spb.rybin.ohl.statemachiner.parser.ast.Transition;

public class Graph {
  public static Graph build(StateMachine ast) {

    final StateRepository stateRepository = new StateRepository(ast.getStates());

    class SideRepository {
      Side get(String name) {
        Side res = map.get(name);
        if (res == null) {
          res = new Side(name, stateRepository);
          map.put(name, res);
        }
        return res;
      }
      private final Map<String, Side> map = new HashMap<String, Side>(2);
    }
    final SideRepository sideRepository = new SideRepository();
    
    class Util {
      StateOnSide getState(StateOnSideReference ref) {
        Side side = sideRepository.get(getSideName(ref.getQualifier()));
        return side.getState(ref.getState().getName());
      }
    }
    Util util = new Util();

    List<Transition> transitions = ast.getTransitions();

    for (final Transition tr : transitions) {
      StateOnSide from = util.getState(tr.getFrom());
      final StateOnSide to = util.getState(tr.getTo());
      if (from.getSide() == to.getSide()) {
        if (tr.getTransitionData() != null) {
          throw new RuntimeException("Same side immediate transition shouldn't have any transition data at " + tr.getPosition());
        }
        from.addImmediateNext(to);
      } else {
        Edge edge = new Edge() {
          @Override
          public StateOnSide getDestination() {
            return to;
          }
          @Override
          public Transition getTransition() {
            return tr;
          }
        };
        from.addOutgoing(edge);
      }
    }
    
    return new Graph(sideRepository.map.values());
  }
  
  public Graph(Collection<Side> sides) {
    this.sides = sides;
    
    for (Side side : sides) {
      side.analyze();
    }
  }

  Collection<Side> getSides() {
    return sides;
  }
  
  private final Collection<Side> sides;
  
  private static String getSideName(StateQualifier.case side) {
    switch (side) {
    case * client() {
      return "client";
    }
    case * server() {
      return "server";
    }
    }
  }
}
