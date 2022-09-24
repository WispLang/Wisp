package dev.wisplang.transpiler

import dev.wisplang.wisp.lexer.Lexer
import dev.wisplang.wisp.tokenizer.Tokenizer
import java.io.File

fun main(args: Array<String>) {
    val tokenList = Tokenizer.tokenize( File( args[0] ).inputStream().readBytes().toString(Charsets.UTF_8) )
    val matureTokens = Tokenizer.matureTokens(tokenList)
    val root = Lexer().lex( matureTokens, args[0] )

    val path = File( "./src/java/" )

    path.mkdirs()

    Transpiler( root, path ).java()
}
