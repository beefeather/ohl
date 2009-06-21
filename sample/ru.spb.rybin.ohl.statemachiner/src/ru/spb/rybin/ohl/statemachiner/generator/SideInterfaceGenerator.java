package ru.spb.rybin.ohl.statemachiner.generator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.spb.rybin.ohl.statemachiner.parser.ast.FormalParameter;
import ru.spb.rybin.ohl.statemachiner.parser.ast.Transition;
import ru.spb.rybin.ohl.statemachiner.parser.ast.TransitionData;

class SideInterfaceGenerator {
  SideInterfaceGenerator(JavaPackage sideDir, StateOnSide state) throws IOException {
    this.state = state;
    javaSource = sideDir.sourceFile(state.getName()); 
  }

  void generate() {
    javaSource.println("public interface " + state.getName() + " {");
    javaSource.indent();
    fields();
    
    immediateNext();
    transitions();
    javaSource.indentBack();
    javaSource.println("}");
    
    javaSource.close();
  }
  
  private void immediateNext() {
    if (state.getImmediateNext() != null) {
      javaSource.println(state.getImmediateNext().getName() + " next();");
    }
  }
  
  private void fields() {
    if (state.getAst() == null) {
      return;
    }
    List<FormalParameter> list = state.getAst().getFields();
    for (FormalParameter f : list) {
      javaSource.println(f.getTypeName() + " " + camelCat("get", f.getName()) + "();");
    }
  }
  
  private void transitions() {
    List<Edge> outgoing = state.getOutgoing();
    for (Edge edge : outgoing) {
      Transition tr = edge.getTransition();
      String methodName = edge.getResolvedName();

      javaSource.print(getReturnTypeName(edge) + " " + methodName + "(");
      if (tr.getTransitionData() != null) {
        List<FormalParameter> params = tr.getTransitionData().getParamters();
        printParams(params);
      }
      javaSource.endline(");");

      if (edge.getSameDestinationEdge() == null) {
        switch (edge.getEdgeReturn()) {
          case * void_type() {
          }
          case instanceof EdgeReturn.Struct {
            javaSource.println("class " + EdgeReturn.Struct.getStructName(edge) + "{");
            javaSource.println("}");
          }
          case instanceof EdgeReturn.Direct {
          }
          case instanceof EdgeReturn.EnumCase{
            String enumName = EdgeReturn.EnumCase.getEnumName(edge);
            javaSource.println("");
            javaSource.println("enum-case " + enumName + "{");
            boolean isFirst = true;
            javaSource.indent();
            for (final Edge returnEdge : edge.getComingBack()) {
              if (!isFirst) {
                javaSource.endline(",");
              }
              
              javaSource.print("case " + returnEdge.getTransition().getTransitionData().getMethodName() + "(");
              List<FormalParameter> trParams;
              if (returnEdge.getTransition().getTransitionData() == null) {
                trParams = Collections.emptyList();
              } else {
                trParams = returnEdge.getTransition().getTransitionData().getParamters();
              }
              List<FormalParameter> params2 = new ArrayList<FormalParameter>();
              params2.addAll(trParams);
              params2.add(new FormalParameter() {
                @Override
                public String getName() {
                  return "state";
                }
                @Override
                public String getTypeName() {
                  return returnEdge.getDestination().getName();
                }
              });
              printParams(params2);
              javaSource.append(")");
              isFirst = false;
            }
            javaSource.endline("");
            javaSource.indentBack();
            javaSource.println("}");
            javaSource.println("");
          }
        }
      }
    }
  }
  
  private String getReturnTypeName(Edge edge) {
    if (edge.getSameDestinationEdge() != null) {
      edge = edge.getSameDestinationEdge();
    }
    switch (edge.getEdgeReturn()) {
    case * void_type() {
      return "void";
    }
    case instanceof EdgeReturn.Struct {
      return EdgeReturn.Struct.getStructName(edge);
    }
    case instanceof EdgeReturn.Direct {
      return edge.getComingBack().iterator().next().getDestination().getName();
    }
    case instanceof EdgeReturn.EnumCase{
      String enumName = EdgeReturn.EnumCase.getEnumName(edge);
      return enumName + ".case";
    }
    }
  }
  
  private void printParams(List<FormalParameter> params) {
    if (params.size() > 1) {
      javaSource.endline("");
      javaSource.indent();
      for (int i = 0; i < params.size(); i++) {
        if (i != 0) {
          javaSource.endline(",");
        }
        FormalParameter p1 = params.get(i);
        javaSource.print(p1.getTypeName() + " " + p1.getName());
      }
      javaSource.indentBack();
    } else {
      if (params.size() == 1) {
        FormalParameter p1 = params.get(0);
        javaSource.append(p1.getTypeName() + " " + p1.getName());
      }
    }
  }
  
  private static String camelCat(String prefix, String name) {
    return GeneratorUtil.camelCat(prefix, name);
  }
  
  private final StateOnSide state;
  private final JavaSource javaSource;
}
