package ru.spb.rybin.ohl.statemachiner.parser.ast;

import java.util.List;

public interface TransitionData {
  String getMethodName();
  List<FormalParameter> getParamters();
}
