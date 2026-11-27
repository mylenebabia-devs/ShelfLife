package com.example.shelflife;
public class LookupChecks {
 static int count;
 static void measurement(String value,String unit,String pack,String number,String symbol,String name){Measurement result=LookupMetadata.measurement(pack,number,symbol,name,"");if(result==null||!value.equals(result.value)||!unit.equals(result.unit))throw new AssertionError(name+" / "+pack);count++;}
 static void description(String text,String value,String unit){Measurement found=LookupMetadata.measurement("","","","Product",text);if(found==null||!found.value.equals(value)||!found.unit.equals(unit))throw new AssertionError(text);count++;}
 public static void main(String[] args){
  measurement("425","g","425 g","","","Sardines");
  measurement("330","ml","","330","ml","Cola");
  measurement("1","kg","1 kg","","","Rice");
  measurement("1","L","","1","L","Milk");
  measurement("150","ml","","","","Drink 150ml");
  measurement("0.50","L","0.50 L","500","ml","Drink");
  measurement("155","g","","155","grams","Fish");
  measurement("","g","g","","","Unknown");
  measurement("","g","6 x 155 g","","","Multipack");
  measurement("24","pcs","","24","pcs","Pack");
  measurement("200","mg","200mg","","","Product");
  measurement("12","oz","12 oz","","","Product");
  measurement("2","lb","2lb","","","Product");
  if(LookupMetadata.measurement("","425","","Century Tuna","")!=null)throw new AssertionError("Guessed a unit from brand / size");count++;
  if(LookupMetadata.measurement("","","","Milk","Nutrition per 100 g")!=null)throw new AssertionError("Used nutrition as pack size");count++;
  if(!"Beverages".equals(LookupMetadata.category("Example brand","en:beverages,en:carbonated-drinks","")))throw new AssertionError("Taxonomy tags");count++;
  if(!"Instant Noodles".equals(LookupMetadata.category("Unknown brand","en:instant-noodles","")))throw new AssertionError("Noodles tags");count++;
  if(!LookupMetadata.category("Unknown item",""," ").isEmpty())throw new AssertionError("Guessed category");count++;
  measurement("1000","mg","1000mg","","","Product");
  measurement("250","ml","250ml","","","Product");
  measurement("500","g","500g","","","Product");
  measurement("425","g","","","","Sardines425g");
  measurement("425","g","425\u00a0g","","","Product");
  measurement("250","ml","250\u202fml","","","Product");
  measurement("425","g","425g / 425g","","","Product");
  description("Sardines in tomato sauce 425g","425","g");
  description("Net weight: 1kg. Nutrition per 100g","1","kg");
  description("Package size: 250ml","250","ml");
  description("Milk carton 1L","1","L");
  description("Pack weight: 1000mg","1000","mg");
  description("Net contents: 500g","500","g");
  description("Canned fruit with sugar 425g","425","g");
  for(String unsafe:new String[]{"Nutrition per 100g","Serving size 30g","Protein 20g","Add 250ml water","Product with no pack size"}){if(LookupMetadata.measurement("","","","Product",unsafe)!=null)throw new AssertionError(unsafe);count++;}
  System.out.println("PASSED: "+count+" lookup metadata mapping checks");
 }
}
