package ru.spb.rybin.ohl.statemachiner;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import ru.spb.rybin.ohl.statemachiner.generator.Generator;
import ru.spb.rybin.ohl.statemachiner.parser.Parser;
import ru.spb.rybin.ohl.statemachiner.parser.ParserException;
import ru.spb.rybin.ohl.statemachiner.parser.ast.StateMachine;

public class Main {
  public static void main(String[] args) throws IOException, ParserException {
    Params params = new Params();
    
    int nextI;
    for (int i = 0; i < args.length; i = nextI) {
      if (args[i].startsWith("-")) {
        if (args[i].equals("-outdir")) {
          if (i + 1 >= args.length) {
            throw new IllegalArgumentException("-outdir should have value");
          }
          params.outputDir = args[i+1];
          nextI = i + 2;
        } else {
          throw new IllegalArgumentException("Unknown param " + args[i]);
        }
      } else {
        if (params.inputFile != null) {
          throw new IllegalArgumentException("Input file has already been set");
        }
        params.inputFile = args[i];
        nextI = i + 1;
      }
    }
    
    params.validate();
    
    run(params.inputFile, params.outputDir, params.packageName );
  }
  
  private static void run(String inputFile, String outDir, String packageName) throws IOException, ParserException {
    File inpFile = new File(inputFile);
    Reader reader = new FileReader(inpFile);
    String contents = readFileString(reader);
    reader.close();
    StateMachine ast = Parser.parse(contents);
    
    Generator.generate(ast, outDir, packageName);
  }
  
  private static String readFileString(Reader reader) throws IOException {
    StringBuilder builder = new StringBuilder();
    char[] buffer = new char[1024];
    while (true) {
      int res = reader.read(buffer);
      if (res == -1) {
        break;
      }
      builder.append(buffer, 0, res);
    }
    return builder.toString();
  }
  
  private static class Params {
    String packageName = "ru.spb.rybin.ohl.statemachine.generated";
    String inputFile;
    String outputDir;

    void validate() {
      if (inputFile == null) {
        throw new RuntimeException("input file name should be specified");
      }
      if (outputDir == null) {
        throw new RuntimeException("Output dir should be specified");
      }
      if (outputDir == null) {
        throw new RuntimeException("Package name should be specified");
      }
    } 
  }
}
