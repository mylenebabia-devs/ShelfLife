# ShelfLife 2.1 — Controlled categories and visual polish

The Category dropdown contains predefined and previously saved categories, with Others last. It does not accept direct typing. Others reveals a custom text field; choosing another item hides it and ignores its contents. Custom categories are stored only when staff saves the product. Existing custom category records are preserved.

Online barcode category data is now mapped conservatively to predefined choices. Clear canned/tinned products and explicitly described sardines/tuna in sauce/oil/brine suggest Canned Goods. Dairy, beverages, bakery, snacks, condiments and other recognizable types map to existing defaults. Unknown taxonomy does not become a new category: the field remains blank. Suggestions remain editable through the dropdown, and selecting a category protects it from being cleared on the next scan. Existing locally saved product categories are retained. No schema migration or data deletion is introduced.

Household Products and Toiletries are excluded from Inventory category chips, including saved occurrences. Their records remain in All/search; the underlying categories remain available for existing product management and registration. No product was removed.

Buttons use coordinated green hover/focus/pressed states and restrained ripple feedback; outlined and secondary buttons receive soft green interaction backgrounds. Dialog surfaces use the cream palette. Existing layout, sizes, navigation and functionality are retained. Hover applies to pointer/mouse input; phones use pressed/ripple feedback.

Version code 13, version 2.1-category. Keep existing app data and update with the same signing key.

Verification — 2 November 2026:
- Passed: 92 executable domain/auth, SKU/gating, measurement and category checks.
- Passed: migration/SQLite constraint and persistence checks, 64 XML/resource checks, 44 Java syntax parses.
- Blocked: Gradle build failed before Android task execution downloading Gradle (Network is unreachable). No compiled APK or phone visual verification is claimed.
- Device checks pending: scan obvious canned/unknown products; ensure no raw category creation; select/change predefined and saved options; Others show/hide/save; category choice survives lookup completion; All/search still display records from hidden filter categories; touch ripple and mouse hover; normal registration/edit/stock/profile flows.


--- Previous version details ---
# ShelfLife 2.0 — Barcode-gated registration and custom options

## Registration

A NEW product's SKU remains blank until barcode, name, category, brand, measurement value/unit and variation are present. Complete missing details from the actual label; no assumed flavor/brand/size is filled. A value such as Original or Unflavored can be entered when accurate. New registration is blocked without these details. Existing products, including older records without barcodes or complete measurements, retain their original SKU and remain editable.

Camera/manual lookup still checks local stock first; known barcodes open product details for Edit or Add stock. Unknown supported numeric codes can retrieve Open Food Facts information; unsupported/unmatched/offline codes allow staff to complete fields manually. No catalog success is required when the barcode is manually entered and actual details are supplied. Public catalog coverage is not universal, particularly for toiletries and household products.

SKU format CATEGORY-BRAND-SIZE-VARIATION derives from entered values, e.g. SAR-MEG-155-TS. Standard category mappings include beverages, dairy, bakery, snacks, canned goods, condiments, toiletries, household, frozen, meat, fruit and grains. Custom category/brand/variation identifiers use consistent abbreviations of the supplied text. Numeric size is used, with product measurement unit retained separately. Different identities that produce the same abbreviated base receive a transactionally allocated numeric suffix. Existing SKU values never change when fields or batches are edited.

## Batches

Every new delivery requires a staff-entered Batch number / stock reference (e.g. B1, B2). It is separate from Supplier Lot and editable in the full Edit form. References are trimmed, 1–40 readable characters and unique case-insensitively WITHIN a product. B1 and b1 conflict for the same product; another product may use B1. A database unique index on productId + stockReference enforces this. Internal numeric batch IDs remain stable keys for movements/reminders and are not user-entered references.

Migration seeds old references from their existing B<number> display, preserving identities and histories. New references are never automatically assigned. Each batch retains received/remaining quantity, received date, printed date and storage location. Add stock keeps the existing parent product/SKU. Cards, Details, notifications and inventory CSV expose the entered reference alongside internal IDs where appropriate.

## Category and measurement dropdowns

Both Add and Edit use dropdowns. Categories include grocery defaults, saved custom categories, and Others at the end. Units offer Kilogram (kg), Gram (g), Milligram (mg), Liter (L), Milliliter (ml), Ounce (oz), Pound (lb), Pieces (pcs), saved custom units and Others. Selecting Others reveals its custom text input. Choosing a regular option hides that input.

Custom options are saved in SQLite saved_options in the same successful product transaction. Cancelling the form does not create an option. They remain available after restart and after the original product is permanently deleted. Automatic unit detection still recognizes explicit standard-unit information; staff can override it or enter a custom unit. Arbitrary custom units are not inferred from unknown words. Value remains editable. Existing stock-count units remain separate from product measurement.

## Install and verification — 2 November 2026

Version code 12, version 2.0-registration, Room database version 10. Extract ZIP and open its settings.gradle.kts folder. Update with the same signing key; do not clear app data.

Passed: 47 executable production domain/auth checks, 15 SKU/gating checks, 19 measurement checks (81 total). Passed: 41 actual SQLite migration statements, case-insensitive batch uniqueness, parent relationships, custom-option persistence after reopen and product deletion, data-retention/rollback checks, 61 XML/resource checks and 42 Java syntax parses.

Android build was attempted but failed BEFORE task execution downloading Gradle: Network is unreachable. No APK or phone-rendering validation is claimed. Instrumentation tests are supplied but not executed here. Test migrations on a copy before using valuable store records.

Phone acceptance checklist:
1. New form has blank SKU; barcode alone or incomplete details keep it blank. Complete all fields and check descriptive preview/save.
2. Scan known barcode, Edit and Add stock; original SKU must remain unchanged.
3. Enter B1 then another B1/b1 for the same product and confirm rejection. B2 succeeds; editing it retains original batch row and quantities. Another product may use B1.
4. Choose Others category/unit; type custom values, save, reopen and verify available options. Cancel and confirm no saved option. Switch away from Others and verify stale custom text is ignored.
5. Scan catalog measurement, manually override and verify it persists. Check saved custom units survive deleting the creating product.
6. Regress photos, search, filters, archived/deleted/restore/permanent delete, batch stock out, reports/CSV, notifications, account settings and password changes.

