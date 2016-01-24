# Discussion, Comments, QA #

**Is it essentially just adding external virtual method to Java?**

It is a bit more than this, but these features are quite close.

**Does it break encapsulation rule?**

Yes. Well, if you mean encapsulating everything inside a single type, then yes. However this feature facilitates encapsulating everything inside a single operation. And you always have to choose here. Sometimes you really should sever your code operation-wise. E.g. data and processor; AST tree not necessarily has to generate opcode by itself; rather it should go into a different module; however conceptually it should remain a polymorphous method of AST.


**Is it based on [GoF Visitor pattern](http://en.wikipedia.org/wiki/Visitor_pattern)?**

Yes.

**Is it just a syntax sugar over [GoF Visitor pattern](http://en.wikipedia.org/wiki/Visitor_pattern)?**

Unfortunately not. Because you almost never actually want to use GoF Visitor. Whenever you choose to give it a try you will 90% have to mess with anonymous classes and create their instances on each operation call. That's too much for a simple switch. That's why nobody uses GoF Visitor.

**What are the open questions?**

There are some obvious questions: interface with annotations (annotations could use enum-cases), interface with enums.