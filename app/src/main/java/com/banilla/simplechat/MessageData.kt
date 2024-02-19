package com.banilla.simplechat

data class MessageData(val user: String?, val encryptedMessage: String?) {
    constructor() : this("", "")
}