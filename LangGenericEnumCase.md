# Generic Enum-Case #

Enum-case may be a generic type:
```
enum-case ThisOrThese<T> {
  case one(T element),
  case many(List<? extends T> elements)
}
```
However, type parameter may _not_ be an element of enum-case:
```
enum-case ThisOrThat<T, S> {
  T, S // this does not work
}
```