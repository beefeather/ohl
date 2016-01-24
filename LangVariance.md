# How to use variance with enum-case #

You may need a generic class/method with some bounds for your type parameter. Suppose you have 2 related enum-cases:
```
enum-case FewOptions {
  case doNothing()
}
enum-case ManyOptions extends FewOptions {
  case goToCinema(String movie),
  case goToWork()
}
```
and a method with generic parameter T
```
// This method returns the same type as it accepts
<T> T validateOption(T option) {
  return option;
}
```
You may need 2 different kinds of a bound for this parameter.

### Enum-case not wider than ###
If you limit your enum-case type this way, you may switch on its value (because you know all possible cases).
```
<T extends ManyOptions.case> T validateOption(T option) {
  switch(option) {
    case * doNothing() {
      // OK
    }
    case * goToCinema(String movie) {
      throw new RuntimeException("No way!");
    }
    case * goToWork() {
      // OK
    }
  }
  return option;
}
```

### Enum-case not narrower than ###
This makes sense if you need to return some particular case without fixing method's return type to any particular type.
```
<P extends FewOptions> P.case validateOption(P.case option) {
  if (!isInTheMood()) {
    return FewOptions.doNothing;
  }
  return option;
}
```