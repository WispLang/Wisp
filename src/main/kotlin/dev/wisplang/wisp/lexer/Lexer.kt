package dev.wisplang.wisp.lexer

import dev.wisplang.wisp.LexerException
import dev.wisplang.wisp.ast.*
import dev.wisplang.wisp.tokenizer.MatureToken
import dev.wisplang.wisp.tokenizer.MatureType
import dev.wisplang.wisp.util.TokenMatch.match


@Suppress("SameParameterValue")
class Lexer {
    private lateinit var tokens: List<MatureToken>
    private var i: Int = 0

    // region util
    private fun peek(offset: Int = 1) = tokens[i + offset]
    private fun atEof() = tokens[if (i >= tokens.size) tokens.size - 1 else i].type == MatureType.EOF
    internal fun consume() = tokens[i++]
    private fun pos() = "[${peek(0).line}:${peek(0).col}]"
    override fun toString() = "Lexer{ i=$i, tokens=$tokens }"

    private fun peekIs(type: MatureType, vararg values: String) =
        peek(0).type == type && (values.isEmpty() || peek(0).value in values)

    private fun peekIs(type: MatureType, ignoreNewlines: Boolean = false, vararg values: String): Boolean {
        var offset = 0

        if (ignoreNewlines)
            while (peek(offset).type == MatureType.NEWLINE)
                offset += 1

        return peek(offset).type == type && (values.isEmpty() || peek(offset).value in values)
    }

    private fun consumeOrThrow(err: String, value: String? = null, vararg types: MatureType): MatureToken {
        if (peek(0).type in types && (value == null || peek(0).value == value))
            return consume()
        else
            throw LexerException("${pos()} $err")
    }

    private fun consumeOrThrow(err: String, types: MatureType, vararg values: String): MatureToken {
        if (peek(0).type == types && (values.isEmpty() || peek(0).value in values))
            return consume()
        else
            throw LexerException("${pos()} $err")
    }

    private fun consumeIfIs(type: MatureType, value: String): Boolean {
        if (peekIs(type, value)) {
            consume()
            return true
        }
        return false
    }

    private fun consumeIfIs(type: MatureType): Boolean {
        if (peekIs(type)) {
            consume()
            return true
        }
        return false
    }
    // endregion util

    @Suppress("unused", "ControlFlowWithEmptyBody")
    fun lex( tokens: List<MatureToken>, file: String ): Root {
        val functions = HashMap<String, DefinedFunction>()
        val globals = HashMap<String, DefinedVariable>()
        val types = HashMap<String, DefinedType>()
        val imports = ArrayList<String>()

        this.tokens = tokens
        this.i = 0

        do {
            // remove all newlines
            while (consumeIfIs(MatureType.NEWLINE)) {
            }

            match {
                on(MatureType.KEYWORD, "func") {
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
                on(MatureType.KEYWORD, "imp") {
                    if (globals.isNotEmpty() || types.isNotEmpty() || functions.isNotEmpty())
                        throw LexerException("${pos()} Imports can only happen on top of a file.")

                    val import = parseImport()
                    imports.add(import)
                }
                on(MatureType.EOF) { }
                default {
                    throw LexerException("${pos()} Expected `func`, `type` or `var` keywords, got $this")
                }
            }
        } while (!atEof())

        return Root( file, types, globals, functions)
    }

    /**
     * Parsers an import statement
     * ```
     * imp "path/to/file.wsp"
     * ```
     */
    private fun parseImport() =
        consumeOrThrow("Expected `path` after `imp` keyword!", MatureType.STRING).value

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
        val name =
            consumeOrThrow("Expected `name` after `func` keyword in function declaration!", MatureType.NAME).value

        // (
        consumeOrThrow("Expected `(` symbol after `name` in function declaration!", "(", MatureType.SYMBOL)

        // paramName: ParamType
        val params = mutableListOf<DefinedVariable>()
        while (i + 1 < tokens.size && peekIs(MatureType.NAME)) {
            params.add(parseVariable())

            if (peekIs(MatureType.NAME))
                throw LexerException("${pos()} Expected `)` or `,` symbols after `param` in function declaration!")

            consumeIfIs(MatureType.SYMBOL, ",")
        }

        // )
        consumeOrThrow("Expected `)` symbol after `params` in function declaration!", ")", MatureType.SYMBOL)

        // : i32
        val type = if (consumeIfIs(MatureType.SYMBOL, ":"))
            parseTypeReference("function")
        else
            VoidType.Void

        // <body>
        val body = parseBlock()

        return DefinedFunction(name, type, params, body)
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
        consumeOrThrow("Expected `{` symbol to start a block!", "{", MatureType.SYMBOL)

        // -> paramName.int + 12
        val statements = ArrayList<Statement>()
        while (i + 1 < tokens.size && !peekIs(MatureType.SYMBOL, true, "}")) {
            statements.add(parseStatement())
            consumeOrThrow("Expected `newline` after `statement` in block!", MatureType.NEWLINE)
        }
        consumeIfIs(MatureType.NEWLINE)
        consumeIfIs(MatureType.NEWLINE)

        consumeOrThrow("Expected `}` symbol after `body` in block!", "}", MatureType.SYMBOL)

        return Block(statements)
    }

