# ShelfLife 2.3 — focused category and lookup update

Changes apply to the existing Android app and its Room/SQLite database. This project contains no MySQL API or server connection; MySQL synchronization has not been implemented or tested.

- Category popup uses explicit downward placement and a height-limited scrolling list. It requests space in the form before opening, instead of allowing Android's default upward flip. Add and Edit share this implementation. Theme retained.
- Inventory and product forms share predefined and saved category choices. Others is last; its custom field is shown only when selected. Custom categories are persisted atomically on successful product save, not merely on typing. Whitespace/case duplicates resolve to the same saved spelling.
- Added Instant Noodles and Laundry Products. Existing equivalents remain named Dairy and Eggs, Bread and Bakery, and Snacks and Biscuits. Specific lookup taxonomy takes priority over ingredient words, e.g. milk biscuits remain Snacks and Biscuits. Unknowns remain unassigned. No lookup is guaranteed to cover every barcode; Open Food Facts plus local inventory are the available sources.
- Barcode scan and manual entry share the same lookup binding. Category and measurement suggestions remain editable; manual changes made while a lookup is pending are protected.
- Measurement dropdown shows full labels while storage retains canonical symbols. Detection retains the explicit numeric value; no inferred weights or conversion. Product Details use bold colon labels and omit absent fields.
- Database v11 migration permanently removes products categorized as Toiletries or Household Products, their batches, stock movements and reminder records, and those options. This is the explicitly requested deletion. Other product rows are preserved. It does not remove personal accounts. Previously removed Grains and Cereals and Flakes choices remain unavailable; products in those categories are not deleted.
- Existing SKU, batch links, stock balances, photos and unrelated workflows retained. Old unreferenced photo files are not purged by the SQL migration.

## Verified here
- 105 executable production domain/auth/SKU/measurement/category checks passed.
- All 47 migration SQL statements executed against SQLite fixtures; parent/batch links, stock retention, rollback and persistence checks passed.
- Dedicated v10->v11 fixtures verify targeted cleanup, child cleanup, case/space deduplication, unchanged unrelated records and foreign-key integrity.
- 64 XML documents parsed; Java resource references checked. 45 Java files parsed for syntax. These are not Android compilation or UI execution.

## Not verified here
Gradle 9.4.1 download failed with `Network is unreachable`, before compilation. No Android SDK/emulator is available. No APK is included. Android instrumentation tests (including new category and unit tests), camera scanning, live external lookup and popup placement need execution on the target device.

## Device acceptance checklist
1. Build with the supplied Android Studio/Gradle configuration; run connectedAndroidTest.
2. Scan a known barcode and repeat via manual entry. Review name, category, value and full unit label. Try an unknown barcode: manual fields must remain usable.
3. Try canned goods, noodles, dairy, drinks, snacks, bakery and laundry. Change a suggestion manually while lookup is pending; confirm it is retained.
4. Choose Others, save a spaced/case-varied custom category, reopen Inventory, restart and log back in. Confirm one canonical choice, no duplicates. Cancelled forms must not create options.
5. Open Add and Edit on small and large screens, with keyboard, large font and TalkBack. Category list must open below the field, scroll and select; cancel/dismiss must not leak a window.
6. Detect 425g, 150ml, 1kg and 1L. Edit value/unit, save, reopen and confirm original numbers and corrected units.
7. Check Details labels, missing fields and full unit labels across categories.
8. Upgrade a copy of the previous database. Confirm only the two removed category groups and their linked records are deleted; remaining products, stock, SKUs, staff and history are unchanged.
9. Add/edit individual batches and check parent metadata, total remaining stock, archive/delete/restore and notifications.
10. MySQL verification is not applicable to this supplied SQLite-only project; a real backend API and its schema are required for that integration.
