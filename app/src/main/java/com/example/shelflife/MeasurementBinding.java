package com.example.shelflife;
import android.text.*;
import android.view.View;
import android.widget.*;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
/** Explicit measurements only; manual corrections take priority over suggestions. */
final class MeasurementBinding {
 private final EditText value,pack,name,description;private final MaterialAutoCompleteTextView unit;private boolean changing,manual;private String lastNamePack;private final SavedChoice choice;
 MeasurementBinding(View root,Product source){value=root.findViewById(R.id.etMeasurementValue);unit=root.findViewById(R.id.etMeasurementUnit);pack=root.findViewById(R.id.etPackSize);name=root.findViewById(R.id.etName);description=root.findViewById(R.id.etDescription);choice=new SavedChoice(root,unit,R.id.etCustomMeasurementUnit,R.id.etCustomMeasurementUnitBox,"unit",Measurement.UNITS);
 changing=true;if(source!=null&&source.measurementUnit!=null&&!source.measurementUnit.isEmpty()){value.setText(source.measurementValue);unit.setText(SavedChoice.displayUnit(source.measurementUnit),false);if(source.measurementValue==null||source.measurementValue.isEmpty()){Measurement recovered=LookupMetadata.measurement(source.packSize,source.measurementValue,source.measurementUnit,source.name,source.description);if(recovered!=null&&!recovered.value.isEmpty()){value.setText(recovered.value);unit.setText(SavedChoice.displayUnit(recovered.unit),false);}}}else detect();changing=false;
 TextWatcher input=new TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int c,int f){}public void onTextChanged(CharSequence s,int a,int b,int c){if(changing)return;manual=true;sync();}public void afterTextChanged(Editable e){}};value.addTextChangedListener(input);unit.addTextChangedListener(input);((EditText)root.findViewById(R.id.etCustomMeasurementUnit)).addTextChangedListener(input);
 TextWatcher hints=new TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int c,int f){}public void onTextChanged(CharSequence s,int a,int b,int c){if(!changing&&!manual)detect();}public void afterTextChanged(Editable e){}};pack.addTextChangedListener(hints);name.addTextChangedListener(hints);description.addTextChangedListener(hints);
 }
 private void detect(){String current=Screens.text(pack);boolean own=lastNamePack!=null&&current.equals(lastNamePack);Measurement found=own?null:Measurement.detect(current);boolean fromName=found==null;if(fromName||found.value.isEmpty()){Measurement richer=LookupMetadata.measurement(own?"":current,"","",Screens.text(name),Screens.text(description));if(richer!=null)found=richer;}changing=true;value.setText(found==null?"":found.value);unit.setText(found==null?"":SavedChoice.displayUnit(found.unit),false);if(fromName&&(current.isEmpty()||own)){lastNamePack=found==null?"":found.value+found.unit;pack.setText(lastNamePack);}else lastNamePack=null;changing=false;}
 void applyLookup(Measurement detected,String rawPack){
  if(manual||detected==null)return;
  changing=true;
  try{value.setText(detected.value);unit.setText(SavedChoice.displayUnit(detected.unit),false);lastNamePack=null;pack.setText(rawPack!=null&&!rawPack.trim().isEmpty()?rawPack:detected.value+detected.unit);}finally{changing=false;}
 }
 void allowLookup(){manual=false;}
 boolean acceptsLookup(){return !manual;}
 private void sync(){changing=true;pack.setText(Screens.text(value)+choice.value());changing=false;}
 String value(){if(Screens.text(unit).equals("Others")&&choice.value().isEmpty())throw new IllegalArgumentException("Enter a custom measurement unit");return Measurement.validate(Screens.text(value),choice.value());}
 String unit(){return choice.value();}
}
