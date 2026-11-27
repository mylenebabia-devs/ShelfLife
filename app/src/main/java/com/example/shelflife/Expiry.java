package com.example.shelflife;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
public final class Expiry {
 private Expiry(){}
 public enum Status {EXPIRED,TODAY,SOON,WITHIN,UNKNOWN}
 public static final DateTimeFormatter DISPLAY=DateTimeFormatter.ofPattern("dd MMM yyyy",Locale.ENGLISH);
 public static long daysLeft(Product p){return daysLeft(p,LocalDate.now());}
 public static long daysLeft(Product p,LocalDate today){return p.expiryDate==null?Long.MAX_VALUE:p.expiryDate-today.toEpochDay();}
 public static Status statusOf(long d){return d==Long.MAX_VALUE?Status.UNKNOWN:d<0?Status.EXPIRED:d==0?Status.TODAY:d<=7?Status.SOON:Status.WITHIN;}
 public static String label(Product p){return Rules.status(p.expiryDate,p.dateType,LocalDate.now());}
 public static String date(Product p){return p.expiryDate==null?"Date not recorded":p.expiry().format(DISPLAY);}
 public static int color(Product p){switch(statusOf(daysLeft(p))){case EXPIRED:return 0xffb91c1c;case TODAY:return 0xff9a3412;case SOON:return 0xff854d0e;case UNKNOWN:return 0xff6b7280;default:return 0xff166534;}}
}
