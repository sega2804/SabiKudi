package com.crypticsamsara.sabikudi.ui.screens.receipt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crypticsamsara.sabikudi.data.local.dao.*
import com.crypticsamsara.sabikudi.data.local.entities.*
import com.crypticsamsara.sabikudi.data.repository.GroupMemoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ItemDestination {
    UNASSIGNED, STOCK, SPLIT
}

enum class SplitType {
    EQUAL, PERCENT, EXACT, SHARES
}

data class DraftItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val priceInKobo: Long,
    val destination: ItemDestination = ItemDestination.UNASSIGNED,
    val category: String = "Groceries",
    val stkQuantity: Int = 10,
    val stkThreshold: Int = 3,
    val note: String = "",
    val splitType: SplitType = SplitType.EQUAL,
    val assignedPeople: List<String> = emptyList(),
    val customWeights: Map<String, Float> = emptyMap(), // For percent, exact, or shares
    val splitAllocations: Map<String, Long> = emptyMap(), // Computed kobo per person
    val isStale: Boolean = false,
    val staleOriginalPriceKobo: Long? = null,
    val isSplitConfirmed: Boolean = false
)

data class NewReceiptUiState(
    val merchant: String = "Shoprite Ikeja",
    val date: String = "2026-11-13",
    val paidBy: String? = null,
    val items: List<DraftItem> = listOf(
        DraftItem(name = "Rice 5kg", priceInKobo = 850000),
        DraftItem(name = "Chicken", priceInKobo = 1240000),
        DraftItem(name = "Detergent", priceInKobo = 320000)
    ),
    val selectedGroupIndex: Int? = null,
    val suggestedGroupId: Long? = null,
    val selectedItemIndexForConfig: Int? = null,
    val error: String? = null,
    val stalePromptItemIndex: Int? = null
)

