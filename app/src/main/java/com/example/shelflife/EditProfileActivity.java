package com.example.shelflife;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.*;
import android.view.View;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.google.android.material.appbar.MaterialToolbar;

public class EditProfileActivity extends ProtectedActivity {
 private EditText name,email,phone,password;private TextView error,initial;private ImageView photo;private Button save,choose,remove;
 private Staff staff;private String draftPath;private boolean removePhoto,saving,saved,loadingPhoto;private long staffId;
 private final ActivityResultLauncher<String[]> picker=registerForActivityResult(new ActivityResultContracts.OpenDocument(),uri->{if(uri==null)return;loadingPhoto=true;buttons(false);error.setText("Preparing your photo…");Auth.IO.execute(()->{try{String path=ProfilePhotos.importDraft(getApplicationContext(),uri);Bitmap bitmap=ProfilePhotos.load(getApplicationContext(),path);runOnUiThread(()->{if(isDestroyed()){ProfilePhotos.delete(getApplicationContext(),path);return;}ProfilePhotos.delete(this,draftPath);draftPath=path;removePhoto=false;loadingPhoto=false;showPhoto(bitmap);error.setText("");buttons(true);});}catch(Exception e){runOnUiThread(()->{if(isDestroyed())return;loadingPhoto=false;buttons(true);error.setText(e.getMessage()==null?"Could not use that photo. Try another image.":e.getMessage());});}});});
 @Override protected void onCreate(Bundle state){super.onCreate(state);if(isFinishing())return;setContentView(R.layout.activity_edit_profile);Screens.insets(this);staffId=Auth.session(this);
  name=findViewById(R.id.editName);email=findViewById(R.id.editEmail);phone=findViewById(R.id.editPhone);password=findViewById(R.id.editPassword);error=findViewById(R.id.profileError);initial=findViewById(R.id.editInitial);photo=findViewById(R.id.editPhoto);save=findViewById(R.id.btnSaveProfile);choose=findViewById(R.id.btnChoosePhoto);remove=findViewById(R.id.btnRemovePhoto);
  if(state!=null){draftPath=state.getString("draft");removePhoto=state.getBoolean("remove",false);}
  ((MaterialToolbar)findViewById(R.id.toolbar)).setNavigationOnClickListener(v->{if(!saving&&!loadingPhoto)finish();});
  getOnBackPressedDispatcher().addCallback(this,new androidx.activity.OnBackPressedCallback(true){public void handleOnBackPressed(){if(!saving&&!loadingPhoto)finish();}});
  email.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int c,int f){}public void onTextChanged(CharSequence s,int a,int b,int c){emailSecurity();}public void afterTextChanged(Editable e){}});
  choose.setOnClickListener(v->picker.launch(new String[]{"image/*"}));remove.setOnClickListener(v->{ProfilePhotos.delete(this,draftPath);draftPath=null;removePhoto=true;showPhoto(null);});save.setOnClickListener(v->saveProfile());buttons(false);error.setText("Loading your profile…");
  Auth.IO.execute(()->{try{Staff loaded=AppDatabase.get(this).productDao().staff(staffId);if(loaded==null)throw new IllegalArgumentException("Account unavailable");String image=removePhoto?null:draftPath!=null?draftPath:loaded.photoPath;Bitmap avatar=ProfilePhotos.load(this,image);runOnUiThread(()->{if(isDestroyed())return;staff=loaded;if(state==null){name.setText(loaded.name);email.setText(loaded.email);phone.setText(loaded.phone);}initial.setText(loaded.name==null||loaded.name.isEmpty()?"SL":loaded.name.substring(0,1).toUpperCase(java.util.Locale.ROOT));showPhoto(avatar);emailSecurity();buttons(true);error.setText("");});}catch(Exception e){runOnUiThread(()->error.setText("Could not load profile. Go back and retry."));}});
 }
 void buttons(boolean enabled){save.setEnabled(enabled);choose.setEnabled(enabled);remove.setEnabled(enabled);}
 void showPhoto(Bitmap bitmap){photo.setImageBitmap(bitmap);photo.setVisibility(bitmap==null?View.GONE:View.VISIBLE);initial.setVisibility(bitmap==null?View.VISIBLE:View.GONE);}
 void emailSecurity(){if(staff!=null)findViewById(R.id.emailPasswordField).setVisibility(Rules.normalizeEmail(Screens.text(email)).equals(staff.email)?View.GONE:View.VISIBLE);}
 void saveProfile(){if(saving||loadingPhoto||staff==null||!Screens.required(name))return;String n=Screens.text(name),e=Screens.text(email),p=Screens.text(phone),pw=password.getText().toString();if(!Rules.email(e)){email.setError("Enter a valid email");return;}if(!Rules.normalizeEmail(e).equals(staff.email)&&pw.isEmpty()){password.setError("Current password is required to change email");return;}final String draft=draftPath;final boolean clear=removePhoto;saving=true;buttons(false);error.setText("Saving profile…");
  Auth.IO.execute(()->{try{ProfileRepository.update(getApplicationContext(),staffId,n,e,p,pw,draft,clear);ProfilePhotos.delete(getApplicationContext(),draft);runOnUiThread(()->{saved=true;if(isDestroyed())return;Toast.makeText(this,"Profile updated",Toast.LENGTH_SHORT).show();finish();});}catch(Exception ex){runOnUiThread(()->{if(isDestroyed())return;saving=false;buttons(true);error.setText(ex instanceof IllegalArgumentException?ex.getMessage():"Could not save your profile. Please retry.");});}});
 }
 @Override protected void onSaveInstanceState(Bundle out){super.onSaveInstanceState(out);out.putString("draft",draftPath);out.putBoolean("remove",removePhoto);}
 @Override protected void onDestroy(){super.onDestroy();if(isFinishing()&&!saved&&!saving)ProfilePhotos.delete(getApplicationContext(),draftPath);}
}
