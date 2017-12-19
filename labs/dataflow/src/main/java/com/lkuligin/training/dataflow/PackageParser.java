package com.lkuligin.training.dataflow;

import java.util.ArrayList;
import java.util.List;

public final class PackageParser {

  private static List<String> splitPackageName(String packageName) {
    // e.g. given com.example.appname.library.widgetname
    // returns com
    // com.example
    // com.example.appname
    // etc.
    List<String> result = new ArrayList<>();
    int end = packageName.indexOf('.');
    while (end > 0) {
      result.add(packageName.substring(0, end));
      end = packageName.indexOf('.', end + 1);
    }
    result.add(packageName);
    return result;
  }

  public static List<String> getPackages(String line, String keyword) {
    int start = line.indexOf(keyword) + keyword.length();
    int end = line.indexOf(";", start);
    if (start < end) {
      String packageName = line.substring(start, end).trim();
      return splitPackageName(packageName);
    }
    return new ArrayList<>();
  }

  public static String[] parsePackageStatement(String[] lines) {
    final String keyword = "package";
    for (String line : lines) {
      if (line.startsWith(keyword)) {
        // only one package statement per file
        return getPackages(line, keyword).toArray(new String[0]);
      }
    }
    return new String[0];
  }

  public static String[] parseImportStatement(String[] lines) {
    final String keyword = "import";
    List<String> result = new ArrayList<>();
    for (String line : lines) {
      if (line.startsWith(keyword)) result.addAll(getPackages(line, keyword));
    }
    return result.toArray(new String[0]);
  }

  public static int countCallsForHelp(String[] lines) {
    int count = 0;
    for (String line : lines) {
      if (line.contains("FIXME") || line.contains("TODO")) {
        ++count;
      }
    }
    return count;
  }

}