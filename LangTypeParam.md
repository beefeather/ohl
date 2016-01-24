# Enum-case as a generic type parameter #

Like any other type, enum-case type may be used as a generic parameter. However, enum-case type has a mandatory ".case" suffix and you may choose put it inside parameter or not.

### Type parameter includes .case ###
```
class CaseHolder<T> {
  T caseInstance;
}

CaseHolder<FewOptions.case> holder = new CaseHolder<FewOptions.case>();
holder.caseInstance = FewOptions.doNothing;
```

### Type parameter does not include .case ###
```
class CaseHolder<P> {
  P.case caseInstance;
}

CaseHolder<FewOptions> holder = new CaseHolder<FewOptions>();
holder.caseInstance = FewOptions.doNothing;
```

This is more a matter of taste which variant to use. However, it makes a real difference if you should need a [variance](LangVariance.md).