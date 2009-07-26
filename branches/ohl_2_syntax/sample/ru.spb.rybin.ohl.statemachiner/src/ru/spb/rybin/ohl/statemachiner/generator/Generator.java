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
    JavaPackage implDir = mainPackage.subpackage("impl");
    for (Side side : graph.getSides()) {
      if (side.getName().equals("client")) {
        buildSideImplementation(implDir, packageName, side, clientProperties);
      } else if (side.getName().equals("server")) {
        buildSideImplementation(implDir, packageName, side, serverProperties);
      } else {
        throw new RuntimeException("Unsupported side");
      }
    }
  }
  
  public static void buildSideInterfaces(JavaPackage apiDir, Side side) throws IOException {
    JavaPackage sideDir = apiDir.subpackage(side.getName());
    for (StateOnSide state : side.getStates()) {
      new SideInterfaceGenerator(sideDir, state).generate();
    }
  }
  public static void buildSideImplementation(JavaPackage implDir, String basePackageName, Side side, SideImplProperties sideProperties) throws IOException {
    JavaPackage sideDir = implDir.subpackage(side.getName());
    for (StateOnSide state : side.getStates()) {
      new SideImplGenerator(sideDir, basePackageName, state, sideProperties).generate();
    }
  }
  
  private final static SideImplProperties clientProperties = new SideImplProperties() {
    @Override
    public String getApiSuffix() {
      return ".api.client";
    }
    @Override
    public String getImplSuffix() {
      return ".impl.client";
    }
    @Override
    public String getSemaphoreSuffix() {
      return "A";
    }
    @Override
    public SideImplProperties getOpposite() {
      return serverProperties;
    }
  };
  private final static SideImplProperties serverProperties = new SideImplProperties() {
    @Override
    public String getApiSuffix() {
      return ".api.server";
    }
    @Override
    public String getImplSuffix() {
      return ".impl.server";
    }
    @Override
    public String getSemaphoreSuffix() {
      return "B";
    }
    @Override
    public SideImplProperties getOpposite() {
      return clientProperties;
    }
  };
}
