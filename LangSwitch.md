# Switch Operator #
If you have a value of [enum-case](LangEnumCase.md) type, the main technique to work with it is to use operator _switch_. If has a standard form:

```
Pets.case aPet = ...;
switch (aPet) {
   ...
}
```

Whenever operator _switch_ is used on enum-case type, it should provide handlers for cases.
Let's take a sample enum-case:
```
enum-case Pets {
  Cat,
  Dog,
  case unrecoginzed(String description, Exception parseError),
  case empty_cage()
}
```

The typical _switch_ operator should look like this:
```
Pets.case aPet = ...;
String description;
switch (aPet) {
  case instanceof Cat cat {
    description = "Cat " + cat.getName();
  }
  case * unrecognized(String description, Exception parseError) {
    description = "Unreconized because " + parseError.getMessage();
  }
  case * empty_cage() {
    description = "empy cage";
  }
  default * {
    throw new RuntimeException("TODO");
  }
}
return description;
```

The main thing about this is that you didn't have to write a single cast. All is statically type-safe. Additionally, compiler checks that you don't forget cases.

In this sample _switch_ contains handlers for 3 cases and one default handler. Braces are mandatory, break statements are not used. "instanceof" is used for cases of [user types](LangUserType.md) and an asterisk syntax is used for [tuple cases](LangTuple.md) and for default handler.

Here the _Dog_ case is not handled. If it hadn't been for default handler, compiler would emit error about this. _Switch_ statement guarantees that exactly one handler will be executed. You may notice that uninitialized local variable _description_ is safely used after _switch_ because Java control flow analysis relies on this guaranty.

### Alternative syntax ###
If _switch_ expression is a final local variable, its type gets _refined_ inside handler body. Case handler may not introduce new variable but access directly to expression.
```
final Pets.case aPet = ...;
switch (aPet) {
  case instanceof Cat {
    aPet.giveMilk();
  }
  case * unrecognized(String description, Exception parseError) {
    aPet.throwBone();
  }
  default * {
     // no action
  }
}
```