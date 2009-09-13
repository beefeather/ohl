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

public class SideImplGenerator {
  SideImplGenerator(JavaPackage sideDir, String basePackageName, StateOnSide state, SideImplProperties sideProperties) throws IOException {
    this.state = state;
    this.basePackageName = basePackageName;
    javaSource = sideDir.sourceFile(state.getName() + "Impl");
    this.sideProperties = sideProperties;
  }

  void generate() {
    javaSource.println("import ru.spb.rybin.ohl.statemachiner.lib.Semaphore;");
    javaSource.println("import ru.spb.rybin.ohl.statemachiner.lib.ValueHolder;");
    javaSource.println("");
    javaSource.println("");
    
    javaSource.println("public class " + state.getName() + "Impl implements " 
        + basePackageName + sideProperties.getApiSuffix() + "." + state.getName() + " {");
    javaSource.indent();
    javaSource.println("public " + state.getName() + "Impl(ValueHolder<"
        + calculateImmediateStatePrefix() + "OutgoingData.case> returnHolder, Semaphore semaphore) {");
    javaSource.println("  this.returnHolder = returnHolder;");
    javaSource.println("  this.semaphore = semaphore;");
    javaSource.println("}");
    
    fields();
    
    
    immediateNext();
    transitions();
    outgoingEnum();
    
    javaSource.println("private final ValueHolder<" + calculateImmediateStatePrefix() + "OutgoingData.case> returnHolder;");
    javaSource.println("private final Semaphore semaphore;");
    javaSource.indentBack();
    javaSource.println("}");
    
    javaSource.close();
  }
  
  private void immediateNext() {
    if (state.getImmediateNext() != null) {
      javaSource.println("public " + basePackageName + sideProperties.getApiSuffix() + "." + state.getImmediateNext().getName() + " next() {");
      javaSource.indent();
      javaSource.println("return new " + state.getImmediateNext().getName() + "Impl(returnHolder, semaphore);");
      javaSource.indentBack();
      javaSource.println("}");
    }
  }
  
  private void fields() {
    if (state.getAst() == null) {
      return;
    }
    List<FormalParameter> list = state.getAst().getFields();
    for (FormalParameter f : list) {
      javaSource.println("public " + f.getTypeName() + " " + camelCat("get", f.getName()) + "() {");
      javaSource.println("  throw new UnsupportedOperationException();");
      javaSource.println("}");
    }
  }
  
