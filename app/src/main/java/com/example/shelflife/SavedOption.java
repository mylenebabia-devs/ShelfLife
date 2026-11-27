package com.example.shelflife;
import androidx.room.*;
import androidx.annotation.NonNull;
@Entity(tableName="saved_options",primaryKeys={"kind","value"})
public class SavedOption { @NonNull public String kind="";@NonNull public String value=""; public SavedOption(){} @Ignore public SavedOption(String k,String v){kind=k;value=v;} }
