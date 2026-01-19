package com.skrainyukov.testmessenger.domain.model

data class User(
    val id: Long,
    val phone: String,
    val username: String,
    val name: String,
    val birthday: String? = null,
    val city: String? = null,
    val avatar: String? = null,
    val about: String? = null,
    val zodiacSign: ZodiacSign? = null
)

enum class ZodiacSign(val displayName: String) {
    ARIES("Овен"),
    TAURUS("Телец"),
    GEMINI("Близнецы"),
    CANCER("Рак"),
    LEO("Лев"),
    VIRGO("Дева"),
    LIBRA("Весы"),
    SCORPIO("Скорпион"),
    SAGITTARIUS("Стрелец"),
    CAPRICORN("Козерог"),
    AQUARIUS("Водолей"),
    PISCES("Рыбы");

    companion object {
        fun fromDate(date: String?): ZodiacSign? {
            if (date.isNullOrEmpty()) return null

            // Date format: yyyy-MM-dd or dd.MM.yyyy
            val parts = if (date.contains("-")) {
                date.split("-")
            } else {
                date.split(".")
            }

            if (parts.size != 3) return null

            val day = if (date.contains("-")) parts[2].toIntOrNull() else parts[0].toIntOrNull()
            val month = parts[1].toIntOrNull()

            if (day == null || month == null) return null

            return when (month) {
                1 -> if (day <= 19) CAPRICORN else AQUARIUS
                2 -> if (day <= 18) AQUARIUS else PISCES
                3 -> if (day <= 20) PISCES else ARIES
                4 -> if (day <= 19) ARIES else TAURUS
                5 -> if (day <= 20) TAURUS else GEMINI
                6 -> if (day <= 20) GEMINI else CANCER
                7 -> if (day <= 22) CANCER else LEO
                8 -> if (day <= 22) LEO else VIRGO
                9 -> if (day <= 22) VIRGO else LIBRA
                10 -> if (day <= 22) LIBRA else SCORPIO
                11 -> if (day <= 21) SCORPIO else SAGITTARIUS
                12 -> if (day <= 21) SAGITTARIUS else CAPRICORN
                else -> null
            }
        }
    }
}