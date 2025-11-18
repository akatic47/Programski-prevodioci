package parser;

import lexer.token.Token;

public class ParseError extends RuntimeException{
    public final Token token;
    public final String message;

    public ParseError(Token token, String message) {
        super("Greška u parsiranju na liniji " + token.line +
                ", kolona " + token.colStart +
                " kod tokena '" + token.lexeme + "': " + message);
        this.token = token;
        this.message = message;
    }
}

