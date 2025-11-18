package lexer;

import lexer.token.Token;
import lexer.token.TokenType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Lexer {

    private final ScannerCore sc;
    private final String source;
    private final List<Token> tokens = new ArrayList<>();

    private static final Map<String, TokenType> KEYWORDS = Map.ofEntries(
            Map.entry("broj", TokenType.BROJ),
            Map.entry("realan", TokenType.REALAN),
            Map.entry("slovo", TokenType.SLOVO),
            Map.entry("tekst", TokenType.TEKST),
            Map.entry("niz", TokenType.NIZ),
            Map.entry("pogodak", TokenType.POGODAK),
            Map.entry("moj", TokenType.MOJ),
            Map.entry("moje", TokenType.MOJE),
            Map.entry("zapocni_igru", TokenType.ZAPOCNI_IGRU),
            Map.entry("zavrsi_igru", TokenType.ZAVRSI_IGRU),
            Map.entry("ako", TokenType.AKO),
            Map.entry("ili", TokenType.ILI),
            Map.entry("i", TokenType.I),
            Map.entry("inace", TokenType.INACE),
            Map.entry("radi", TokenType.RADI),
            Map.entry("vrati", TokenType.VRATI),
            Map.entry("upisi", TokenType.UPISI),
            Map.entry("napisi", TokenType.NAPISI),
            Map.entry("tacno", TokenType.BOOL_LIT),
            Map.entry("netacno", TokenType.BOOL_LIT),
            Map.entry("enkriptuj", TokenType.ENKRIPTUJ),
            Map.entry("dekriptuj", TokenType.DEKRIPTUJ)
    );

    public Lexer(String source) {
        this.source = source;
        this.sc = new ScannerCore(source);
    }

    public List<Token> scanTokens() {
        while (!sc.isAtEnd()) {
            sc.beginToken();
            scanToken();
        }

        tokens.add(new Token(TokenType.EOF, "\0", null,
                sc.getLine(), sc.getCol(), sc.getCol()));

        return tokens;
    }


    private void scanToken() {
        char c = sc.advance();

        switch (c) {
            case '(' -> add(TokenType.LPAREN);
            case ')' -> add(TokenType.RPAREN);
            case '[' -> add(TokenType.LBRACKET);
            case ']' -> add(TokenType.RBRACKET);
            case '{' -> add(TokenType.LBRACE);
            case '}' -> add(TokenType.RBRACE);
            case ',' -> add(TokenType.SEPARATOR_COMMA);
            case ';' -> add(TokenType.SEMICOLON);
            case ':' -> add(TokenType.TYPE_COLON);

            case '+' -> add(TokenType.ADD);
            case '-' -> add(TokenType.SUBTRACT);
            case '*' -> add(TokenType.MULTIPLY);
            case '/' -> add(TokenType.DIVIDE);
            case '%' -> add(TokenType.PERCENT);

            case '<' -> add(sc.match('=') ? TokenType.LE : TokenType.LT);
            case '>' -> add(sc.match('=') ? TokenType.GE : TokenType.GT);
            case '=' -> add(sc.match('=') ? TokenType.EQ : TokenType.ASSIGN);
            case '!' -> add(sc.match('=') ? TokenType.NEQ : TokenType.NE);

            case '"' -> stringLiteral();
            case '\'' -> charLiteral();

            case '\n', ' ', '\r', '\t' -> { /* ignore whitespace */ }

            default -> {
                if (Character.isDigit(c)) number();
                else if (isIdentStart(c)) identifier();
                else throw error("Unexpected character");
            }
        }
    }




    private void number() {

        boolean isReal = false;

        while (Character.isDigit(sc.peek())) sc.advance();

        if (sc.peek() == '.' && Character.isDigit(sc.peekNext())) {
            isReal = true;
            sc.advance();
            while (Character.isDigit(sc.peek())) sc.advance();
        }

        String text = source.substring(sc.getStartIdx(), sc.getCur());

        if (Character.isAlphabetic(sc.peek()))
            throw error("Invalid numeric literal");

        if (isReal) {
            tokens.add(new Token(
                    TokenType.REAL_LIT,
                    text,
                    Double.parseDouble(text),
                    sc.getStartLine(), sc.getStartCol(), sc.getCol() - 1
            ));
        } else {
            tokens.add(new Token(
                    TokenType.INT_LIT,
                    text,
                    Integer.parseInt(text),
                    sc.getStartLine(), sc.getStartCol(), sc.getCol() - 1
            ));
        }
    }



    private void stringLiteral() {
        StringBuilder sb = new StringBuilder();

        while (!sc.isAtEnd() && sc.peek() != '"') {
            sb.append(sc.advance());
        }

        if (sc.isAtEnd())
            throw error("Unterminated string literal");

        sc.advance();

        String value = sb.toString();

        tokens.add(new Token(
                TokenType.STR_LIT,
                value,
                value,
                sc.getStartLine(), sc.getStartCol(), sc.getCol() - 1
        ));
    }



    private void charLiteral() {

        if (sc.isAtEnd())
            throw error("Unterminated char literal");

        char ch = sc.advance();

        if (sc.peek() != '\'')
            throw error("Char literal must contain exactly one character");

        sc.advance();

        tokens.add(new Token(
                TokenType.CH_LIT,
                String.valueOf(ch),
                ch,
                sc.getStartLine(), sc.getStartCol(), sc.getCol() - 1
        ));
    }



    private void identifier() {
        while (isIdentPart(sc.peek())) sc.advance();

        String text = source.substring(sc.getStartIdx(), sc.getCur());

        if (text.equals("tacno")) {
            tokens.add(new Token(TokenType.BOOL_LIT, text, true,
                    sc.getStartLine(), sc.getStartCol(), sc.getCol() - 1));
            return;
        }

        if (text.equals("netacno")) {
            tokens.add(new Token(TokenType.BOOL_LIT, text, false,
                    sc.getStartLine(), sc.getStartCol(), sc.getCol() - 1));
            return;
        }

        TokenType type = KEYWORDS.getOrDefault(text, TokenType.IDENT);

        tokens.add(new Token(type, text, null,
                sc.getStartLine(), sc.getStartCol(), sc.getCol() - 1));
    }


    private boolean isIdentStart(char c) {
        return Character.isLetter(c) || c == '_';
    }

    private boolean isIdentPart(char c) {
        return isIdentStart(c) || Character.isDigit(c);
    }


    private void add(TokenType type) {
        String lexeme = source.substring(sc.getStartIdx(), sc.getCur());
        tokens.add(new Token(
                type, lexeme, null,
                sc.getStartLine(), sc.getStartCol(), sc.getCol() - 1
        ));
    }


    private RuntimeException error(String msg) {
        String near = source.substring(
                sc.getStartIdx(),
                Math.min(sc.getCur(), source.length())
        );

        return new RuntimeException(
                "LEXER > " + msg +
                        " at " + sc.getStartLine() + ":" + sc.getStartCol() +
                        " near '" + near + "'"
        );
    }
}
