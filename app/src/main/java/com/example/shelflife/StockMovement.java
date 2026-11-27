package com.example.shelflife;

import androidx.room.*;
@Entity(tableName="grocery_movements",foreignKeys={@ForeignKey(entity=Product.class,parentColumns="id",childColumns="batchId"),@ForeignKey(entity=Staff.class,parentColumns="id",childColumns="staffId")},indices={@Index("batchId"),@Index("staffId"),@Index(value="reversalOf",unique=true)})
public class StockMovement {
 @PrimaryKey(autoGenerate=true) public long id;
 public long batchId,staffId,time;
 public double delta;
 public String reason,note,staffName,unit;
 public Long reversalOf;
}
