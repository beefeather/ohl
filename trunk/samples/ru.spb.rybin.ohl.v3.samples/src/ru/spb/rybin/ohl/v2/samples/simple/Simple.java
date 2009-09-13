package ru.spb.rybin.ohl.v2.samples.simple;

public class Simple {
  static class A implements case {
    void doAOperation() {
    }
  }
  
  static class B implements case {
    String doBOperation(String s) {
      return s;
    }
  }
  
  static enum-case Variant {
    A, B,
    case unknown(String description)
  }
  
  void checker(final Variant.case v) {
    switch (v) {
    case instanceof A {
      v.doAOperation();
    }
    case instanceof B vB {
      vB.doBOperation(null);
    }
    case * unknown(String description) {
      
    }
    }
  }
}
