package com.example.shelflife;
import android.content.Context;
public final class ProfileRepository {
 private ProfileRepository(){}
 public static Staff update(Context c,long id,String name,String email,String phone,String password,String draft,boolean remove)throws Exception{
  if(Auth.session(c)!=id||id==0)throw new IllegalArgumentException("Sign in again");
  if(name==null||name.trim().isEmpty())throw new IllegalArgumentException("Name is required");
  String normalized=Rules.normalizeEmail(email);if(!Rules.email(normalized))throw new IllegalArgumentException("Enter a valid email");
  if(phone!=null&&phone.trim().length()>40)throw new IllegalArgumentException("Phone number is too long");
  AppDatabase db=AppDatabase.get(c);String newPath=remove||draft==null?null:ProfilePhotos.commitDraft(c,draft);String[] previous={null};
  try{Staff saved=db.runInTransaction(()->{Staff staff=db.productDao().staff(id);if(staff==null)throw new IllegalArgumentException("Account unavailable");
   Staff duplicate=db.productDao().staff(normalized);if(duplicate!=null&&duplicate.id!=id)throw new IllegalArgumentException("Another account uses this email");
   if(!normalized.equals(staff.email)&&!Passwords.verify(password,staff.salt,staff.passwordHash))throw new IllegalArgumentException("Enter your current password to change email");
   previous[0]=staff.photoPath;staff.name=name.trim();staff.email=normalized;staff.phone=phone==null?"":phone.trim();if(remove)staff.photoPath=null;else if(newPath!=null)staff.photoPath=newPath;db.productDao().update(staff);return staff;
  });if((remove||newPath!=null)&&previous[0]!=null&&!previous[0].equals(saved.photoPath))ProfilePhotos.delete(c,previous[0]);return saved;
  }catch(Exception e){if(newPath!=null)ProfilePhotos.delete(c,newPath);throw e;}
 }
}
