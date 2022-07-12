package dev.wisplang.wisp.lexer

import dev.wisplang.wisp.LexerException
import dev.wisplang.wisp.tokenizer.Tokenizer.MatureToken
import dev.wisplang.wisp.tokenizer.Tokenizer.MatureType
import dev.wisplang.wisp.ast.*
import dev.wisplang.wisp.util.TokenMatch.match
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import dev.wisplang.wisp.ast.Ast.DefinedVariable
import dev.wisplang.wisp.ast.Ast.DefinedFunction
import dev.wisplang.wisp.ast.Ast.Root
import dev.wisplang.wisp.tokenizer.MatureType


class Lexer {
    private val functions = HashMap<String, DefinedFunction>()
    private val types = HashMap<String, DefinedType>()
    private val globals = HashMap<String, DefinedVariable>()

    private var tokens: List<MatureToken> = listOf()
    private var i: Int = 0

    private fun isType( type: MatureType ) = peek(0).type == type
    private fun peek( offset: Int = 1 ) = tokens[i + offset]
    internal fun consume() = tokens[++i]

    private fun peekIs( type: MatureType, value: String? = null, offset: Int = 1 ) =
        peek( offset ).type == type && ( value == null || peek( offset ).value == value )

    private fun consumeOrThrow( err: String, vararg types: MatureType ): MatureToken {
        if ( peek(0).type in types )
            return consume()
        else
            throw LexerException(err)
    }

    private fun consumeOrThrow( err: String, value: String, vararg types: MatureType ): MatureToken {
        if ( peek(0).type in types && peek(0).value == value )
            return consume()
        else
            throw LexerException(err)
    }

    private fun consumeIfType( type: MatureType ) {
        if ( peek(0).type == type )
            consume()
    }

    fun lex(tokens: List<MatureToken>): Root {
        this.tokens = tokens
        this.i = 0

        do {
            match {
                on( MatureType.KEYWORD, "func" ) {
                    val func = parseFunction()
                    functions[func.name] = func
                }
                on(MatureType.KEYWORD, "type") {
                    val triple = parseType()
                }
                on(MatureType.KEYWORD, "var") {
                    val triple = parseVariable()
                }
                default { }
            }
        } while (++i < tokens.size)
        return Root(types, globals, functions)
    }

    private fun parseFunction(): DefinedFunction {
        // check if the function name is specified
        var current = tokens[i]
        if (current.type != MatureType.NAME)
            throw LexerException("")
        val name = tokens[i].value
        // check if the function has parenthesis
        current = tokens[++i]
        if (current.value != "(")
            throw LexerException("")
        // parse out parameters
        val parameters = HashMap<String, BaseType>()
        while (tokens[++i].type == MatureType.NAME) {
            val paramName = tokens[i].value
            // check if the param has a type
            if (tokens[++i].value != ":")
                throw LexerException("")
            current = tokens[++i]
            // parse out the type
            var type: BaseType? = null
            when (current.type) {
                MatureType.PRIMITIVE ->
                    for (prim in PrimitiveTypes.values())
                        if (prim.name.lowercase() == current.value)
                            type = prim
                MatureType.NAME -> type = DefinedTypeRef(current.value)
                else -> throw LexerException("")
            }
            parameters[paramName] = type!!
            // check if a close paren
            if (tokens[++i].value == ")")
                break
            // check if comma
            if (tokens[i].value != ",")
                throw LexerException("")
        }
        var ret: BaseType = VoidType.Void
        // check if function has return type and parse it out
        if (tokens[++i].value == ":")
            when (tokens[++i].type) {
                MatureType.PRIMITIVE ->
                    for (prim in PrimitiveTypes.values())
                        if (prim.name.lowercase() == tokens[i].value)
                            ret = prim
                MatureType.NAME -> ret = DefinedTypeRef(current.value)
                else -> throw LexerException("")
            }
        else
            i--
        // check if function has open brace
        if (tokens[++i].value != "{")
            throw LexerException("")

        // TODO: parse out function body
        while (++i < tokens.size) {

        }
        return DefinedFunction(ret, parameters, Unit)
    }

