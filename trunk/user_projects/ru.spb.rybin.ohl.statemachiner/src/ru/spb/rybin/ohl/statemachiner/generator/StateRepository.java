package ru.spb.rybin.ohl.statemachiner.generator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.spb.rybin.ohl.statemachiner.parser.ast.StateDefinition;

class StateRepository {
  StateRepository(List<StateDefinition> astStates) {
    states = new HashMap<String, StateDefinition>(astStates.size());
    
    for (StateDefinition st : astStates) {
      states.put(st.getName(), st);
    }
  }

  public StateDefinition getState(String name) {
    StateDefinition res = states.get(name);
    return res;
  }
  
  private final Map<String, StateDefinition> states;
}
