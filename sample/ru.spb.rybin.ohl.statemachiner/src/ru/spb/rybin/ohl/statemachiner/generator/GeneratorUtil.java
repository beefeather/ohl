package ru.spb.rybin.ohl.statemachiner.generator;

class GeneratorUtil {
  static String camelCat(String prefix, String name) {
    if (Character.isLowerCase(name.charAt(0))) {
      return prefix + Character.toUpperCase(name.charAt(0)) + name.substring(1);
    } else {
      return prefix + "_" + name;
    }
  }
}