    /**
     * Parsers a variable declaration
     *
     * `bar: u1`
     *
     * NOTE: `var` has already been removed before this is called!
     */
    private fun parseVariable(): DefinedVariable {
        consumeIfIs(MatureType.KEYWORD, "var")
        // bar
        val name = consumeOrThrow("Expected `name` for variable declaration!", MatureType.NAME).value
        // :
        consumeOrThrow("Expected `:` symbol after `name` in variable declaration!", ":", MatureType.SYMBOL)
        // u1
        val type = parseTypeReference("variable")

        return DefinedVariable(
            name,
            type,
            if (consumeIfIs(MatureType.SYMBOL, "="))
                parseExpression()
            else
                null
        )
    }

    private fun parseTypeReference(where: String) = BaseType.findType(
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
            if (consumeIfIs(MatureType.SYMBOL, "."))
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
        consumeOrThrow("Expected `[` symbol after `name` in type declaration!", "[", MatureType.SYMBOL)
        // \n
        consumeIfIs(MatureType.NEWLINE)
        // variables
        val vars = ArrayList<DefinedVariable>()
        while (i + 1 < tokens.size && peekIs(MatureType.NAME)) {
            vars.add(parseVariable())
            consumeOrThrow("Expected `newline` after `var` declaration", MatureType.NEWLINE)
        }
        // ]
        consumeOrThrow("Expected `]` symbol after `newline` in type declaration!", "]", MatureType.SYMBOL)

        return DefinedType(name, vars)
    }

    /**
     * Parsers a statement
     * ```
     * -> paramName.int + 12
     * ```
     */
    private fun parseStatement(): Statement {
        var statement: Statement = ExpressionStatement(LiteralExpression(LiteralType.String, ""))
        consumeIfIs(MatureType.NEWLINE)
        // Check if token is a keyword, a name, or return, else throw error
        match {
            on(MatureType.KEYWORD, "var") {
                statement = VarDefStatement(parseVariable())
            }
            on(MatureType.KEYWORD, "if") {
                statement = parseIfChain()
            }
            on(MatureType.NAME) {
                val isEqual = peekIs( MatureType.SYMBOL, "=" )
                i--
                statement = if ( isEqual )
                    parseAssign()
                else
                    ExpressionStatement( unary() )
            }
            on(MatureType.SYMBOL, "->") {
                statement = ReturnStatement(parseExpression())
            }
            on(MatureType.SYMBOL, "--", "++") {
                i--
                statement = ExpressionStatement( unary() )
            }
            on(MatureType.KEYWORD, "for") {
                statement = parseForLoop()
            }
            on(MatureType.KEYWORD, "while") {
                statement = parseWhileLoop()
            }
            on(MatureType.KEYWORD, "do") {
                statement = parseDoWhileLoop()
            }
            default {
                throw LexerException("${pos()} Expected keyword, name, or a return; but got '$this'")
            }
        }
        return statement
    }

    // region statements
    /**
     * Parsers a while loop
     * ```
     * while cond {
     *   // code
     * }
     * ```
     */
    private fun parseWhileLoop(): Statement {
        // cond
        val condition = parseExpression()
        val block = parseBlock()

        return WhileStatement(condition, block)
    }

