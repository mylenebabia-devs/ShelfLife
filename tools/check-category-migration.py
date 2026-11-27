"""Exercise the actual v10->11 SQL against related/excluded and unrelated records."""
import sqlite3,re,pathlib
root=pathlib.Path(__file__).resolve().parents[1]
s=(root/'app/src/main/java/com/example/shelflife/AppDatabase.java').read_text()
prior,latest=s.split('public static final Migration MIGRATION_10_11=')
extract=lambda text:re.findall(r'db\.execSQL\("([^"\n]+)"\);',text)
db=sqlite3.connect(':memory:');db.execute('PRAGMA foreign_keys=ON')
db.execute('CREATE TABLE products(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,name TEXT,sku TEXT,batchNo TEXT,category TEXT,quantity INTEGER NOT NULL,expiryDate INTEGER NOT NULL)')
for sql in extract(prior):db.execute(sql)
db.execute("INSERT INTO grocery_staff(id,name,email) VALUES(1,'Test','test@example.com')")
for n,cat in enumerate([' Toiletries ','Household   Products','Laundry Products','Canned Goods','Personal Toiletries Collection'],1):
 db.execute('INSERT INTO product_catalog(id,sku,name,category,lowStock,archived,deleted) VALUES(?,?,?,?,0,0,0)',(n,str(n),'Product',cat))
 db.execute("INSERT INTO products(id,sku,name,category,quantity,received,lowStock,receivedDay,archived,deleted,productId,stockReference) VALUES(?,?,?,?,5,5,0,0,0,0,?,'B1')",(n,str(n),'Product',cat,n))
 db.execute("INSERT INTO grocery_movements(batchId,staffId,time,delta) VALUES(?,1,0,5)",(n,))
 db.execute("INSERT INTO grocery_reminders(batchId,createdDay,milestone) VALUES(?,0,7)",(n,))
for c in [' Toiletries ','Household   Products','Pet Food',' pet   food ','PET FOOD']:
 db.execute("INSERT INTO saved_options VALUES('category',?)",(c,))
original=db.execute('SELECT * FROM products WHERE id>=3').fetchall()
db.commit()
with db:
 for sql in extract(latest):db.execute(sql)
assert db.execute('SELECT * FROM products').fetchall()==original
assert db.execute('SELECT id FROM product_catalog').fetchall()==[(3,),(4,),(5,)]
assert db.execute('SELECT batchId FROM grocery_movements').fetchall()==[(3,),(4,),(5,)]
assert db.execute('SELECT batchId FROM grocery_reminders').fetchall()==[(3,),(4,),(5,)]
assert db.execute('SELECT value FROM saved_options').fetchall()==[('Pet Food',)]
assert db.execute('PRAGMA foreign_key_check').fetchall()==[]
print('PASSED: targeted category cleanup, linked stock/reminders/history cleanup, unrelated records unchanged, whitespace/case dedup, foreign keys')
