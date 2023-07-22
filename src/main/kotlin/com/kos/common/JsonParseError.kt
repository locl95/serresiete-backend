package com.kos.common

data class JsonParseError(val json: String, val path: String) {
    fun error(): String = "ParsedJson: ${json}\nPath: $path"
}