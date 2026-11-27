package com.example.shelflife;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.*;
import android.text.*;
import android.view.*;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.*;
import androidx.work.*;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.core.app.NotificationManagerCompat;
import android.net.Uri;
import android.provider.Settings;
import com.google.android.material.chip.*;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.*;
import com.journeyapps.barcodescanner.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class MainActivity extends ProtectedActivity {
 private ShelfViewModel vm;private StockRepository repo;private List<Product> all=new ArrayList<>();private boolean scanning;private Consumer<String> scanTarget;
 private ProductPhotoEditor photoEditor;
 private final ActivityResultLauncher<String[]> productPhotoPicker=registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.OpenDocument(),uri->{if(uri!=null&&photoEditor!=null)photoEditor.selected(uri);});
 private int selectedTab=R.id.nav_home;
 private long day=LocalDate.now().toEpochDay();private final Handler clock=new Handler(Looper.getMainLooper());
 private final Runnable midnight=new Runnable(){public void run(){if(LocalDate.now().toEpochDay()!=day){day=LocalDate.now().toEpochDay();vm.refresh();stats();collectReminders();}clock.postDelayed(this,60000);}};
 private final ActivityResultLauncher<String> notificationPermission=registerForActivityResult(new ActivityResultContracts.RequestPermission(),granted->{collectReminders();updateNotificationState();});
 private final ActivityResultLauncher<String> cameraPermission=registerForActivityResult(new ActivityResultContracts.RequestPermission(),granted->{if(granted)launchCamera();else{scanning=false;manual("Camera permission denied. Enter the barcode instead.");}});
 private final ActivityResultLauncher<ScanOptions> camera=registerForActivityResult(new ScanContract(),result->{scanning=false;if(result.getContents()!=null)scanned(result.getContents());else manual("Scan cancelled or camera unavailable. Manual entry is available.");});
 @Override protected void onCreate(Bundle b){super.onCreate(b);if(isFinishing())return;setContentView(R.layout.activity_main);Screens.insets(this);repo=new StockRepository(this);vm=new ViewModelProvider(this).get(ShelfViewModel.class);
  MaterialToolbar toolbar=findViewById(R.id.toolbar);toolbar.setSubtitle(null);findViewById(R.id.headerProfile).setOnClickListener(v->profile());findViewById(R.id.btnSort).setOnClickListener(v->{nameSort=!nameSort;vm.sortName(nameSort);((Button)v).setText(nameSort?"Sort: product name":"Sort: earliest date");});
  ProductAdapter adapter=new ProductAdapter(new ProductAdapter.Listener(){public void onDetails(Product p){details(p.id);}public void onAddStock(Product p){productForm(p,true,"");}});RecyclerView recycler=findViewById(R.id.recycler);recycler.setLayoutManager(new LinearLayoutManager(this));recycler.setAdapter(adapter);
  vm.getVisible().observe(this,list->{adapter.submitList(list);findViewById(R.id.emptyPanel).setVisibility(list.isEmpty()?View.VISIBLE:View.GONE);});
  vm.getAll().observe(this,list->{all=list;stats();categories();long requested=getIntent().getLongExtra("batch_id",0);if(requested!=0){getIntent().removeExtra("batch_id");details(requested);}});
  TextInputEditText search=findViewById(R.id.etSearch);search.setText(vm.query());search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int c,int f){}public void onTextChanged(CharSequence s,int a,int b,int c){vm.setQuery(s.toString());}public void afterTextChanged(Editable s){}});
  ((TextInputLayout)findViewById(R.id.tilSearch)).setEndIconOnClickListener(v->scan(null));findViewById(R.id.fabScan).setOnClickListener(v->scan(null));findViewById(R.id.fabAdd).setOnClickListener(v->productForm(null,false,""));
  ChipGroup filters=findViewById(R.id.groupFilter);for(ExpiryFilter f:ExpiryFilter.values()){Chip c=chip(f.title);c.setTag(f);filters.addView(c);c.setChecked(f==vm.filter());}filters.setOnCheckedStateChangeListener((group,ids)->{if(!ids.isEmpty())vm.setFilter((ExpiryFilter)((Chip)group.findViewById(ids.get(0))).getTag());});
  BottomNavigationView nav=findViewById(R.id.bottomNavigation);
  findViewById(R.id.btnPassword).setOnClickListener(v->PasswordDialog.show(this,repo.db,repo.staffId));
  findViewById(R.id.btnSettings).setOnClickListener(v->settings());
  findViewById(R.id.btnEditProfile).setOnClickListener(v->startActivity(new Intent(this,EditProfileActivity.class)));
  findViewById(R.id.btnLogout).setOnClickListener(v->logout());
  selectedTab=b==null?R.id.nav_home:b.getInt("selectedTab",R.id.nav_home);
  nav.setSelectedItemId(selectedTab);showTab(selectedTab,false);
  nav.setOnItemSelectedListener(item->{showTab(item.getItemId(),true);return true;});
  androidx.activity.OnBackPressedCallback back=new androidx.activity.OnBackPressedCallback(true){@Override public void handleOnBackPressed(){if(selectedTab!=R.id.nav_home){((BottomNavigationView)findViewById(R.id.bottomNavigation)).setSelectedItemId(R.id.nav_home);}else{setEnabled(false);getOnBackPressedDispatcher().onBackPressed();}}};
  getOnBackPressedDispatcher().addCallback(this,back);
  WorkManager.getInstance(this).cancelUniqueWork("expiry_check");WorkManager.getInstance(this).enqueueUniquePeriodicWork("grocery_expiry",ExistingPeriodicWorkPolicy.KEEP,new PeriodicWorkRequest.Builder(ExpiryWorker.class,6,TimeUnit.HOURS).build());
 }
 @Override protected void onResume(){super.onResume();if(vm!=null&&!isFinishing()){vm.refresh();stats();collectReminders();updateNotificationState();loadProfile();clock.postDelayed(midnight,60000);}}
 @Override protected void onPause(){super.onPause();clock.removeCallbacks(midnight);}
 @Override protected void onNewIntent(Intent i){super.onNewIntent(i);setIntent(i);long id=i.getLongExtra("batch_id",0);if(id!=0&&!all.isEmpty()){getIntent().removeExtra("batch_id");details(id);}}
 void collectReminders(){if(Auth.session(this)!=0)Auth.IO.execute(()->{try{ExpiryWorker.check(getApplicationContext());}catch(Exception ignored){/* Foreground date statuses remain available; worker will retry. */}});}
 Chip chip(String text){Chip c=new Chip(this);c.setText(text);c.setId(View.generateViewId());c.setCheckable(true);c.setEnsureMinTouchTargetSize(true);c.setTextSize(12);c.setChipCornerRadius(dp(14));c.setChipStrokeWidth(0);return c;}
 private int categoryGeneration;
 void categories(){final int token=++categoryGeneration;Auth.IO.execute(()->{java.util.List<String> stored=repo.dao.categories();runOnUiThread(()->{
  if(isDestroyed()||token!=categoryGeneration)return;
  ChipGroup group=findViewById(R.id.groupCategory);List<String> names=new ArrayList<>();names.add("All");names.addAll(CategorySuggestions.choices(stored));
  String selected="All";for(String name:names)if(CategorySuggestions.key(name).equals(CategorySuggestions.key(vm.category()))){selected=name;break;}
  group.setOnCheckedStateChangeListener(null);group.setSelectionRequired(false);
  Map<String,Chip> existing=new LinkedHashMap<>();for(int i=0;i<group.getChildCount();i++){Chip item=(Chip)group.getChildAt(i);existing.put(item.getText().toString(),item);}
  for(Map.Entry<String,Chip> entry:existing.entrySet())if(!names.contains(entry.getKey()))group.removeView(entry.getValue());
  for(int i=0;i<names.size();i++){String name=names.get(i);Chip item=existing.get(name);if(item==null){item=chip(name);item.setSingleLine(true);group.addView(item,i);}else if(group.indexOfChild(item)!=i){group.removeView(item);group.addView(item,i);}if(name.equals(selected))group.check(item.getId());}
  group.setSelectionRequired(true);vm.setCategory(selected);
  group.setOnCheckedStateChangeListener((g,ids)->{if(!ids.isEmpty()){Chip item=g.findViewById(ids.get(0));if(item!=null)vm.setCategory(item.getText().toString());}});
 });});}

 void stats(){if(vm==null)return;Set<String> products=new HashSet<>();Map<String,Double> totals=new HashMap<>();Map<String,Double>thresholds=new HashMap<>();int soon=0,today=0,expired=0;for(Product p:all)if(!p.archived){products.add(p.sku);totals.put(p.sku,totals.getOrDefault(p.sku,0.0)+p.quantity);thresholds.put(p.sku,p.lowStock);if(p.quantity>0&&"Expiry".equals(p.dateType)){long d=Expiry.daysLeft(p);if(d<0)expired++;else if(d==0)today++;else if(d<=7)soon++;}}int low=0;for(String sku:products)if(Rules.rounded(totals.get(sku))<=thresholds.get(sku))low++;((TextView)findViewById(R.id.tvTotal)).setText(""+products.size());((TextView)findViewById(R.id.tvSoon)).setText(""+soon);((TextView)findViewById(R.id.tvExpired)).setText(""+expired);((TextView)findViewById(R.id.tvExtraStats)).setText(today+" due today · "+low+" low stock");}
 interface Job{void run()throws Exception;}
 void save(AlertDialog dialog,TextView error,Job job){Button button=dialog.getButton(-1);button.setEnabled(false);dialog.getButton(-2).setEnabled(false);dialog.setCancelable(false);error.setVisibility(View.VISIBLE);error.setText("Saving…");Auth.IO.execute(()->{try{if(Auth.session(this)!=repo.staffId)throw new IllegalArgumentException("Session ended; sign in again");job.run();runOnUiThread(()->{if(isDestroyed())return;dialog.dismiss();collectReminders();Toast.makeText(this,"Saved",Toast.LENGTH_SHORT).show();});}catch(Exception ex){runOnUiThread(()->{if(isDestroyed())return;button.setEnabled(true);dialog.getButton(-2).setEnabled(true);dialog.setCancelable(true);error.setText(Screens.error(ex));});}});}
 void productForm(Product source,boolean additional,String scanned){
  boolean edit=source!=null&&!additional;View view=getLayoutInflater().inflate(R.layout.dialog_product,null);EditText name=view.findViewById(R.id.etName),sku=view.findViewById(R.id.etSku),code=view.findViewById(R.id.etBarcode),brand=view.findViewById(R.id.etBrand),lot=view.findViewById(R.id.etBatch),qty=view.findViewById(R.id.etQty),unit=view.findViewById(R.id.etUnit),low=view.findViewById(R.id.etLow),location=view.findViewById(R.id.etLocation);MaterialAutoCompleteTextView category=view.findViewById(R.id.etCategory);Spinner type=view.findViewById(R.id.spDateType);type.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"Expiry","Best Before","Unknown"}));Button date=view.findViewById(R.id.btnDate),received=view.findViewById(R.id.btnReceived);CheckBox past=view.findViewById(R.id.checkPast);TextView error=view.findViewById(R.id.tvError),hint=view.findViewById(R.id.tvFormHint);
  EditText description=view.findViewById(R.id.etDescription),pack=view.findViewById(R.id.etPackSize);description.setText(source==null?"":source.description);pack.setText(source==null?"":source.packSize);
  EditText stockReference=view.findViewById(R.id.etStockReference);stockReference.setText(edit?source.stockReference:"");
  EditText receivedQty=view.findViewById(R.id.etReceivedQty);receivedQty.setText(edit?Rules.qty(source.received):"");
  EditText variation=view.findViewById(R.id.etVariation);variation.setText(source==null?"":source.variation);
  Long[] expiry={edit?source.expiryDate:null};long[] receivedDay={edit?source.receivedDay:LocalDate.now().toEpochDay()};
  name.setText(source==null?"":source.name);sku.setText(source==null?"":source.sku);code.setText(source==null?scanned:source.barcode);brand.setText(source==null?"":source.brand);lot.setText(edit?source.batchNo:"");qty.setText(edit?Rules.qty(source.quantity):"");unit.setText(source==null?"pieces":source.unit);low.setText(source==null?"5":Rules.qty(source.lowStock));location.setText(source==null?"":source.location);category.setText(source==null?"":source.category,false);type.setSelection(edit?("Best Before".equals(source.dateType)?1:"Unknown".equals(source.dateType)?2:0):0);
  date.setText(expiry[0]==null?"Choose printed date":LocalDate.ofEpochDay(expiry[0]).format(Expiry.DISPLAY));received.setText(receivedDay[0]==0?"Received date not recorded":"Received: "+LocalDate.ofEpochDay(receivedDay[0]).format(Expiry.DISPLAY));
  date.setOnClickListener(v->{LocalDate d=expiry[0]==null?LocalDate.now():LocalDate.ofEpochDay(expiry[0]);DatePickerDialog picker=new DatePickerDialog(this,(p,y,m,day)->{expiry[0]=LocalDate.of(y,m+1,day).toEpochDay();date.setText(LocalDate.ofEpochDay(expiry[0]).format(Expiry.DISPLAY));},d.getYear(),d.getMonthValue()-1,d.getDayOfMonth());picker.show();});
  received.setOnClickListener(v->{LocalDate d=receivedDay[0]==0?LocalDate.now():LocalDate.ofEpochDay(receivedDay[0]);new DatePickerDialog(this,(p,y,m,day)->{receivedDay[0]=LocalDate.of(y,m+1,day).toEpochDay();received.setText("Received: "+LocalDate.ofEpochDay(receivedDay[0]).format(Expiry.DISPLAY));},d.getYear(),d.getMonthValue()-1,d.getDayOfMonth()).show();});
  type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){public void onItemSelected(AdapterView<?>a,View v,int pos,long id){date.setEnabled(pos!=2);if(pos==2){expiry[0]=null;date.setText("Date not recorded");}}public void onNothingSelected(AdapterView<?> a){}});
  if(additional){view.findViewById(R.id.productFields).setVisibility(View.GONE);TextView title=view.findViewById(R.id.formProductName);title.setVisibility(View.VISIBLE);title.setText(source.name+" · "+source.unit);for(View v:new View[]{name,sku,code,brand,unit,low})v.setEnabled(false);}

  SavedChoice categoryChoice=new SavedChoice(view,category,R.id.etCustomCategory,R.id.etCustomCategoryBox,"category",Validators.CATEGORIES);
  MeasurementBinding measurement=new MeasurementBinding(view,source);
  sku.setFocusable(false);if(edit){hint.setText("Product fields apply to all batches. Stock fields update batch "+source.stockReference+" only. Unit changes relabel quantities; numbers are not converted.");}else if(source==null){Runnable preview=()->{String value=Screens.text((EditText)view.findViewById(R.id.etMeasurementValue));sku.setText(SkuGenerator.ready(Screens.text(code),Screens.text(name),categoryChoice.value(),Screens.text(brand),value,measurement.unit(),Screens.text(variation))?SkuGenerator.generate(Screens.text(name),categoryChoice.value(),Screens.text(brand),value,Screens.text(variation)):"");};TextWatcher skuWatcher=new TextWatcher(){public void beforeTextChanged(CharSequence s,int start,int count,int after){}public void onTextChanged(CharSequence s,int start,int before,int count){preview.run();}public void afterTextChanged(Editable e){}};for(EditText field:new EditText[]{name,category,brand,pack,variation,code,(EditText)view.findViewById(R.id.etCustomCategory),(EditText)view.findViewById(R.id.etMeasurementValue),(EditText)view.findViewById(R.id.etMeasurementUnit),(EditText)view.findViewById(R.id.etCustomMeasurementUnit)})field.addTextChangedListener(skuWatcher);preview.run();hint.setText("Enter a barcode and complete product details to generate the SKU. Enter your batch reference below.");}
  ((TextInputLayout)view.findViewById(R.id.tilBarcode)).setEndIconOnClickListener(v->{if(!additional)scan(code::setText);});
  AlertDialog dialog=new MaterialAlertDialogBuilder(this).setTitle(edit?"Edit grocery product":additional?"Add stock":"Add grocery product").setView(view).setPositiveButton("Save",null).setNegativeButton("Cancel",null).create();
  photoEditor=new ProductPhotoEditor(this,view,dialog,source==null?null:source.photoPath,()->productPhotoPicker.launch(new String[]{"image/*"}));final ProductPhotoEditor editor=photoEditor;if(additional)view.findViewById(R.id.productPhotoEditor).setVisibility(View.GONE);
  if(!additional)new ProductLookupBinding(this,view,dialog,repo.dao,source==null,measurement);
  dialog.setOnShowListener(d->{sizeProductForm(dialog);dialog.getButton(-1).setOnClickListener(v->{try{if(!Screens.required(category)||!Screens.required(name)||!Screens.required(unit)||!Screens.required(stockReference))return;if(source==null)SkuGenerator.requireReady(Screens.text(code),Screens.text(name),categoryChoice.value(),Screens.text(brand),measurement.value(),measurement.unit(),Screens.text(variation));Product p=source==null?new Product():source.copy();p.variation=Screens.text(variation);p.description=Screens.text(description);p.packSize=Screens.text(pack);p.measurementValue=measurement.value();p.measurementUnit=measurement.unit();p.name=Screens.text(name);p.sku=Screens.text(sku);p.barcode=Screens.text(code);p.brand=Screens.text(brand);p.category=categoryChoice.value();p.unit=Screens.text(unit);p.lowStock=Rules.quantity(Screens.text(low),true);{if(!edit)p.id=0;p.stockReference=Screens.text(stockReference);p.batchNo=Screens.text(lot);p.quantity=Rules.quantity(Screens.text(qty),true);p.received=Rules.quantity(Screens.text(receivedQty),true);if(p.quantity>p.received)throw new IllegalArgumentException("Quantity remaining cannot exceed quantity received");if(additional&&p.received==0)throw new IllegalArgumentException("Received stock must be greater than zero");p.location=Screens.text(location);p.dateType=type.getSelectedItem().toString();p.expiryDate=expiry[0];p.receivedDay=receivedDay[0];}Validators.product(p);boolean ack=past.isChecked();editor.lock();save(dialog,error,()->{String photo=null;try{photo=additional?p.photoPath:editor.commit();p.photoPath=photo;if(edit)repo.editFull(p,ack);else if(additional)repo.receiveQuantities(p,true,ack);else repo.registerQuantities(p,ack);editor.saved=true;}catch(Exception ex){editor.rollback(photo);editor.unlock();throw ex;}});}catch(Exception e){error.setVisibility(View.VISIBLE);error.setText(Screens.error(e));}});});dialog.show();
 }
 // Give the long form a stable viewport instead of remeasuring wrap_content on focus.
 // Android may constrain it while the IME is visible; the requested height stays intact.
 private void sizeProductForm(AlertDialog dialog){
  Window window=dialog.getWindow();if(window==null)return;
  int availableHeight;
  if(Build.VERSION.SDK_INT>=30){
   WindowMetrics metrics=getWindowManager().getCurrentWindowMetrics();
   android.graphics.Insets bars=metrics.getWindowInsets().getInsetsIgnoringVisibility(WindowInsets.Type.systemBars()|WindowInsets.Type.displayCutout());
   availableHeight=metrics.getBounds().height()-bars.top-bars.bottom;
  }else{
   android.graphics.Rect frame=new android.graphics.Rect();getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
   availableHeight=frame.height();
  }
  if(availableHeight<=0)availableHeight=getResources().getDisplayMetrics().heightPixels;
  window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
  // Keep the theme's existing width and rounded card styling.
  window.setLayout(window.getAttributes().width,Math.max(1,(int)(availableHeight*0.85f)));
 }
 Product local(long id){for(Product p:all)if(p.id==id)return p;return null;}
 void details(long id){
  Product p=local(id);if(p==null)return;double total=0;for(Product row:all)if(row.sku.equals(p.sku))total+=row.quantity;
  Product recommended=StockRepository.fefo(all,p.sku);
  String fefo=recommended==null?"No eligible stock to recommend.":recommended.expiryDate==null?"No recorded date for eligible stock. Inspect the packaging before removal.":"Use earliest date first · recommended stock "+recommended.stockReference+". Confirm the actual stock removed.";
  View v=getLayoutInflater().inflate(R.layout.dialog_details,null);
  ProductImages.show((ImageView)v.findViewById(R.id.detailPhoto),p.photoPath,0,true);
  ((TextView)v.findViewById(R.id.detailCategory)).setText(p.category.toUpperCase(Locale.ROOT));
  ((TextView)v.findViewById(R.id.detailName)).setText(p.name);
  ((TextView)v.findViewById(R.id.detailCode)).setText("SKU: "+p.sku+"\nBarcode: "+(p.barcode==null||p.barcode.isEmpty()?"not recorded":p.barcode));
  ((TextView)v.findViewById(R.id.detailQuantity)).setText(Rules.qty(p.quantity)+" "+p.unit);
  TextView status=v.findViewById(R.id.detailStatus);status.setText(p.deleted?"Deleted":p.archived?"Archived":Expiry.label(p));status.setTextColor(Expiry.color(p));
  android.text.SpannableStringBuilder info=new android.text.SpannableStringBuilder();
  detailRow(info,"Description",p.description);detailRow(info,"Variation",p.variation);
  String pack=p.packSize;if(p.measurementValue!=null&&!p.measurementValue.isEmpty()&&p.measurementUnit!=null&&!p.measurementUnit.isEmpty())pack=p.measurementValue+" "+SavedChoice.displayUnit(p.measurementUnit);
  detailRow(info,"Pack Size",pack);detailRow(info,"Quantity Received",Rules.qty(p.received)+" "+p.unit);detailRow(info,"Quantity Remaining",Rules.qty(p.quantity)+" "+p.unit);
  detailRow(info,"Stock Reference",p.stockReference);detailRow(info,"Supplier Lot",p.batchNo);detailRow(info,"Storage",p.location);
  if(p.expiryDate!=null)detailRow(info,p.dateType,Expiry.date(p));if(p.receivedDay!=0)detailRow(info,"Received",LocalDate.ofEpochDay(p.receivedDay).format(Expiry.DISPLAY));
  detailRow(info,"All Stock for This Product",Rules.qty(Rules.rounded(total))+" "+p.unit);((TextView)v.findViewById(R.id.detailInfo)).setText(info);

  ((TextView)v.findViewById(R.id.detailFefo)).setText(fefo);
  AlertDialog dialog=new MaterialAlertDialogBuilder(this).setView(v).setNegativeButton("Close",null).create();
  Button out=v.findViewById(R.id.btnDetailOut);out.setEnabled(!p.archived&&p.quantity>0);out.setOnClickListener(x->{dialog.dismiss();out(p);});
  Button add=v.findViewById(R.id.btnDetailAdd);add.setEnabled(!p.archived);add.setOnClickListener(x->{dialog.dismiss();productForm(p,true,"");});
  v.findViewById(R.id.btnDetailEdit).setOnClickListener(x->{dialog.dismiss();productForm(p,false,"");});
  v.findViewById(R.id.btnDetailAll).setOnClickListener(x->{dialog.dismiss();((BottomNavigationView)findViewById(R.id.bottomNavigation)).setSelectedItemId(R.id.nav_inventory);if(p.archived)selectFilter(p.deleted?ExpiryFilter.DELETED:ExpiryFilter.ARCHIVED);((EditText)findViewById(R.id.etSearch)).setText(p.sku);});
  Button archive=v.findViewById(R.id.btnDetailArchive);archive.setText(p.archived?"Restore product":"Archive product");archive.setOnClickListener(x->{dialog.dismiss();confirmAction(p.archived?"Restore product?":"Archive product?",p.archived?"Return this product and all its stock to the active list?":"Move this product and all its stock to Archived? Quantities and history are kept. Archived stock is excluded from active alerts.",()->repo.archive(p.sku,!p.archived));});
  Button permanent=v.findViewById(R.id.btnPermanentDelete);permanent.setVisibility(p.deleted?View.VISIBLE:View.GONE);permanent.setOnClickListener(x->{dialog.dismiss();confirmAction("Permanently delete "+p.name+"?","This removes every batch, stock movement, reminder and unshared photo for this product. It cannot be restored. Reports will no longer contain its history.",()->{java.util.List<Product> removed=repo.dao.sku(p.sku);java.util.List<String> paths=repo.permanentlyDelete(p.sku);android.app.NotificationManager manager=getSystemService(android.app.NotificationManager.class);for(Product batch:removed)for(int milestone:new int[]{0,1,3,7})manager.cancel((int)(batch.id*10+milestone));for(String path:paths)ProfilePhotos.delete(getApplicationContext(),path);});});
  Button delete=v.findViewById(R.id.btnDetailDelete);delete.setVisibility(p.deleted?View.GONE:View.VISIBLE);delete.setOnClickListener(x->{dialog.dismiss();confirmAction("Delete product?","Move this product to Deleted? Its stock and history are kept. Restore it from Inventory → Deleted.",()->repo.delete(p.sku));});dialog.show();dialog.getWindow().setLayout((int)(getResources().getDisplayMetrics().widthPixels*0.94),(int)(getResources().getDisplayMetrics().heightPixels*0.85));
 }
 void out(Product p){View view=getLayoutInflater().inflate(R.layout.dialog_stock_out,null);EditText qty=view.findViewById(R.id.etOutQty),note=view.findViewById(R.id.etOutNote);Spinner reason=view.findViewById(R.id.spReason);reason.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"Sold","Expired","Damaged","Returned to Supplier"}));CheckBox confirmed=view.findViewById(R.id.checkBatch);TextView info=view.findViewById(R.id.tvOutInfo),error=view.findViewById(R.id.tvError);info.setText(p.name+" · Batch "+p.stockReference+"\n"+p.location+" · "+Rules.qty(p.quantity)+" "+p.unit+" available\n"+p.dateType+": "+Expiry.date(p)+"\n"+Expiry.label(p));AlertDialog dialog=new MaterialAlertDialogBuilder(this).setTitle("Record stock out").setView(view).setPositiveButton("Save movement",null).setNegativeButton("Cancel",null).create();dialog.setOnShowListener(d->dialog.getButton(-1).setOnClickListener(v->{try{double amount=Rules.quantity(Screens.text(qty),false);if(!confirmed.isChecked())throw new IllegalArgumentException("Confirm the actual batch removed");String why=reason.getSelectedItem().toString(),notes=Screens.text(note);save(dialog,error,()->repo.out(p.id,amount,why,notes));}catch(Exception e){error.setVisibility(View.VISIBLE);error.setText(Screens.error(e));}}));dialog.show();}
 void confirmAction(String title,String message,Job action){TextView error=new TextView(this);error.setPadding(32,20,32,20);error.setText(message);AlertDialog d=new MaterialAlertDialogBuilder(this).setTitle(title).setView(error).setPositiveButton("Confirm",null).setNegativeButton("Cancel",null).create();d.setOnShowListener(x->d.getButton(-1).setOnClickListener(v->save(d,error,action)));d.show();}
 int dp(int value){return Math.round(getResources().getDisplayMetrics().density*value);}
 @Override protected void onSaveInstanceState(Bundle out){super.onSaveInstanceState(out);out.putInt("selectedTab",selectedTab);}
 void logout(){Auth.logout(this);WorkManager.getInstance(this).cancelUniqueWork("grocery_expiry");Screens.login(this);}
 void selectFilter(ExpiryFilter filter){ChipGroup group=findViewById(R.id.groupFilter);for(int i=0;i<group.getChildCount();i++){Chip c=(Chip)group.getChildAt(i);if(c.getTag()==filter){group.check(c.getId());break;}}vm.setFilter(filter);}
 private boolean nameSort=false;
 void showTab(int tab,boolean reset){
  if(tab==R.id.nav_inventory){nameSort=getSharedPreferences("preferences",MODE_PRIVATE).getBoolean("sort_"+repo.staffId,false);vm.sortName(nameSort);((Button)findViewById(R.id.btnSort)).setText(nameSort?"Sort: product name":"Sort: earliest date");}
  boolean tabChanged=selectedTab!=tab;selectedTab=tab;if(tabChanged)((com.google.android.material.appbar.AppBarLayout)findViewById(R.id.dashboardAppBar)).setExpanded(true,false);boolean profile=tab==R.id.nav_profile,home=tab==R.id.nav_home,alerts=tab==R.id.nav_alerts;
  findViewById(R.id.inventoryPanel).setVisibility(profile?View.GONE:View.VISIBLE);findViewById(R.id.profilePanel).setVisibility(profile?View.VISIBLE:View.GONE);
  findViewById(R.id.homeIntro).setVisibility(home?View.VISIBLE:View.GONE);findViewById(R.id.homeStats).setVisibility(home?View.VISIBLE:View.GONE);
  findViewById(R.id.quickActions).setVisibility(alerts?View.GONE:View.VISIBLE);findViewById(R.id.filterStrip).setVisibility(home||alerts?View.GONE:View.VISIBLE);findViewById(R.id.categoryStrip).setVisibility(home||alerts?View.GONE:View.VISIBLE);
  ((MaterialToolbar)findViewById(R.id.toolbar)).setTitle("ShelfLife");((MaterialToolbar)findViewById(R.id.toolbar)).setSubtitle(null);
  ((TextView)findViewById(R.id.sectionTitle)).setText(alerts?"Expiry alerts":home?"Products":"Inventory");
  ((TextView)findViewById(R.id.sectionSubtitle)).setText(alerts?"Active stock · earliest recorded date first":home?"Earliest dates appear first":"Search, filter and manage your store stock");
  ((TextView)findViewById(R.id.tvEmpty)).setText(alerts?"No active date alerts match this search.":"No matching stock. Add a product or try another filter.");
  if(reset&&!profile){((EditText)findViewById(R.id.etSearch)).setText("");vm.setCategory("All");((HorizontalScrollView)findViewById(R.id.categoryStrip)).scrollTo(0,0);selectFilter(alerts?ExpiryFilter.ATTENTION:ExpiryFilter.ALL);categories();}
  findViewById(R.id.btnSort).setVisibility(!home&&!alerts&&!profile?View.VISIBLE:View.GONE);if(alerts){nameSort=false;vm.sortName(false);((Button)findViewById(R.id.btnSort)).setText("Sort: earliest date");}if(profile)loadProfile();
 }
 void alerts(){((BottomNavigationView)findViewById(R.id.bottomNavigation)).setSelectedItemId(R.id.nav_alerts);}
 void profile(){((BottomNavigationView)findViewById(R.id.bottomNavigation)).setSelectedItemId(R.id.nav_profile);}
 void loadProfile(){Auth.IO.execute(()->{try{Staff staff=repo.dao.staff(repo.staffId);if(staff==null)return;android.graphics.Bitmap avatar=ProfilePhotos.load(getApplicationContext(),staff.photoPath);runOnUiThread(()->{if(isDestroyed())return;((TextView)findViewById(R.id.profileName)).setText(staff.name);ImageView header=findViewById(R.id.headerPhoto);header.setImageBitmap(avatar);header.setVisibility(avatar==null?View.GONE:View.VISIBLE);TextView initials=findViewById(R.id.headerInitial);initials.setText(staff.name==null||staff.name.isEmpty()?"S":staff.name.substring(0,1).toUpperCase(Locale.ROOT));initials.setVisibility(avatar==null?View.VISIBLE:View.GONE);findViewById(R.id.headerProfile).setContentDescription("Open profile for "+staff.name);((TextView)findViewById(R.id.profileEmail)).setText(staff.email+(staff.phone==null||staff.phone.isEmpty()?"":"\n"+staff.phone));ImageView image=findViewById(R.id.profilePhoto);image.setImageBitmap(avatar);image.setVisibility(avatar==null?View.GONE:View.VISIBLE);findViewById(R.id.profileInitial).setVisibility(avatar==null?View.VISIBLE:View.GONE);((TextView)findViewById(R.id.profileInitial)).setText(staff.name.isEmpty()?"SL":staff.name.substring(0,1).toUpperCase(Locale.ROOT));updateNotificationState();});}catch(Exception e){runOnUiThread(()->Toast.makeText(this,"Could not load profile",Toast.LENGTH_LONG).show());}});}
 void updateNotificationState(){}
 void notificationSettings(){if(Build.VERSION.SDK_INT>=33&&androidx.core.content.ContextCompat.checkSelfPermission(this,Manifest.permission.POST_NOTIFICATIONS)!=android.content.pm.PackageManager.PERMISSION_GRANTED){new MaterialAlertDialogBuilder(this).setTitle("Stay ahead of expiry").setMessage("Allow ShelfLife to remind you about approaching dates. Android may delay background delivery.").setPositiveButton("Allow notifications",(d,w)->notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)).setNeutralButton("Open Android settings",(d,w)->openNotificationSettings()).setNegativeButton("Not now",null).show();}else openNotificationSettings();}
 void openNotificationSettings(){try{startActivity(new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE,getPackageName()));}catch(Exception e){startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+getPackageName())));}}
 void scan(Consumer<String> target){if(scanning)return;scanTarget=target;new MaterialAlertDialogBuilder(this).setTitle("Product barcode").setItems(new String[]{"Scan with camera","Enter barcode manually"},(d,pos)->{if(pos==0){scanning=true;cameraPermission.launch(Manifest.permission.CAMERA);}else manual("Enter all digits, including leading zeros.");}).show();}
 void launchCamera(){try{camera.launch(new ScanOptions().setDesiredBarcodeFormats("EAN_13","EAN_8","UPC_A","UPC_E","CODE_128").setPrompt("Scan grocery barcode. Enter its printed date separately.").setBeepEnabled(false).setOrientationLocked(false));}catch(Exception e){scanning=false;manual("Camera unavailable. Use manual entry.");}}
 void manual(String prompt){View v=getLayoutInflater().inflate(R.layout.dialog_barcode,null);((TextView)v.findViewById(R.id.dialogHint)).setText(prompt);EditText input=v.findViewById(R.id.dialogInput);AlertDialog dialog=new MaterialAlertDialogBuilder(this).setTitle("Find a product").setView(v).setPositiveButton("Find product",null).setNegativeButton("Cancel",(x,w)->scanTarget=null).create();dialog.setOnShowListener(x->dialog.getButton(-1).setOnClickListener(w->{if(!Screens.required(input))return;String code=Screens.text(input);dialog.dismiss();scanned(code);}));dialog.show();}
 void scanned(String code){if(scanTarget!=null){Consumer<String> result=scanTarget;scanTarget=null;result.accept(code);return;}Auth.IO.execute(()->{try{Product p=repo.dao.barcode(code);runOnUiThread(()->{if(p==null)productForm(null,false,code);else details(p.id);});}catch(Exception e){runOnUiThread(()->Toast.makeText(this,"Could not look up barcode",Toast.LENGTH_LONG).show());}});}
 static void detailRow(android.text.SpannableStringBuilder out,String label,String value){if(value==null||value.trim().isEmpty())return;if(out.length()>0)out.append("\n");int start=out.length();out.append(label).append(": ");out.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),start,out.length(),android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);out.append(value.trim());}
 void configureCategories(MaterialAutoCompleteTextView field){
  Set<String> choices=new LinkedHashSet<>(Arrays.asList(Validators.CATEGORIES));for(Product p:all)if(CategorySuggestions.inventoryVisible(p.category))choices.add(p.category.trim());
  field.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_dropdown_item_1line,new ArrayList<>(choices)));field.setThreshold(0);field.setOnClickListener(v->field.showDropDown());
 }
 void reviewScannedProduct(Product product){
  View view=getLayoutInflater().inflate(R.layout.dialog_scan_result,null);((TextView)view.findViewById(R.id.scanProduct)).setText(product.name);((TextView)view.findViewById(R.id.scanBarcode)).setText("Barcode "+product.barcode+" · SKU "+product.sku);
  MaterialAutoCompleteTextView category=view.findViewById(R.id.etCategory);configureCategories(category);category.setText(product.category,false);TextView error=view.findViewById(R.id.tvError);
  Button stock=view.findViewById(R.id.btnScanStock),only=view.findViewById(R.id.btnScanCategory),details=view.findViewById(R.id.btnScanDetails);stock.setEnabled(!product.archived);
  AlertDialog dialog=new MaterialAlertDialogBuilder(this).setView(view).setNegativeButton("Close",null).create();
  java.util.function.Consumer<Boolean> saveCategory=addStock->{if(!Screens.required(category))return;Product changed=product.copy();changed.category=Screens.text(category);stock.setEnabled(false);only.setEnabled(false);details.setEnabled(false);category.setEnabled(false);dialog.setCancelable(false);dialog.getButton(-2).setEnabled(false);error.setText("Saving category…");Auth.IO.execute(()->{try{if(Auth.session(this)!=repo.staffId)throw new IllegalArgumentException("Sign in again");repo.edit(changed);runOnUiThread(()->{if(isDestroyed())return;dialog.dismiss();Toast.makeText(this,"Category updated",Toast.LENGTH_SHORT).show();if(addStock)productForm(changed,true,"");});}catch(Exception e){runOnUiThread(()->{if(isDestroyed())return;stock.setEnabled(!product.archived);only.setEnabled(true);details.setEnabled(true);category.setEnabled(true);dialog.setCancelable(true);dialog.getButton(-2).setEnabled(true);error.setText(Screens.error(e));});}});};
  stock.setOnClickListener(v->saveCategory.accept(true));only.setOnClickListener(v->saveCategory.accept(false));details.setOnClickListener(v->{dialog.dismiss();details(product.id);});dialog.show();
 }
 void settings(){String[] options={"Notification settings","Default inventory sort","Reports & stock history","Product lookup & data"};new MaterialAlertDialogBuilder(this).setTitle("Settings").setItems(options,(d,which)->{if(which==0)notificationSettings();else if(which==2)startActivity(new Intent(this,ReportActivity.class));else if(which==3)new MaterialAlertDialogBuilder(this).setTitle("Product lookup").setMessage("Local inventory is checked first. Unknown numeric barcodes are sent to Open Food Facts over HTTPS. Suggestions may be incomplete; review before saving. Stock quantities and expiry dates are entered by staff.\n\nProduct data: Open Food Facts contributors, ODbL. https://world.openfoodfacts.org/terms-of-use\n\nAccounts, inventory and photos are stored on this device. No cloud sync.").setPositiveButton("OK",null).show();else{android.content.SharedPreferences prefs=getSharedPreferences("preferences",MODE_PRIVATE);String key="sort_"+repo.staffId;new MaterialAlertDialogBuilder(this).setTitle("Default inventory sort").setSingleChoiceItems(new String[]{"Earliest date","Product name"},prefs.getBoolean(key,false)?1:0,(choice,index)->{prefs.edit().putBoolean(key,index==1).apply();nameSort=index==1;vm.sortName(nameSort);((Button)findViewById(R.id.btnSort)).setText(nameSort?"Sort: product name":"Sort: earliest date");choice.dismiss();}).setNegativeButton("Cancel",null).show();}}).setNegativeButton("Close",null).show();}
}
