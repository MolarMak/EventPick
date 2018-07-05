package com.molarmak.eventpick.app

class Cache private constructor(var email: String? = null,
                                var password: String? = null,
                                var token: String? = null) {
    companion object {
        val instance = Cache()
    }
}