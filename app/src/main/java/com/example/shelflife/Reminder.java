package com.example.shelflife;

import androidx.room.*;
@Entity(tableName="grocery_reminders",foreignKeys=@ForeignKey(entity=Product.class,parentColumns="id",childColumns="batchId"),indices={@Index("batchId"),@Index(value={"batchId","milestone"},unique=true)})
public class Reminder {
 @PrimaryKey(autoGenerate=true) public long id;
 public long batchId,createdDay;
 public int milestone;
}
