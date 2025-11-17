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
            Map.entry( "i", TokenType.I),
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
        tokens.add(new Token(TokenType.EOF, "\0", null, sc.getLine(), sc.getCol(), sc.getCol()));
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
            case '=' -> add(sc.match('=') ? TokenType.EQ:TokenType.ASSIGN);
            case '!' -> add(sc.match('=') ? TokenType.NEQ:TokenType.NE);
            case '"' -> string();
            case '\'' -> charLiteral();
            //case '\n' -> tokens.add(new Token(
            //        TokenType.NEWLINE, "\n", null, sc.getStartLine(), sc.getStartCol(), sc.getStartCol()
            //));
            case '\n' -> {}
            case ' ', '\r', '\t' -> {}
            default -> {
                if (Character.isDigit(c)) number();
                else if (isIdentStart(c)) identifier();
                else throw error("Unexpected character");
            }
        }

    }

    private void number() {
        while (Character.isDigit(sc.peek())) sc.advance();
        String text = source.substring(sc.getStartIdx(), sc.getCur());
        char nextChar = sc.peek();
        if (Character.isAlphabetic(nextChar)) {
            throw error("Error: Character in int literal");
        }
        addLiteralInt(text);
    }

    private void string() {
        StringBuilder literal = new StringBuilder();

        while (sc.peek() != '"' && !sc.isAtEnd()) {
            literal.append(sc.peek());
            sc.advance();
        }
        if (sc.isAtEnd()) throw error("Unterminated string literal");
        sc.advance();
        String text = literal.toString();
        if (literal.isEmpty()) {
            throw error("String literal must not be empty");
        }
        tokens.add(new Token(TokenType.STR_LIT, text, null,
                sc.getStartLine(), sc.getStartCol(), sc.getCol() - 1));

    }
    private void charLiteral(){
       if (sc.isAtEnd())
            throw error("Unterminated char literal");

        while (sc.peek() == ' ' && !sc.isAtEnd()) {
            sc.advance();
        }

        if (sc.isAtEnd())
            throw error("Unterminated char literal");
        char value = sc.peek();
        sc.advance();
        while (sc.peek() == ' ' && !sc.isAtEnd()) {
            sc.advance();
        }
        if (sc.peek() != '\'')
            throw error("Char literal must contain exactly one character");
        sc.advance();
        tokens.add(new Token(TokenType.CH_LIT, String.valueOf(value), null,
                sc.getStartLine(), sc.getStartCol(), sc.getCol() - 1));
        
    }

    private void identifier() {
        while (isIdentPart(sc.peek())) sc.advance();
        String text = source.substring(sc.getStartIdx(), sc.getCur());
        TokenType type = KEYWORDS.getOrDefault(text, TokenType.IDENT);
        add(type, text);
    }

    private boolean isIdentStart(char c) { return Character.isLetter(c) || c == '_'; }
    private boolean isIdentPart(char c)  { return isIdentStart(c) || Character.isDigit(c); }

    private void add(TokenType type) {
        String lex = source.substring(sc.getStartIdx(), sc.getCur());
        tokens.add(new Token(type, lex, null,
                sc.getStartLine(), sc.getStartCol(), sc.getCol() - 1));
    }

    private void add(TokenType type, String text) {
        tokens.add(new Token(type, text, null,
                sc.getStartLine(), sc.getStartCol(), sc.getCol() - 1));
    }

    private void addLiteralInt(String literal) {
        tokens.add(new Token(TokenType.INT_LIT, literal, Integer.valueOf(literal),
                sc.getStartLine(), sc.getStartCol(), sc.getCol() - 1));
    }




    private RuntimeException error(String msg) {
        String near = source.substring(sc.getStartIdx(), Math.min(sc.getCur(), source.length()));
        return new RuntimeException("LEXER > " + msg + " at " + sc.getStartLine() + ":" + sc.getStartCol() + " near '" + near + "'");
    }
}