    /**
     * Parsers a variable declaration
     *
     * `bar: u1`
     */
    private fun parseVariable(): DefinedVariable {
        // bar
        val name = consumeOrThrow( "Expected `name` for variable declaration!", MatureType.NAME ).value
        // :
        consumeOrThrow( "Expected `:` symbol after `name` in variable declaration!", ":", MatureType.SYMBOL )
        // u1
        val type = BaseType.findType(
            consumeOrThrow(
                "Expected `name` or `primitive` after `:` symbol in variable declaration!",
                ":",
                MatureType.PRIMITIVE,
                MatureType.NAME
            ).value
        )

        return DefinedVariable(
            name,
            type,
            if ( peekIs( MatureType.SYMBOL, "=", 0 ) )
                parseExpression()
            else
                null
        )
    }

    /**
     * Parsers a type structure
     * ```
     * type Name [
     *   foo: i64
     *   bar: u1
     * ]
     * ```
     * NOTE: `type` has already been removed before this is called!
     */
    private fun parseType(): DefinedType {
        // Name
        val name = consumeOrThrow("Expected `name` after `type` keyword!", MatureType.NAME).value
        // [
        consumeOrThrow( "Expected `[` symbol after `name` in type declaration!", "[", MatureType.SYMBOL )
        // variables
        val vars = ArrayList<DefinedVariable>()
        while ( ++i < tokens.size && isType( MatureType.NAME ) ) {
            vars.add( parseVariable()!! ) // TODO: Remove after adding variables
            consumeOrThrow( "Expected `newline` after `var` declaration", MatureType.NEWLINE )
        }

        consumeOrThrow( "Expected `]` symbol after `newline` in type declaration!", "]", MatureType.SYMBOL ) // ]
        return DefinedType( name, vars )
    }

    fun parseLine(): Operator? {
        // Check if token is a keyword, a name, or return, else throw error
        match {
            on( MatureType.KEYWORD, "var" ) {
                val local = parseVariable()
                // TODO: Finish local declaration
            }
            on( MatureType.KEYWORD, "for" ) {
                // TODO: Parse out for loop
            }
            on( MatureType.KEYWORD, "while" ) {
                // TODO: Parse out while loop
            }
            on( MatureType.KEYWORD, "if" ) {
                // TODO: Parse out if, if/else, if/elseif, etc
            }
            on( MatureType.NAME ) {
                // TODO: Parse out function calls vs variable assigns
            }
            on( MatureType.SYMBOL, "->" ) {
                // TODO: Parse out expression
            }
            on( MatureType.SYMBOL ) {
                // TODO: throw error
            }
            default {
                throw LexerException("Expected keyword, name, or a return; but got '${consume()}'")
            }
        }

        // TODO: return the operator
        return null
    }

    private fun parseExpression(): ArrayList<PrimitiveOperator> {
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
                    if (token.value in Operator) {
                        for (j in (operatorStack.size - 1)..0) {
                            if (operatorStack.isEmpty() || i < operatorStack.size)
                                break
                            val op2 = operatorStack[j].value
                            if (op2.sym == "(")
                                break
                            val op1 = Operator.of(token.value)
                            if (op2.precedence > op1.precedence || (op2.precedence > op1.precedence && op2.rightAssociative))
                                outputQueue.add(operatorStack.removeLast())
                            else
                                break
                        }
                        operatorStack.add(PrimitiveOperator(token.value))
                    } else
                        break
                }
                MatureType.NEWLINE -> break
                else -> throw IllegalStateException("Invalid token received: $token")
            }
            i++
        }
        if (operatorStack.isNotEmpty())
            for (j in (operatorStack.size - 1)..0)
                outputQueue.add(operatorStack.removeLast())
        return outputQueue
    }
}
