package com.crypticsamsara.sabikudi.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.crypticsamsara.sabikudi.core.data.entities.SplitAssignment
import com.crypticsamsara.sabikudi.core.data.entities.SplitAssignmentStatus
import kotlinx.coroutines.flow.Flow

interface SplitAssignmentDao {
    data class PersonBalance(
        val personId: Long,
        val personName: String,
        val totalUnsettledMinor: Long
    )

    /**
     * Directional balance: `debtorPersonId` owes `creditorPersonId` `amountMinor`, derived
     * from Receipt.paidByPersonId. This is what "Ada owes Chidi ₦2,000" needs — the plain
     * PersonBalance above can only say "Ada owes ₦2,000" with no creditor.
     */
    data class PairwiseBalance(
        val debtorPersonId: Long,
        val debtorName: String,
        val creditorPersonId: Long,
        val creditorName: String,
        val amountMinor: Long
    )

    @Dao
    interface SplitAssignmentDao {
        @Insert
        suspend fun insert(assignment: SplitAssignment): Long

        @Insert
        suspend fun insertAll(assignments: List<SplitAssignment>): List<Long>

        @Update
        suspend fun update(assignment: SplitAssignment)

        /**
         * Deterministic ordering for the rounding-remainder rule (PRT §2, edge-case
         * rule 1): the remainder in the smallest currency unit goes to whichever
         * assignment has the lowest `id` for this LineItem — i.e. the first row
         * returned here. Never rely on unordered query results for this decision.
         */
        @Query("SELECT * FROM split_assignments WHERE lineItemId = :lineItemId ORDER BY id ASC")
        suspend fun getByLineItemOrdered(lineItemId: Long): List<SplitAssignment>

        /** Flips every assignment on a LineItem to STALE after its price is edited post-split. */
        @Query("UPDATE split_assignments SET status = :status WHERE lineItemId = :lineItemId")
        suspend fun markStatusForLineItem(lineItemId: Long, status: SplitAssignmentStatus)

        @Query("UPDATE split_assignments SET settled = 1, settledAt = :settledAt WHERE id = :id")
        suspend fun markSettled(id: Long, settledAt: Long)

        @Query(
            """
        SELECT p.id AS personId, p.name AS personName,
               COALESCE(SUM(sa.shareAmountMinor), 0) AS totalUnsettledMinor
        FROM people p
        LEFT JOIN split_assignments sa ON sa.personId = p.id AND sa.settled = 0
        GROUP BY p.id
        """
        )
        fun observeBalances(): Flow<List<PersonBalance>>

        /**
         * Debtor -> creditor breakdown for the Balance Summary UI. Joins through LineItem
         * to Receipt to find who paid, and excludes rows where the "debtor" IS the payer
         * (a person doesn't owe themselves for their own purchase).
         */
        @Query(
            """
        SELECT
            debtor.id AS debtorPersonId, debtor.name AS debtorName,
            creditor.id AS creditorPersonId, creditor.name AS creditorName,
            SUM(sa.shareAmountMinor) AS amountMinor
        FROM split_assignments sa
        INNER JOIN line_items li ON li.id = sa.lineItemId
        INNER JOIN receipts r ON r.id = li.receiptId
        INNER JOIN people debtor ON debtor.id = sa.personId
        INNER JOIN people creditor ON creditor.id = r.paidByPersonId
        WHERE sa.settled = 0 AND r.paidByPersonId IS NOT NULL AND sa.personId != r.paidByPersonId
        GROUP BY debtor.id, creditor.id
        """
        )
        fun observePairwiseBalances(): Flow<List<PairwiseBalance>>

        /** Guard checked by the repository before hard-deleting a Person as a debtor. */
        @Query("SELECT COUNT(*) FROM split_assignments WHERE personId = :personId AND settled = 0")
        suspend fun countUnsettledForPerson(personId: Long): Int

        /**
         * Second guard for hard-delete: a Person can also be owed money (as a Receipt's
         * payer) rather than owing it. Counts unsettled SplitAssignments on receipts they
         * paid for — if non-zero, deleting them would lose track of who to pay back.
         */
        @Query(
            """
        SELECT COUNT(*) FROM split_assignments sa
        INNER JOIN line_items li ON li.id = sa.lineItemId
        INNER JOIN receipts r ON r.id = li.receiptId
        WHERE r.paidByPersonId = :personId AND sa.settled = 0
        """
        )
        suspend fun countUnsettledReceiptsPaidByPerson(personId: Long): Int
    }

}