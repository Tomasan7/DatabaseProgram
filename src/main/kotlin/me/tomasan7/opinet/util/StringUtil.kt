package me.tomasan7.opinet.util

fun String.trimAndCut(maxLength: Int) = this.trim().let { it.substring(0..it.length.coerceAtMost(maxLength) - 1) }
