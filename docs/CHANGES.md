# ShelfLife 1.8 — SQLite product identities and batches

## Open and update

Extract the ZIP and open the folder containing settings.gradle.kts. Version code 10; database version 8. Update using the same signing key and do not clear app data. Back up valuable records before testing a migration. This is a local SQLite/Room Android app.

## Requested changes

- Profile no longer displays storage/cloud/timezone notes. Settings no longer offers demo groceries. Notifications, sorting, reports, account editing and barcode lookup remain available.
- product_catalog stores one product identity with a UNIQUE internal SKU. Each stock batch in the legacy products table links to its parent by productId foreign key. Existing batch IDs and movement references are preserved. The old table name is retained for compatibility; its rows are stock batches, not independent product identities. Existing metadata columns remain synchronized batch snapshots for the existing UI/report queries. Repository writes update parent and batch snapshots in one transaction.
- Migration creates one catalog record per existing SKU without changing existing SKUs, quantities, dates, photos or batch IDs. Stock movements now retain their recorded quantity unit.
- New registration automatically generates CATEGORY-BRAND-SIZE-VARIATION. Examples: SAR-MEG-155G-TS, SAR-LIG-155G-CH, TUN-CEN-180G-FL, BEV-COC-330ML-OR. Units are included in size to distinguish grams from millilitres. Missing optional size/variation use STD and missing brand uses GEN. Other category/brand/variation names use consistent three-character codes. Different products that generate the same code receive -2, -3, etc. Allocation happens inside a database transaction with a UNIQUE index. Exact matching identity fields and duplicate barcodes direct staff to the existing product rather than create another one.
- The SKU preview updates with new registration information; the final SKU is allocated at Save. Existing SKU stays read-only and unchanged after edits, even if brand, size or flavor changes.
- Existing-barcode scans open the saved product's Details: Edit product or Add stock. New deliveries reuse the same catalog ID/SKU and get a unique batch ID B<number>. Supplier lot is a separate editable field and can repeat across deliveries.
- Add/Edit show the same product and stock fields. Product identity, photo, unit and low-stock threshold changes apply to all batches; quantity, supplier lot, date type, expiry date, received date and location edits affect only the selected batch. Product size/weight uses Pack size; Variation/flavor has its own field.
- Quantity edits append a Quantity correction movement. Corrections are not reversed through the old reversal menu; edit the batch again to record a further correction. Unit edits relabel quantities, not convert them; prior movements keep their original unit. Reversing a movement in a different unit is rejected until the product unit matches.
- Archive and Delete preserve all batches and history. Restore returns all associated batches. Inventory → Deleted → Details → Delete permanently removes the catalog record, batches, movements, reminder rows and unshared referenced photo files. The confirmation explains that recovery/history are lost. Current notifications for removed batches are cancelled. Permanent delete is rejected for active or merely archived products.
- Images remain private on-device files referenced by SQLite; they are not stored as BLOBs. Barcode catalog retrieval still needs internet; manual entry remains available offline.

## Verification — 1 November 2026

PASSED: 47 existing executable domain/auth checks and 8 new production SKU checks.
PASSED: 31 actual migration SQL statements in SQLite, parent SKU uniqueness, product/batch FK, existing quantity retention, file-backed close/reopen persistence, deletion rollback, complete product/batch cleanup, and foreign-key checks.
PASSED: 61 XML files/resource references and 37 Java source syntax parses. These are not Android type checks.
BLOCKED: Gradle testDebugUnitTest/assembleDebug failed before tasks ran because downloading Gradle returned Network is unreachable. No APK build or live phone verification is claimed.
NOT RUN: Room instrumentation tests (included), Android migration validation, UI layout, camera, notifications and photo-file deletion on a device. Added instrumentation covers generated SKU collision, same-parent multiple batches, in-place batch edits and permanent-delete guards/cleanup.

## Phone acceptance checklist

1. Upgrade a test copy of the prior database; confirm existing account, photos, SKUs, batch IDs and stock history remain.
2. Create sardines with brand, 155g size and Tomato sauce variation; verify descriptive SKU. Register a different name with the same abbreviated code and verify suffix. Reject duplicate barcode/exact identity.
3. Add two deliveries to that product; verify one parent/SKU and different B IDs, quantities and dates.
4. Edit first batch quantity/date/location/lot; verify second batch stock details stay unchanged. Edit brand/size/variation/unit and confirm shared metadata updates without changing SKU. Check quantity correction and historical movement units in Reports/CSV.
5. Archive, restore, soft delete and restore the product with all batches. Then soft delete and permanently delete on disposable test data. Confirm absent from lists, reports, reminders and SQLite. Cancel permanent delete and confirm nothing changes.
6. Close/reopen the app; check persistence. Check Settings/Profile cleanup, photo flows, manual/online barcode lookup, notification permission, search/category/status filters and all navigation buttons.

START-HERE.md describes this version. Older version documents are historical, not a statement that earlier limitations still apply.
