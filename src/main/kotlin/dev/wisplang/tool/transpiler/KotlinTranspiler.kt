package dev.wisplang.tool.transpiler

import dev.wisplang.wispc.ast.*
import java.io.File

class KotlinTranspiler( root: Root, dir: File ) : Transpiler(root, dir.apply { mkdirs() }, ".kt") {
    override fun Root.transpile() {
        TODO("Not yet implemented")
    }

    override fun DefinedType.transpile(): File {
        TODO("Not yet implemented")
    }

    override fun DefinedVariable.transpile(): String {
        TODO("Not yet implemented")
    }

    override fun BaseType.transpile(): String {
        TODO("Not yet implemented")
    }

    override fun DefinedFunction.transpile(): String {
        TODO("Not yet implemented")
    }

}
