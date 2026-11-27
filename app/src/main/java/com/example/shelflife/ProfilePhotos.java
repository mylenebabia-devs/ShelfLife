package com.example.shelflife;
import android.content.Context;
import android.graphics.*;
import android.media.ExifInterface;
import android.net.Uri;
import java.io.*;
import java.util.UUID;
/** Photos are copied into private storage; no dependence on a temporary gallery URI. */
public final class ProfilePhotos {
 private ProfilePhotos(){}
 public static String importDraft(Context c,Uri uri)throws IOException{
  File dir=new File(c.getCacheDir(),"profile_drafts");if(!dir.isDirectory()&&!dir.mkdirs())throw new IOException("Cannot prepare image storage");
  File raw=File.createTempFile("selected-",".image",dir);Bitmap bitmap=null;
  try{
   try(InputStream in=c.getContentResolver().openInputStream(uri);OutputStream out=new FileOutputStream(raw)){if(in==null)throw new IOException("Cannot open selected photo");byte[] buffer=new byte[8192];int n;long total=0;while((n=in.read(buffer))!=-1){total+=n;if(total>15*1024*1024)throw new IOException("Choose a photo smaller than 15 MB");out.write(buffer,0,n);}}
   BitmapFactory.Options options=new BitmapFactory.Options();options.inJustDecodeBounds=true;BitmapFactory.decodeFile(raw.getPath(),options);
   if(options.outWidth<=0||options.outHeight<=0||(long)options.outWidth*options.outHeight>60000000)throw new IOException("Choose a supported photo under 60 megapixels");
   options.inJustDecodeBounds=false;options.inSampleSize=1;while(Math.max(options.outWidth,options.outHeight)/options.inSampleSize>768)options.inSampleSize*=2;
   bitmap=BitmapFactory.decodeFile(raw.getPath(),options);if(bitmap==null)throw new IOException("Photo cannot be decoded");
   int orientation=ExifInterface.ORIENTATION_NORMAL;try{orientation=new ExifInterface(raw.getPath()).getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_NORMAL);}catch(IOException ignored){}
   Matrix matrix=new Matrix();switch(orientation){case 2:matrix.setScale(-1,1);break;case 3:matrix.setRotate(180);break;case 4:matrix.setScale(1,-1);break;case 5:matrix.setRotate(90);matrix.postScale(-1,1);break;case 6:matrix.setRotate(90);break;case 7:matrix.setRotate(270);matrix.postScale(-1,1);break;case 8:matrix.setRotate(270);break;}
   if(!matrix.isIdentity()){Bitmap transformed=Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);if(transformed!=bitmap){bitmap.recycle();bitmap=transformed;}}
   File draft=new File(dir,UUID.randomUUID()+".jpg");try(OutputStream out=new FileOutputStream(draft)){if(!bitmap.compress(Bitmap.CompressFormat.JPEG,88,out))throw new IOException("Cannot save photo");}return draft.getPath();
  }finally{raw.delete();if(bitmap!=null)bitmap.recycle();}
 }
 public static String commitDraft(Context c,String draft)throws IOException{
  File source=new File(draft);File allowed=new File(c.getCacheDir(),"profile_drafts");if(!source.getCanonicalPath().startsWith(allowed.getCanonicalPath()+File.separator)||!source.isFile())throw new IOException("Selected photo expired. Choose it again.");
  File dir=new File(c.getFilesDir(),"profile_photos");if(!dir.isDirectory()&&!dir.mkdirs())throw new IOException("Cannot save profile photo");File target=new File(dir,UUID.randomUUID()+".jpg");
  try(InputStream in=new FileInputStream(source);OutputStream out=new FileOutputStream(target)){byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1)out.write(b,0,n);}catch(IOException e){target.delete();throw e;}return target.getPath();
 }
 public static Bitmap load(Context c,String path){try{if(path==null)return null;File f=new File(path);String base=c.getFilesDir().getCanonicalPath()+File.separator;String cache=new File(c.getCacheDir(),"profile_drafts").getCanonicalPath()+File.separator;String canonical=f.getCanonicalPath();if(!canonical.startsWith(base)&&!canonical.startsWith(cache))return null;return BitmapFactory.decodeFile(canonical);}catch(IOException e){return null;}}
 public static void delete(Context c,String path){try{if(path==null)return;File f=new File(path);String canonical=f.getCanonicalPath();if(canonical.startsWith(new File(c.getFilesDir(),"profile_photos").getCanonicalPath()+File.separator)||canonical.startsWith(new File(c.getCacheDir(),"profile_drafts").getCanonicalPath()+File.separator))f.delete();}catch(IOException ignored){}}
}
