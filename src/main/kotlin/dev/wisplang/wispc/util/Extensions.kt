@file:Suppress("NOTHING_TO_INLINE")

package dev.wisplang.wispc.util

import java.io.File
import java.util.*

fun <T> MutableList<T>.immutable(): List<T> = Collections.unmodifiableList(this)

operator fun File.div(path: String ) = this.resolve( path )

inline fun StringBuilder.appendLine(indent: Int, value: String) = appendLine("\t".repeat(indent) + value)

inline fun StringBuilder.append(indent: Int, value: String): StringBuilder = append("\t".repeat(indent) + value)

inline operator fun Regex.contains( char: Char ) = matches( char.toString() )

