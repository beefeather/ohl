# Project OHL (Охламон) #

Adding sub-type switch to Java (actual syntax):

Type declaration
```
  enum-case Figures {
    Circle,
    Square  
  }
```

Client code
```
Circle circle = ...;
Square square = ...;
final Figures.case figure = rand ? circle : square;

switch (figure) {
  case instanceof Circle {
    return "Radius: " + figure.getRadius();
  }
  case instanceof Square {
    return "Side length: " + figure.getSideLen();
  }
}
// unreachable -- all cases are covered
```

This is a research project. It extends Eclipse Java compiler to support a new language feature.

A condensed introduction is in downloads. Everything else is on [Wiki](http://code.google.com/p/ohl/wiki/HowToTryOut).

### Current status ###
Versions for Eclipse 3.4.2, 3.5.0 and 3.6.0 are ready. Current syntax version is "v. 3". Current documentation version is "v. 4".