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

    fun parseLine(idx: Int, tokens: List<MatureToken>): Pair<Unit, Int> {
        var i = idx
        var token = tokens[i]

        // Check if token is a keyword, a name, or return, else TODO: throw error
        when (token.type) {
            MatureType.KEYWORD -> {
                when (token.value) {
                    "var" -> {
                        val local = parseVariable(idx, tokens)
                        // TODO: Finish local declaration
                    }
                    "for" -> {
                        // TODO: Parse out for loop
                    }
                    "while" -> {
                        // TODO: Parse out while loop
                    }
                    "if" -> {
                        // TODO: Parse out if, if/else, if/elseif, etc
                    }
                }
            }
            MatureType.NAME -> {
                // TODO: Parse out function calls vs variable assigns
            }
            MatureType.SYMBOL -> {
                if (token.value == "->") {
                    // TODO: Parse out expression
                } else {
                    // TODO: throw error
                }
            }
            else -> {
                // TODO: throw error
            }
        }

        // TODO: return the operator
        return Pair(Unit, i)
    }

    fun parseExpression(idx: Int, tokens: List<MatureToken>): Pair<Unit, Int> {
        var i = idx
        val outputQueue = ArrayList<Array<MatureToken>>()
        val operatorStack = ArrayList<Array<MatureToken>>()
        while (i < tokens.size) {
            val token = tokens[i]
            when (token.type) {
                MatureType.INTEGER, MatureType.FLOAT, MatureType.STRING ->
                    outputQueue.add(arrayOf(token))
                MatureType.NAME -> {
                    // TODO: parse out function calls vs variable calls
                }
                MatureType.SYMBOL -> {
                    when (token.value) {
                        //TODO: Figure out precedence for other operators (&&, ||, !)
                        "+", "-" -> {
                            // Precedence: 2
                            // TODO: Pop operators with higher precedence to output queue and push current to operator stack
                        }
                        "*", "/" -> {
                            // Precedence: 3
                            // TODO: Pop operators with higher precedence to output queue and push current to operator stack
                        }
                    }
                }
                else -> {
                    // TODO: throw error
                }
            }
            i++
        }

        // TODO: return the operator
        return Pair(Unit, idx)
    }
}