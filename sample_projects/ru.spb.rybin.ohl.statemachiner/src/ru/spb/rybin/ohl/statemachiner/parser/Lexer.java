package ru.spb.rybin.ohl.statemachiner.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

public class Lexer {
  Lexer(final String text) {
    tokens = new ArrayList<Tokens.case>();
    positions = new ArrayList<Position>();

    class BuildSession {
      int p = 0;
      int lastLineBegin = 0;
      int lineNumber = 1;

      {
        
        while (p < text.length()) {
          final int nextP;
          nextStep: {
            if (text.charAt(p) == '"') {
              int p1 = text.indexOf('"', p + 1);
              if (p1 == -1) {
                throw new RuntimeException("Failed to find closing quote");
              }
              String identifier = text.substring(p + 1, p1);
              putToken(new Tokens.identifier(identifier));
              nextP = p1 + 1;
              break nextStep;
            }
            if (Character.isWhitespace(text.charAt(p))) {
              if (text.charAt(p) == '\n') {
                lastLineBegin = p + 1;
                lineNumber++;
              }
              nextP = p + 1;
              break nextStep;
            }
            Map<String, Tokens.case> submap = simpleTokens2.get(text.charAt(p));
            if (submap != null) {
              for (Map.Entry<String, Tokens.case> en : submap.entrySet()) {
                if (text.startsWith(en.getKey(), p + 1)) {
                  putToken(en.getValue());
                  nextP = p + 1 + en.getKey().length();
                  break nextStep;
                }
              }
            }
            if (Character.isJavaIdentifierStart(text.charAt(p))) {
              int p1 = p + 1;
              while (p1 < text.length() && Character.isJavaIdentifierPart(text.charAt(p1))) {
                p1++;
              }
              String identifier = text.substring(p, p1);
              putToken(new Tokens.identifier(identifier));
              nextP = p1;
              break nextStep;
            }
            throw new RuntimeException("Unexpected char " + text.charAt(p) + " at " + createPosition());
          }
          p = nextP;
        }
      }
      private Position createPosition() {
        final int line = lineNumber;
        final int col = p - lastLineBegin + 1;
        return new Position() {
          @Override
          public String toString() {
            return line + ":" + col;
          }
        };
      }
      private void putToken(Tokens.case token) {
        tokens.add(token);
        positions.add(createPosition());
      }
    }

    new BuildSession();
    
    pos = 0;
  }
  public TokensEof.case peek() {
    if (pos < tokens.size()) {
      return tokens.get(pos);
    } else {
      return TokensEof.eof; 
    }
  }
  public void consume() {
    if (pos < tokens.size()) {
      pos++;
    } else {
      throw new NoSuchElementException("No more tokens"); 
    }
  }
  public Position currentPosition() {
    return positions.get(pos);
  }

  private final ArrayList<Tokens.case> tokens;
  private final ArrayList<Position> positions;

  private int pos;
  private static final Map<Character, Map<String, Tokens.case>> simpleTokens2;
  static {
    Map<String, Tokens.case> simpleTokens = new HashMap<String, Tokens.case>();
    simpleTokens.put(">>", ConnectorTokens.toRight);
    simpleTokens.put("<<", ConnectorTokens.toLeft);
    simpleTokens.put("->", ConnectorTokens.toRightFork);
    simpleTokens.put("<-", ConnectorTokens.toLeftFork);
    simpleTokens.put("client", StateQualifier.client);
    simpleTokens.put("server", StateQualifier.server);
    simpleTokens.put("state", Tokens.state);
    simpleTokens.put("(", Tokens.openParen);
    simpleTokens.put(")", Tokens.closeParen);
    simpleTokens.put("{", Tokens.openBrace);
    simpleTokens.put("}", Tokens.closeBrace);
    simpleTokens.put(",", Tokens.comma);
    simpleTokens.put(";", Tokens.semicolon);
    
    simpleTokens2 = new HashMap<Character, Map<String,Tokens.case>>();
    
    for (Map.Entry<String, Tokens.case> en : simpleTokens.entrySet()) {
      char ch = en.getKey().charAt(0);
      String suffix = en.getKey().substring(1);
      Map<String,Tokens.case> submap = simpleTokens2.get(ch);
      if (submap == null) {
        submap = new TreeMap<String, Tokens.case>();
        simpleTokens2.put(ch, submap);
      }
      Object conflict = submap.put(suffix, en.getValue());
      if (conflict != null) {
        throw new RuntimeException();
      }
    }
  }
}
