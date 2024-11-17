package io.github.arcaneplugins.polyconomy.api

import java.util.*

interface Economy {

    companion object {
        var instance: Economy? = null
            set(value) = if(field == null) {
                field = value
            } else {
                throw IllegalStateException("Economy instance is already set")
            }
            get() = Objects.requireNonNull(field, "Economy instance is not set yet")
    }

    

}