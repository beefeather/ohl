# User type as an element of enum-case #

User type may be used as an element of enum-case. To make it happen the type must have a special mark-up: extend/implement pseudo-interface "case":
```
interface Cat extends case {
  String getName();
}

class Capybara implements case {
  float weight;
}
```
That's basically it. You may use these user types in any enum-cases without limitation.

However, there is other kind of limitation, quite important one. No class or interface may have "case" mark-up and become an element of enum-case, if its ancestor (class or interface) already has "case" mark-up.

It means that enum-case element is not polymorphous: while one instance may be both a _Mamal_ and a _Cow_, it cannot be enum-case element as both a _Mamal_ and a _Cow_. In general, if you need something similar, you should create helper classes or use [tuples](LangTuple.md).

### Other less obvious limitation ###
Another sad limitation is that both _Cow_ and _Horse_ may have _case_ mark-up (if they are  siblings hopefully) and be listed in _MamalTypes_ enum-case and thus be subtypes of _MamalTypes.case_ type. It is desirable but impossible for their super-interface _Mamal_ to be a subtype of _MamalTypes.case_ itself. _MamalTypes.case_ and _Mamal_ will be unrelated common supertypes of _Cow_ and _Horse_. The best thing which can be done here is to provide getter from _Mamal_ which internally always returns _this_:
```
interface Mamal {
  // must be implemented as "return this;"
  MamalTypes.case getSubtype();
}
```