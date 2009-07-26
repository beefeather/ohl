package ru.spb.rybin.ohl.statemachiner.generator;

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
