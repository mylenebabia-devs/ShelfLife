package com.example.shelflife;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.*;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
@Database(entities={SavedOption.class,CatalogProduct.class,Product.class,Staff.class,StockMovement.class,Reminder.class},version=11,exportSchema=false)
public abstract class AppDatabase extends RoomDatabase {
 public abstract ProductDao productDao();
 private static volatile AppDatabase instance;
 public static AppDatabase get(Context c){if(instance==null)synchronized(AppDatabase.class){if(instance==null)instance=Room.databaseBuilder(c.getApplicationContext(),AppDatabase.class,databaseName(c)).addMigrations(MIGRATION_1_3,MIGRATION_3_4,MIGRATION_4_5,MIGRATION_5_6,MIGRATION_6_7,MIGRATION_7_8,MIGRATION_8_9,MIGRATION_9_10,MIGRATION_10_11).build();}return instance;}
 // The earlier alternative edition used v2 with independent per-account inventories.
 // Keep that file untouched; do not merge those accounts into this single-store model.
 static String databaseName(Context c){
  if(c.getDatabasePath("shelflife_grocery.db").exists())return "shelflife_grocery.db";
  java.io.File legacy=c.getDatabasePath("shelflife.db");
  if(legacy.exists()){
   android.database.sqlite.SQLiteDatabase probe=android.database.sqlite.SQLiteDatabase.openDatabase(legacy.getPath(),null,android.database.sqlite.SQLiteDatabase.OPEN_READONLY);
   try{if(probe.getVersion()==2)return "shelflife_grocery.db";}finally{probe.close();}
  }
  return "shelflife.db";
 }
 public static final Migration MIGRATION_1_3=new Migration(1,3){@Override public void migrate(@NonNull SupportSQLiteDatabase db){
  db.execSQL("CREATE TABLE products_v3 (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT, sku TEXT, batchNo TEXT, category TEXT, quantity REAL NOT NULL, expiryDate INTEGER, barcode TEXT, brand TEXT, unit TEXT, location TEXT, dateType TEXT, received REAL NOT NULL, lowStock REAL NOT NULL, receivedDay INTEGER NOT NULL, archived INTEGER NOT NULL)");
  db.execSQL("INSERT INTO products_v3 SELECT id,name,CASE WHEN sku IS NULL OR TRIM(sku)='' THEN 'LEGACY-'||id ELSE sku END,batchNo,CASE category WHEN 'Dairy' THEN 'Dairy and Eggs' WHEN 'Bakery' THEN 'Bread and Bakery' WHEN 'Pantry' THEN 'Packaged Foods' ELSE category END,quantity,expiryDate,'','','pieces','Verify location','Expiry',quantity,5,0,0 FROM products");
  db.execSQL("DROP TABLE products");
  db.execSQL("ALTER TABLE products_v3 RENAME TO products");
  db.execSQL("CREATE INDEX index_products_sku ON products(sku)");
  db.execSQL("CREATE INDEX index_products_name ON products(name)");
  db.execSQL("CREATE INDEX index_products_batchNo ON products(batchNo)");
  db.execSQL("CREATE TABLE grocery_staff (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT, email TEXT, salt TEXT, passwordHash TEXT)");
  db.execSQL("CREATE UNIQUE INDEX index_grocery_staff_email ON grocery_staff(email)");
  db.execSQL("CREATE TABLE grocery_movements (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, batchId INTEGER NOT NULL, staffId INTEGER NOT NULL, time INTEGER NOT NULL, delta REAL NOT NULL, reason TEXT, note TEXT, staffName TEXT, reversalOf INTEGER, FOREIGN KEY(batchId) REFERENCES products(id) ON UPDATE NO ACTION ON DELETE NO ACTION, FOREIGN KEY(staffId) REFERENCES grocery_staff(id) ON UPDATE NO ACTION ON DELETE NO ACTION)");
  db.execSQL("CREATE INDEX index_grocery_movements_batchId ON grocery_movements(batchId)");
  db.execSQL("CREATE INDEX index_grocery_movements_staffId ON grocery_movements(staffId)");
  db.execSQL("CREATE UNIQUE INDEX index_grocery_movements_reversalOf ON grocery_movements(reversalOf)");
  db.execSQL("CREATE TABLE grocery_reminders (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, batchId INTEGER NOT NULL, createdDay INTEGER NOT NULL, milestone INTEGER NOT NULL, FOREIGN KEY(batchId) REFERENCES products(id) ON UPDATE NO ACTION ON DELETE NO ACTION)");
  db.execSQL("CREATE INDEX index_grocery_reminders_batchId ON grocery_reminders(batchId)");
  db.execSQL("CREATE UNIQUE INDEX index_grocery_reminders_batchId_milestone ON grocery_reminders(batchId,milestone)");
 }};
 public static final Migration MIGRATION_3_4=new Migration(3,4){@Override public void migrate(@NonNull SupportSQLiteDatabase db){
  db.execSQL("ALTER TABLE grocery_staff ADD COLUMN phone TEXT");
  db.execSQL("ALTER TABLE grocery_staff ADD COLUMN photoPath TEXT");
 }};
 public static final Migration MIGRATION_4_5=new Migration(4,5){@Override public void migrate(@NonNull SupportSQLiteDatabase db){
  db.execSQL("ALTER TABLE products ADD COLUMN deleted INTEGER NOT NULL DEFAULT 0");
 }};
public static final Migration MIGRATION_5_6=new Migration(5,6){@Override public void migrate(@NonNull SupportSQLiteDatabase db){db.execSQL("ALTER TABLE products ADD COLUMN photoPath TEXT");}};
public static final Migration MIGRATION_6_7=new Migration(6,7){@Override public void migrate(@NonNull SupportSQLiteDatabase db){db.execSQL("ALTER TABLE products ADD COLUMN description TEXT");db.execSQL("ALTER TABLE products ADD COLUMN packSize TEXT");}};
public static final Migration MIGRATION_7_8=new Migration(7,8){@Override public void migrate(@NonNull SupportSQLiteDatabase db){
db.execSQL("ALTER TABLE products ADD COLUMN variation TEXT");
db.execSQL("CREATE TABLE product_catalog (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, sku TEXT NOT NULL, name TEXT, barcode TEXT, brand TEXT, category TEXT, unit TEXT, description TEXT, packSize TEXT, variation TEXT, photoPath TEXT, lowStock REAL NOT NULL, archived INTEGER NOT NULL, deleted INTEGER NOT NULL)");
db.execSQL("CREATE UNIQUE INDEX index_product_catalog_sku ON product_catalog(sku)");
db.execSQL("INSERT INTO product_catalog(sku,name,barcode,brand,category,unit,description,packSize,variation,photoPath,lowStock,archived,deleted) SELECT sku,name,barcode,brand,category,unit,description,packSize,variation,photoPath,lowStock,archived,deleted FROM products WHERE id IN (SELECT MIN(id) FROM products GROUP BY sku)");
db.execSQL("ALTER TABLE products ADD COLUMN productId INTEGER REFERENCES product_catalog(id) ON UPDATE NO ACTION ON DELETE NO ACTION");
db.execSQL("CREATE INDEX index_products_productId ON products(productId)");
db.execSQL("UPDATE products SET productId=(SELECT id FROM product_catalog WHERE product_catalog.sku=products.sku)");
db.execSQL("ALTER TABLE grocery_movements ADD COLUMN unit TEXT");
db.execSQL("UPDATE grocery_movements SET unit=(SELECT unit FROM products WHERE products.id=grocery_movements.batchId)");
}};
public static final Migration MIGRATION_8_9=new Migration(8,9){@Override public void migrate(@NonNull SupportSQLiteDatabase db){db.execSQL("ALTER TABLE products ADD COLUMN measurementValue TEXT");db.execSQL("ALTER TABLE products ADD COLUMN measurementUnit TEXT");db.execSQL("ALTER TABLE product_catalog ADD COLUMN measurementValue TEXT");db.execSQL("ALTER TABLE product_catalog ADD COLUMN measurementUnit TEXT");}};
public static final Migration MIGRATION_9_10=new Migration(9,10){@Override public void migrate(@NonNull SupportSQLiteDatabase db){db.execSQL("ALTER TABLE products ADD COLUMN stockReference TEXT COLLATE NOCASE");db.execSQL("UPDATE products SET stockReference='B'||id");db.execSQL("CREATE UNIQUE INDEX index_products_productId_stockReference ON products(productId,stockReference)");db.execSQL("CREATE TABLE saved_options (kind TEXT NOT NULL, value TEXT NOT NULL, PRIMARY KEY(kind,value))");db.execSQL("INSERT OR IGNORE INTO saved_options SELECT 'category',category FROM product_catalog WHERE category IS NOT NULL AND TRIM(category)<>''");db.execSQL("INSERT OR IGNORE INTO saved_options SELECT 'unit',measurementUnit FROM product_catalog WHERE measurementUnit IS NOT NULL AND TRIM(measurementUnit)<>''");}};

public static final Migration MIGRATION_10_11=new Migration(10,11){@Override public void migrate(@NonNull SupportSQLiteDatabase db){
db.execSQL("DELETE FROM grocery_reminders WHERE batchId IN (SELECT id FROM products WHERE productId IN (SELECT id FROM product_catalog WHERE lower(trim(replace(replace(replace(replace(category,'  ',' '),'  ',' '),'  ',' '),'  ',' '))) IN ('toiletries','household products')) OR (productId IS NULL AND lower(trim(replace(replace(replace(replace(category,'  ',' '),'  ',' '),'  ',' '),'  ',' '))) IN ('toiletries','household products')))");
db.execSQL("DELETE FROM grocery_movements WHERE batchId IN (SELECT id FROM products WHERE productId IN (SELECT id FROM product_catalog WHERE lower(trim(replace(replace(replace(replace(category,'  ',' '),'  ',' '),'  ',' '),'  ',' '))) IN ('toiletries','household products')) OR (productId IS NULL AND lower(trim(replace(replace(replace(replace(category,'  ',' '),'  ',' '),'  ',' '),'  ',' '))) IN ('toiletries','household products')))");
db.execSQL("DELETE FROM products WHERE id IN (SELECT id FROM products WHERE productId IN (SELECT id FROM product_catalog WHERE lower(trim(replace(replace(replace(replace(category,'  ',' '),'  ',' '),'  ',' '),'  ',' '))) IN ('toiletries','household products')) OR (productId IS NULL AND lower(trim(replace(replace(replace(replace(category,'  ',' '),'  ',' '),'  ',' '),'  ',' '))) IN ('toiletries','household products')))");
db.execSQL("DELETE FROM product_catalog WHERE lower(trim(replace(replace(replace(replace(category,'  ',' '),'  ',' '),'  ',' '),'  ',' '))) IN ('toiletries','household products')");
db.execSQL("DELETE FROM saved_options WHERE kind='category' AND lower(trim(replace(replace(replace(replace(value,'  ',' '),'  ',' '),'  ',' '),'  ',' '))) IN ('toiletries','household products','grains and cereals','flakes','others')");
db.execSQL("DELETE FROM saved_options WHERE rowid NOT IN (SELECT min(rowid) FROM saved_options GROUP BY kind,lower(trim(replace(replace(replace(replace(value,'  ',' '),'  ',' '),'  ',' '),'  ',' '))))");
}};
}
