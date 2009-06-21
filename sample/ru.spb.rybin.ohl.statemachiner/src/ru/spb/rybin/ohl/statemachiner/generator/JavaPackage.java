package ru.spb.rybin.ohl.statemachiner.generator;

import java.io.File;
import java.io.IOException;

public class JavaPackage {
  JavaPackage(File dir, String name) {
    this.dir = dir;
    this.fullName = name;
  }
  
  JavaPackage subpackage(String shortName) {
    return new JavaPackage(new File(dir, shortName), fullName + "." + shortName);
  }
  
  JavaSource sourceFile(String name) throws IOException {
    dir.mkdirs();
    return new JavaSource(new File(dir, name + ".java"), fullName);
  }
  
  private final File dir;
  private final String fullName;
}
