package com.fncompany.randomfootballteam.commons.utils

import com.fncompany.randomfootballteam.commons.utils.extensions.emptyString

enum class LanguageEnum(val country: String) {
    PT("BR"),
    EN(emptyString);

    companion object {
        fun get(name: String) = entries.find { it.name.lowercase() == name.lowercase() }
    }
}