    /**
     * Parsers a do-while loop
     * ```
     * do {
     *   // code
     * } while cond
     * ```
     */
    private fun parseDoWhileLoop(): Statement {
        // cond
        val block = parseBlock()
        consumeOrThrow("Expected `while` keyword after `}` in do-while loop!", "while", MatureType.KEYWORD)
        val condition = parseExpression()

        return DoWhileStatement(condition, block)
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
            if (consumeIfIs(MatureType.KEYWORD, "else"))
                if (peekIs(MatureType.SYMBOL, "{"))
                    ElseStatement(parseBlock())  // if else-block
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
        consumeOrThrow("Expected `=` symbol after `name` in assign statement!", "=", MatureType.SYMBOL)
        return AssignStatement(id, parseExpression())
    }
    // endregion statements

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

        while (peekIs(MatureType.SYMBOL, "==", "!="))
            expr = BinaryExpression(expr, Operator.of(consume().value), comparison())

        return expr
    }

    private fun comparison(): Expression {
        var expr = term()

        while (peekIs(MatureType.SYMBOL, ">", ">=", "<", "<=", "&&", "||"))
            expr = BinaryExpression(expr, Operator.of(consume().value), term())

        return expr
    }

    private fun term(): Expression {
        var expr = factor()

        while (peekIs(MatureType.SYMBOL, "-", "+"))
            expr = BinaryExpression(expr, Operator.of(consume().value), factor())

        return expr
    }

    private fun factor(): Expression {
        var expr = access()

        while (peekIs(MatureType.SYMBOL, "/", "*", "%"))
            expr = BinaryExpression(expr, Operator.of(consume().value), access())

        return expr
    }

    private fun access(): Expression {
        var expr = unary()

        while (consumeIfIs(MatureType.SYMBOL, "."))
            expr = BinaryExpression(expr, Operator.ACC, unary())

        return expr
    }

    private fun unary(): Expression =
        if (peekIs(MatureType.SYMBOL, "!", "-"))
            UnaryExpression(Operator.of(consume().value), unary())
        else if (peekIs(MatureType.SYMBOL, "--", "++"))
            UnaryExpression(Operator.of(consume().value), call())
        else {
            val expr = call()
            if (peekIs(MatureType.SYMBOL, "--", "++"))
                InverseUnaryExpression(expr, Operator.of(consume().value))
            else
                expr
        }

    private fun call(): Expression {
        var expression: Expression = primary()

        while (true) {
            expression = if (peekIs(MatureType.SYMBOL, "("))
                finishCall((expression as NamedExpression).name)
            else if (peekIs(MatureType.SYMBOL, "["))
                finishConstruct((expression as NamedExpression).name)
            else
                break
        }

        return expression
    }

    private fun finishCall(name: Identifier): Expression {
        val arguments = ArrayList<Expression>()

        if (consumeIfIs(MatureType.SYMBOL, "(")) {
            while (peekIs(MatureType.NAME) || consumeIfIs(MatureType.SYMBOL, ",")) {
                arguments.add(parseExpression())
            }
        }

        consumeOrThrow("Expect ')' after arguments.", MatureType.SYMBOL, ")")
        return CallExpression(NamedExpression(name), arguments)
    }

    private fun finishConstruct(name: Identifier): Expression {
        val arguments = ArrayList<Expression>()

        if (consumeIfIs(MatureType.SYMBOL, "[")) {
            do {
                arguments.add(parseExpression())
            } while (!peekIs(MatureType.SYMBOL, "]"))
        }

        consumeOrThrow("Expect ']' after arguments.", MatureType.SYMBOL, "]")
        return ConstructExpression(name, arguments)
    }

    private fun primary(): Expression {
        var expression: Expression = LiteralExpression(LiteralType.String, "")

        match {
            on(MatureType.INTEGER, MatureType.FLOAT) {
                expression = LiteralExpression( LiteralType.Number, value )
            }
            on(MatureType.STRING) {
                expression = LiteralExpression( LiteralType.String, value )
            }
            on(MatureType.SYMBOL, "(") {
                val expr = equality()
                consumeOrThrow("Expected `)` symbol after `expr` in grouped expression!", ")", MatureType.SYMBOL)
                expression = GroupedExpression(expr)
            }
            on(MatureType.NAME) {
                i-- // needed as match always consumes a token
                val id = parseIdentifier()
                expression = NamedExpression(id)
            }
            default {
                throw LexerException("${pos()} Expected expression, got $this..")
            }
        }

        return expression
    }
    // endregion expressions
}
