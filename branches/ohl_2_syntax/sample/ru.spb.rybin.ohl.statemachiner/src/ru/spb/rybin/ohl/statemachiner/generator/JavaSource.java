package ru.spb.rybin.ohl.statemachiner.generator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

class JavaSource {
  JavaSource(File file, String packageName) throws IOException {
    file.createNewFile();
    output = new PrintWriter(new FileWriter(file));
    
    output.println("package " + packageName + ";");
    output.println();
  }
  
  void close() {
    output.close();
  }
  
  void indent() {
    currentIndent++;
  }
  void indentBack() {
    currentIndent--;
  }
  
  void println(String line) {
    if (mayAppend) {
      throw new IllegalStateException();
    }
    for (int i = 0; i < currentIndent; i++) {
      output.print("  ");
    }
    output.println(line);
    mayAppend = false;
  }
  public void endline(String line) {
    if (!mayAppend) {
      throw new IllegalStateException();
    }
    output.println(line);
    mayAppend = false;
  }

  public void print(String line) {
    if (mayAppend) {
      throw new IllegalStateException();
    }
    for (int i = 0; i < currentIndent; i++) {
      output.print("  ");
    }
    output.print(line);
    mayAppend = true;
  }
  
  public void append(String line) {
    if (!mayAppend) {
      throw new IllegalStateException();
    }
    output.print(line);
    mayAppend = true;
  }
  
  private int currentIndent = 0;
  boolean mayAppend = false;
  private final PrintWriter output;
}
