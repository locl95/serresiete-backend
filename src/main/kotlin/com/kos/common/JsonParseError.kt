package com.kos.common

import com.kos.views.ControllerError

interface HttpError : ControllerError {
    fun error(): String
}

data class JsonParseError(val json: String, val path: String): HttpError {
    override fun error(): String = "ParsedJson: ${json}\nPath: $path"
}