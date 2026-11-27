package com.example.shelflife;

public final class Csv {
 private Csv(){}
 public static String text(String value){String s=value==null?"":value;String t=s.trim();if(!t.isEmpty()&&"=+-@".indexOf(t.charAt(0))>=0)s="'"+s;return "\""+s.replace("\"","\"\"")+"\"";}
}
