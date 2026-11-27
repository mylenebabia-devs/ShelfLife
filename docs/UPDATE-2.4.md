# Detection and Inventory fixes — 2.4

Scope: fixes to lookup mapping, form population and Inventory category state. Theme, downward category popup, Others custom categories, SKU/batch rules, photos, notifications and database schema remain unchanged. SQLite database stays at version 11. No new deletion or migration is introduced by this update.

## Findings and changes

- The lookup requested only display categories and quantity. Products exposing taxonomy tags, English fallback names or separate quantity/unit fields could therefore have insufficient mapped data. The request/parser now accepts those fields too.
- Product-name text watchers can change the hidden pack-size field before the response reaches it. The old unchanged-field guard could then skip the actual lookup pack measurement. A dedicated measurement update now applies value and full unit label together under the existing watcher guard, instead of depending on hidden-field side effects.
- Category response values used AutoCompleteTextView's filtering setter. Programmatic suggestions now use setText(value, false), preserving the full dropdown list.
- Local lookup previously stopped even if the saved record lacked metadata. Missing category/measurement may now be supplemented from the existing online source. Saved names/details are retained. Offline and incomplete responses still allow manual entry.
- Both camera results and manual barcode input continue through the same barcode field watcher / lookup process. Stale barcode responses are ignored. Category/measurement edits made during pending lookups remain protected.
- Inventory formerly removed and recreated every chip on tab changes and stock updates. It now reuses unchanged views, reconciles only changed categories and updates selection with listeners temporarily disabled. Category matching normalizes case/spacing without modifying database records. Returning to the tab resets the horizontal scroll with the existing All reset. The dashboard app bar expands without animation when switching tabs to avoid carrying a stale collapsed position across different header heights.

The existing category label "Dairy and Eggs" remains the dairy option; this update does not rename or duplicate it. Units depend on actual pack data: milk may be grams or milliliters, and cola may be ml or L. No unit is inferred from a brand name alone.

## Verification completed

- 123 executable logic checks: 47 domain/auth, 15 SKU, 19 measurement, 24 category, 18 new lookup mapping checks.
- 47 SQLite migration statements and existing persistence, rollback, foreign-key, stock and targeted-cleanup regression fixtures passed.
- XML/resource checks passed for 64 XML files. Syntax parser passed for 47 Java files; this is not Android type checking.
- New device instrumentation tests cover JSON taxonomy tags, separate numeric quantity/unit, missing units, direct measurement dropdown updates and preservation of manual corrections. These tests are included but not executed here.

## Android verification still pending

Attempted `./gradlew testDebugUnitTest assembleDebug --no-daemon`. Gradle 9.4.1 download failed with `Network is unreachable`, before compilation. No SDK/emulator or live camera testing was available. No APK or fully tested runtime claim is made.

On the target Android device:
1. Run Gradle sync/build and connectedAndroidTest.
2. Scan and manually enter the same known barcode, confirming identical suggestions. Test quantity text and structured quantity/unit responses.
3. Try unknown/offline codes; enter details manually. Verify no guessed unit appears for a name without pack information.
4. Change barcode rapidly and manually correct category/value/unit while lookup is pending; confirm the latest barcode and manual changes win.
5. Save/reopen the product; confirm category and canonical unit/value persist. Add a batch and confirm the SKU stays fixed.
6. Switch Home/Inventory/Alerts repeatedly after scrolling. Select each category, including a custom category. Confirm stable chips, correct checked selection, horizontal scrolling, case/space matching and no duplicate entries.
7. Create a custom category using Others, save and restart the app. Confirm it remains in Inventory and Add/Edit.

Existing Open Food Facts availability/coverage limits still apply. A barcode encodes an identifier, not the product's category or weight. If the source does not provide sufficient data, staff must fill it in.
