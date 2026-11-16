# Sabikudi Navigation Map v4 & Clickable Mockup v11 Analysis & Implementation Plan

## Executive Overview

This analysis evaluates the new draft specifications (**Sabikudi Nav Map v4** and **Clickable Mockup v11**) against the current Android app codebase. It identifies existing capabilities, architectural gaps, data model changes, UI requirements, and provides a structured implementation plan to align the SabiKudi codebase with the v4/v11 standards.

---

## Key Features & Improvements in Drafts v4 & v11

### 1. Workspace Switcher & Multi-Workspace Support
- **Multi-Workspace Model**: One phone number can belong to multiple workspaces. Each workspace has its own mode (`BUSINESS` or `SHARED`), role (`OWNER`, `STAFF`, `ORGANISER`, `MEMBER`), receipts, balances, and offline sync queue.
- **Workspace Switcher Sheet**: Replaces static mode switching with an interactive bottom sheet accessible from top bar. Enables switching active workspace, joining a workspace via invite code (`flt-` or `sbk-`), or creating a new workspace.

### 2. Role-Based Permissions & Feature Gating
- **Business Mode**:
  - **Owner**: Full access (add/approve receipts, full split types, cost/margin view, balances/settlement, CSV/statement export, staff device management).
  - **Staff**: Can add receipts (pending owner approval or auto-approved under limit), scan receipts, view stock. Hidden: costs, margins, selling prices, balances, settlement, exports.
- **Shared Expenses Mode**:
  - **Organiser**: Full control over people/groups, 4 split types (Equal, Percent, Exact, Shares), settlement & payment confirmation, recurring bills, statements.
  - **Member**: Can add receipts (equal split only), view own balances, report payments ("I've paid"). Hidden: settling for others, manage groups, custom splits.

### 3. Advanced Receipt Pipeline & Split Rules
- **Stale Split Prompt**: Editing a line item's price after a split is saved flags the split as `STALE`. Displays warning badges in Receipt Detail and Balances. Blocks settlement until re-split.
- **Destination Switch Guard**: Moving an item between `Split`, `Stock`, and `Unassigned` checks `countSettledForLineItem`. Blocked if any share is settled; otherwise confirms and deletes old split/stock entries atomically.
- **Rounding Remainder**: Fractional kobo from division goes to the lowest `SplitAssignment` ID. Split Review displays explicit odd kobo notice.
- **Part Payments & Settlement Undo**: Settle sheet supports partial payment entry. Records history in `SettlementHistory` and allows single-tap `Undo` from history tab and toast notifications.
- **Group Memory**: Automatically remembers `merchantName -> groupId` mapping to suggest the last-used group when scanning/creating receipts from the same merchant.

### 4. Stock, Inventory & Financial Metrics
- **Stock Calculations**: Selling price input automatically computes margin percentage `(sellPrice - cost) / sellPrice * 100` and profit per unit.
- **Restock & Expiry**: Restock action adds `2 * threshold` to inventory. Displays expiry countdowns ("Expires in X days" / "Expired").

### 5. App Lock & Receipt Retention
- **App Lock**: PIN-protected security overlay requiring authentication upon app launch or manual lock.
- **Receipt Image Purge**: Configurable auto-purge window (30, 60, 90 days) stored in DataStore, with a per-receipt `isKeepPermanent` toggle in `ReceiptDetailScreen`.

---

## Codebase Audit & Gap Analysis

