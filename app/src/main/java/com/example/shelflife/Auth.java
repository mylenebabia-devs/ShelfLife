package com.example.shelflife;

import android.content.Context;
import java.util.concurrent.*;
public final class Auth {
 public static final ExecutorService IO=Executors.newSingleThreadExecutor();
 private Auth(){}
 public static long session(Context c){return c.getSharedPreferences("grocery_session",0).getLong("staff",0);}
 public static void remember(Context c,long id){if(!c.getSharedPreferences("grocery_session",0).edit().putLong("staff",id).commit())throw new IllegalStateException("Could not save session");}
 public static void logout(Context c){c.getSharedPreferences("grocery_session",0).edit().clear().commit();}
 public static Staff login(ProductDao dao,String email,String password){Staff a=dao.staff(Rules.normalizeEmail(email));if(a==null||!Passwords.verify(password,a.salt,a.passwordHash))throw new IllegalArgumentException("Incorrect email or password");return a;}
 public static Staff register(AppDatabase db,String name,String email,String pass,String approvingEmail,String approvingPass){
  if(name.trim().isEmpty())throw new IllegalArgumentException("Staff name is required");if(!Rules.email(email))throw new IllegalArgumentException("Enter a valid email");if(pass.trim().isEmpty()||pass.length()<8)throw new IllegalArgumentException("Password needs at least 8 characters");
  String normalized=Rules.normalizeEmail(email),salt=Passwords.salt(),hash=Passwords.hash(pass,salt);
  return db.runInTransaction(()->{ProductDao dao=db.productDao();if(dao.staffCount()>0)login(dao,approvingEmail,approvingPass);if(dao.staff(normalized)!=null)throw new IllegalArgumentException("Email is already registered");Staff a=new Staff();a.name=name.trim();a.email=normalized;a.salt=salt;a.passwordHash=hash;a.id=dao.insert(a);return a;});
 }
}
