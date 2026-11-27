# Measurement-only update

The only production code change is BarcodeLookup.java. Retrieves omitted localized and packaging measurement data and avoids caching incomplete measurement results. Explicit 155g becomes Value 155 and Unit Gram (g). The existing scanner and manual barcode entry use the same lookup path. Value and Unit remain editable; Others reveals custom input. Categories, layout, database, SKU and batch functionality remain unchanged.

Validation: 142 existing logic checks, 21 production JSON response checks, SQLite regression checks and Java/XML syntax checks passed. Added an Android test for 155g population, manual editing and Others visibility, but it has NOT been executed. Gradle download failed (network unreachable), so Android compilation, camera-to-form behavior and on-device persistence remain unverified.

No measurement is invented when the lookup source lacks explicit pack data. Install/update without clearing existing app data.
