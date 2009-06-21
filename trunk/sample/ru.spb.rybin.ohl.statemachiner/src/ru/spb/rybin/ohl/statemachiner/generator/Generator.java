package ru.spb.rybin.ohl.statemachiner.generator;

import java.io.File;
import java.io.IOException;

import ru.spb.rybin.ohl.statemachiner.parser.ast.StateMachine;

public class Generator {
  public static void generate(StateMachine inp, String outputDir, String packageName) throws IOException {
    Graph graph = Graph.build(inp);
    
    JavaPackage mainPackage = new JavaPackage(new File(outputDir), packageName);
    JavaPackage apiDir = mainPackage.subpackage("api");
    for (Side side : graph.getSides()) {
      if (true ||    side.getName() == "client") {
        buildSideInterfaces(apiDir, side);
      }
    }
  }
  
  public static void buildSideInterfaces(JavaPackage apiDir, Side side) throws IOException {
    JavaPackage sideDir = apiDir.subpackage(side.getName());
    for (StateOnSide state : side.getStates()) {
      new SideInterfaceGenerator(sideDir, state).generate();
    }
  }
}
