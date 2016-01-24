# Tuple Cases #

Tuple is an auxiliary kind of cases (as opposed to [user type cases](LangUserType.md)). Instead of referring to existing type, a new type is defined in place by keyword _case_:
```
enum-case InetAddresses {
  DomainNameAddress, // user type
  case ipAddress(byte b1, byte b2, byte b3, byte b4), // tuple
  case localhost() // tuple
}
```

New instance of tuple may be created similar to a regular class creation; however parameter-less tuples are singletones.
```
InetAddresses.case router = new InetAddresses.ipAddress(192.168.1.1);
InetAddresses.case me = InetAddresses.localhost;
```

Read [switch](LangSwitch.md) article to see how one should read tuples in _switch_ operator.