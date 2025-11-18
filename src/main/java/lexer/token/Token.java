package lexer.token;

public class Token {
    public final TokenType type;
    public final String lexeme;
    public final Object literal;
    public final int line, colStart, colEnd;

    public Token(TokenType type, String lexeme, Object literal,
                 int line, int colStart, int colEnd) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
        this.colStart = colStart;
        this.colEnd = colEnd;
    }

    @Override
    public String toString() {
        return (type + " '" + lexeme + "' lit=" + literal + " at line " + line)
                .replace("\n", "\\n")
                .replace("\0", "\\0");
    }

    public String formatted() {
        return TokenFormatter.format(this);
    }
}
