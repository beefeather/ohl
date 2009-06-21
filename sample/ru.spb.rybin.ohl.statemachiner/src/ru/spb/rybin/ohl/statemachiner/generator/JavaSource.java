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
    for (int i = 0; i < currentIndent; i++) {
      output.print("  ");
    }
    output.println(line);
  }
  public void endline(String line) {
    output.println(line);
  }

  public void print(String line) {
    for (int i = 0; i < currentIndent; i++) {
      output.print("  ");
    }
    output.print(line);
  }
  
  public void append(String line) {
    output.print(line);
  }
  
  private int currentIndent = 0;
  private final PrintWriter output;
}
