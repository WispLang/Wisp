package dev.wisplang

import dev.wisplang.Tokenizer.MatureToken
import dev.wisplang.Tokenizer.MatureType
import dev.wisplang.ast.*

object Lexer {
    fun lex(tokens: List<MatureToken>): Root {
        var i = 0
        val functions = HashMap<String, DefinedFunction>()
        val types = HashMap<String, DefinedType>()
        val globals = HashMap<String, DefinedVariable>()
        do {
            val token = tokens[i]
            when (token.type) {
                MatureType.KEYWORD -> {
                    when (token.value) {
                        "func" -> {
                            val triple = parseFunction(i, tokens)
                            i = triple.second
                            functions[triple.third] = triple.first
                        }
                        "type" -> {
                            val triple = parseType(i, tokens)
                        }
                        "var" -> {
                            val triple = parseVariable(i, tokens)
                        }
                        else -> {}
                    }
                }
                else -> {}
            }
        } while (++i < tokens.size)
        return Root(types, globals, functions)
    }

    fun parseFunction(idx: Int, tokens: List<MatureToken>): Triple<DefinedFunction, Int, String> {
        var i = idx+1
        // check if the function name is specified
        var current = tokens[i]
        if (current.type != MatureType.NAME) throw Exception()
        val name = tokens[i].value
        // check if the function has parenthesis
        current = tokens[++i]
        if (current.value != "(") throw Exception()
        // parse out parameters
        val parameters = HashMap<String, BasicType>()
        while (tokens[++i].type == MatureType.NAME) {
            val paramName = tokens[i].value
            // check if the param has a type
            if (tokens[++i].value != ":") throw Exception()
            current = tokens[++i]
            // parse out the type
            var type: BasicType? = null
            when (current.type) {
                MatureType.PRIMITIVE ->
                    for (prim in PrimitiveTypes.values())
                        if (prim.name.lowercase() == current.value)
                            type = prim
                MatureType.NAME ->
                    type = DefinedTypeRef(current.value)
                else ->
                    throw Exception()
            }
            parameters[paramName] = type!!
            // check if a close paren
            if (tokens[++i].value == ")") break
            // check if comma
            if (tokens[i].value != ",") throw Exception()
        }
        var ret: BasicType? = null
        // check if function has return type and parse it out
        if (tokens[++i].value == ":") when (tokens[++i].type) {
            MatureType.PRIMITIVE ->
                for (prim in PrimitiveTypes.values())
                    if (prim.name.lowercase() == tokens[i].value)
                        ret = prim
            MatureType.NAME ->
                ret = DefinedTypeRef(current.value)
            else ->
                throw Exception()
        } else i--
        // check if function has open brace
        if (tokens[++i].value != "{") throw Exception()

        // TODO: parse out function body
        while (++i < tokens.size) {
            break
        }
        return Triple(DefinedFunction(ret, parameters, Unit), i, name)
    }
    fun parseVariable(idx: Int, tokens: List<MatureToken>): Triple<DefinedVariable?, Int, String> {
        var i = idx+1
        if (tokens[i].type != MatureType.NAME) throw Exception()
        val name = tokens[i].value
        do {

        } while (++i < tokens.size)
        return Triple(null, i, name)
    }
    fun parseType(idx: Int, tokens: List<MatureToken>): Triple<DefinedType? ,Int, String> {
        var i = idx+1
        if (tokens[i].type != MatureType.NAME) throw Exception()
        val name = tokens[i].value
        do {

        } while (++i < tokens.size)
        return Triple(null, i, name)
    }
}