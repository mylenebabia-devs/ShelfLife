package com.example.shelflife;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.*;
/** Detect only explicit measurement tokens; never infer weight from a category. */
public final class Measurement {
 public static final String[] UNITS={"kg","g","mg","L","ml","oz","lb","pcs"};
 private static final String TOKEN="kilograms?|milligrams?|millilit(?:er|re)s?|kilogrammes?|grams?|grammes?|lit(?:er|re)s?|ounces?|pounds?|pieces?|pcs|kg|mg|ml|oz|lbs?|g|l";
 private static final Pattern FULL=Pattern.compile("(?i)(?<![0-9.,])([0-9]+(?:[.,][0-9]+)?)\\s*("+TOKEN+")(?![a-z])");
 private static final Pattern UNIT=Pattern.compile("(?i)(?<![a-z])("+TOKEN+")(?![a-z])");
 public final String value,unit;
 Measurement(String value,String unit){this.value=value;this.unit=unit;}
 public static String canonical(String raw){String u=raw.toLowerCase(Locale.ROOT);if(u.startsWith("kg")||u.startsWith("kilo"))return "kg";if(u.equals("mg")||u.startsWith("milligram"))return "mg";if(u.equals("ml")||u.startsWith("millilit"))return "ml";if(u.equals("l")||u.startsWith("lit"))return "L";if(u.equals("g")||u.startsWith("gram"))return "g";if(u.equals("oz")||u.startsWith("ounce"))return "oz";if(u.startsWith("lb")||u.startsWith("pound"))return "lb";return "pcs";}
 /** Normalize lookup text without changing its numeric meaning. */
 static String text(String raw){return raw==null?"":raw.replace('\u00a0',' ').replace('\u202f',' ').replace('\u2009',' ').replace("&nbsp;"," ").replace("&#160;"," ").replaceAll("<[^>]*>"," ");}
 public static Measurement detect(String raw){
  String text=text(raw);Matcher m=FULL.matcher(text);Measurement found=null;String identity=null;boolean conflicting=false;
  while(m.find()){
   String value=m.group(1).replace(',','.');String unit=canonical(m.group(2));
   String key=new BigDecimal(value).stripTrailingZeros().toPlainString()+":"+unit;
   if(identity!=null&&!identity.equals(key))conflicting=true;
   if(identity==null)identity=key;found=new Measurement(value,unit);
  }
  if(found!=null){if(conflicting||text.matches("(?is).*\\d\\s*[x×]\\s*\\d.*"))return new Measurement("",found.unit);return found;}
  Matcher unit=UNIT.matcher(text);return unit.find()?new Measurement("",canonical(unit.group(1))):null;
 }
 /** A generic description can contain nutrition numbers; prefer explicit net/pack labels. */
 static Measurement description(String raw){
  String text=text(raw);
  Matcher labelled=Pattern.compile("(?i)(?:net\\s*(?:weight|wt|contents?|volume)|pack(?:age)?\\s*(?:size|weight|volume)|weight|volume)\\s*[:=.-]?\\s*([0-9]+(?:[.,][0-9]+)?\\s*(?:"+TOKEN+"))(?![a-z])").matcher(text);
  StringBuilder packs=new StringBuilder();while(labelled.find())packs.append(labelled.group(1)).append("; ");
  if(packs.length()>0)return detect(packs.toString());
  StringBuilder safe=new StringBuilder();
  for(String sentence:text.split("[;\\n]|(?<=[.!?])\\s+")){
   if(sentence.matches("(?is).*\\b(?:nutrition|nutritional|per\\s+[0-9]|serving|(?:protein|fat|sodium|carbohydrate)\\s*:?\\s*[0-9]|sugars?\\s*:|recipe|add\\s+[0-9]|mix\\s+[0-9]|dissolve).*"))continue;
   safe.append(sentence).append("; ");
  }
  Measurement found=detect(safe.toString());return found!=null&&!found.value.isEmpty()?found:null;
 }
 public static String validate(String value,String unit){value=value==null?"":value.trim();unit=unit==null?"":unit.trim();if(!unit.isEmpty()&&(unit.equalsIgnoreCase("Others")||unit.length()>32||unit.matches(".*[\\p{Cntrl}].*")))throw new IllegalArgumentException("Enter a measurement unit of 1–32 readable characters");if(value.isEmpty())return "";if(unit.isEmpty())throw new IllegalArgumentException("Choose a measurement unit");try{if(!value.matches("[0-9]+(?:[.,][0-9]+)?"))throw new NumberFormatException();BigDecimal n=new BigDecimal(value.replace(',','.'));if(n.signum()<=0||n.compareTo(new BigDecimal("1000000000"))>0)throw new NumberFormatException();return n.stripTrailingZeros().toPlainString();}catch(NumberFormatException e){throw new IllegalArgumentException("Enter a positive measurement up to 1,000,000,000");}}
}
