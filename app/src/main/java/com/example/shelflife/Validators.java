package com.example.shelflife;

import java.util.*;
public final class Validators {
 public static final String[] CATEGORIES=CategorySuggestions.DEFAULTS;
 public static void product(Product p){
  if(p.name==null||p.name.trim().isEmpty())throw new IllegalArgumentException("Product name is required");
  if(p.sku==null||p.sku.trim().isEmpty())throw new IllegalArgumentException("Internal SKU is required");
  if(p.unit==null||p.unit.trim().isEmpty())throw new IllegalArgumentException("Quantity unit is required");
  if(p.category==null||p.category.trim().isEmpty())throw new IllegalArgumentException("Choose or type a grocery category");
  p.category=p.category.trim();
  if(p.category.length()>60||p.category.matches(".*[\\p{Cntrl}].*"))throw new IllegalArgumentException("Category must be 1–60 readable characters");
  p.measurementValue=Measurement.validate(p.measurementValue,p.measurementUnit);
  Rules.quantity(Double.toString(p.lowStock),true);
 }
}
