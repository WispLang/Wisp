package dev.wisplang.tool.transpiler

import dev.wisplang.wispc.ast.Root
import sun.misc.Unsafe
import java.io.File

class PythonTranspiler( root: Root, dir: File ) : Transpiler( root, dir.apply { mkdirs() }, "py" ) {

}
