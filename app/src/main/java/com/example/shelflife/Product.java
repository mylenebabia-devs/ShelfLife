package com.example.shelflife;

import androidx.room.*;
import java.time.LocalDate;
/** Existing model retained: each row is a stock batch; SKU identifies its grocery product. */
@Entity(tableName="products",foreignKeys={@ForeignKey(entity=CatalogProduct.class,parentColumns="id",childColumns="productId")},indices={@Index("productId"),@Index(value={"productId","stockReference"},unique=true),@Index("sku"),@Index("name"),@Index("batchNo")})
public class Product {
 @PrimaryKey(autoGenerate=true) public long id;
 public String name,sku,batchNo,category;
 public String measurementValue,measurementUnit;
 public double quantity;
 public Long expiryDate;
 public String barcode,brand,unit,location,dateType;
 public double received,lowStock;
 public long receivedDay;
 public String photoPath,description,packSize,variation;
 @ColumnInfo(collate=ColumnInfo.NOCASE) public String stockReference;
 public Long productId;
 public boolean archived;
 @ColumnInfo(defaultValue="0") public boolean deleted;
 public Product(){}
 @Ignore public Product(String name,String sku,String batch,String category,double qty,Long expiry){this.name=name;this.sku=sku;batchNo=batch;this.category=category;quantity=qty;expiryDate=expiry;barcode="";brand="";unit="pieces";location="Shelf A";dateType=expiry==null?"Unknown":"Expiry";received=qty;lowStock=5;receivedDay=LocalDate.now().toEpochDay();}
 public LocalDate expiry(){return expiryDate==null?null:LocalDate.ofEpochDay(expiryDate);}
 @Ignore public Product copy(){Product p=new Product(name,sku,batchNo,category,quantity,expiryDate);p.id=id;p.stockReference=stockReference;p.measurementValue=measurementValue;p.measurementUnit=measurementUnit;p.productId=productId;p.variation=variation;p.photoPath=photoPath;p.description=description;p.packSize=packSize;p.barcode=barcode;p.brand=brand;p.unit=unit;p.location=location;p.dateType=dateType;p.received=received;p.lowStock=lowStock;p.receivedDay=receivedDay;p.archived=archived;p.deleted=deleted;return p;}
}
