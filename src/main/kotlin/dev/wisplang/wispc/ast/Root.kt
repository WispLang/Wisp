package dev.wisplang.wispc.ast

import java.io.File

data class Root(
    val file: File,
    val types: Map<String, DefinedType>,
    val globals: Map<String, DefinedVariable>,
    val functions: Map<String, DefinedFunction>
)
