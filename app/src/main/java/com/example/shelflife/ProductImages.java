package com.example.shelflife;
import android.view.View;
import android.widget.ImageView;
public final class ProductImages {
 private ProductImages(){}
 public static void show(ImageView view,String path,int fallback,boolean hideEmpty){
  Object token=new Object();view.setTag(token);int padding=fallback==0?0:Math.round(8*view.getResources().getDisplayMetrics().density);view.setPadding(padding,padding,padding,padding);view.setImageResource(fallback);view.setVisibility(hideEmpty?View.GONE:View.VISIBLE);
  if(path==null||path.isEmpty())return;
  Auth.IO.execute(()->{android.graphics.Bitmap bitmap=ProfilePhotos.load(view.getContext().getApplicationContext(),path);view.post(()->{if(view.getTag()!=token)return;if(bitmap!=null){view.setImageBitmap(bitmap);view.setPadding(0,0,0,0);view.setVisibility(View.VISIBLE);}else if(hideEmpty)view.setVisibility(View.GONE);});});
 }
}
