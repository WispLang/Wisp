package dev.wisplang.wispc

import dev.wisplang.wispc.util.Arguments
import dev.wisplang.wispc.util.TranspilationTarget.*
import tool.transpiler.JavaTranspiler
import java.io.File

class Compiler {
    fun lex() {
        TODO("Not yet implemented")
    }

    fun compile() {
        TODO("Not yet implemented")
    }

    fun runTool() {
        when {
            Arguments.dumpTokens == true -> { }
            Arguments.dumpAst == true -> { }
        }
        when ( Arguments.transpilationTarget ){
            Java -> JavaTranspiler( root, File("./src/java/").apply { mkdirs() } ).transpile()
            Kotlin -> TODO()
            Python -> TODO()
            null -> TODO()
        }
    }

    private val srcFolder: File = File( Arguments.folder )

}
