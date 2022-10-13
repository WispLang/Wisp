package tool

import dev.wisplang.wispc.Compiler
import dev.wisplang.wispc.util.Arguments

fun main(argv: Array<String>) {
    Arguments.parse( argv )
    val compiler = Compiler()
    compiler.lex()

    if ( Arguments.dumpAst == true || Arguments.dumpTokens == true || Arguments.transpilationTarget != null )
        compiler.runTool()
    else
        compiler.compile()
}
