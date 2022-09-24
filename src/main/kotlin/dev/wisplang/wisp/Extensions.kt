package dev.wisplang.wisp

import java.io.File
import java.util.*

fun <T> MutableList<T>.immutable(): List<T> = Collections.unmodifiableList(this)

operator fun File.div(path: String ) = this.resolve( path )

