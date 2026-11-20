package com.sabikudi.shared

object PolicyMatrix {

    fun allows(
        mode: WorkspaceMode,
        role: Role,
        perms: Set<Permission>,
        action: Action,
    ): Boolean {
        if (mode !in action.modes) return false

        val privileged = role == Role.OWNER || role == Role.ORGANISER

        // No `else` branch on purpose: adding an Action breaks the build until you decide its rule.
        return when (action) {
            Action.MANAGE_MEMBERS,
            Action.MANAGE_DEVICES,
            Action.CREATE_INVITE,
            Action.APPROVE_RECEIPT,
            Action.SEND_ANNOUNCEMENT,
            Action.EDIT_STOCK_COUNTS,
            Action.SETTLE -> privileged

            // Destructive and owner-only: an organiser who is not the owner cannot delete.
            Action.DELETE_WORKSPACE -> role == Role.OWNER

            Action.CREATE_RECEIPT -> privileged || role == Role.STAFF || Permission.ADD_RECEIPTS in perms
            Action.VIEW_OTHER_RECEIPTS -> privileged || Permission.OTH in perms
            Action.EXPORT -> privileged || Permission.EXP in perms
            Action.VIEW_STOCK -> privileged || Permission.STK in perms
            Action.VIEW_STOCK_FINANCIALS -> privileged || Permission.COST in perms
            Action.VIEW_ALL_SPLITS -> privileged || Permission.ALL in perms

            Action.VIEW_OWN_SPLITS,
            Action.REPORT_PAID -> true
        }
    }
}
