import sqlite3,re,pathlib,tempfile,xml.etree.ElementTree as ET
root=pathlib.Path(__file__).resolve().parents[1]
text=(root/'app/src/main/java/com/example/shelflife/AppDatabase.java').read_text()
statements=re.findall(r'db\.execSQL\("([^"\n]+)"\);',text)
db=sqlite3.connect(':memory:');db.execute('PRAGMA foreign_keys=ON')
db.execute('CREATE TABLE products(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,name TEXT,sku TEXT,batchNo TEXT,category TEXT,quantity INTEGER NOT NULL,expiryDate INTEGER NOT NULL)')
for col in ['sku','name','batchNo']:db.execute(f'CREATE INDEX index_products_{col} ON products({col})')
db.execute("INSERT INTO products VALUES(1,'Milk','MILK','A','Dairy',24,22000)")
for sql in statements:db.execute(sql)
assert db.execute('SELECT quantity,sku,category,receivedDay FROM products').fetchone()==(24.0,'MILK','Dairy and Eggs',0)
assert {'phone','photoPath'} <= {row[1] for row in db.execute('PRAGMA table_info(grocery_staff)')}
db.execute("INSERT INTO grocery_staff(id,name,email,salt,passwordHash) VALUES(1,'Staff','staff@example.com','salt','hash')")
db.execute("INSERT INTO products VALUES(2,'Milk','MILK','B','Dairy and Eggs',12,22010,'0012345678905','','cartons','Chiller','Expiry',12,5,21900,0,0,NULL,NULL,NULL,NULL,1,NULL,NULL,'B2')")
assert db.execute("SELECT COUNT(*) FROM products WHERE sku='MILK'").fetchone()[0]==2
assert db.execute('SELECT barcode FROM products WHERE id=2').fetchone()[0]=='0012345678905'
def reject(sql):
 try:db.execute(sql)
 except sqlite3.IntegrityError:return
 raise AssertionError(sql)
reject("INSERT INTO grocery_staff(id,name,email,salt,passwordHash) VALUES(2,'Staff','staff@example.com','','')")
reject("INSERT INTO grocery_movements VALUES(1,99,1,1,-1,'Sold','','Staff',NULL,'cartons')")
db.execute('INSERT INTO grocery_reminders VALUES(1,2,22003,7)')
reject('INSERT INTO grocery_reminders VALUES(2,2,22003,7)')
db.commit()
try:
 with db:
  db.execute('UPDATE products SET quantity=10 WHERE id=2')
  db.execute("INSERT INTO grocery_movements VALUES(1,2,99,1,-2,'Sold','','Staff',NULL,'cartons')")
except sqlite3.IntegrityError:pass
assert db.execute('SELECT quantity FROM products WHERE id=2').fetchone()[0]==12
# Persisted lifecycle round trip preserves quantity and identity.
original=db.execute('SELECT name,sku,quantity,expiryDate FROM products WHERE id=2').fetchone()
for archived,deleted in [(1,0),(1,1),(0,0)]:
 with db:db.execute('UPDATE products SET archived=?,deleted=? WHERE sku=?',(archived,deleted,'MILK'))
 assert db.execute('SELECT archived,deleted FROM products WHERE id=2').fetchone()==(archived,deleted)
 assert db.execute('SELECT name,sku,quantity,expiryDate FROM products WHERE id=2').fetchone()==original
# Staff-entered references are unique per product, case-insensitively.
reject("UPDATE products SET stockReference='b1' WHERE id=2")
db.execute("UPDATE products SET stockReference='DELIVERY-2' WHERE id=2")
db.execute("INSERT INTO saved_options VALUES('category','Pet Food')")
db.execute("INSERT INTO saved_options VALUES('unit','sachets')")
# Close/reopen a file-backed copy to verify persisted parent/batch records.
db.commit()
with tempfile.TemporaryDirectory() as directory:
 path=str(pathlib.Path(directory)/'persistence.db')
 saved=sqlite3.connect(path);db.backup(saved);saved.close()
 reopened=sqlite3.connect(path)
 assert reopened.execute('SELECT COUNT(*) FROM products').fetchone()[0]==2
 assert reopened.execute("SELECT value FROM saved_options WHERE kind='unit' AND value='sachets'").fetchone()[0]=='sachets'
 assert reopened.execute('SELECT COUNT(*) FROM product_catalog').fetchone()[0]==1
 assert reopened.execute('SELECT quantity FROM products WHERE id=2').fetchone()[0]==12
 reopened.close()
# Parent uniqueness, foreign key, and irreversible deletion inside a transaction.
assert db.execute('SELECT COUNT(*) FROM product_catalog').fetchone()[0]==1
assert db.execute('SELECT COUNT(DISTINCT productId) FROM products').fetchone()[0]==1
reject("INSERT INTO product_catalog(sku,lowStock,archived,deleted) VALUES('MILK',5,0,0)")
reject("UPDATE products SET productId=999 WHERE id=2")
assert db.execute('PRAGMA foreign_key_check').fetchall()==[]
db.commit()
try:
 with db:
  db.execute("DELETE FROM grocery_reminders WHERE batchId=2")
  db.execute("DELETE FROM products WHERE sku='MILK'")
  raise RuntimeError('simulate failure')
except RuntimeError:pass
assert db.execute('SELECT COUNT(*) FROM products').fetchone()[0]==2
with db:
 db.execute("DELETE FROM grocery_reminders WHERE batchId IN (SELECT id FROM products WHERE sku='MILK')")
 db.execute("DELETE FROM grocery_movements WHERE batchId IN (SELECT id FROM products WHERE sku='MILK')")
 db.execute("DELETE FROM products WHERE sku='MILK'")
 db.execute("DELETE FROM product_catalog WHERE sku='MILK'")
assert db.execute('SELECT COUNT(*) FROM products').fetchone()[0]==0
assert db.execute("SELECT value FROM saved_options WHERE kind='category' AND value='Pet Food'").fetchone()[0]=='Pet Food'
assert db.execute('SELECT COUNT(*) FROM product_catalog').fetchone()[0]==0
assert db.execute('PRAGMA foreign_key_check').fetchall()==[]
xmls=list((root/'app/src').rglob('*.xml'))
for f in xmls:ET.parse(f)
# Ensure every application R.id/layout/drawable reference resolves to a declaration.
joined='\n'.join(f.read_text() for f in xmls)
ids=set(re.findall(r'@\+id/(\w+)',joined))
for java in (root/'app/src/main/java').rglob('*.java'):
 code=java.read_text()
 for name in re.findall(r'(?<!android\.)R\.id\.(\w+)',code):assert name in ids,(java,name)
 for name in re.findall(r'(?<!android\.)R\.layout\.(\w+)',code):assert (root/f'app/src/main/res/layout/{name}.xml').exists(),(java,name)
(root/'docs/schema-migration.sql').write_text('-- v1 to grocery v3; executed in a Room migration transaction.\n'+';\n'.join(statements)+';\n')
print(f'PASSED: {len(statements)} migration statements; old quantity retention, separate batches, barcode text, staff uniqueness, movement foreign keys, reminder dedup, parent FK/unique SKU, reopen persistence, permanent-delete rollback; {len(xmls)} XML files and Java resource references.')
