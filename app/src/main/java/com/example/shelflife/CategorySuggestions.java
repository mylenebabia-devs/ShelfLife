package com.example.shelflife;
import java.util.Locale;
/** Maps clear catalog evidence to predefined choices; never invents a category. */
public final class CategorySuggestions {
 public static final String[] DEFAULTS={"Canned Goods","Dairy and Eggs","Beverages","Bread and Bakery","Snacks and Biscuits","Packaged Foods","Fruits and Vegetables","Meat and Seafood","Frozen Foods","Condiments and Spices","Instant Noodles","Laundry Products"};
 private CategorySuggestions(){}
 public static String suggest(String name,String categories,String description){String c=words(categories),n=words(name),t=c+" "+n+" "+words(description);
  // Specific catalog taxonomy takes precedence over ingredients in a product name.
  if(has(c,"laundry","laundry detergents"))return "Laundry Products";
  if(has(c,"instant noodles","instant ramen"))return "Instant Noodles";
  if(has(c,"canned","tinned","canned foods","canned fish"))return "Canned Goods";
  if(has(c,"toiletries","household products"))return "";
  if(has(c,"frozen"))return "Frozen Foods";
  if(has(c,"condiments","sauces","spices"))return "Condiments and Spices";
  if(has(c,"biscuits","snacks","cookies","chips"))return "Snacks and Biscuits";
  if(has(c,"breads","bakery"))return "Bread and Bakery";
  if(has(c,"dairy","milks","cheeses","yogurts","eggs"))return "Dairy and Eggs";
  if(has(c,"beverages","juices","soft drinks","waters"))return "Beverages";
  if(has(t,"laundry","laundry detergents")||has(n,"surf detergent","laundry detergent"))return "Laundry Products";
  if(has(t,"instant noodles","pancit canton","instant ramen"))return "Instant Noodles";
  if(has(c,"toiletries","household products")||has(n,"shampoo","toothpaste","deodorant","dishwashing liquid"))return "";
  if(has(n,"century tuna","mega sardines"))return "Canned Goods";
  if(has(n,"piattos"))return "Snacks and Biscuits";
  if(has(t,"canned","tinned")||((has(n,"sardines","tuna"))&&has(t,"tomato sauce","in oil","in brine")))return "Canned Goods";
  if(has(c,"frozen")||has(n,"frozen"))return "Frozen Foods";
  if(has(c,"condiments","sauces","spices")||has(n,"ketchup","soy sauce","vinegar","mayonnaise"))return "Condiments and Spices";
  if(has(c,"dairy","milks","cheeses","yogurts","eggs")||has(n,"milk","cheese","yogurt","yoghurt"))return "Dairy and Eggs";
  if(has(c,"beverages","juices","soft drinks","waters")||has(n,"juice","cola","mineral water","iced tea"))return "Beverages";
  if(has(c,"biscuits","snacks","cookies")||has(n,"biscuit","biscuits","cookies","crackers","potato chips"))return "Snacks and Biscuits";
  if(has(c,"breads","bakery")||has(n,"bread","bagel","croissant"))return "Bread and Bakery";
  if(has(c,"cereals","grains","rices")||has(n,"rice","oats","oatmeal"))return "Packaged Foods";
  if(has(c,"fruits","vegetables"))return "Fruits and Vegetables";
  if(has(c,"meats","seafood","fresh fish"))return "Meat and Seafood";
  for(String known:DEFAULTS)if(words(known).equals(c))return known;
  return "";
 }
 private static String words(String s){return (s==null?"":s).toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+"," ").trim();}
 private static boolean has(String text,String... terms){String padded=" "+text+" ";for(String term:terms)if(padded.contains(" "+term+" "))return true;return false;}
 public static String clean(String value){return value==null?"":value.trim().replaceAll("\\s+"," ");}
 public static String key(String value){return clean(value).toLowerCase(Locale.ROOT);}
 public static boolean inventoryVisible(String category){String k=key(category);return !k.isEmpty()&&!java.util.Arrays.asList("others","household products","toiletries","grains and cereals","flakes").contains(k);}
 public static java.util.List<String> choices(java.util.Collection<String> saved){java.util.LinkedHashMap<String,String> result=new java.util.LinkedHashMap<>();for(String s:DEFAULTS)result.put(key(s),s);for(String s:saved)if(inventoryVisible(s))result.putIfAbsent(key(s),clean(s));return new java.util.ArrayList<>(result.values());}
}
