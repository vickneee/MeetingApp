package com.meetup.meetingapp.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ExampleEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String = ""
)
