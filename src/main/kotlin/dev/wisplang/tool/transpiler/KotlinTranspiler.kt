package dev.wisplang.tool.transpiler

import dev.wisplang.wispc.ast.Root
import java.io.File

class KotlinTranspiler( root: Root, dir: File ) : Transpiler(root, dir.apply { mkdirs() }, ".kt") {

}
