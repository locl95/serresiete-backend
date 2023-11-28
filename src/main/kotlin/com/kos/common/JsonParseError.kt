package com.kos.common

interface HttpError {
    fun error(): String
}

data class JsonParseError(val json: String, val path: String): HttpError {
    override fun error(): String = "ParsedJson: ${json}\nPath: $path"
}