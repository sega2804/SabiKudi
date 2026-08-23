package com.crypticsamsara.sabikudi.core.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey


enum class PersonStatus { ACTIVE, ARCHIVED }

@Entity(tableName = "people")
data class Person(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val status: PersonStatus = PersonStatus.ACTIVE
)
