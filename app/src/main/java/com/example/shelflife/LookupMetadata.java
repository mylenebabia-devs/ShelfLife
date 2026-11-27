package com.example.shelflife;
/** Maps explicit catalog evidence. A product type alone never establishes its pack unit. */
final class LookupMetadata {
 static Measurement measurement(String quantity,String structuredValue,String structuredUnit,String name,String description){
  Measurement explicit=Measurement.detect(quantity);
  if(explicit!=null&&!explicit.value.isEmpty())return explicit;
  // Only accept an actual unit token, never invent a default for a numeric quantity.
  Measurement unit=Measurement.detect(structuredUnit);
  if(unit!=null){try{String value=Measurement.validate(structuredValue,unit.unit);if(!value.isEmpty())return new Measurement(structuredValue.trim().replace(',','.'),unit.unit);}catch(IllegalArgumentException ignored){}}
  Measurement fromName=Measurement.detect(name);if(fromName!=null&&!fromName.value.isEmpty())return fromName;
  Measurement fromDescription=Measurement.description(description);if(fromDescription!=null&&!fromDescription.value.isEmpty())return fromDescription;
  if(explicit!=null)return explicit;if(unit!=null)return new Measurement("",unit.unit);return fromName;
 }
 static String category(String name,String categories,String description){return CategorySuggestions.suggest(name,categories,description);}
}
