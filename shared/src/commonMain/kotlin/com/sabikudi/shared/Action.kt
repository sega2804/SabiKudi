package com.sabikudi.shared

private val BOTH = WorkspaceMode.entries.toSet()
private val BUSINESS_ONLY = setOf(WorkspaceMode.BUSINESS)
private val SHARED_ONLY = setOf(WorkspaceMode.SHARED)

enum class Action(val modes: Set<WorkspaceMode>) {
    // Both modes
    CREATE_RECEIPT(BOTH),
    APPROVE_RECEIPT(BOTH),
    VIEW_OTHER_RECEIPTS(BOTH),
    EXPORT(BOTH),
    MANAGE_MEMBERS(BOTH),
    MANAGE_DEVICES(BOTH),
    CREATE_INVITE(BOTH),
    SEND_ANNOUNCEMENT(BOTH),
    DELETE_WORKSPACE(BOTH),

    // Business only
    VIEW_STOCK(BUSINESS_ONLY),
    EDIT_STOCK_COUNTS(BUSINESS_ONLY),
    VIEW_STOCK_FINANCIALS(BUSINESS_ONLY),

    // Shared only
    VIEW_OWN_SPLITS(SHARED_ONLY),
    VIEW_ALL_SPLITS(SHARED_ONLY),
    SETTLE(SHARED_ONLY),
    REPORT_PAID(SHARED_ONLY),
}
