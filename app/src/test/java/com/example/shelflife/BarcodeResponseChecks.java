package com.example.shelflife;
/** Exercises the production JSON mapper, not just the measurement regex. */
public final class BarcodeResponseChecks {
 static int count;
 static BarcodeLookup.Result parse(String product)throws Exception{return BarcodeLookup.parse("{\"status\":1,\"product\":"+product+"}");}
 static void check(String product,String value,String unit)throws Exception{
  BarcodeLookup.Result r=parse(product);
  if(r.measurement==null||!value.equals(r.measurement.value)||!unit.equals(r.measurement.unit))throw new AssertionError(product);
  count++;
 }
 static void missing(String product)throws Exception{if(BarcodeLookup.complete(parse(product)))throw new AssertionError(product);count++;}
 public static void main(String[] args)throws Exception{
  for(String[] pair:new String[][]{{"155","g"},{"425","g"},{"1","kg"},{"250","ml"},{"1","L"},{"1000","mg"},{"12","oz"},{"2","lb"},{"6","pcs"}})
   check("{\"quantity\":\""+pair[0]+pair[1]+"\"}",pair[0],pair[1]);
  check("{\"product_quantity\":425,\"product_quantity_unit\":\"g\"}","425","g");
  check("{\"product_name\":\"Mega Green\",\"product_name_fil\":\"Sardines 425g\"}","425","g");
  check("{\"generic_name_fil\":\"Net weight: 500g\"}","500","g");
  check("{\"packagings\":[{\"quantity_per_unit\":\"250ml\",\"number_of_units\":1}]}","250","ml");
  check("{\"packagings\":[{\"quantity_per_unit_value\":1,\"quantity_per_unit_unit\":\"L\"}]}","1","L");
  check("{\"quantity\":\"1kg\",\"packagings\":[{\"quantity_per_unit\":\"500g\"}]}","1","kg");
  missing("{\"product_name\":\"Mega Green\"}");
  missing("{\"product_quantity\":425}");
  missing("{\"serving_size\":\"100g\",\"packagings\":[{\"weight_measured\":25}]}");
  missing("{\"packagings\":[{\"quantity_per_unit\":\"250ml\",\"number_of_units\":6}]}");
  missing("{\"packagings\":[{\"quantity_per_unit\":\"250ml\"},{\"quantity_per_unit\":\"1L\"}]}");
  if(BarcodeLookup.complete(parse("{\"product_name\":\"Milk\"}")))throw new AssertionError("Incomplete result cacheable");count++;
  System.out.println("PASSED: "+count+" production JSON response checks");
 }
}
