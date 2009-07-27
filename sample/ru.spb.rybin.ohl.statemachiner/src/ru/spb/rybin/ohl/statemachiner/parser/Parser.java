package ru.spb.rybin.ohl.statemachiner.parser;

import java.util.ArrayList;
import java.util.List;

import ru.spb.rybin.ohl.lang.OhlClass;
import ru.spb.rybin.ohl.statemachiner.parser.ast.ConnectorDirection;
import ru.spb.rybin.ohl.statemachiner.parser.ast.FormalParameter;
import ru.spb.rybin.ohl.statemachiner.parser.ast.StateDefinition;
import ru.spb.rybin.ohl.statemachiner.parser.ast.StateMachine;
import ru.spb.rybin.ohl.statemachiner.parser.ast.StateOnSideReference;
import ru.spb.rybin.ohl.statemachiner.parser.ast.StateReference;
import ru.spb.rybin.ohl.statemachiner.parser.ast.Transition;
import ru.spb.rybin.ohl.statemachiner.parser.ast.TransitionData;

public class Parser {
  private final Lexer lexer;

  public static StateMachine parse(String contents) throws ParserException {
    Lexer lexer = new Lexer(contents);
    Parser parser = new Parser(lexer);
    return parser.parse();
  }
  
  public Parser(Lexer lexer) {
    this.lexer = lexer;
  }

  StateMachine parse() throws ParserException {
    final List<StateDefinition> states = new ArrayList<StateDefinition>();
    final List<Transition> transitions = new ArrayList<Transition>();
    while (lexer.peek() != TokensEof.eof) {
      switch (lexer.peek()) {
      case * client() {
        transitions.add(parseTransition());
      }
      case * server() {
        transitions.add(parseTransition());
      }
      case * state() {
        states.add(parseStateDefinition());
      }
      default * {
        throw new AutoParserException("Unknown token " + lexer.peek());
      }
      }
    }
    return new StateMachine() {
      @Override
      public List<StateDefinition> getStates() {
        return states;
      }
      @Override
      public List<Transition> getTransitions() {
        return transitions;
      }
    };
  }
  
  private StateDefinition parseStateDefinition() throws ParserException {
    lexer.consume();
    final String name = expectIdentifier();
    final StateReference nextState;
    if (optionalToken(ConnectorTokens.toRight)) {
      nextState = parseStateReference();
    } else {
      nextState = null;
    }
    expectToken(Tokens.openBrace);
    final List<FormalParameter> fields = parseStateBody();
    expectToken(Tokens.closeBrace);
    
    return new StateDefinition() {
      @Override
      public List<FormalParameter> getFields() {
        return fields;
      }
      @Override
      public StateReference getNextImmediate() {
        return nextState;
      }
      @Override
      public String getName() {
        return name;
      }
    };
  }

  List<FormalParameter> parseStateBody() throws ParserException {
    List<FormalParameter> result = new ArrayList<FormalParameter>();
    while (true) {
      FormalParameter next = parseFormalParameter();
      if (next == null) {
        break;
      }
      result.add(next);
      expectToken(Tokens.semicolon);
    }
    return result;
  }
  
  private FormalParameter parseFormalParameter() throws ParserException {
    final String typeName = optionalIdentifier();
    if (typeName == null) {
      return null;
    }
    final String name = expectIdentifier();
    return new FormalParameter() {
      @Override
      public String getName() {
        return name;
      }
      @Override
      public String getTypeName() {
        return typeName;
      }
    };
  }
  
