package com.kos.common

import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class WithLogger(val name: String) {
    val logger: Logger = LoggerFactory.getLogger(name)
}