@HiltViewModel
class NewReceiptViewModel @Inject constructor(
    private val receiptsDao: ReceiptsDao,
    private val balancesDao: BalancesDao,
    private val inventoryDao: InventoryDao,
    private val auditDao: AuditDao,
    private val peopleDao: PeopleDao,
    private val groupsDao: GroupsDao,
    private val groupMemoryRepository: GroupMemoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewReceiptUiState())
    val uiState: StateFlow<NewReceiptUiState> = _uiState.asStateFlow()

    init {
        checkGroupMemoryForMerchant(_uiState.value.merchant)
    }

    fun updateMerchant(merchant: String) {
        _uiState.update { it.copy(merchant = merchant, error = null) }
        checkGroupMemoryForMerchant(merchant)
    }

    private fun checkGroupMemoryForMerchant(merchant: String) {
        viewModelScope.launch {
            val suggestedId = groupMemoryRepository.getSuggestedGroupForMerchant(merchant)
            _uiState.update { it.copy(suggestedGroupId = suggestedId) }
        }
    }

    fun updateDate(date: String) {
        _uiState.update { it.copy(date = date) }
    }

    fun updatePaidBy(paidBy: String?) {
        _uiState.update { it.copy(paidBy = paidBy, error = null) }
    }

    fun addItem() {
        _uiState.update { state ->
            state.copy(
                items = state.items + DraftItem(name = "New item", priceInKobo = 0),
                error = null
            )
        }
    }

    fun updateItemName(index: Int, name: String) {
        _uiState.update { state ->
            val updated = state.items.toMutableList()
            if (index in updated.indices) {
                updated[index] = updated[index].copy(name = name)
            }
            state.copy(items = updated, error = null)
        }
    }

    fun updateItemPrice(index: Int, newPriceInKobo: Long) {
        _uiState.update { state ->
            val updated = state.items.toMutableList()
            var promptIdx: Int? = null
            if (index in updated.indices) {
                val currentItem = updated[index]
                if (currentItem.isSplitConfirmed && currentItem.destination == ItemDestination.SPLIT && currentItem.priceInKobo != newPriceInKobo) {
                    updated[index] = currentItem.copy(
                        priceInKobo = newPriceInKobo,
                        isStale = true,
                        staleOriginalPriceKobo = currentItem.priceInKobo
                    )
                    promptIdx = index
                } else {
                    updated[index] = currentItem.copy(priceInKobo = newPriceInKobo)
                }
            }
            state.copy(items = updated, error = null, stalePromptItemIndex = promptIdx)
        }
    }

    fun dismissStalePrompt() {
        _uiState.update { it.copy(stalePromptItemIndex = null) }
    }

    fun updateItemNote(index: Int, note: String) {
        _uiState.update { state ->
            val updated = state.items.toMutableList()
            if (index in updated.indices) {
                updated[index] = updated[index].copy(note = note)
            }
            state.copy(items = updated)
        }
    }

    fun setItemDestination(index: Int, destination: ItemDestination) {
        _uiState.update { state ->
            val updated = state.items.toMutableList()
            if (index in updated.indices) {
                val item = updated[index]
                updated[index] = item.copy(
                    destination = destination,
                    isSplitConfirmed = if (destination != ItemDestination.SPLIT) false else item.isSplitConfirmed
                )
            }
            state.copy(items = updated, selectedItemIndexForConfig = index)
        }
    }

    fun updateStockConfig(index: Int, category: String, quantity: Int, threshold: Int) {
        _uiState.update { state ->
            val updated = state.items.toMutableList()
            if (index in updated.indices) {
                updated[index] = updated[index].copy(
                    category = category,
                    stkQuantity = quantity,
                    stkThreshold = threshold,
                    destination = ItemDestination.STOCK
                )
            }
            state.copy(items = updated, selectedItemIndexForConfig = null)
        }
    }

    fun applySplitConfig(
        splitType: SplitType,
        assignedPeople: List<String>,
        weights: Map<String, Float>,
        groupId: Long? = null
    ) {
        _uiState.update { state ->
            val updated = state.items.toMutableList()
            for (i in updated.indices) {
                if (updated[i].destination == ItemDestination.SPLIT) {
                    val item = updated[i]
                    val price = item.priceInKobo
                    val allocations = computeAllocations(price, splitType, assignedPeople, weights)
                    updated[i] = item.copy(
                        splitType = splitType,
                        assignedPeople = assignedPeople,
                        customWeights = weights,
                        splitAllocations = allocations,
                        isSplitConfirmed = true,
                        isStale = false,
                        staleOriginalPriceKobo = null
                    )
                }
            }
            state.copy(items = updated, error = null)
        }

        if (groupId != null && _uiState.value.merchant.isNotBlank()) {
            viewModelScope.launch {
                groupMemoryRepository.saveGroupMemory(_uiState.value.merchant, groupId)
            }
        }
    }

    fun computeAllocations(
        totalAmount: Long,
        splitType: SplitType,
        people: List<String>,
        weights: Map<String, Float>
    ): Map<String, Long> {
        if (people.isEmpty()) return emptyMap()
        val result = mutableMapOf<String, Long>()

        when (splitType) {
            SplitType.EQUAL -> {
                val base = totalAmount / people.size
                val remainder = (totalAmount % people.size).toInt()
                people.forEachIndexed { idx, person ->
                    result[person] = base + if (idx < remainder) 1 else 0
                }
            }
            SplitType.PERCENT -> {
                val totalWeight = people.sumOf { (weights[it] ?: 0f).toDouble() }.toFloat()
                val effectiveTotal = if (totalWeight <= 0f) 100f else totalWeight
                var allocated = 0L
                people.forEachIndexed { idx, person ->
                    val w = weights[person] ?: (100f / people.size)
                    val amt = if (idx == people.size - 1) {
                        totalAmount - allocated
                    } else {
                        Math.round(totalAmount * (w / effectiveTotal)).toLong()
                    }
                    result[person] = amt
                    allocated += amt
                }
            }
            SplitType.EXACT -> {
                people.forEach { person ->
                    val exactAmountKobo = Math.round((weights[person] ?: 0f) * 100).toLong()
                    result[person] = exactAmountKobo
                }
            }
            SplitType.SHARES -> {
                val totalShares = people.sumOf { (weights[it] ?: 1f).toDouble() }.toFloat()
                val effectiveShares = if (totalShares <= 0f) people.size.toFloat() else totalShares
                var allocated = 0L
                people.forEachIndexed { idx, person ->
                    val s = weights[person] ?: 1f
                    val amt = if (idx == people.size - 1) {
                        totalAmount - allocated
                    } else {
                        Math.round(totalAmount * (s / effectiveShares)).toLong()
                    }
                    result[person] = amt
                    allocated += amt
                }
            }
        }
        return result
    }

    fun saveReceipt(mode: String, role: String, onSaved: (Long) -> Unit) {
        val state = _uiState.value
        if (state.merchant.isBlank()) {
            _uiState.update { it.copy(error = "Add a merchant name.") }
            return
        }
        if (state.items.isEmpty()) {
            _uiState.update { it.copy(error = "Add at least one item.") }
            return
        }
        if (state.items.any { it.name.isBlank() }) {
            _uiState.update { it.copy(error = "Every item needs a name.") }
            return
        }
        if (state.items.any { it.priceInKobo <= 0 }) {
            _uiState.update { it.copy(error = "Every item needs a price above zero.") }
            return
        }
        if (mode != "BUSINESS" && state.items.any { it.destination == ItemDestination.SPLIT } && state.paidBy.isNullOrBlank()) {
            _uiState.update { it.copy(error = "Choose who paid before saving a split.") }
            return
        }

        viewModelScope.launch {
            val isPending = role == "staff" || role == "member"
            val displayDate = "Nov 13"
            val receiptId = receiptsDao.insertReceipt(
                ReceiptEntity(
                    merchant = state.merchant,
                    displayDate = displayDate,
                    isoDate = state.date,
                    mode = mode,
                    paidByPersonName = state.paidBy ?: "—",
                    isPending = isPending,
                    createdByRole = role
                )
            )

            state.items.forEach { draft ->
                // In Business mode, SPLIT is disabled; enforce STOCK or UNASSIGNED.
                val effectiveDestination = if (mode == "BUSINESS" && draft.destination == ItemDestination.SPLIT) {
                    ItemDestination.UNASSIGNED
                } else if (mode != "BUSINESS" && draft.destination == ItemDestination.STOCK) {
                    ItemDestination.UNASSIGNED
                } else {
                    draft.destination
                }

                val destCode = when (effectiveDestination) {
                    ItemDestination.UNASSIGNED -> "U"
                    ItemDestination.STOCK -> "K"
                    ItemDestination.SPLIT -> "S"
                }
                val assignedStr = if (effectiveDestination == ItemDestination.SPLIT) {
                    draft.assignedPeople.joinToString(", ")
                } else {
                    draft.category
                }

                val itemId = receiptsDao.insertReceiptItem(
                    ReceiptItemEntity(
                        receiptId = receiptId,
                        name = draft.name,
                        priceAmountKobo = draft.priceInKobo,
                        destination = destCode,
                        category = draft.category,
                        stkQuantity = draft.stkQuantity,
                        stkThreshold = draft.stkThreshold,
                        note = draft.note,
                        assignedPeopleOrGroup = assignedStr,
                        splitType = draft.splitType.name.lowercase(),
                        isStale = draft.isStale,
                        staleOriginalPriceKobo = draft.staleOriginalPriceKobo
                    )
                )

                if (effectiveDestination == ItemDestination.STOCK && mode == "BUSINESS") {
                    inventoryDao.insertInventoryItem(
                        InventoryEntity(
                            name = draft.name,
                            category = draft.category,
                            quantity = draft.stkQuantity,
                            threshold = draft.stkThreshold,
                            costBasisKobo = draft.priceInKobo
                        )
                    )
                }

                if (effectiveDestination == ItemDestination.SPLIT && mode != "BUSINESS") {
                    draft.splitAllocations.forEach { (person, amount) ->
                        receiptsDao.insertSplitAssignment(
                            ItemSplitAssignmentEntity(
                                receiptItemId = itemId,
                                personName = person,
                                amountKobo = amount
                            )
                        )

                        if (person != state.paidBy && state.paidBy != null) {
                            balancesDao.insertBalance(
                                BalanceEntity(
                                    mode = mode,
                                    debtorName = person,
                                    creditorName = state.paidBy,
                                    amountKobo = amount,
                                    itemCount = 1,
                                    isStale = draft.isStale
                                )
                            )
                        }
                    }
                }
            }

            auditDao.insertLog(AuditLogEntity(logMessage = "Nov 13 · Added receipt \"${state.merchant}\""))
            onSaved(receiptId)
        }
    }
}
