package com.meetup.meetingapp.data.db.mapper

import com.meetup.meetingapp.data.db.entities.UserEntity
import com.meetup.meetingapp.data.model.User

object UserMapper {
    fun User.toEntity(): UserEntity =
        UserEntity(
            uid = uid,
            createdEventIds = createdEventIds,
            joinedEventIds = joinedEventIds,
        )

    fun UserEntity.toDomain(): User =
        User(
            uid = uid,
            createdEventIds = createdEventIds,
            joinedEventIds = joinedEventIds,
        )
}