  private Transition parseTransition() throws ParserException {
    class Side implements StateOnSideReference {
      @Override
      public StateReference getState() {
        return state;
      }
      @Override
      public StateQualifier.case getQualifier() {
        return qualifier;
      }
      StateQualifier.case qualifier;
      StateReference state;
      ConnectorTokens.case connector;
    }

    final Position position = lexer.currentPosition();
    Side side1 = new Side();
    side1.qualifier = expectTokenClass(StateQualifier.ohl_class);
    side1.state = parseStateReference();
    side1.connector = expectTokenClass(ConnectorTokens.ohl_class);
    final TransitionData transitionData = parseTransitionData();
    Side side2 = new Side();
    if (transitionData == null) {
      side2.connector = side1.connector;
    } else {
      side2.connector = expectTokenClass(ConnectorTokens.ohl_class);
    }
    side2.qualifier = expectTokenClass(StateQualifier.ohl_class);
    side2.state = parseStateReference();
    
    expectToken(Tokens.semicolon);

    final Side sideFrom;
    final Side sideTo;
    
    ConnectorDirection.case direction = getConnectorDirection(side1.connector); 
    if (direction != getConnectorDirection(side2.connector)) {
      throw new AutoParserException("Differently directed connectors", position);
    }
    
    switch (direction) {
    case * left() {
      sideFrom = side2;
      sideTo = side1;
    }
    case * right() {
      sideFrom = side1;
      sideTo = side2;
    }
    }

    final boolean isFork = isForkConnector(sideFrom.connector);
    if (isFork && transitionData == null) {
      throw new AutoParserException("Fork connector requires transition data", position);
    }

    if (isForkConnector(sideTo.connector)) {
      throw new AutoParserException("Second connector shouldn't be forking", position);
    }
    
    return new Transition() {
      @Override
      public StateOnSideReference getFrom() {
        return sideFrom;
      }
      @Override
      public StateOnSideReference getTo() {
        return sideTo;
      }
      @Override
      public boolean isFork() {
        return isFork;
      }
      @Override
      public TransitionData getTransitionData() {
        return transitionData;
      }
      @Override
      public Position getPosition() {
        return position;
      }
    };
  }
  
  private ConnectorDirection.case getConnectorDirection(ConnectorTokens.case connector) {
    switch (connector) {
    case * toLeft() {
      return ConnectorDirection.left;
    }
    case * toRight() {
      return ConnectorDirection.right;
    }
    case * toLeftFork() {
      return ConnectorDirection.left;
    }
    case * toRightFork() {
      return ConnectorDirection.right;
    }
    }
  }
  
  private boolean isForkConnector(ConnectorTokens.case connector) {
    switch (connector) {
    case * toLeft() {
      return false;
    }
    case * toRight() {
      return false;
    }
    case * toLeftFork() {
      return true;
    }
    case * toRightFork() {
      return true;
    }
    }
  }
  
  private TransitionData parseTransitionData() throws ParserException {
    final String name = optionalIdentifier();
    if (name == null) {
      if (!optionalToken(Tokens.openParen)) {
        return null;
      }
    } else {
      expectToken(Tokens.openParen);
    }
    final List<FormalParameter> params = new ArrayList<FormalParameter>();
    FormalParameter parameter = parseFormalParameter();
    if (parameter != null) {
      params.add(parameter);
      while (optionalToken(Tokens.comma)) {
        parameter = parseFormalParameter();
        if (parameter == null) {
          throw new AutoParserException("Parameter expected");
        }
        params.add(parameter);
      }
    }
    expectToken(Tokens.closeParen);
    return new TransitionData() {
      @Override
      public String getMethodName() {
        return name;
      }
      @Override
      public List<FormalParameter> getParamters() {
        return params;
      }
    };
  }
  
  private StateReference parseStateReference() throws ParserException {
    final String name = expectIdentifier();
    return new StateReference() {
      @Override
      public String getName() {
        return name;
      }
    };
  }
  
  private String expectIdentifier() throws ParserException {
    switch (lexer.peek()) {
    case * identifier(String id) {
      lexer.consume();
      return id;
    }
    default * {
      throw new AutoParserException("Identifier expected");
    }
    }
  }
  private void expectToken(Tokens.case token) throws ParserException {
    if (lexer.peek() != token) {
      throw new AutoParserException("Token " + token + " expected");
    }
    lexer.consume();
  }
  private boolean optionalToken(Tokens.case token) {
    if (lexer.peek() == token) {
      lexer.consume();
      return true;
    } else {
      return false;
    }
  }
  private String optionalIdentifier() {
    switch (lexer.peek()) {
    case * identifier(String id) {
      lexer.consume();
      return id;
    }
    default * {
      return null;
    }
    }
  }

  private <T> T.case expectTokenClass(OhlClass<? super T> tokenClass) {
    T.case res = tokenClass.cast_if(lexer.peek());
    if (res != null) {
      lexer.consume();
    }
    return res;
  }
  
  private Tokens.case expectTokenClass2(OhlClass<? super Tokens> tokenClass) {
    Tokens.case res = tokenClass.cast_if(lexer.peek());
    if (res != null) {
      lexer.consume();
    }
    return res;
  }
  
  private class AutoParserException extends ParserException {
    private static final long serialVersionUID = 1L;

    AutoParserException(String errorMessage) {
      this(errorMessage, lexer.currentPosition());
    }
    AutoParserException(String errorMessage, Position position) {
      super(errorMessage + " at " + position);
    }
  }
}
