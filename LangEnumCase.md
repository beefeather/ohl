# Enum-case declaration #

This is a main declaration here. It is a closed list of all possible cases of something. Each declaration introduces a new type.

Pseudo-keyword "enum-case" was chosen because 1. it doesn't introduce new keywords into Java and does not invalidate any existing Java program; 2. I didn't have a good idea how to name it anyway.

"Enum-case" is similar to enum and may be put wherever enum declaration is allowed.

Example:
```
enum-case Animals {
  Cat,
  Dog,
  Capybara
}
```
This declaration defines a new type: _Animals.case_ (i.e. type name must have a special suffix ".case"; technically speaking there is also type _Animals_, but it is not really used).
_Cat_, _Dog_ and _Capybara_ are all regular user types; however these user types must have a [special mark-up](LangUserType.md).

The new type _Animals.case_ is a supertype of _Cat_, _Dog_ and _Capybara_:
```
Animals.case animal = null; // it is a regular reference type
Cat cat = new Cat();
animal = cat;
Dog dog = new DogImpl();
animal = dog;
```
Once you have value of type _Animals.case_, you can cast it back to _Cat_, _Dog_ or _Capybara_ using new operator _switch_ (TBD).

Enum-case declares set of cases (order doesn't matter). Each case might be a user type (must have a special mark-up) or it can be a tuple (primarily for those types which you can't mark up, but also when you need several values in one case):
```
enum-case RecognizedPets {
  Cat,
  Dog,
  case unrecoginzed(String description, Exception parseError),
  case empty_cage()
}
```
Both kinds of cases (user types and tuples) are intermixable. See more details about tuples (TBD).
You may notice here, that user types may be used in any number of enum-cases.

One enum-case (as a set of cases) may [extend](LangEnumCaseExtends.md) several other enum-cases, generating related types.

Enum-case may be a [generic type](LangGenericEnumCase.md).