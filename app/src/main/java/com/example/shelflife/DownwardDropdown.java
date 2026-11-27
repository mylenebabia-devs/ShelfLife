package com.example.shelflife;

import android.graphics.Rect;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.*;
import com.google.android.material.textfield.*;

/** Explicit positioning avoids the platform popup's automatic upward flip. */
final class DownwardDropdown {
 private final MaterialAutoCompleteTextView field;
 private PopupWindow popup;
 DownwardDropdown(MaterialAutoCompleteTextView field){
  this.field=field;
  ViewParent parent=field.getParent();
  while(parent instanceof View && !(parent instanceof TextInputLayout))parent=parent.getParent();
  if(parent instanceof TextInputLayout){TextInputLayout box=(TextInputLayout)parent;android.graphics.drawable.Drawable arrow=box.getEndIconDrawable();box.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);box.setEndIconDrawable(arrow);box.setEndIconContentDescription("Choose category");box.setEndIconOnClickListener(v->open());}
  field.setOnClickListener(v->open());
  field.setOnKeyListener((v,key,event)->{if(event.getAction()==KeyEvent.ACTION_UP&&(key==KeyEvent.KEYCODE_DPAD_CENTER||key==KeyEvent.KEYCODE_ENTER||key==KeyEvent.KEYCODE_SPACE)){open();return true;}return false;});
  field.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener(){public void onViewAttachedToWindow(View v){}public void onViewDetachedFromWindow(View v){dismiss();}});
 }
 private int dp(int n){return Math.round(n*field.getResources().getDisplayMetrics().density);}
 private void dismiss(){if(popup!=null){popup.dismiss();popup=null;}}
 private void open(){
  if(!field.isEnabled())return;if(popup!=null&&popup.isShowing()){dismiss();return;}
  field.dismissDropDown();((InputMethodManager)field.getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(field.getWindowToken(),0);
  // Request room BELOW the anchor in its enclosing form scroll view.
  field.requestRectangleOnScreen(new Rect(0,0,field.getWidth(),field.getHeight()+dp(220)),true);
  field.post(()->show());
 }
 private void show(){
  if(!field.isAttachedToWindow()||field.getAdapter()==null)return;
  Rect frame=new Rect();field.getWindowVisibleDisplayFrame(frame);int[] anchor=new int[2];field.getLocationOnScreen(anchor);int below=anchor[1]+field.getHeight();int height=Math.min(dp(320),frame.bottom-below-dp(8));
  if(height<dp(48))return; // Never overlay the anchor or flip above it.
  ListView list=new ListView(field.getContext());list.setAdapter(field.getAdapter());list.setDividerHeight(0);list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);list.setContentDescription("Product categories");
  GradientDrawable bg=new GradientDrawable();bg.setColor(Color.rgb(248,249,243));bg.setCornerRadius(dp(8));bg.setStroke(dp(1),Color.rgb(214,224,213));
  popup=new PopupWindow(list,field.getWidth(),height,true);popup.setBackgroundDrawable(bg);popup.setElevation(dp(8));popup.setOutsideTouchable(true);popup.setInputMethodMode(PopupWindow.INPUT_METHOD_NOT_NEEDED);
  list.setOnItemClickListener((parent,row,position,id)->{String selected=String.valueOf(parent.getItemAtPosition(position));field.setText(selected,false);if(field.getOnItemClickListener()!=null)field.getOnItemClickListener().onItemClick(parent,row,position,id);dismiss();field.requestFocus();});
  // showAtLocation uses window coordinates; compensate for dialog window origin.
  int[] screen=new int[2],window=new int[2];field.getRootView().getLocationOnScreen(screen);field.getRootView().getLocationInWindow(window);
  popup.showAtLocation(field,Gravity.TOP|Gravity.LEFT,anchor[0]-(screen[0]-window[0]),below-(screen[1]-window[1]));
 }
}
