package com.example.shelflife;
import android.os.*;
import android.text.*;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
final class ProductLookupBinding {
 private final MainActivity a;private final AlertDialog dialog;private final ProductDao dao;private final EditText code;private final EditText[] fields;private final TextView status;private final Handler handler=new Handler(Looper.getMainLooper());private int generation;private final String[] suggested=new String[5];private final boolean newProduct;private boolean categoryChosen;private final MeasurementBinding measurement;
 ProductLookupBinding(MainActivity a,View view,AlertDialog dialog,ProductDao dao,boolean fresh,MeasurementBinding measurement){this.measurement=measurement;this.a=a;this.dialog=dialog;this.dao=dao;newProduct=fresh;code=view.findViewById(R.id.etBarcode);status=view.findViewById(R.id.lookupStatus);fields=new EditText[]{view.findViewById(R.id.etName),view.findViewById(R.id.etBrand),view.findViewById(R.id.etCategory),view.findViewById(R.id.etDescription),view.findViewById(R.id.etPackSize)};
 ((AutoCompleteTextView)fields[2]).setOnItemClickListener((parent,item,position,id)->{categoryChosen=true;suggested[2]=null;});
 code.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int start,int count,int after){}public void onTextChanged(CharSequence s,int start,int before,int count){queue();}public void afterTextChanged(Editable e){}});if(fresh)queue();else status.setText("Saved product · Edit details before saving. Internal SKU stays fixed.");}
 private void queue(){categoryChosen=false;measurement.allowLookup();final int token=++generation;handler.removeCallbacksAndMessages(null);for(int i=0;i<fields.length;i++){if(suggested[i]!=null&&fields[i].getText().toString().equals(suggested[i]))setField(i,"");suggested[i]=null;}String barcode=Screens.text(code);if(barcode.isEmpty()){status.setText("Scan or enter a barcode, then review the suggestions.");return;}status.setText("Looking up product…");handler.postDelayed(()->lookup(barcode,token),900);}
 private boolean current(String barcode,int token){return !a.isDestroyed()&&dialog.isShowing()&&token==generation&&barcode.equals(Screens.text(code));}
 private void lookup(String barcode,int token){if(!current(barcode,token))return;String[] before=new String[5];for(int i=0;i<5;i++)before[i]=fields[i].getText().toString();Auth.IO.execute(()->{try{Product local=dao.barcode(barcode);if(local!=null){BarcodeLookup.Result r=new BarcodeLookup.Result();r.name=local.name;r.brand=local.brand;r.category=CategorySuggestions.inventoryVisible(local.category)?local.category:CategorySuggestions.suggest(local.name,local.category,local.description);r.description=local.description;r.pack=local.packSize;if((r.pack==null||r.pack.isEmpty())&&local.measurementUnit!=null)r.pack=(local.measurementValue==null?"":local.measurementValue)+local.measurementUnit;r.measurement=LookupMetadata.measurement(r.pack,local.measurementValue,local.measurementUnit,local.name,local.description);r.message=newProduct?"Already in inventory. Use its Details → Add stock; registration will not create a duplicate.":"Saved product information found. Review before saving.";if(r.category==null||r.category.isEmpty()||r.measurement==null||r.measurement.value.isEmpty()){BarcodeLookup.NETWORK.execute(()->{BarcodeLookup.Result remote=BarcodeLookup.fetch(barcode);if(r.category==null||r.category.isEmpty())r.category=remote.category;if((r.measurement==null||r.measurement.value.isEmpty())&&remote.measurement!=null&&!remote.measurement.value.isEmpty()){r.measurement=remote.measurement;r.pack=remote.pack;}if(r.name==null||r.name.isEmpty())r.name=remote.name;if(r.brand==null||r.brand.isEmpty())r.brand=remote.brand;if(r.measurement==null)r.message+=" Pack measurement unavailable; enter it manually.";a.runOnUiThread(()->apply(barcode,token,before,r));});}else a.runOnUiThread(()->apply(barcode,token,before,r));}else BarcodeLookup.NETWORK.execute(()->{BarcodeLookup.Result r=BarcodeLookup.fetch(barcode);a.runOnUiThread(()->apply(barcode,token,before,r));});}catch(Exception e){a.runOnUiThread(()->{if(current(barcode,token))status.setText("Lookup unavailable. Enter details manually.");});}});}
 private void setField(int index,String value){if(fields[index] instanceof AutoCompleteTextView)((AutoCompleteTextView)fields[index]).setText(value,false);else fields[index].setText(value);}
 private void apply(String barcode,int token,String[] before,BarcodeLookup.Result r){
  if(!current(barcode,token))return;
  String[] values={r.name,r.brand,r.category,r.description};
  for(int i=0;i<values.length;i++){String value=values[i];if(value!=null&&!value.isEmpty()&&(i==2?!categoryChosen:before[i].isEmpty())&&fields[i].getText().toString().equals(before[i])){setField(i,value);suggested[i]=value;}}
  // Apply the explicit measurement directly, not indirectly through a hidden pack field
  // whose value may already have changed as the name watcher ran above.
  if(measurement.acceptsLookup()){
   Measurement detected=r.measurement!=null?r.measurement:LookupMetadata.measurement(r.pack,"","",r.name,r.description);
   if(detected!=null){measurement.applyLookup(detected,r.pack);suggested[4]=fields[4].getText().toString();}
  }
  status.setText(r.message);
 }
}
