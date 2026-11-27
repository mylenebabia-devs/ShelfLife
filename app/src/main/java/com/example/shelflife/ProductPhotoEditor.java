package com.example.shelflife;
import android.net.Uri;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
/** Stages an optional gallery image; only Save commits a private photo copy. */
final class ProductPhotoEditor {
 private final MainActivity activity;private final AlertDialog dialog;private final ImageView preview;private final Button choose,remove;private final String original;
 private String draft;private boolean cleared,closed;volatile boolean saved;
 ProductPhotoEditor(MainActivity a,View root,AlertDialog d,String existing,Runnable picker){activity=a;dialog=d;original=existing;preview=root.findViewById(R.id.photoPreview);choose=root.findViewById(R.id.chooseProductPhoto);remove=root.findViewById(R.id.removeProductPhoto);ProductImages.show(preview,existing,0,true);
 choose.setOnClickListener(v->picker.run());remove.setOnClickListener(v->{ProfilePhotos.delete(a,draft);draft=null;cleared=true;ProductImages.show(preview,null,0,true);});dialog.setOnDismissListener(v->{closed=true;ProfilePhotos.delete(a,draft);});}
 void selected(Uri uri){if(closed)return;choose.setEnabled(false);remove.setEnabled(false);dialog.getButton(-1).setEnabled(false);choose.setText("Preparing photo…");
 Auth.IO.execute(()->{try{String next=ProfilePhotos.importDraft(activity.getApplicationContext(),uri);activity.runOnUiThread(()->{if(closed||activity.isDestroyed()){ProfilePhotos.delete(activity,next);return;}ProfilePhotos.delete(activity,draft);draft=next;cleared=false;ProductImages.show(preview,next,0,true);ready();});}catch(Exception e){activity.runOnUiThread(()->{if(closed||activity.isDestroyed())return;ready();Toast.makeText(activity,"Could not open photo. Choose a supported image under 15 MB.",Toast.LENGTH_LONG).show();});}});
 }
 void lock(){choose.setEnabled(false);remove.setEnabled(false);}
 void unlock(){activity.runOnUiThread(()->{if(!closed)ready();});}
 private void ready(){choose.setEnabled(true);remove.setEnabled(true);dialog.getButton(-1).setEnabled(true);choose.setText("Choose photo (optional)");}
 String commit()throws Exception{return cleared?null:draft==null?original:ProfilePhotos.commitDraft(activity.getApplicationContext(),draft);}
 void rollback(String path){if(path!=null&&!path.equals(original))ProfilePhotos.delete(activity.getApplicationContext(),path);}
}
