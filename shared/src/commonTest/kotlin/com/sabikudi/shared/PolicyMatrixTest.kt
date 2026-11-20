package com.sabikudi.shared

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PolicyMatrixTest {

    @Test
    fun ownerHasFullAccessInBothModes() {
        Action.entries.forEach { action ->
            action.modes.forEach { mode ->
                assertTrue(
                    PolicyMatrix.allows(mode, Role.OWNER, emptySet(), action),
                    "Owner should be allowed $action in $mode",
                )
            }
        }
    }

    @Test
    fun modeScopingRejectsInvalidModes() {
        assertFalse(PolicyMatrix.allows(WorkspaceMode.SHARED, Role.OWNER, emptySet(), Action.VIEW_STOCK))
        assertFalse(PolicyMatrix.allows(WorkspaceMode.BUSINESS, Role.OWNER, emptySet(), Action.SETTLE))
    }

    @Test
    fun staffPermissionsCheck() {
        assertFalse(PolicyMatrix.allows(WorkspaceMode.BUSINESS, Role.STAFF, emptySet(), Action.VIEW_STOCK))
        assertTrue(PolicyMatrix.allows(WorkspaceMode.BUSINESS, Role.STAFF, setOf(Permission.STK), Action.VIEW_STOCK))
    }

    @Test
    fun memberReceiptCreationNeedsPermission() {
        assertFalse(PolicyMatrix.allows(WorkspaceMode.SHARED, Role.MEMBER, emptySet(), Action.CREATE_RECEIPT))
        assertTrue(PolicyMatrix.allows(WorkspaceMode.SHARED, Role.MEMBER, setOf(Permission.ADD_RECEIPTS), Action.CREATE_RECEIPT))
    }

    @Test
    fun deleteWorkspaceIsOwnerOnly() {
        assertTrue(PolicyMatrix.allows(WorkspaceMode.BUSINESS, Role.OWNER, emptySet(), Action.DELETE_WORKSPACE))
        assertFalse(PolicyMatrix.allows(WorkspaceMode.BUSINESS, Role.ORGANISER, emptySet(), Action.DELETE_WORKSPACE))
    }
}
