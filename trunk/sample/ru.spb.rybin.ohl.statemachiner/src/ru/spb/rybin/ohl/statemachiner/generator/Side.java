package ru.spb.rybin.ohl.statemachiner.generator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ru.spb.rybin.ohl.statemachiner.parser.ast.StateDefinition;

class Side {
  Side(String name, StateRepository stateRepositiory) {
    this.name = name;
    this.stateRepositiory = stateRepositiory;
  }
  
  StateOnSide getState(String name) {
    StateOnSide res = states.get(name);
    if (res == null) {
      StateDefinition definition = stateRepositiory.getState(name);
      res = new StateOnSide(name, this, definition);
      states.put(name, res);
      // Make sure we are in map already
      res.init();
    }
    return res;
  }

  public String getName() {
    return name;
  }

  public Collection<StateOnSide> getStates() {
    return states.values();
  }

  public void analyze() {
    for (StateOnSide st : getStates()) {
      st.analyze();
    }
  } 
  
  @Override
  public String toString() {
    return name;
  }

  private final String name;
  private final StateRepository stateRepositiory;
  private final Map<String, StateOnSide> states = new HashMap<String, StateOnSide>();
}