| Feature / Screen | Current Implementation | Draft v4/v11 Requirement | Gap Status |
| :--- | :--- | :--- | :--- |
| **Workspace Management** | Single mode/role toggled via DataStore | Workspace entity, multi-workspace switcher sheet, workspace-scoped data | **Missing** |
| **Role-Gated Custom Splits** | Equal split only | Equal, Percent, Exact, Shares splits with validation & role gating | **Partial** (Equal only) |
| **Stale Split Logic** | Basic stale flag in entity | Price edit trigger -> Stale prompt -> Re-split flow -> Settle blocking | **Partial** (Entity ready) |
| **Part Payment & Undo** | Full settlement only | Partial payment entry, `SettlementHistory`, instant Undo from toast & list | **Missing** |
| **Destination Guard** | Simple destination chip toggle | Check `countSettledForLineItem`, block if settled, confirm deletion transaction | **Missing** |
| **Group Memory** | No memory of past groups | Map merchant to group ID; pin "Suggested" group in Group Picker | **Missing** |
| **Stock Margin & Expiry** | Basic inventory item fields | Live margin % / profit calculation, price trend chart, expiry date picker | **Partial** |
| **Approval Flow (Staff)** | Basic inbox sheet | Receipt status (`SAVED`, `WAITING`, `APPROVED`, `REJECTED`), Owner Approve/Reject/Edit flow | **Partial** |
| **App Lock Overlay** | Standalone screen | Global app lock overlay requesting 4-digit PIN | **Partial** |
| **Retention & Keep Override** | DataStore retention preferences | Per-receipt `isKeepPermanent` toggle in Receipt Detail overriding auto-purge | **Partial** |

---

## Comprehensive Implementation Plan

### Phase 1: Database & Data Layer Enhancements
- **Task 1.1**: Add `GroupMemoryEntity` (`merchantName`, `groupId`, `lastUsedTimestamp`) to `SabiKudiDatabase`.
- **Task 1.2**: Update `ReceiptEntity` and `ReceiptItemEntity` with fields for `isoDate`, `createdByRole`, `isKeepPermanent`, `status`, `splitType`, `staleOriginalPriceKobo`, `isStale`, `note`.
- **Task 1.3**: Add `WorkspaceEntity` and `WorkspaceMembershipEntity` to support multi-workspace model and workspace switcher.
- **Task 1.4**: Update `SabiKudiDao` with queries for `GroupMemory`, settlement undo, stale split state updates, and destination change validations.

### Phase 2: Core Business Logic & ViewModel Updates
- **Task 2.1**: Implement `GroupMemoryRepository` to store and retrieve suggested groups per merchant.
- **Task 2.2**: Implement Stale Split logic in `NewReceiptViewModel` & `MainViewModel` (detect price changes on split items, set `isStale`, block settlement).
- **Task 2.3**: Implement Part Payment and Undo settlement methods in `MainViewModel`.
- **Task 2.4**: Add Destination Switch Safety Guard (`countSettledForLineItem` check and atomic Room transaction).
- **Task 2.5**: Expand `SplitAssignment` logic to support Equal, Percent, Exact, and Shares split types with validation (Percent = 100%, Exact = Total Amount).

### Phase 3: UI Screen Enhancements & New Components
- **Task 3.1**: Build `WorkspaceSwitcherSheet` composable with workspace list, join code entry, and create workspace options.
- **Task 3.2**: Enhance `SplitAssignmentScreen` with split type selector chips (Equal, Percent, Exact, Shares) and live validation feedback.
- **Task 3.3**: Update `ChooseGroupScreen` to display the "Suggested" group pinned at top when a merchant match is found.
- **Task 3.4**: Update `ReceiptDetailScreen` with `isKeepPermanent` toggle switch and Stale Split warning banner with "Re-split" action.
- **Task 3.5**: Update `SettleSheet` to accept custom amounts for part payments and show settlement confirmation.
- **Task 3.6**: Enhance `StockDetailScreen` with selling price input, auto-computed margin % & profit per unit, and expiry date picker.
- **Task 3.7**: Implement global `AppLockOverlay` checking `isAppLockEnabled` and verifying PIN.

### Phase 4: Verification & Testing
- **Task 4.1**: Unit tests for split allocation formulas (Equal, Percent, Exact, Shares) and integer kobo rounding.
- **Task 4.2**: Unit tests for stale split prompt triggers and settlement blocking.
- **Task 4.3**: End-to-end UI verification of receipt creation -> split -> price edit -> stale prompt -> re-split -> part settlement -> undo.

---
