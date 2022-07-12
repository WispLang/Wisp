package dev.wisplang.wisp.lexer

import dev.wisplang.wisp.LexerException
import dev.wisplang.wisp.ast.*
import dev.wisplang.wisp.tokenizer.MatureToken
import dev.wisplang.wisp.util.TokenMatch.match
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import dev.wisplang.wisp.tokenizer.MatureType


@Suppress("SameParameterValue")
class Lexer {
    private var tokens: List<MatureToken> = listOf()
    private var i: Int = 0

    private fun peek( offset: Int = 1 ) = tokens[i + offset]
    internal fun consume() = tokens[++i]

    private fun peekIs( type: MatureType, value: String? = null, offset: Int = 0 ) =
        peek( offset ).type == type && ( value == null || peek( offset ).value == value )

    private fun consumeOrThrow( err: String, vararg types: MatureType ) =
        if ( peek(0).type in types ) consume() else throw LexerException(err)

    private fun consumeOrThrow( err: String, value: String, vararg types: MatureType ): MatureToken {
        if ( peek(0).type in types && peek(0).value == value )
            return consume()
        else
            throw LexerException(err)
    }

    private fun consumeIfIs( type: MatureType, value: String? = null ): Boolean {
        if ( peekIs( type, value ) ) {
            consume()
            return true
        }
        return false
    }

    fun lex(tokens: List<MatureToken>): Root {
        val functions = HashMap<String, DefinedFunction>()
        val types = HashMap<String, DefinedType>()
        val globals = HashMap<String, DefinedVariable>()

        this.tokens = tokens
        this.i = 0

        do {
            match {
                on( MatureType.KEYWORD, "func" ) {
                    val func = parseFunction()
                    functions[func.name] = func
                }
                on(MatureType.KEYWORD, "type") {
                    val type = parseType()
                    types[type.name] = type
                }
                on(MatureType.KEYWORD, "var") {
                    val variabl = parseVariable()
                    globals[variabl.name] = variabl
                }
                default {
                    throw LexerException( "Expected `func`, `type` or `var` keywords, got $this" )
                }
            }
        } while (++i < tokens.size)
        return Root(types, globals, functions)
    }

    /**
     * Parsers a function declaration
     * ```
     * func Name(paramName: ParamType): i32 {
     *     -> paramName.int + 12
     * }
     * ```
     * NOTE: `func` has already been removed before this is called!
     */
    private fun parseFunction(): DefinedFunction {
        // check if the function name is specified
        // Name
        val name = consumeOrThrow( "Expected `name` after `func` keyword in function declaration!", MatureType.NAME ).value

        // (
        consumeOrThrow( "Expected `(` symbol after `name` in function declaration!", "(", MatureType.SYMBOL )

        // paramName: ParamType
        val params = mutableListOf<DefinedVariable>()
        while ( ++i < tokens.size && peekIs( MatureType.NAME ) ) {
            params.add( parseVariable() )

            if ( peekIs( MatureType.NAME ) )
                throw LexerException("Expected `)` or `,` symbols after `param` in function declaration!")

            consumeIfIs( MatureType.SYMBOL, "," )
        }

        // )
        consumeOrThrow( "Expected `)` symbol after `params` in function declaration!", ")", MatureType.SYMBOL )

        // : i32
        val type = if ( consumeIfIs( MatureType.SYMBOL, ":" ) ) parseTypeReference( "function" ) else VoidType.Void

        consumeOrThrow( "Expected `{` symbol after `header` in function declaration!", "{", MatureType.SYMBOL )

        // -> paramName.int + 12
        val statements = ArrayList<Statement>()
        while ( ++i < tokens.size && !peekIs( MatureType.SYMBOL, "}" ) ) {
            statements.add( parseStatement()!! )  // TODO: Remove after statements are impl
            consumeOrThrow( "Expected `newline` after `statement` in function declaration!", MatureType.NEWLINE )
        }

        consumeOrThrow( "Expected `}` symbol after `body` in function declaration!", "}", MatureType.SYMBOL )

        return DefinedFunction( name, type, params, Unit )
    }

    /**
     * Parsers a variable declaration
     *
     * `bar: u1`
     *
     * NOTE: `var` has already been removed before this is called!
     */
    private fun parseVariable(): DefinedVariable {
        // bar
        val name = consumeOrThrow( "Expected `name` for variable declaration!", MatureType.NAME ).value
        // :
        consumeOrThrow( "Expected `:` symbol after `name` in variable declaration!", ":", MatureType.SYMBOL )
        // u1
        val type = parseTypeReference( "variable" )

        return DefinedVariable(
            name,
            type,
            if ( peekIs( MatureType.SYMBOL, "=", 0 ) )
                parseExpression()
            else
                null
        )
    }

    private fun parseTypeReference( where: String ) =  BaseType.findType(
        consumeOrThrow(
            "Expected `name` or `primitive` after `:` symbol in $where declaration!",
            ":",
            MatureType.PRIMITIVE,
            MatureType.NAME
        ).value
    )

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
        while ( ++i < tokens.size && peekIs( MatureType.NAME ) ) {
            vars.add( parseVariable() )
            consumeOrThrow( "Expected `newline` after `var` declaration", MatureType.NEWLINE )
        }

        consumeOrThrow( "Expected `]` symbol after `newline` in type declaration!", "]", MatureType.SYMBOL ) // ]
        return DefinedType( name, vars )
    }

    /**
     * Parsers a statement
     * ```
     * -> paramName.int + 12
     * ```
     */
    fun parseStatement(): Statement? {
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

    // TODO: Replace with readable code
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
                     *      of the operator stack, and (o2 has greater precedence than o1,
                     *      or they have the same precedence and o1 is left-associative)
                     * ):
                     *      pop o2 from the operator stack into the output queue
                     * push o1 onto the operator stack
                     */
                    if (token.value in Statement) {
                        for (j in (operatorStack.size - 1)..0) {
                            if (operatorStack.isEmpty() || i < operatorStack.size)
                                break
                            val op2 = operatorStack[j].value
                            if (op2.sym == "(")
                                break
                            val op1 = Statement.of(token.value)
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
