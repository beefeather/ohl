package ru.spb.rybin.ohl.statemachiner.generator;

import java.util.List;

import ru.spb.rybin.ohl.statemachiner.parser.ast.FormalParameter;

public interface EdgeReturn {
  enum-case Type {
    case void_type(),
    Direct,
    Struct,
    EnumCase
  }
  
  class Direct implements case {
  }
  
  class Struct implements case {
    Struct(List<? extends FormalParameter> parameters) {
      this.parameters = parameters;
    }
    
    private final List<? extends FormalParameter> parameters;
    
    public List<? extends FormalParameter> getParameters() {
      return parameters;
    }
    static String getStructName(Edge edge) {
      return GeneratorUtil.camelCat("ReturnStruct", edge.getResolvedName()); 
    }
  }
  
  class EnumCase implements case {
    static String getEnumName(Edge edge) {
      return GeneratorUtil.camelCat("ReturnCase", edge.getResolvedName()); 
    }
  }
}
