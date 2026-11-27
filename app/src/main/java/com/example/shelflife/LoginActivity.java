package com.example.shelflife;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.*;
public class LoginActivity extends AppCompatActivity {
 private boolean creating,busy;private int staffCount=-1;
 private EditText email,pass,name,confirmation,approverEmail,approverPass;private TextView error,title,hint;private Button submit,toggle;private View registration,approval;
 @Override protected void onCreate(Bundle state){AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);super.onCreate(state);setContentView(R.layout.activity_login);Screens.insets(this);
  email=findViewById(R.id.etUser);pass=findViewById(R.id.etPass);name=findViewById(R.id.etStaffName);confirmation=findViewById(R.id.etConfirm);approverEmail=findViewById(R.id.etApprover);approverPass=findViewById(R.id.etApproverPass);error=findViewById(R.id.tvError);title=findViewById(R.id.loginTitle);hint=findViewById(R.id.loginHint);submit=findViewById(R.id.btnLogin);toggle=findViewById(R.id.btnRegister);registration=findViewById(R.id.registrationFields);approval=findViewById(R.id.approvalFields);
  submit.setOnClickListener(v->submit());toggle.setOnClickListener(v->{if(!busy){creating=!creating;display();}});submit.setEnabled(false);toggle.setEnabled(false);
  Auth.IO.execute(()->{try{AppDatabase db=AppDatabase.get(this);int count=db.productDao().staffCount();long current=Auth.session(this);boolean valid=current!=0&&db.productDao().staff(current)!=null;runOnUiThread(()->{staffCount=count;creating=state==null?count==0:state.getBoolean("creating",count==0);submit.setEnabled(true);toggle.setEnabled(true);display();if(valid)enter();});}catch(Exception e){runOnUiThread(()->{error.setVisibility(View.VISIBLE);error.setText("Cannot open the database. Do not clear app data. See START-HERE.md for version compatibility and retry after correcting setup.");});}});
 }
 void display(){findViewById(R.id.confirmationField).setVisibility(creating?View.VISIBLE:View.GONE);((TextView)findViewById(R.id.loginSubtitle)).setText(creating?(staffCount==0?"Create the first staff account for this store.":"Create your account with approval from existing staff."):"Sign in to your store workspace.");registration.setVisibility(creating?View.VISIBLE:View.GONE);approval.setVisibility(creating&&staffCount>0?View.VISIBLE:View.GONE);title.setText(creating?(staffCount==0?"Set up store staff":"Enroll a staff member"):"Welcome back");submit.setText(creating?"Create staff account":"Log in");toggle.setText(creating?"Already registered? Log in":"Create staff account");hint.setText(staffCount==0?"First setup: only the authorized person managing this device should create the first store account.":"One grocery store on this device. New staff need approval using an existing staff member’s credentials.");error.setVisibility(View.GONE);}
 @Override protected void onSaveInstanceState(Bundle out){super.onSaveInstanceState(out);out.putBoolean("creating",creating);}
 void enter(){if(isFinishing()||isDestroyed())return;startActivity(new Intent(this,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));finish();}
 void submit(){if(busy||!Screens.required(email)||!Screens.required(pass))return;String mail=Rules.normalizeEmail(Screens.text(email)),password=pass.getText().toString();if(!Rules.email(mail)){email.setError("Enter a valid email");return;}if(creating){if(!Screens.required(name))return;if(password.length()<8){pass.setError("At least 8 characters");return;}if(!password.equals(confirmation.getText().toString())){confirmation.setError("Passwords do not match");return;}if(staffCount>0&&(!Screens.required(approverEmail)||!Screens.required(approverPass)))return;}
  final boolean create=creating;String staffName=Screens.text(name),aEmail=Screens.text(approverEmail),aPass=approverPass.getText().toString();busy=true;submit.setEnabled(false);toggle.setEnabled(false);error.setVisibility(View.VISIBLE);error.setText("Please wait…");
  Auth.IO.execute(()->{try{AppDatabase db=AppDatabase.get(this);Staff a=create?Auth.register(db,staffName,mail,password,aEmail,aPass):Auth.login(db.productDao(),mail,password);Auth.remember(this,a.id);runOnUiThread(this::enter);}catch(Exception e){runOnUiThread(()->{busy=false;submit.setEnabled(true);toggle.setEnabled(true);error.setText(Screens.error(e));});}});
 }
}
