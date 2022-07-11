package dev.wisplang.wisp

import dev.wisplang.wisp.Tokenizer.MatureToken
import dev.wisplang.wisp.Tokenizer.MatureType
import dev.wisplang.wisp.ast.*

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
                            //i = triple.second
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
        if (current.type != MatureType.NAME)
            throw Exception()
        val name = tokens[i].value
        // check if the function has parenthesis
        current = tokens[++i]
        if (current.value != "(")
            throw Exception()
        // parse out parameters
        val parameters = HashMap<String, BasicType>()
        while (tokens[++i].type == MatureType.NAME) {
            val paramName = tokens[i].value
            // check if the param has a type
            if (tokens[++i].value != ":")
                throw Exception()
            current = tokens[++i]
            // parse out the type
            var type: BasicType? = null
            when (current.type) {
                MatureType.PRIMITIVE ->
                    for (prim in PrimitiveTypes.values())
                        if (prim.name.lowercase() == current.value)
                            type = prim
                MatureType.NAME -> type = DefinedTypeRef(current.value)
                else -> throw Exception()
            }
            parameters[paramName] = type!!
            // check if a close paren
            if (tokens[++i].value == ")")
                break
            // check if comma
            if (tokens[i].value != ",")
                throw Exception()
        }
        var ret: BasicType = VoidType.Void
        // check if function has return type and parse it out
        if (tokens[++i].value == ":")
            when (tokens[++i].type) {
                MatureType.PRIMITIVE ->
                    for (prim in PrimitiveTypes.values())
                        if (prim.name.lowercase() == tokens[i].value)
                            ret = prim
                MatureType.NAME -> ret = DefinedTypeRef(current.value)
                else -> throw Exception()
            }
        else
            i--
        // check if function has open brace
        if (tokens[++i].value != "{")
            throw Exception()

        // TODO: parse out function body
        while (++i < tokens.size) {

        }
        return Triple(DefinedFunction(ret, parameters, Unit), i, name)
    }
    fun parseVariable(idx: Int, tokens: List<MatureToken>): Triple<DefinedVariable?, Int, String> {
        var i = idx+1
        if (tokens[i].type != MatureType.NAME)
            throw Exception()
        val name = tokens[i].value
        var type: BasicType?
        if (tokens[++i].value == ":")
            when (tokens[++i].type) {
                MatureType.PRIMITIVE ->
                    for (prim in PrimitiveTypes.values())
                        if (prim.name.lowercase() == tokens[i].value)
                            type = prim
                MatureType.NAME -> type = DefinedTypeRef(tokens[i].value)
                else -> throw Exception()
            }
        else
            i--
        var op: Any
        if (tokens[++i].value == "=")
            op = parseExpression(++i, tokens)
        return Triple(null, i, name)
    }
    fun parseType(idx: Int, tokens: List<MatureToken>): Triple<DefinedType? ,Int, String> {
        var i = idx+1
        if (tokens[i].type != MatureType.NAME)
            throw Exception()
        val name = tokens[i].value
        do {

        } while (++i < tokens.size)
        return Triple(null, i, name)
    }

    fun parseLine(idx: Int, tokens: List<MatureToken>): Pair<Unit, Int> {
        var i = idx
        var token = tokens[i]

        // Check if token is a keyword, a name, or return, else throw error
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
            else -> throw LexerException("Expected keyword, name, or a return; but got '${token.type}'")
        }

        // TODO: return the operator
        return Pair(Unit, i)
    }

    fun parseExpression(idx: Int, tokens: List<MatureToken>): Pair<ArrayList<PrimitiveOperator>, Int> {
        var i = idx
        val outputQueue = ArrayList<PrimitiveOperator>()
        val operatorStack = ArrayList<PrimitiveOperator>()
        while (i < tokens.size) {
            val token = tokens[i]
            when (token.type) {
                MatureType.INTEGER, MatureType.FLOAT, MatureType.STRING -> outputQueue.add(PrimitiveOperator(token.value))
                MatureType.NAME -> {
                    // TODO: parse out function calls vs variable calls
                }
                MatureType.SYMBOL -> {
                    /* while (
                     *      there is an operator o2 other than the left parenthesis at the top
                     *      of the operator stack, and (o2 has greater precedence than o1
                     *      or they have the same precedence and o1 is left-associative)
                     * ):
                     *      pop o2 from the operator stack into the output queue
                     * push o1 onto the operator stack
                     */
                    if (token.value in operators) {
                        for (j in (operatorStack.size - 1)..0) {
                            if (operatorStack.isEmpty() || i < operatorStack.size)
                                break
                            val o2 = operatorStack[j]
                            if (o2.value == "(")
                                break
                            var o1Prec = 0
                            for (k in 0..precedence.size) {
                                if (token.value in precedence[k]) {
                                    o1Prec = k
                                    break
                                }
                            }
                            var o2Prec = 0
                            for (k in 0..precedence.size) {
                                if (o2.value in precedence[k]) {
                                    o2Prec = k
                                    break
                                }
                            }
                            if (o2Prec > o1Prec || (o2Prec > o1Prec && o2.value in rightAssociative))
                                outputQueue.add(operatorStack.removeLast())
                            else
                                break
                        }
                        operatorStack.add(PrimitiveOperator(token.value))
                    } else
                        break
                }
                MatureType.NEWLINE -> break
                else -> {
                    // TODO: throw error
                }
            }
            i++
        }
        if (operatorStack.isNotEmpty())
            for (j in (operatorStack.size-1)..0)
                outputQueue.add(operatorStack.removeLast())
        return Pair(outputQueue, idx)
    }

    // TODO: Replace with Operator enum
    val precedence = arrayOf(arrayOf("&&"), arrayOf("+","-"), arrayOf("*","/"), arrayOf("||"), arrayOf("!"))
    val rightAssociative = arrayOf("!")
    val operators = arrayOf("&&","+","-","*","/","||","!")

    data class PrimitiveOperator(
        val value: Operator,
        val operator: PrimitiveOperator? = null
    )

    enum class Operator( val sym: String, val precedence: Int, val rightAssociative: Boolean = false ) {
        And( "&&",0 ),
        Add( "+", 1 ),
        Sub( "-", 1 ),
        Mul( "*" ,2 ),
        Div( "/", 2 ),
        Or ( "||",3 ),
        Not( "!", 4, true );

        companion object {
            fun of( value: String ): Operator {
                for ( op in Operator.values() )
                    if ( op.sym == value )
                        return op
                throw IllegalStateException("Invalid operator: '$value'!")
            }
        }
    }
}