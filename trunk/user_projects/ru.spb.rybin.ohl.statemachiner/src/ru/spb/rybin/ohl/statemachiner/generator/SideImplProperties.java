package ru.spb.rybin.ohl.statemachiner.generator;

public interface SideImplProperties {
  String getApiSuffix();
  String getImplSuffix();

  String getSemaphoreSuffix();
  
  SideImplProperties getOpposite();
}
