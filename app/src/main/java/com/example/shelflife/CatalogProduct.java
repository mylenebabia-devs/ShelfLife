package com.example.shelflife;
import androidx.room.*;
import androidx.annotation.NonNull;
/** One identity per product. The existing products table holds its stock batches. */
@Entity(tableName="product_catalog",indices={@Index(value="sku",unique=true)})
public class CatalogProduct {
 @PrimaryKey(autoGenerate=true) public long id;
 @NonNull public String sku="";
 public String name,barcode,brand,category,unit,description,packSize,variation,photoPath;
 public String measurementValue,measurementUnit;
 public double lowStock;
 public boolean archived,deleted;
 static CatalogProduct from(Product p){CatalogProduct c=new CatalogProduct();c.id=p.productId==null?0:p.productId;c.sku=p.sku;c.measurementValue=p.measurementValue;c.measurementUnit=p.measurementUnit;c.name=p.name;c.barcode=p.barcode;c.brand=p.brand;c.category=p.category;c.unit=p.unit;c.description=p.description;c.packSize=p.packSize;c.variation=p.variation;c.photoPath=p.photoPath;c.lowStock=p.lowStock;c.archived=p.archived;c.deleted=p.deleted;return c;}
}
