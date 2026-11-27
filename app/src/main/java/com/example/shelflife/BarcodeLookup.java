package com.example.shelflife;
import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import org.json.*;
final class BarcodeLookup {
 static final ExecutorService NETWORK=Executors.newSingleThreadExecutor();
 static final Map<String,Result> CACHE=new LinkedHashMap<String,Result>(){protected boolean removeEldestEntry(Map.Entry<String,Result> e){return size()>100;}};
 static final class Result {String name="",brand="",category="",description="",pack="",message="";Measurement measurement;}
 static String clean(JSONObject p,String key,int max){String s=p.optString(key,"");if(s.equals("null"))return "";s=s.replaceAll("<[^>]*>","").trim();return s.length()>max?s.substring(0,max):s;}
 static String first(JSONObject p,int max,String... keys){for(String key:keys){String value=clean(p,key,max);if(!value.isEmpty())return value;}return "";}
 static Result parse(String body)throws JSONException {
  JSONObject root=new JSONObject(body);Result r=new Result();JSONObject p=root.optJSONObject("product");
  if(root.optInt("status")!=1||p==null){r.message="No catalog match. Enter product details manually.";return r;}
  r.name=first(p,200,"product_name","product_name_en","abbreviated_product_name");r.brand=clean(p,"brands",200);r.description=first(p,2000,"generic_name","generic_name_en");r.pack=clean(p,"quantity",120);
  String categories=clean(p,"categories",2000)+" "+clean(p,"categories_tags",4000)+" "+clean(p,"categories_hierarchy",4000);
  r.category=LookupMetadata.category(r.name,categories,r.description);
  r.measurement=measurement(p);
  r.message="Open Food Facts · Review suggestions before saving.";
  if(r.category.isEmpty())r.message+=" Select a category; catalog evidence is insufficient.";
  if(r.measurement==null||r.measurement.value.isEmpty())r.message+=" No explicit pack measurement was found in the catalog data. Enter it manually.";
  return r;
 }
 /** Read only product/pack evidence, never serving sizes, nutrition or empty-container weight. */
 static Measurement measurement(JSONObject p){
  Measurement primary=LookupMetadata.measurement(clean(p,"quantity",500),clean(p,"product_quantity",80),clean(p,"product_quantity_unit",80),"","");
  if(complete(primary))return primary;
  // The full response retains localized names that a fixed fields= list discarded.
  for(String prefix:new String[]{"product_name","abbreviated_product_name","generic_name","packaging_text"}){
   List<String> keys=new ArrayList<>();Iterator<String> it=p.keys();while(it.hasNext()){String key=it.next();if(key.equals(prefix)||key.startsWith(prefix+"_"))keys.add(key);}Collections.sort(keys);
   for(String key:keys){Object raw=p.opt(key);if(!(raw instanceof String))continue;
    Measurement candidate=prefix.equals("product_name")||prefix.equals("abbreviated_product_name")?Measurement.detect((String)raw):Measurement.description((String)raw);
    if(complete(candidate))return candidate;
    if(primary==null&&candidate!=null)primary=candidate;
   }
  }
  // A single food-containing component is safe; multi-unit packs are ambiguous.
  JSONArray components=p.optJSONArray("packagings");Measurement packaging=null;boolean ambiguous=false;
  if(components!=null)for(int i=0;i<components.length();i++){
   JSONObject component=components.optJSONObject(i);if(component==null)continue;
   Measurement candidate=LookupMetadata.measurement(clean(component,"quantity_per_unit",500),clean(component,"quantity_per_unit_value",80),clean(component,"quantity_per_unit_unit",80),"","");
   if(!complete(candidate))continue;
   if(component.optDouble("number_of_units",1)!=1){ambiguous=true;continue;}
   if(packaging!=null&&(!packaging.unit.equals(candidate.unit)||new java.math.BigDecimal(packaging.value).compareTo(new java.math.BigDecimal(candidate.value))!=0))ambiguous=true;
   packaging=candidate;
  }
  if(!ambiguous&&packaging!=null)return packaging;
  return primary;
 }
 static boolean complete(Measurement m){return m!=null&&!m.value.isEmpty()&&!m.unit.isEmpty();}
 static boolean complete(Result r){return complete(r.measurement);}
 static Result fetch(String code){if(!code.matches("(?:[0-9]{8}|[0-9]{12,14})")){Result r=new Result();r.message="No online lookup for this barcode format. Enter details manually.";return r;}if(CACHE.containsKey(code))return CACHE.get(code);HttpURLConnection c=null;try{URL url=new URL("https://world.openfoodfacts.org/api/v2/product/"+code+"");c=(HttpURLConnection)url.openConnection();c.setConnectTimeout(5000);c.setReadTimeout(7000);c.setRequestProperty("User-Agent","ShelfLife/1.7 (Android; grocery inventory student project)");c.setRequestProperty("Accept","application/json");int status=c.getResponseCode();if(status==404){Result r=new Result();r.message="No catalog match. Enter product details manually.";if(complete(r))CACHE.put(code,r);return r;}if(status!=200)throw new IOException("HTTP "+status);try(InputStream in=c.getInputStream();ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] bytes=new byte[4096];int n;while((n=in.read(bytes))!=-1){out.write(bytes,0,n);if(out.size()>2097152)throw new IOException("Response too large");}Result r=parse(out.toString("UTF-8"));if(complete(r))CACHE.put(code,r);return r;}}catch(Exception e){Result r=new Result();r.message="Catalog unavailable or offline. Enter details manually, or re-enter the barcode to retry.";return r;}finally{if(c!=null)c.disconnect();}}
}
