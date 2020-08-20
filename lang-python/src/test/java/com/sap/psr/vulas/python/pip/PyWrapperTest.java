package com.sap.psr.vulas.python.pip;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class PyWrapperTest {

  @Test
  public void testPythonVersion() {
    final List<String> list = new ArrayList<String>();
    list.add("python");
    list.add("--version");

    try {
      // Perform call
      final ProcessBuilder pb = new ProcessBuilder(list);

      // Start and wait
      final Process process = pb.start();
      BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      String line;
      System.out.print("Output of [python --version]:");
      while ((line = reader.readLine()) != null) {
        System.out.println(line);
      }
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }

  @Test
  public void testPipVersion() {
    final List<String> list = new ArrayList<String>();
    list.add("python");
    list.add("-m");
    list.add("pip");
    list.add("--version");

    try {
      // Perform call
      final ProcessBuilder pb = new ProcessBuilder(list);

      // Start and wait
      final Process process = pb.start();
      BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      String line;
      System.out.print("Output of [python -m pip --version]:");
      while ((line = reader.readLine()) != null) {
        System.out.println(line);
      }
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }
}
