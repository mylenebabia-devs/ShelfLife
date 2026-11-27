# Measurement detection — 2.5

Focused changes only: Measurement, LookupMetadata, BarcodeLookup, MeasurementBinding and ProductLookupBinding. Category selection, Others, Inventory rendering, theme, SKU rules, batch processing and database schema are unchanged.

## Causes found

- LookupMetadata explicitly ignored description text, even when it contained the pack weight.
- Package text and alternative product names were not included as measurement evidence.
- The parser treated repeated identical measurements as ambiguous, rejected nonbreaking/narrow spaces, and rejected measurements attached directly to a product name.
- An existing unit without a numerical value was treated as sufficient local metadata, preventing an online attempt to fill in the value. Edit form initialization also skipped recovery in this case.

## Changes

- Extract measurements from quantity, explicit numeric quantity/unit, product names, generic descriptions and packaging text. Existing supported abbreviations map to full dropdown labels through the existing SavedChoice mapper. No default weight or unit is assigned from the product's category or brand.
- Parse Unicode spaces, name suffixes such as Product425g, and repeated identical values. Conflicting measurements and ambiguous multipacks still require staff review; no guessed number is chosen.
- Descriptions use explicit net/pack labels when present. Otherwise, unambiguous product description measurements are accepted while common nutrition/serving/recipe clauses are excluded.
- Request packaging_text and packaging_text_en from the existing lookup source, and consider alternate English names/descriptions.
- Incomplete local measurements can be supplemented from lookup. An edit form can recover a missing value from saved pack/name/description while preserving an existing unit if no value is known.
- Name, description and pack field edits share the same detection path. The existing guarded direct form update preserves staff measurement corrections during a pending lookup. Both barcode entry methods continue to use the same handler.
- Existing save mapping continues to store measurementValue/measurementUnit on the parent catalog and its batch snapshots. No migration, table or deletion change is introduced.

## Checks run

142 executable Java logic checks passed: 47 domain/auth, 15 SKU, 19 measurement, 24 category and 37 lookup mapping checks. New cases include 425g, 1kg, 250ml, 1L, 500g, 1000mg, description-only values, package labels, repeated values, nonbreaking spaces and missing measurement/manual fallback. Existing SQLite regression scripts passed, including parent/batch relationships, persistence and unchanged migration behavior.

Android instrumentation tests now include JSON description/package responses and repository persistence of detected values. Existing tests cover full dropdown labels and protection of manual edits. These instrumentation tests are included, but were not run here.

## Verification limitation

Gradle 9.4.1 could not download: Network is unreachable. Android compilation, camera scanning, manual-entry UI execution and on-device persistence remain unverified. There is no APK in this archive. The supplied project uses Room/SQLite and has no MySQL backend; MySQL synchronization cannot be tested or claimed.

## On-device acceptance

1. Build and run connectedAndroidTest with the supplied configuration.
2. Scan and manually enter the same known barcode. Verify identical value and full unit label.
3. Try products exposing quantity text, names, descriptions and package text separately, across g/kg/ml/L/mg.
4. Correct the value and unit during a pending lookup, then save and reopen. Confirm corrections remain and parent/batch measurement fields match.
5. Test an unknown/no-measurement barcode. Confirm no invented value and manual entry is available.
6. Confirm category selection, Others and Inventory filtering behave as before.

Online coverage still depends on the source. A successful barcode scan alone does not guarantee a lookup record or a pack measurement. If an actual product still fails after this update, retain its barcode and the displayed lookup status to distinguish missing source data from parsing/UI errors.
