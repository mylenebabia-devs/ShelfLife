package com.example.shelflife;
public class SkuChecks {public static void main(String[] a){
 String sardine=SkuGenerator.generate("Mega sardines","Canned Goods","Mega","155g","Tomato sauce");eq("SAR-MEG-155-TS",sardine);
 eq(sardine,SkuGenerator.generate("mega sardines","canned goods","mega","155 g","tomato sauce"));
 eq("SAR-MEG-155-CH",SkuGenerator.generate("Mega sardines","Canned Goods","Mega","155g","Chili"));
 eq("SAR-LIG-155-TS",SkuGenerator.generate("Ligo sardines","Canned Goods","Ligo","155g","Tomato sauce"));
 eq("TUN-CEN-180-FL",SkuGenerator.generate("Tuna","Canned Goods","Century","180g","Flakes"));
 eq("BEV-COC-330-OR",SkuGenerator.generate("Coke","Beverages","Coca-Cola","330ml","Original"));
 eq("GEN-GEN-STD-STD",SkuGenerator.generate(null,null,null,null,null));
 eq("DAI-BRA-1-WHO",SkuGenerator.generate("Milk","Dairy","Brand","1L","Whole"));
 if(SkuGenerator.ready("","Milk","Dairy","Brand","1","L","Whole"))throw new AssertionError("SKU before barcode");
 if(SkuGenerator.ready("12345678","Milk","Dairy","","1","L","Whole"))throw new AssertionError("SKU without brand");
 if(SkuGenerator.ready("12345678","Milk","Dairy","Brand","","L","Whole"))throw new AssertionError("SKU without measurement");
 if(!SkuGenerator.ready("12345678","Soap","Toiletries","Brand","100","g","Original"))throw new AssertionError("Complete info rejected");
 eq("TOI-BRA-100-OR",SkuGenerator.generate("Soap","Toiletries","Brand","100","Original"));
 eq("HOU-BRA-500-LEM",SkuGenerator.generate("Cleaner","Household Products","Brand","500","Lemon"));
 eq("PET-BRA-500-CHI",SkuGenerator.generate("Dog food","Pet Food","Brand","500","Chicken"));
 System.out.println("PASSED: 15 production SKU generation and gating checks");}
 static void eq(String x,String y){if(!x.equals(y))throw new AssertionError(x+" != "+y);}}
