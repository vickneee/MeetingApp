package com.meetup.meetingapp.data.db.mapper

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.meetup.meetingapp.data.db.entities.UserEntity
import com.meetup.meetingapp.data.model.User

object UserMapper {

    private val gson = Gson()

    fun User.toEntity(): UserEntity = UserEntity(
        uid = uid,
        createdEventIdsJson = gson.toJson(createdEventIds),
        joinedEventIdsJson = gson.toJson(joinedEventIds)
    )

    fun UserEntity.toDomain(): User = User(
        uid = uid,
        createdEventIds = gson.fromJson(createdEventIdsJson, object : TypeToken<List<String>>() {}.type),
        joinedEventIds = gson.fromJson(joinedEventIdsJson, object : TypeToken<List<String>>() {}.type)
    )
}