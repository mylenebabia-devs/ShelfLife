package com.example.shelflife;

import java.time.LocalDate;
import java.util.Locale;
public final class Rules {
 private Rules(){}
 public static boolean email(String s){return s!=null && s.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+");}
 public static String normalizeEmail(String s){return s.trim().toLowerCase(Locale.ROOT);}
 public static double quantity(String s,boolean zero){
  double n; try {n=Double.parseDouble(s.trim());}catch(Exception e){throw new IllegalArgumentException("Enter a valid quantity");}
  if(!Double.isFinite(n)||n<0||(!zero&&n==0)||n>1e9||Math.abs(n*1000-Math.rint(n*1000))>0.00001) throw new IllegalArgumentException("Use "+(zero?"zero or ":"")+"a positive quantity, up to 3 decimal places and 1 billion");
  return n;
 }
 public static double rounded(double n){return Math.rint(n*1000)/1000;}
 public static String status(Long date,String type,LocalDate today){
  if(date==null||"Unknown".equals(type))return "Expiry unknown · Date not recorded";
  long d=date-today.toEpochDay();
  if("Best Before".equals(type)) return d<0?"Past best-before":d==0?"Best before today":d<=7?"Best before soon":"Within best-before date";
  return d<0?"Expired":d==0?"Expires today":d<=7?"Expiring soon":"Within date";
 }
 public static void stockOut(double amount,double available,Long date,String label,String reason,LocalDate today){
  quantity(Double.toString(amount),false);
  if(amount>available)throw new IllegalArgumentException("Quantity exceeds remaining stock");
  if("Sold".equals(reason)&&date!=null&&"Expiry".equals(label)&&date<today.toEpochDay())throw new IllegalArgumentException("Expired stock cannot be sold");
 }
 public static int milestone(Long day,LocalDate today){if(day==null)return -1;long d=day-today.toEpochDay();return d==7||d==3||d==1||d==0?(int)d:-1;}
 public static String qty(double n){return java.math.BigDecimal.valueOf(n).stripTrailingZeros().toPlainString();}
}
