package com.example.shelflife;
import android.text.*;
import android.view.View;
import android.widget.*;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import java.util.*;
/** Options are committed only with a successful product save. */
final class SavedChoice {
 private final MaterialAutoCompleteTextView field;private final EditText custom;private final String kind;
 private static final String[] LABELS={"Kilogram (kg)","Gram (g)","Milligram (mg)","Liter (L)","Milliliter (ml)","Ounce (oz)","Pound (lb)","Pieces (pcs)"};
 SavedChoice(View root,MaterialAutoCompleteTextView field,int customId,int boxId,String kind,String[] defaults){this.field=field;this.kind=kind;custom=root.findViewById(customId);View box=root.findViewById(boxId);field.setKeyListener(null);
 if(kind.equals("category"))new DownwardDropdown(field);else field.setOnClickListener(v->field.showDropDown());
 field.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int c,int f){}public void onTextChanged(CharSequence s,int a,int b,int c){box.setVisibility("Others".equals(s.toString())?View.VISIBLE:View.GONE);}public void afterTextChanged(Editable e){}});
 List<String> base=new ArrayList<>(Arrays.asList(defaults));fill(base);Auth.IO.execute(()->{ProductDao dao=AppDatabase.get(root.getContext()).productDao();List<String> saved=kind.equals("category")?dao.categories():dao.options(kind);root.post(()->{List<String> all=new ArrayList<>(base);all.addAll(saved);fill(all);});});}
 private void fill(List<String> values){Map<String,String> choices=new LinkedHashMap<>();if(kind.equals("category"))values=CategorySuggestions.choices(values);for(String v:values){v=CategorySuggestions.clean(v);if(v.isEmpty()||v.equalsIgnoreCase("Others"))continue;String display=kind.equals("unit")?displayUnit(v):v;choices.putIfAbsent(CategorySuggestions.key(display),display);}choices.put("others","Others");field.setAdapter(new ArrayAdapter<>(field.getContext(),android.R.layout.simple_dropdown_item_1line,new ArrayList<>(choices.values())));String current=field.getText().toString();String canonical=choices.get(CategorySuggestions.key(current));if(canonical!=null&&!canonical.equals(current))field.setText(canonical,false);}
 static String normalize(String value){for(int i=0;i<LABELS.length;i++)if(LABELS[i].equalsIgnoreCase(value)||Measurement.UNITS[i].equalsIgnoreCase(value))return Measurement.UNITS[i];return CategorySuggestions.clean(value);}
 static String displayUnit(String unit){String normalized=normalize(unit);for(int i=0;i<Measurement.UNITS.length;i++)if(Measurement.UNITS[i].equals(normalized))return LABELS[i];return normalized;}
 String value(){String selected=field.getText().toString().trim();return selected.equals("Others")?CategorySuggestions.clean(Screens.text(custom)):kind.equals("unit")?normalize(selected):CategorySuggestions.clean(selected);}
}
