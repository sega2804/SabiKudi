package com.sabikudi.shared

import kotlinx.serialization.Serializable

@Serializable
enum class Permission {
    COST,          // see cost and sell prices on stock (Business)
    STK,           // see stock (Business staff)
    ADD_RECEIPTS,  // add receipts when the role could not otherwise (Shared members)
    OTH,           // see other people's receipts
    EXP,           // export data
    ALL,           // see everyone's balances (Shared)
}
