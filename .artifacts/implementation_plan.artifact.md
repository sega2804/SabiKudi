# Navigation 3 + FQN Arguments Migration Plan

## Goal Description

Migrate the SabiKudi Android app from string-based navigation routes with manual `navArgument` DSL and `getArgument()` extraction to **Navigation 3 with Fully Qualified Name (FQN) references** and typed argument access. This improves type safety, enables compile-time navigation validation, and reduces runtime ClassCastException risks from missing/incorrect arguments.

The migration covers all three mode-dependent navigation graphs (`SabikudiNavGraph`, `ExpenseNavGraph`, `BusinessNavGraph`) plus the onboarding host, and updates all screen composables that navigate between destinations.

## User Review Required

- [x] **Breaking Change**: All navigation string routes (`ROUTE_REVIEW`, `ROUTE_SPLIT`, etc.) will change their reference mechanism. Screens calling `navController.navigate("review/{receiptId}")` must be updated to use the new typed API.
- [x] **API Surface Change**: New centralized `NavRoutes` object will be the single source of truth for all route strings. Existing `const val ROUTE_*` definitions in each nav graph file can be removed or deprecated.
- [x] **Bottom Bar Logic Change**: `hideBottomBarRoutes` list of strings will become a `Set<String>` from `NavRoutes`. The `currentRoute !in ...` check logic remains semantically identical but uses the new set.
- [x] **Hilt/ViewModel Impact**: None expected. `hiltViewModel()` usage remains unchanged. Navigation argument extraction changes only affect how args are read, not how ViewModels are scoped.

## Open Questions

- [x] Should we keep the `ROUTE_*` constant strings in each nav graph file for backward compatibility, or remove them entirely after migration?
- [x] Do we want to generate `@NavArgs` classes via the Navigation Gradle plugin, or continue using manual `navArgument` DSL with typed accessors?
- [x] Should the `SabikudiBottomBar` receive the `NavRoutes` instance via composition or ambient lookup?

## Proposed Changes

### 1. Create Centralized `NavRoutes` Object
**File:** `app/src/main/java/com/crypticsamsara/sabikudi/shared/navigation/NavRoutes.kt` (NEW)

A new object containing all route strings as `val route: String` properties, plus a computed `hideBottomBarRoutesSet: Set<String>`. This becomes the single source of truth for all navigation routes.

```kotlin
object NavRoutes {
    val splash = "splash"
    val modeSelection = "modeSelection"
    val cameraPermission = "cameraPermission"
    val scan = "scan"
    val newReceipt = "newReceipt"
    val dashboard = "dashboard"
    val management = "management"
    val groups = "groups"
    val people = "people"
    val history = "history"
    val receiptDetail = "receiptDetail/{receiptId}"
    val settings = "settings"
    val retention = "retention"
    val group = "group/{groupId}"
    val review = "review/{receiptId}"
    val destinationAssignment = "destinationAssignment/{receiptId}"
    val whoPaid = "whoPaid/{receiptId}"
    val groupPicker = "groupPicker/{receiptId}"
    val adHocPerson = "adHocPerson/{receiptId}"
    val split = "split/{receiptId}"
    val splitReview = "splitReview/{receiptId}/{lineItemId}"
    val stock = "stock/{receiptId}"

    // Computed set of routes that hide the bottom bar (mode-dependent logic still needed)
    val expenseHideBottomBarRoutes: Set<String> = listOf(
        splash, modeSelection, cameraPermission, scan,
        group, receiptDetail, retention, review,
        destinationAssignment, whoPaid, groupPicker,
        split, splitReview
    ).toSet()

    val businessHideBottomBarRoutes: Set<String> = listOf(
        scan, receiptDetail, retention, review, stock
    ).toSet()
}
```

### 2. Update `SabikudiNavGraph.kt`
- Replace all `composable(ROUTE_X, arguments = listOf(navArgument("arg") { type = NavType.LongType })) { backStackEntry -> ...}` blocks with `composable(navRoutes.ROUTE_X) { ... }`
- Update `navController.navigate(ROUTE_X) { ... }` to `navController.navigate(navRoutes.ROUTE_X) { ... }` or `navController.navigate(navRoutes.ROUTE_X) { arg("receiptId", receiptId) }`
- Remove `ROUTE_*` constant definitions that are now in `NavRoutes`
- Keep `hideBottomBarRoutes` logic but reference `NavRoutes.expenseHideBottomBarRoutes` (or keep local set based on `NavRoutes`)

