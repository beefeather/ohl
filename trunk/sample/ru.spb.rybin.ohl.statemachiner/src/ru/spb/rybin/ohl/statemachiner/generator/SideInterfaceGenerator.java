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
    
    if (state.getImmediateNext() != null && !state.getOutgoing().isEmpty()) {
      throw new RuntimeException("Both outgoing transitions and immediate next");
    }
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
    Map<StateOnSide, String> destination2EnumName = new HashMap<StateOnSide, String>(0);
    
    List<Edge> outgoing = state.getOutgoing();
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

      Collection<Edge> comingBack = Analyze.findReturningEdges(
          edge, state.getSide());
      
      String returnType;
      String enumName = null;

      if (comingBack.size() == 0) {
        returnType = "void";
      } else if (comingBack.size() == 1) {
        returnType = comingBack.iterator().next().getDestination().getName();
      } else {
        
        String alreadyGenerated = destination2EnumName.get(edge.getDestination());
        if (alreadyGenerated == null) {
          returnType = camelCat("ReturnCase", methodName);
          enumName = returnType; 
        } else {
          returnType = alreadyGenerated;
        }
      }
      
      javaSource.print(returnType + " " + methodName + "(");
      if (tr.getTransitionData() != null) {
        List<FormalParameter> params = tr.getTransitionData().getParamters();
        printParams(params);
      }
      javaSource.endline(");");
      
      if (enumName != null) {
        javaSource.println("");
        javaSource.println("enum-case " + enumName + "{");
        boolean isFirst = true;
        javaSource.indent();
        for (final Edge returnEdge : comingBack) {
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
        
        destination2EnumName.put(edge.getDestination(), enumName);
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
        javaSource.print(p1.getTypeName() + " " + p1.getName());
      }
    }
  }
  
  private static String camelCat(String prefix, String name) {
    if (Character.isLowerCase(name.charAt(0))) {
      return prefix + Character.toUpperCase(name.charAt(0)) + name.substring(1);
    } else {
      return prefix + "_" + name;
    }
  }
  
  private final StateOnSide state;
  private final JavaSource javaSource;
}
