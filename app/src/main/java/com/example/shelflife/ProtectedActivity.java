package com.example.shelflife;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
public abstract class ProtectedActivity extends AppCompatActivity {
 @Override protected void onCreate(Bundle b){super.onCreate(b);if(Auth.session(this)==0)Screens.login(this);}
 @Override protected void onResume(){super.onResume();long id=Auth.session(this);if(id==0){Screens.login(this);return;}Auth.IO.execute(()->{try{if(AppDatabase.get(this).productDao().staff(id)==null)runOnUiThread(()->{Auth.logout(this);Screens.login(this);});}catch(Exception e){runOnUiThread(()->{new androidx.appcompat.app.AlertDialog.Builder(this).setTitle("Database unavailable").setMessage("Could not open this grocery database. Existing data has not been deleted. Check the setup guide for version compatibility.").setPositiveButton("Back to login",(d,w)->{Auth.logout(this);Screens.login(this);}).setCancelable(false).show();});}});}
}