### 3. Update `ExpenseNavGraph.kt`
- Same route reference updates as `SabikudiNavGraph.kt`
- Align `hideBottomBarRoutes` with `NavRoutes.expenseHideBottomBarRoutes`
- Ensure `AppMode.EXPENSE` destinations map correctly (some routes differ from individual mode)

### 4. Update `BusinessNavGraph.kt`
- Same route reference updates
- Align `hideBottomBarRoutes` with `NavRoutes.businessHideBottomBarRoutes`
- Ensure `AppMode.BUSINESS` destinations map correctly

### 5. Update `RootNavHost.kt` & `OnboardingNavHost.kt`
- Mode-switching logic (`AppMode.EXPENSE` → `ExpenseNavGraph()`, etc.) remains unchanged
- May need to pass `NavRoutes` instance if bottom bar needs it (or keep using `AppMode` enum to select route set)

### 6. Update All Screen Composables That Navigate
Key files to modify (updating navigation calls, not UI logic):

| File | Current Navigation Pattern | New Pattern |
|---|---|---|
| `ReceiptReviewScreen.kt` | `onContinue = { navController.navigate(destinationAssignmentRoute(receiptId)) }` | `onContinue = { navController.navigate(navRoutes.destinationAssignment) { arg("receiptId", receiptId) } }` |
| `WhoPaidPromptScreen.kt` | `onContinue = { navController.navigate(groupPickerRoute(receiptId)) }` | `onContinue = { navController.navigate(navRoutes.groupPicker) { arg("receiptId", receiptId) } }` |
| `ScanReceiptScreen.kt` | `navController.navigate(reviewRoute(receiptId))` | `navController.navigate(navRoutes.review) { arg("receiptId", receiptId) }` |
| `NewReceiptScreen.kt` | Similar pattern | Same |
| `DashboardScreen.kt` | `navController.navigateToDashboard()` | Update to use `navRoutes.dashboard` |
| `GroupDetailScreen.kt` | `navController.navigate(groupDetailRoute(groupId))` | `navController.navigate(navRoutes.group) { arg("groupId", groupId) }` |
| `SabikudiBottomBar.kt` | `onDestinationSelected = { destination -> navController.navigate(destination.route) { ... } }` | `onDestinationSelected = { destination -> navController.navigate(destination.route) { ... } }` (if destination has `route` property, else update) |
| `ReceiptDetailScreen.kt` | `navController.navigate(splitReviewRoute(receiptId, lineItemId))` | `navController.navigate(navRoutes.splitReview) { arg("receiptId", receiptId); arg("lineItemId", lineItemId) }` |
| `SplitReviewScreen` (inside nav graphs) | Uses `hiltViewModel()` + `uiState` | Unchanged, but ensure `navArgument` references are now typed |

### 7. Update `SabikudiBottomBar.kt`
- Replace `currentRoute !in hideBottomBarRoutes` with `currentRoute !in navRoutes.hideBottomBarRoutesForMode(mode)`
- Or keep a `when` on `mode` and use `NavRoutes.expenseHideBottomBarRoutes` / `NavRoutes.businessHideBottomBarRoutes`

### 8. Version Bump & Build Sync
- Verify `build.gradle.kts` has `androidx.navigation:navigation-compose` version `2.7.+` or `2.8.+` (Navigation 3)
- Run `gradle_sync` tool
- Run `gradle_build(":app:assembleDebug")` to verify no compile errors

---
## Verification Plan

### Automated Tests
- Run existing unit tests: `gradle_build(":app:unitTest")`
- Run instrumented tests: `gradle_build(":app:androidTest")`
- Verify navigation graph compiles: `gradle_build(":app:compileDebugKotlin")`

### Manual Verification
- Deploy debug APK to device/emulator
- Flow through full user journey: Splash → Mode Selection → Camera Permission → Scan Receipt → Review → Who Paid → Group Picker → Split → Dashboard
- Verify all "Continue" and "Back" navigations work correctly
- Verify bottom bar appears/disappears at correct screens
- Verify receipt ID is correctly threaded through the navigation chain

### Post-Migration Checks
- No `ClassCastException` or `IllegalArgumentException` from missing arguments
- All `navController.navigate(...)` calls compile without `@Suppress("unused")`
- Bottom bar visibility matches expected UI state at each screen
- Hilt ViewModels still scoping correctly across navigation pops