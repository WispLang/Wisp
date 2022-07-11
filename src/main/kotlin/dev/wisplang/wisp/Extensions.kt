package dev.wisplang.wisp

import java.util.*

fun <T> MutableList<T>.immutable(): List<T> = Collections.unmodifiableList( this )