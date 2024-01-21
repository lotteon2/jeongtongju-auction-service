package com.jeontongju.auction.util;

public class Mosaic {
  public static String nameMosaic(String str) {
    if (str != null && str.length() > 2) {
      str = str.replace(str.charAt(1), '*');
    }
    return str;
  }
}
