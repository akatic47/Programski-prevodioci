package parser;

import lexer.token.Token;
import lexer.token.TokenType;

import java.util.ArrayList;
import java.util.List;

public class Parser {

    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    // =====================================================
    // BASIC UTILITIES
    // =====================================================

    private Token currentToken() {
        return tokens.get(current);
    }

    private Token previousToken() {
        return tokens.get(current - 1);
    }

    private boolean isAtEnd() {
        return currentToken().type == TokenType.EOF;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previousToken();
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return currentToken().type == type;
    }

    private boolean match(TokenType... types) {
        for (TokenType t : types) {
            if (check(t)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw error(currentToken(), message);
    }

    private ParseException error(Token token, String msg) {
        return new ParseException(
                "Parse error at line " + token.line +
                        ", near '" + token.lexeme + "': " + msg
        );
    }

    // =====================================================
    // ENTRY POINT
    // =====================================================

    public AstNode parse() {
        return parseProgram();
    }

    // =====================================================
    // PROGRAM
    // =====================================================

    // program = { definicija_funkcije } glavni_blok ;
    private AstNode parseProgram() {
        List<AstNode> functions = new ArrayList<>();

        while (!check(TokenType.ZAPOCNI_IGRU) && !isAtEnd()) {
            functions.add(parseFunction());
        }

        AstNode main = parseMainBlock();

        return AstNode.node("Program", null,
                AstNode.list("Functions", functions),
                main
        );
    }

    // glavni_blok = "zapocni_igru" { naredba } "zavrsi_igru" ";" ;
    private AstNode parseMainBlock() {
        consume(TokenType.ZAPOCNI_IGRU, "Expected 'zapocni_igru'.");

        List<AstNode> statements = new ArrayList<>();
        while (!check(TokenType.ZAVRSI_IGRU) && !isAtEnd()) {
            statements.add(parseStatement());
        }

        consume(TokenType.ZAVRSI_IGRU, "Expected 'zavrsi_igru'.");
        consume(TokenType.SEMICOLON, "Expected ';' after zavrsi_igru.");

        return AstNode.node("MainBlock", null,
                AstNode.list("Statements", statements));
    }

    // =====================================================
    // FUNCTIONS
    // =====================================================

    // definicija_funkcije = tip identifikator "(" [lista_parametara] ")" blok ;
    private AstNode parseFunction() {
        AstNode type = parseType();

        Token name = consume(TokenType.IDENT, "Expected function name.");

        consume(TokenType.LPAREN, "Expected '('.");

        List<AstNode> params = new ArrayList<>();
        if (!check(TokenType.RPAREN)) {
            params = parseParamList();
        }

        consume(TokenType.RPAREN, "Expected ')'.");

        AstNode block = parseBlock();

        return AstNode.node("Function", name,
                type,
                AstNode.list("Params", params),
                block
        );
    }

    // tip = "broj" | "realan" | "slovo" | "tekst" | "pogodak" | "niz" ;
    private AstNode parseType() {
        if (match(TokenType.BROJ, TokenType.REALAN, TokenType.SLOVO,
                TokenType.TEKST, TokenType.POGODAK, TokenType.NIZ)) {
            return AstNode.leaf("Type", previousToken());
        }
        throw error(currentToken(), "Expected type name.");
    }

    private List<AstNode> parseParamList() {
        List<AstNode> params = new ArrayList<>();

        params.add(parseParam());

        while (match(TokenType.SEPARATOR_COMMA)) {
            params.add(parseParam());
        }
        return params;
    }

    private AstNode parseParam() {
        AstNode type = parseType();
        Token name = consume(TokenType.IDENT, "Expected parameter name.");
        return AstNode.node("Param", name, type);
    }

    // =====================================================
    // BLOCK
    // =====================================================

    // blok = "{" { naredba } "}"
    private AstNode parseBlock() {
        consume(TokenType.LBRACE, "Expected '{'.");

        List<AstNode> stmts = new ArrayList<>();

        while (!check(TokenType.RBRACE) && !isAtEnd()) {
            stmts.add(parseStatement());
        }

        consume(TokenType.RBRACE, "Expected '}'.");
        return AstNode.list("Block", stmts);
    }

    // =====================================================
    // STATEMENTS
    // =====================================================

    private AstNode parseStatement() {

        if (match(TokenType.MOJ)) return parseVarDecl(false);
        if (match(TokenType.MOJE)) return parseVarDecl(true);

        if (check(TokenType.AKO)) return parseIfStatement();

        if (check(TokenType.RADI)) return parseWhileStatement();

        if (check(TokenType.VRATI)) return parseReturnStatement();

        if (check(TokenType.NAPISI)) return parsePrintStatement();

        if (check(TokenType.UPISI)) return parseReadStatement();

        if (match(TokenType.SEMICOLON))
            return AstNode.node("EmptyStmt", previousToken());

        return parseAssignOrFunctionCall();
    }

    // =====================================================
    // DECLARATIONS
    // =====================================================

    private AstNode parseVarDecl(boolean mojeSlovo) {

        AstNode type;

        if (mojeSlovo) {
            consume(TokenType.SLOVO, "Expected 'slovo' after 'moje'.");
            type = AstNode.leaf("Type", previousToken());
        } else {
            type = parseType();
        }

        Token name = consume(TokenType.IDENT, "Expected variable name.");

        AstNode dims = null;
        if (check(TokenType.LBRACKET)) {
            dims = parseArrayDimensions();
        }

        AstNode initializer = null;
        if (match(TokenType.ASSIGN)) {
            if (mojeSlovo) {
                Token ch = consume(TokenType.CH_LIT, "Expected char literal.");
                initializer = AstNode.leaf("CharLiteral", ch);
            } else {
                initializer = parseExpression();
            }
        }

        consume(TokenType.SEMICOLON, "Expected ';'.");

        return AstNode.node("VarDecl", name, type, dims, initializer);
    }

    private AstNode parseArrayDimensions() {
        List<AstNode> dims = new ArrayList<>();

        do {
            consume(TokenType.LBRACKET, "Expected '['.");
            Token size = consume(TokenType.INT_LIT, "Expected array size.");
            consume(TokenType.RBRACKET, "Expected ']'.");
            dims.add(AstNode.leaf("ArraySize", size));
        } while (check(TokenType.LBRACKET));

        return AstNode.list("ArrayDims", dims);
    }

    // =====================================================
    // ASSIGN OR FUNCTION CALL
    // =====================================================

    private AstNode parseAssignOrFunctionCall() {

        Token name = consume(TokenType.IDENT, "Expected identifier.");

        if (match(TokenType.LPAREN)) {
            List<AstNode> args = new ArrayList<>();

            if (!check(TokenType.RPAREN)) {
                args = parseArgumentList();
            }

            consume(TokenType.RPAREN, "Expected ')'.");
            consume(TokenType.SEMICOLON, "Expected ';'.");

            return AstNode.node("FunctionCall", name,
                    AstNode.list("Args", args));
        }

        AstNode indices = null;
        if (check(TokenType.LBRACKET)) {
            indices = parseIndices();
        }

        consume(TokenType.ASSIGN, "Expected '='.");
        AstNode expr = parseExpression();
        consume(TokenType.SEMICOLON, "Expected ';'.");

        return AstNode.node("Assign", name, indices, expr);
    }

    private List<AstNode> parseArgumentList() {
        List<AstNode> args = new ArrayList<>();
        args.add(parseExpression());

        while (match(TokenType.SEPARATOR_COMMA)) {
            args.add(parseExpression());
        }
        return args;
    }

    private AstNode parseIndices() {
        List<AstNode> out = new ArrayList<>();

        do {
            consume(TokenType.LBRACKET, "Expected '['.");
            AstNode expr = parseExpression();
            consume(TokenType.RBRACKET, "Expected ']'.");
            out.add(expr);
        } while (check(TokenType.LBRACKET));

        return AstNode.list("Indices", out);
    }

    // =====================================================
    // IF / WHILE / RETURN / PRINT / READ
    // =====================================================

    private AstNode parseIfStatement() {
        consume(TokenType.AKO, "Expected 'ako'.");
        consume(TokenType.LPAREN, "Expected '(' after 'ako'.");
        AstNode cond = parseExpression();
        consume(TokenType.RPAREN, "Expected ')'.");
        AstNode thenBlock = parseBlock();

        AstNode elseBlock = null;
        if (match(TokenType.INACE)) {
            elseBlock = parseBlock();
        }

        return AstNode.node("IfStmt", null, cond, thenBlock, elseBlock);
    }

    private AstNode parseWhileStatement() {
        consume(TokenType.RADI, "Expected 'radi'.");
        consume(TokenType.LPAREN, "Expected '(' after radi.");
        AstNode cond = parseExpression();
        consume(TokenType.RPAREN, "Expected ')'.");
        AstNode body = parseBlock();
        return AstNode.node("WhileStmt", null, cond, body);
    }

    private AstNode parseReturnStatement() {
        Token kw = consume(TokenType.VRATI, "Expected 'vrati'.");
        AstNode expr = parseExpression();
        consume(TokenType.SEMICOLON, "Expected ';'.");
        return AstNode.node("ReturnStmt", kw, expr);
    }

    private AstNode parsePrintStatement() {
        Token kw = consume(TokenType.NAPISI, "Expected 'napisi'.");
        consume(TokenType.LPAREN, "Expected '('.");

        AstNode expr;
        if (check(TokenType.STR_LIT))
            expr = AstNode.leaf("StringLiteral", advance());
        else
            expr = parseExpression();

        consume(TokenType.RPAREN, "Expected ')'.");
        consume(TokenType.SEMICOLON, "Expected ';'.");
        return AstNode.node("PrintStmt", kw, expr);
    }

    private AstNode parseReadStatement() {
        Token kw = consume(TokenType.UPISI, "Expected 'upisi'.");
        consume(TokenType.LPAREN, "Expected '('.");
        Token ident = consume(TokenType.IDENT, "Expected identifier.");
        consume(TokenType.RPAREN, "Expected ')'.");
        consume(TokenType.SEMICOLON, "Expected ';'.");
        return AstNode.node("ReadStmt", kw,
                AstNode.leaf("Identifier", ident));
    }

    // =====================================================
    // EXPRESSION GRAMMAR (FULL)
    // =====================================================

    // izraz = log_ili
    private AstNode parseExpression() {
        return parseLogOr();
    }

    private AstNode parseLogOr() {
        AstNode expr = parseLogAnd();

        while (match(TokenType.ILI)) {
            Token op = previousToken();
            AstNode right = parseLogAnd();
            expr = AstNode.node("Binary", op, expr, right);
        }
        return expr;
    }

    private AstNode parseLogAnd() {
        AstNode expr = parseEquality();

        while (match(TokenType.I)) {
            Token op = previousToken();
            AstNode right = parseEquality();
            expr = AstNode.node("Binary", op, expr, right);
        }
        return expr;
    }

    private AstNode parseEquality() {
        AstNode expr = parseComparison();

        while (match(TokenType.EQ, TokenType.NEQ)) {
            Token op = previousToken();
            AstNode right = parseComparison();
            expr = AstNode.node("Binary", op, expr, right);
        }
        return expr;
    }

    private AstNode parseComparison() {
        AstNode expr = parseTerm();

        while (match(TokenType.LT, TokenType.LE, TokenType.GT, TokenType.GE)) {
            Token op = previousToken();
            AstNode right = parseTerm();
            expr = AstNode.node("Binary", op, expr, right);
        }
        return expr;
    }

    private AstNode parseTerm() {
        AstNode expr = parseFactor();

        while (match(TokenType.ADD, TokenType.SUBTRACT)) {
            Token op = previousToken();
            AstNode right = parseFactor();
            expr = AstNode.node("Binary", op, expr, right);
        }
        return expr;
    }

    private AstNode parseFactor() {
        AstNode expr = parseUnary();

        while (match(TokenType.MULTIPLY, TokenType.DIVIDE, TokenType.PERCENT)) {
            Token op = previousToken();
            AstNode right = parseUnary();
            expr = AstNode.node("Binary", op, expr, right);
        }
        return expr;
    }

    private AstNode parseUnary() {
        if (match(TokenType.NE)) {   // "!" operator
            Token op = previousToken();
            AstNode right = parseUnary();
            return AstNode.node("Unary", op, right);
        }
        return parsePrimary();
    }

    private AstNode parsePrimary() {

        if (match(TokenType.INT_LIT, TokenType.CH_LIT, TokenType.STR_LIT, TokenType.BOOL_LIT)) {
            return AstNode.leaf("Literal", previousToken());
        }

        if (match(TokenType.IDENT)) {

            Token ident = previousToken();

            if (match(TokenType.LPAREN)) {
                List<AstNode> args = new ArrayList<>();
                if (!check(TokenType.RPAREN)) {
                    args = parseArgumentList();
                }
                consume(TokenType.RPAREN, "Expected ')'.");

                return AstNode.node("FunctionCall", ident,
                        AstNode.list("Args", args));
            }

            if (check(TokenType.LBRACKET)) {
                AstNode idx = parseIndices();
                return AstNode.node("ArrayAccess", ident, idx);
            }

            return AstNode.leaf("Identifier", ident);
        }

        if (match(TokenType.LPAREN)) {
            AstNode expr = parseExpression();
            consume(TokenType.RPAREN, "Expected ')'.");
            return expr;
        }

        throw error(currentToken(), "Unexpected token in expression.");
    }
}