  private void transitions() {
    List<Edge> outgoing = state.getOutgoing();
    for (Edge edge : outgoing) {
      Transition tr = edge.getTransition();
      String methodName = edge.getResolvedName();

      Collection<Edge> comingBack = edge.getComingBack();

      String returnType;
      Edge edge1 = edge;
      if (edge.getSameDestinationEdge() != null) {
        edge1 = edge.getSameDestinationEdge();
      }
      switch (edge1.getEdgeReturn()) {
        case * void_type() {
          returnType = "void";
        }
        case instanceof EdgeReturn.Direct {
          returnType = basePackageName + sideProperties.getApiSuffix() + "." + edge1.getComingBack().iterator().next().getDestination().getName();
        }
        case instanceof EdgeReturn.Struct {
          returnType = EdgeReturn.Struct.getStructName(edge1);
        }
        case instanceof EdgeReturn.EnumCase {
          returnType = EdgeReturn.EnumCase.getEnumName(edge1) + ".case";
        }
      }
      
      
      javaSource.print("public " + returnType + " " + methodName + "(");
      if (tr.getTransitionData() != null) {
        List<FormalParameter> params = tr.getTransitionData().getParamters();
        printParams(params);
      }
      javaSource.endline(") {");
      javaSource.indent();
      String holderType = "ValueHolder<" 
          + getOppositePackagePrefix() 
          + edge.getDestination().getFinalImmegiate().getName() + "Impl.OutgoingData.case>";
      javaSource.println(holderType + " nextResult = ");
      javaSource.println("    new " + holderType + "();");

      javaSource.print("returnHolder.setValue(new OutgoingData." + methodName + "(");
      if (tr.getTransitionData() != null) {
        List<FormalParameter> params = tr.getTransitionData().getParamters();
        for (FormalParameter p1 : params) {
          javaSource.append(p1.getName() + ", ");
        }
      }
      javaSource.endline("nextResult));");

      javaSource.println("semaphore.yield" + sideProperties.getSemaphoreSuffix() + "();");
      
      if (!comingBack.isEmpty()) {
        javaSource.println("switch(nextResult.getValue()) {");
        javaSource.indent();
  
        for (final Edge returnEdge : comingBack) {
          javaSource.print("case * " + returnEdge.getResolvedName() + "(");
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
              return "holder";
            }
            @Override
            public String getTypeName() {
              return "ValueHolder<" + basePackageName + sideProperties.getImplSuffix() + "." + returnEdge.getDestination().getFinalImmegiate().getName() + "Impl.OutgoingData.case>";
            }
          });
          printParams(params2);
          javaSource.endline(") {");
          javaSource.indent();
          javaSource.print("return ");
          String stateConstructor = "new " +  returnEdge.getDestination().getName() + "Impl(holder, semaphore)";
          switch (edge1.getEdgeReturn()) {
            case * void_type() {
              javaSource.endline(";");
            }
            case instanceof EdgeReturn.Struct {
              javaSource.append("new " + EdgeReturn.Struct.getStructName(edge1) + "(");
              for (FormalParameter p1 : trParams) {
                javaSource.append(p1.getName() + ", ");
              }
              javaSource.endline("");
              javaSource.println("    " + stateConstructor + ");");
            }
            case instanceof EdgeReturn.Direct {
              javaSource.endline(stateConstructor + ";");
            }
            case instanceof EdgeReturn.EnumCase{
              javaSource.append(EdgeReturn.EnumCase.getEnumName(edge1) + "." + returnEdge.getResolvedName() + "(");
              for (FormalParameter p1 : trParams) {
                javaSource.append(p1.getName() + ", ");
              }
              javaSource.endline("");
              javaSource.println("    " + stateConstructor + ");");
            }
          }
          
          javaSource.indentBack();
          javaSource.println("}");
        }
        
        javaSource.indentBack();
        javaSource.println("}");

      }        
      javaSource.indentBack();
      javaSource.println("}");
    }
  }
  
  private void outgoingEnum() {
    if (state.getImmediateNext() != null) {
      return;
    }
    javaSource.println("");
    javaSource.println("public static enum-case OutgoingData {");
    boolean isFirst = true;
    javaSource.indent();
    
    List<Edge> outgoing = state.getOutgoing();
    for (final Edge edge : outgoing) {
      if (!isFirst) {
        javaSource.endline(",");
      }

      javaSource.print("case " + edge.getResolvedName() + "(");
      Transition tr = edge.getTransition();
      TransitionData data = tr.getTransitionData();
      
      List<FormalParameter> trParams;
      if (data == null) {
        trParams = Collections.emptyList();
      } else {
        trParams = data.getParamters();
      }
      List<FormalParameter> params2 = new ArrayList<FormalParameter>();
      params2.addAll(trParams);
      params2.add(new FormalParameter() {
        @Override
        public String getName() {
          return "holder";
        }
        @Override
        public String getTypeName() {
          return "ValueHolder<" + getOppositePackagePrefix() + edge.getDestination().getFinalImmegiate().getName() + "Impl.OutgoingData.case>";
        }
      });
      printParams(params2);
      javaSource.append(")");
      isFirst = false;
    }
    if (!isFirst) {
      javaSource.endline("");
    }
    javaSource.indentBack();
    javaSource.println("}");
    javaSource.println("");
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
  
  private String getOppositePackagePrefix() {
    return basePackageName + sideProperties.getOpposite().getImplSuffix() + ".";
  }
  
  private String calculateImmediateStatePrefix() {
    if (state.getImmediateNext() == null) {
      return "";
    }
    StateOnSide current = state.getImmediateNext();
    while (current.getImmediateNext() != null) {
      current = current.getImmediateNext();
    }
    return current.getName() + "Impl.";
  }
  
  private static String camelCat(String prefix, String name) {
    return GeneratorUtil.camelCat(prefix, name);
  }
  
  private final StateOnSide state;
  private final JavaSource javaSource;
  private final String basePackageName;
  private final SideImplProperties sideProperties;
}
