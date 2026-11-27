package com.example.shelflife;

import android.app.Activity;
import android.content.Intent;
import android.view.*;
import android.widget.*;
import androidx.core.view.*;
public final class Screens {
 private Screens(){}
 public static void insets(Activity a){View root=((android.view.ViewGroup)a.findViewById(android.R.id.content)).getChildAt(0);ViewCompat.setOnApplyWindowInsetsListener(root,(v,i)->{androidx.core.graphics.Insets b=i.getInsets(WindowInsetsCompat.Type.systemBars()|WindowInsetsCompat.Type.ime());v.setPadding(b.left,b.top,b.right,b.bottom);return WindowInsetsCompat.CONSUMED;});ViewCompat.requestApplyInsets(root);}
 public static void login(Activity a){a.startActivity(new Intent(a,LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));a.finish();}
 public static String text(EditText e){return e.getText()==null?"":e.getText().toString().trim();}
 public static boolean required(EditText e){if(text(e).isEmpty()){e.setError("Required");e.requestFocus();return false;}return true;}
 public static String error(Exception e){return e instanceof IllegalArgumentException?e.getMessage():"Operation failed. Nothing was confirmed saved. Please retry.";}
}
