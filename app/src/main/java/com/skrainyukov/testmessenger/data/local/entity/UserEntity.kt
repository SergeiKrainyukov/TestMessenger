package com.skrainyukov.testmessenger.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.skrainyukov.testmessenger.domain.model.User
import com.skrainyukov.testmessenger.domain.model.ZodiacSign

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Long,
    val phone: String,
    val username: String,
    val name: String,
    val birthday: String? = null,
    val city: String? = null,
    val avatar: String? = null,
    val about: String? = null
)

fun UserEntity.toDomain(): User {
    return User(
        id = id,
        phone = phone,
        username = username,
        name = name,
        birthday = birthday,
        city = city,
        avatar = avatar,
        about = about,
        zodiacSign = ZodiacSign.fromDate(birthday)
    )
}

fun User.toEntity(): UserEntity {
    return UserEntity(
        id = id,
        phone = phone,
        username = username,
        name = name,
        birthday = birthday,
        city = city,
        avatar = avatar,
        about = about
    )
}