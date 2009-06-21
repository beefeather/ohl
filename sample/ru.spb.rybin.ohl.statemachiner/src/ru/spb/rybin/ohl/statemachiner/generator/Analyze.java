package ru.spb.rybin.ohl.statemachiner.generator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class Analyze {
  static Collection<Edge> findReturningEdges(Edge edge, final Side side) {
    final List<Edge> result = new ArrayList<Edge>(3); 
    final Set<StateOnSide> visited = new HashSet<StateOnSide>(3); 

    class Recursion {
      void go(Edge edge) {
        reach(edge.getDestination(), edge);
      }
      void next(StateOnSide st) {
        for (Edge edge : st.getOutgoing()) {
          go(edge);
        }
        if (st.getImmediateNext() != null) {
          reach(st.getImmediateNext(), null);
        }
      }
      void reach(StateOnSide st, Edge edge) {
        boolean res = visited.add(st);
        if (res) {
          if (st.getSide() == side) {
            if (edge == null) {
              throw new RuntimeException("Should have come here by edge");
            }
            result.add(edge);
          } else {
            next(st);
          }
        }
      }
    }
    
    new Recursion().go(edge);
    return result;
  }
}
