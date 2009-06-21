package ru.spb.rybin.ohl.statemachiner.generator;

import java.util.Collection;
import java.util.List;

import ru.spb.rybin.ohl.statemachiner.parser.ast.Transition;

public abstract class Edge {
  abstract StateOnSide getDestination();
  abstract Transition getTransition();
  
  String getResolvedName() {
    return resolvedName;
  }
  void setResolvedName(String resolvedName) {
    this.resolvedName = resolvedName;
  }
  
  public Edge getSameDestinationEdge() {
    return sameDestinationEdge;
  }
  public void setSameDestinationEdge(Edge sameDestinationEdge) {
    this.sameDestinationEdge = sameDestinationEdge;
  }
  public Collection<Edge> getComingBack() {
    return comingBack;
  }
  public void setComingBack(Collection<Edge> comingBack) {
    this.comingBack = comingBack;
  }
  public EdgeReturn.Type.case getEdgeReturn() {
    return edgeReturn;
  }
  public void setEdgeReturn(EdgeReturn.Type.case edgeReturn) {
    this.edgeReturn = edgeReturn;
  }

  private String resolvedName;
  private Edge sameDestinationEdge;
  private Collection<Edge> comingBack;
  private EdgeReturn.Type.case edgeReturn;
}
