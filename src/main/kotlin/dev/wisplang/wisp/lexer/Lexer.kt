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

    // region util
    private fun peek( offset: Int = 1 ) = tokens[i + offset]
    private fun atEof() = tokens[i].type == MatureType.EOF
    internal fun consume() = tokens[i++]

    private fun peekIs( type: MatureType, vararg values: String ) =
        peek( 0 ).type == type && ( values.isEmpty() || peek( 0 ).value in values )

    private fun consumeOrThrow( err: String, value: String? = null, vararg types: MatureType ): MatureToken {
        if ( peek(0).type in types && ( value == null || peek(0).value == value ) )
            return consume()
        else
            throw LexerException(err)
    }

    private fun consumeOrThrow( err: String, types: MatureType, vararg values: String ): MatureToken {
        if ( peek(0).type == types && ( values.isEmpty() || peek(0).value in values ) )
            return consume()
        else
            throw LexerException(err)
    }

    private fun consumeIfIs( type: MatureType, value: String ): Boolean {
        if ( peekIs( type, value ) ) {
            consume()
            return true
        }
        return false
    }

    private fun consumeIfIs( type: MatureType ): Boolean {
        if ( peekIs( type ) ) {
            consume()
            return true
        }
        return false
    }
    // endregion util

    @Suppress("unused", "ControlFlowWithEmptyBody")
    fun lex(tokens: List<MatureToken>): Root {
        val functions = HashMap<String, DefinedFunction>()
        val types = HashMap<String, DefinedType>()
        val globals = HashMap<String, DefinedVariable>()

        this.tokens = tokens
        this.i = 0

        do {
            // remove all newlines
            while( consumeIfIs( MatureType.NEWLINE ) ) { }

            match {
                on( MatureType.KEYWORD, "func" ) {
                    val func = parseFunction()
                    functions[func.name] = func
                }
                on( MatureType.KEYWORD, "type" ) {
                    val type = parseType()
                    types[type.name] = type
                }
                on( MatureType.KEYWORD, "var" ) {
                    val variabl = parseVariable()
                    globals[variabl.name] = variabl
                }
                default {
                    throw LexerException( "Expected `func`, `type` or `var` keywords, got $this" )
                }
            }
        } while (! atEof() )
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
        while ( i + 1 < tokens.size && peekIs( MatureType.NAME ) ) {
            params.add( parseVariable() )

            if ( peekIs( MatureType.NAME ) )
                throw LexerException("Expected `)` or `,` symbols after `param` in function declaration!")

            consumeIfIs( MatureType.SYMBOL, "," )
        }

        // )
        consumeOrThrow( "Expected `)` symbol after `params` in function declaration!", ")", MatureType.SYMBOL )

        // : i32
        val type = if ( consumeIfIs( MatureType.SYMBOL, ":" ) ) parseTypeReference( "function" ) else VoidType.Void

        // <body>
        val body = parseBlock()

        return DefinedFunction( name, type, params, body )
    }

    /**
     * Parsers a block declaration
     * ```
     * {
     *     -> paramName.int + 12
     * }
     * ```
     */
    private fun parseBlock(): Block {
        consumeOrThrow( "Expected `{` symbol to start a block!", "{", MatureType.SYMBOL )

        // -> paramName.int + 12
        val statements = ArrayList<Statement>()
        while ( i + 1 < tokens.size && !peekIs( MatureType.SYMBOL, "}" ) ) {
            statements.add( parseStatement() )
            consumeOrThrow( "Expected `newline` after `statement` in block!", MatureType.NEWLINE )
        }

        consumeOrThrow( "Expected `}` symbol after `body` in block!", "}", MatureType.SYMBOL )

        return Block( statements )
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
            if ( consumeIfIs( MatureType.SYMBOL, "=" ) )
                parseExpression()
            else
                null
        )
    }

    private fun parseTypeReference( where: String ) = BaseType.findType(
        consumeOrThrow(
            "Expected `name` or `primitive` after `:` symbol in $where declaration!",
            null,
            MatureType.PRIMITIVE,
            MatureType.NAME
        ).value
    )

    /**
     * Parsers an identifier/name
     * ```
     * name.a.f
     * ```
     */
    private fun parseIdentifier(): Identifier =
        Identifier(
            consume().value,
            if ( consumeIfIs( MatureType.SYMBOL, "." ) )
                parseIdentifier()
            else
                null
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
        // \n
        consumeIfIs( MatureType.NEWLINE )
        // variables
        val vars = ArrayList<DefinedVariable>()
        while ( i + 1 < tokens.size && peekIs( MatureType.NAME ) ) {
            vars.add( parseVariable() )
            consumeOrThrow( "Expected `newline` after `var` declaration", MatureType.NEWLINE )
        }
        // ]
        consumeOrThrow( "Expected `]` symbol after `newline` in type declaration!", "]", MatureType.SYMBOL )

        return DefinedType( name, vars )
    }

    /**
     * Parsers a statement
     * ```
     * -> paramName.int + 12
     * ```
     */
    private fun parseStatement(): Statement {
        var statement: Statement = ExpressionStatement( LiteralExpression("") )
        consumeIfIs(MatureType.NEWLINE)
        // Check if token is a keyword, a name, or return, else throw error
        match {
            on( MatureType.KEYWORD, "var" ) {
                statement = VarDefStatement( parseVariable() )
            }
            on( MatureType.KEYWORD, "if" ) {
                statement = parseIfChain()
            }
            on( MatureType.NAME ) {
                statement = parseAssign()
            }
            on( MatureType.SYMBOL, "->" ) {
                statement = ReturnStatement( parseExpression() )
            }
            on( MatureType.KEYWORD, "for" ) {
                statement = parseForLoop()
            }
            on( MatureType.KEYWORD, "while" ) {
                // TODO: Parse out while loop
            }
            on( MatureType.KEYWORD, "do" ) {
                // TODO: Parse out do-while loop
            }
            on( MatureType.KEYWORD, "imp" ) {
                // TODO: Parse out imp
            }
            default {
                throw LexerException("Expected keyword, name, or a return; but got '$this'")
            }
        }
        return statement
    }

    /**
     * Parsers an if/else chain
     * ```
     * for var i: i32 = 1, i < max, i++ {
     *    num = TestType[i num].add()
     * }
     * ```
     */
    private fun parseForLoop(): Statement {
        val variable = parseVariable()
        consumeOrThrow("Expected `,` symbol after `vardef` in for loop!", ",", MatureType.SYMBOL)
        val condition = parseExpression()
        consumeOrThrow("Expected `,` symbol after `condition` in for loop!", ",", MatureType.SYMBOL)
        val operation = parseExpression()
        consumeOrThrow("Expected `{` symbol after `updater` in for loop!", "{", MatureType.SYMBOL)
        val block = parseBlock()

        return ForStatement(
            variable,
            condition,
            operation,
            block
        )
    }

    /**
     * Parsers an if/else chain
     * ```
     * if number == 0 {
     *   -> 1
     * } [else statement|block]
     * ```
     */
    private fun parseIfChain(): Statement {
        val cond = parseExpression()
        val block = parseBlock()

        return IfStatement(
            cond,
            block,
            if ( consumeIfIs( MatureType.KEYWORD, "else" ) )
                if ( peekIs(MatureType.SYMBOL, "{") )
                    ElseStatement( parseBlock() )  // if else-block
                else
                    parseStatement() // if else-statement
            else
                null  // if
        )
    }

    /**
     * Parsers an assignment
     * ```
     * paramName.int = 12
     * ```
     */
    private fun parseAssign(): Statement {
        val id = parseIdentifier()
        consumeOrThrow("Expected `=` symbol after `name` in assign statement!", "=", MatureType.SYMBOL )
        return AssignStatement( id, parseExpression() )
    }

    /**
     * Parsers an expression
     * ```
     * paramName.int + 12
     * ```
     */
    private fun parseExpression() = equality()

    // region expressions
    private fun equality(): Expression {
        var expr = comparison()

        while ( peekIs( MatureType.SYMBOL, "==", "!=" ) )
            expr = BinaryExpression( expr, Operator.of( consume().value ), comparison() )

        return expr
    }

    private fun comparison(): Expression {
        var expr = term()

        while ( peekIs( MatureType.SYMBOL, ">", ">=", "<", "<=", "&&", "||" ) )
            expr = BinaryExpression( expr, Operator.of( consume().value ), term() )

        return expr
    }

    private fun term(): Expression {
        var expr = factor()

        while ( peekIs( MatureType.SYMBOL, "-", "+" ) )
            expr = BinaryExpression( expr, Operator.of( consume().value ), factor() )

        return expr
    }

    private fun factor(): Expression {
        var expr = unary()

        while ( peekIs( MatureType.SYMBOL, "/", "*", "%" ) )
            expr = BinaryExpression( expr, Operator.of( consume().value ), unary() )

        return expr
    }

    private fun unary(): Expression =
        if ( peekIs( MatureType.SYMBOL, "!", "-" ) )
            UnaryExpression( Operator.of( consume().value ), unary() )
        else
            primary()

    private fun primary(): Expression {
        var expression: Expression = LiteralExpression( "" )

        match {
            on( MatureType.INTEGER, MatureType.FLOAT, MatureType.STRING ) {
                expression = LiteralExpression( value )
            }
            on( MatureType.SYMBOL, "(" ) {
                val expr = equality()
                consumeOrThrow( "Expected `)` symbol after `expr` in grouped expression!", ")", MatureType.SYMBOL )
                expression = GroupedExpression( expr )
            }
            on( MatureType.NAME ) {
                i-- // needed as match always consumes a token
                val id = parseIdentifier()

                expression = if ( consumeIfIs( MatureType.SYMBOL, "(" ) ) {
                    val params = ArrayList<String>()
                    while ( i + 1 < tokens.size && peekIs( MatureType.NAME ) ) {
                        params.add( consume().value )
                        consumeOrThrow( "Expected `,` or `)` symbols after `name` in call expression", MatureType.SYMBOL, ",", ")" )
                    }
                    CallExpression( id, params )
                } else
                    NamedExpression( id )
            }
            default {
                throw LexerException( "Expected expression.." )
            }
        }

        return expression
    }
    // endregion expressions
}
