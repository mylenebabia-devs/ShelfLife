package com.example.shelflife;
public class MeasurementChecks {
 static int n;
 static void check(String text,String value,String unit){Measurement m=Measurement.detect(text);if(m==null||!value.equals(m.value)||!unit.equals(m.unit))throw new AssertionError(text);n++;}
 public static void main(String[] args){check("Juice 150ml","150","ml");check("Rice 1kg","1","kg");check("500g flour","500","g");check("Milk 1L","1","L");check("2.5 pounds","2.5","lb");check("5 ounces","5","oz");check("10 pieces","10","pcs");check("250 milligrams","250","mg");check("1,5 liters","1.5","L");check("Weight in kg","","kg");check("6 x 150ml","","ml");check("150ml / 5oz","","oz");if(Measurement.detect("Milk")!=null)throw new AssertionError("Assumed measurement");n++;if(!Measurement.validate("001.50","L").equals("1.5"))throw new AssertionError();n++;for(String bad:new String[]{"-1","0","abc","NaN"}){try{Measurement.validate(bad,"g");throw new AssertionError(bad);}catch(IllegalArgumentException expected){n++;}}try{Measurement.validate("1","");throw new AssertionError();}catch(IllegalArgumentException expected){n++;}System.out.println("PASSED: "+n+" production measurement detection/validation checks");}
}
