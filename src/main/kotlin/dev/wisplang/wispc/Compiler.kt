package dev.wisplang.wispc

import dev.wisplang.tool.AstPrinter
import dev.wisplang.tool.transpiler.KotlinTranspiler
import dev.wisplang.wispc.util.Arguments
import dev.wisplang.wispc.util.TranspilationTarget.*
import dev.wisplang.tool.transpiler.JavaTranspiler
import dev.wisplang.tool.transpiler.PythonTranspiler
import dev.wisplang.wispc.ast.Root
import dev.wisplang.wispc.lexer.Lexer
import dev.wisplang.wispc.tokenizer.Tokenizer
import java.io.File
import kotlin.reflect.typeOf

class Compiler {
    private lateinit var root: Root
    private var errored = false

    // TODO: Decouple from Arguments
    fun lex() {
        val (tokenList, tokErrors) = Tokenizer.tokenize(Arguments.file.inputStream().readBytes().toString(Charsets.UTF_8))
        val matureTokens = Tokenizer.matureTokens(tokenList)
        val (root, lexErrors) = Lexer().lex(matureTokens, Arguments.file)

        this.root = root

        for (error in tokErrors + lexErrors)
            System.err.println("${error::class.simpleName} at line ${error.line} column ${error.col}: ${error.message}")
    }

    // TODO: What does the me need to do here?
    fun compile(): Nothing = TODO("Not yet implemented")

    fun runTool() {
        lex()
        if (!errored)
            when {
                Arguments.dumpTokens == true -> AstPrinter(root, Arguments.prettyPrint == true).print()
                Arguments.dumpAst == true -> {}
                Arguments.transpilationTarget != null -> when (Arguments.transpilationTarget) {
                    Java -> JavaTranspiler(root, File("./src/java/")).transpile()
                    Kotlin -> KotlinTranspiler(root, File("./src/kotlin")).transpile()
                    Python -> PythonTranspiler(root, File("./src/python")).transpile()
                    null -> throw IllegalStateException("How did we get here?")
                }

                else -> throw IllegalStateException("How did we get here?")
            }
    }
}
