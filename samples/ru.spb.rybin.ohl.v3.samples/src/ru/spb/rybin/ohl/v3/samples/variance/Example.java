package ru.spb.rybin.ohl.v3.samples.variance;

public class Example {
  <T extends ManyOptions.case> T validateOption1(T option) {
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

  <P extends FewOptions> P.case validateOption2(P.case option) {
    if (!isInTheMood()) {
      return FewOptions.doNothing;
    }
    return option;
  }

  private static boolean isInTheMood() {
    return new java.util.Random().nextBoolean();
  }
}
