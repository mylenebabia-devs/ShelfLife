package com.example.shelflife;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.*;
import androidx.core.content.ContextCompat;
import androidx.work.*;
import java.time.LocalDate;
import java.util.*;
/** Best-effort reminders with persistent per-batch milestone deduplication. */
public class ExpiryWorker extends Worker {
 private static final String CHANNEL="grocery_expiry";
 public ExpiryWorker(@NonNull Context c,@NonNull WorkerParameters p){super(c,p);}
 @NonNull @Override public Result doWork(){try{check(getApplicationContext());return Result.success();}catch(Exception e){return Result.retry();}}
 public static void check(Context c){if(Auth.session(c)==0)return;AppDatabase db=AppDatabase.get(c);ProductDao dao=db.productDao();if(dao.staff(Auth.session(c))==null)return;List<Product> notifications=new ArrayList<>();
  boolean allowed=(Build.VERSION.SDK_INT<33||ContextCompat.checkSelfPermission(c,Manifest.permission.POST_NOTIFICATIONS)==PackageManager.PERMISSION_GRANTED)&&NotificationManagerCompat.from(c).areNotificationsEnabled();
  NotificationManager manager=c.getSystemService(NotificationManager.class);manager.createNotificationChannel(new NotificationChannel(CHANNEL,"Grocery expiry reminders",NotificationManager.IMPORTANCE_DEFAULT));
  for(Product p:dao.getAll()){if(p.archived||p.quantity<=0){for(int milestone:new int[]{0,1,3,7})manager.cancel((int)(p.id*10+milestone));continue;}int milestone=Rules.milestone(p.expiryDate,LocalDate.now());if(milestone<0||"Unknown".equals(p.dateType)||!allowed)continue;
   boolean claimed=db.runInTransaction(()->{Product latest=dao.get(p.id);if(latest==null||latest.archived||latest.quantity<=0)return false;Reminder r=new Reminder();r.batchId=p.id;r.createdDay=LocalDate.now().toEpochDay();r.milestone=milestone;return dao.insert(r)!=-1;});if(claimed)notifications.add(p);
  }
  for(Product p:notifications){Product latest=dao.get(p.id);if(latest==null||latest.archived||latest.quantity<=0)continue;int milestone=Rules.milestone(p.expiryDate,LocalDate.now()),id=(int)(p.id*10+milestone);Intent open=new Intent(c,MainActivity.class).putExtra("batch_id",p.id).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);PendingIntent pending=PendingIntent.getActivity(c,id,open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
   Notification notification=new NotificationCompat.Builder(c,CHANNEL).setSmallIcon(R.drawable.ic_inventory).setContentTitle(p.name+" · Batch "+p.stockReference).setContentText(p.dateType+": "+Expiry.date(p)+" · "+Expiry.label(p)).setContentIntent(pending).setAutoCancel(true).build();try{manager.notify(id,notification);}catch(SecurityException ignored){/* Permission changed while the worker was running. */}
  }
 }
}
