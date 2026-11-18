package com.crypticsamsara.sabikudi.ui.screens.receipt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crypticsamsara.sabikudi.data.local.dao.*
import com.crypticsamsara.sabikudi.data.local.entities.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReceiptDetailUiState(
    val receipt: ReceiptEntity? = null,
    val items: List<ReceiptItemEntity> = emptyList(),
    val isAddedToBalance: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ReceiptDetailViewModel @Inject constructor(
    private val receiptsDao: ReceiptsDao,
    private val balancesDao: BalancesDao,
    private val auditDao: AuditDao,
    private val inventoryDao: InventoryDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReceiptDetailUiState())
    val uiState: StateFlow<ReceiptDetailUiState> = _uiState.asStateFlow()

    fun loadReceipt(receiptId: Long) {
        viewModelScope.launch {
            val receipt = receiptsDao.getReceiptById(receiptId)
            val effectiveReceipt = receipt ?: ReceiptEntity(
                id = receiptId,
                merchant = "Shoprite Lekki",
                displayDate = "Nov 12, 2026",
                isoDate = "2026-11-12",
                mode = "SHARED",
                paidByPersonName = "Chidi",
                isPending = false,
                createdByRole = "owner",
                status = "approved"
            )

            viewModelScope.launch {
                receiptsDao.getItemsForReceipt(receiptId).collect { items ->
                    val effectiveItems = if (items.isEmpty()) {
                        listOf(
                            ReceiptItemEntity(receiptId = receiptId, name = "Rice 5kg", priceAmountKobo = 850000L, destination = "S", assignedPeopleOrGroup = "Ada, Chidi"),
                            ReceiptItemEntity(receiptId = receiptId, name = "Chicken", priceAmountKobo = 1240000L, destination = "S", assignedPeopleOrGroup = "Flatmates")
                        )
                    } else {
                        items
                    }
                    _uiState.update { it.copy(receipt = effectiveReceipt, items = effectiveItems, isAddedToBalance = !effectiveReceipt.isPending) }
                }
            }
        }
    }

    fun addToBalance(onComplete: () -> Unit) {
        val state = _uiState.value
        val receipt = state.receipt ?: return
        viewModelScope.launch {
            val updated = receipt.copy(isPending = false, status = "approved")
            receiptsDao.updateReceipt(updated)

            auditDao.insertLog(AuditLogEntity(logMessage = "Approved receipt \"${receipt.merchant}\" and added to balances"))

            state.items.forEach { item ->
                if (item.destination == "S") {
                    balancesDao.insertBalance(
                        BalanceEntity(
                            mode = receipt.mode,
                            debtorName = "Ada",
                            creditorName = receipt.paidByPersonName,
                            amountKobo = item.priceAmountKobo,
                            itemCount = 1
                        )
                    )
                } else if (item.destination == "K") {
                    inventoryDao.insertInventoryItem(
                        InventoryEntity(
                            name = item.name,
                            category = item.category,
                            quantity = item.stkQuantity,
                            threshold = item.stkThreshold,
                            costBasisKobo = item.priceAmountKobo
                        )
                    )
                }
            }

            _uiState.update { it.copy(isAddedToBalance = true) }
            onComplete()
        }
    }
}
