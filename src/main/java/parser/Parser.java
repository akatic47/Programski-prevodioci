package parser;

import lexer.token.Token;
import lexer.token.TokenType;

import java.text.ParseException;
import java.util.List;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    private Token currentToken() {
        return tokens.get(current);
    }

    private Token previousToken() {
        return tokens.get(current - 1);
    }

    private boolean isAtEnd() {
        return current >= tokens.size() || currentToken().type == TokenType.EOF;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previousToken();
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return currentToken().type == type;
    }


}