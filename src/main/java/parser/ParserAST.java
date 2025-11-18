package parser;

import lexer.token.Token;

import java.util.ArrayList;
import java.util.List;
import lexer.token.TokenType;
public class ParserAST {

    private final List<Token> tokens;
    private int current = 0;

    public ParserAST(List<Token> tokens) {
        this.tokens = tokens;
    }


    // Metoda za prepoznavanje tipa
    private Token consumeTip(String message) {
        if (match(TokenType.BROJ, TokenType.REALAN, TokenType.SLOVO, TokenType.TEKST, TokenType.POGODAK, TokenType.NIZ)) {
            return previous();
        }
        throw error(peek(), message);
    }

    private boolean checkTip() {
        return check(TokenType.BROJ) || check(TokenType.REALAN) || check(TokenType.SLOVO)
                || check(TokenType.TEKST) || check(TokenType.POGODAK) || check(TokenType.NIZ);
    }

    // GLAVNI PROGRAM
    public Ast.Program parseProgram() {
        List<Ast.TopItem> items = new ArrayList<>();

        // 1. Sve funkcije (dok god počinje sa tipom – to je definicija funkcije)
        while (!isAtEnd() && checkTip()) {
            Stmt.FunDecl funDecl = definicijaFunkcije();

            // Pretvori Stmt.FunDecl u Ast.FuncDef
            Ast.Type returnType = new Ast.Type(
                    switch (funDecl.tip.type) {
                        case BROJ -> Ast.Type.Kind.INT;
                        case REALAN -> Ast.Type.Kind.REAL;
                        case SLOVO -> Ast.Type.Kind.CHAR;
                        case TEKST -> Ast.Type.Kind.STRING;
                        case POGODAK -> Ast.Type.Kind.BOOL;
                        case NIZ -> Ast.Type.Kind.ARRAY;
                        default -> Ast.Type.Kind.VOID;
                    },
                    funDecl.tip,
                    0 // rank (broj dimenzija) – za sada 0, jer funkcije ne vraćaju nizove
            );

            List<Ast.Param> params = new ArrayList<>();
            for (Stmt.FunDecl.Param p : funDecl.parametri) {
                Ast.Type paramType = new Ast.Type(
                        switch (p.tip.type) {
                            case BROJ -> Ast.Type.Kind.INT;
                            case REALAN -> Ast.Type.Kind.REAL;
                            case SLOVO -> Ast.Type.Kind.CHAR;
                            case TEKST -> Ast.Type.Kind.STRING;
                            case POGODAK -> Ast.Type.Kind.BOOL;
                            case NIZ -> Ast.Type.Kind.ARRAY;
                            default -> throw error(p.tip, "Nepoznat tip parametra");
                        },
                        p.tip,
                        0 // za sada bez dimenzija u parametrima
                );
                params.add(new Ast.Param(p.ime, paramType));
            }

            Ast.FuncDef funcDef = new Ast.FuncDef(
                    funDecl.ime,
                    params,
                    returnType,
                    ((Stmt.Block)funDecl.telo).statements  // telo funkcije
            );

            items.add(funcDef);
        }
        // Ako posle funkcija ima deklaracija (moj broj x;), dodaj kao TopVarDecl
        while (match(TokenType.MOJ, TokenType.MOJE)) {
            Stmt.VarDecl decl = deklaracija();
            items.add(new Ast.TopVarDecl(decl));
        }

        // 2. Glavni blok – zapocni_igru ... zavrsi_igru
        Stmt.Block mainBlock = glavniBlok();
        items.add(new Ast.TopStmt(mainBlock));

        // 3. Vrati Ast.Program
        return new Ast.Program(items);
    }

    private Stmt.FunDecl definicijaFunkcije() {
        Token tip = consumeTip("Očekivan tip povratne vrednosti funkcije");
        Token ime = consume(TokenType.IDENT, "Očekivan identifikator funkcije");
        consume(TokenType.LPAREN, "Očekivano '(' posle imena funkcije");

        List<Stmt.FunDecl.Param> parametri = new ArrayList<>();
        if (!check(TokenType.RPAREN)) {
            parametri = listaParametara(); // vraća List<Param>, baš ono što FunDecl očekuje
        }

        consume(TokenType.RPAREN, "Očekivano ')' nakon liste parametara");

        Stmt.Block telo = blok();

        return new Stmt.FunDecl(tip, ime, parametri, telo);
    }

    private List<Stmt.FunDecl.Param> listaParametara() {
        List<Stmt.FunDecl.Param> parametri = new ArrayList<>();
        do {
            Token tip = consumeTip("Očekivan tip parametra");
            Token ime = consume(TokenType.IDENT, "Očekivan identifikator parametra");
            parametri.add(new Stmt.FunDecl.Param(tip, ime));
        } while (match(TokenType.SEPARATOR_COMMA));
        return parametri;
    }

    // GLAVNI BLOK
    private Stmt.Block glavniBlok() {
        consume(TokenType.ZAPOCNI_IGRU, "Očekivano 'zapocni_igru'");
        List<Stmt> statements = new ArrayList<>();
        while (!check(TokenType.ZAVRSI_IGRU) && !isAtEnd()) {
            statements.add(naredba());
        }
        consume(TokenType.ZAVRSI_IGRU, "Očekivano 'zavrsi_igru'");
        consume(TokenType.SEMICOLON, "Očekivano ';' posle glavnog bloka");
        return new Stmt.Block(statements);
    }

    // NAREDBE
    private Stmt naredba() {
        // 1. Blok { ... } – može biti bilo gde (npr. posle radi, posle inace, samostalno)
        if (match(TokenType.LBRACE)) {
            List<Stmt> stmts = new ArrayList<>();
            while (!check(TokenType.RBRACE) && !isAtEnd()) {
                stmts.add(naredba());
            }
            consume(TokenType.RBRACE, "Očekivano '}' nakon bloka");
            return new Stmt.Block(stmts);
        }
        if (match(TokenType.MOJ, TokenType.MOJE)) return deklaracija();
        if (match(TokenType.NAPISI)) return napisi();
        if (match(TokenType.UPISI)) return upisi();
        if (match(TokenType.AKO)) return ifNaredba();
        if (match(TokenType.RADI)) return whileNaredba();
        if (match(TokenType.VRATI)) return vratiNaredba();
        if (match(TokenType.SEMICOLON)) return new Stmt.Empty();

        // Dodela ili poziv funkcije (počinje sa IDENT)
        if (check(TokenType.IDENT)) {
            return dodelaIliPoziv();
        }

        throw error(peek(), "Nepoznata naredba");
    }

    // DEKLARACIJA
    private Stmt.VarDecl deklaracija() {
        boolean isMoje = previous().type == TokenType.MOJE;

        Token typeToken;
        if (isMoje) {
            consume(TokenType.SLOVO, "Nakon 'moje' očekuje se 'slovo'");
            typeToken = previous();
        } else {
            typeToken = consumeTip("Očekivan tip (broj, realan, tekst, niz, ...)");
        }

        Token name = consume(TokenType.IDENT, "Očekivano ime promenljive");

        // Nizovi: moj niz n[5][a+1] ili moj broj x
        List<Expr> dimensions = new ArrayList<>();
        while (match(TokenType.LBRACKET)) {
            if (match(TokenType.RBRACKET)) {
                // prazan [] → nepoznata veličina (npr. moj niz n[][])
                dimensions.add(null);
            } else {
                dimensions.add(izraz());
                consume(TokenType.RBRACKET, "Očekivano ']' nakon dimenzije");
            }
        }

        // Inicijalizator: = izraz
        Expr initializer = null;
        if (match(TokenType.ASSIGN)) {
            initializer = izraz();
        }

        consume(TokenType.SEMICOLON, "Očekivano ';' nakon deklaracije");

        // Napomena: tvoja VarDecl klasa ima String type – mi ćemo proslediti lexeme
        return new Stmt.VarDecl(typeToken.lexeme, name, initializer);
    }
    private Expr lVrednost() {
        Token name = consume(TokenType.IDENT, "Očekivan identifikator");
        List<Expr> indeksi = new ArrayList<>();

        // indeksi niza
        while (match(TokenType.LBRACKET)) {
            indeksi.add(izraz());
            consume(TokenType.RBRACKET, "Očekivano ']' u indeksu niza");
        }

        if (match(TokenType.LPAREN)) {
            // Poziv funkcije u izrazu LVREDNOST
            List<Expr> args = new ArrayList<>();
            if (!check(TokenType.RPAREN)) {
                do {
                    args.add(izraz());
                } while (match(TokenType.SEPARATOR_COMMA));
            }
            consume(TokenType.RPAREN, "Očekivano ')' nakon argumenata");
            // LVREDNOST može biti funkcijski poziv, vraća Expr.Call
            return new Expr.Call(name, args);
        }

        if (indeksi.isEmpty()) return new Expr.Ident(name);
        else return new Expr.Index(name, indeksi);
    }
    private List<Expr> listaArgumenata() {
        List<Expr> args = new ArrayList<>();

        do {
            args.add(izraz());   // svaki argument je običan izraz
        } while (match(TokenType.SEPARATOR_COMMA));

        return args;
    }

    // DODELA ili POZIV FUNKCIJE
    private Stmt dodelaIliPoziv() {
        // LVREDNOST: identifikator ili indeksirani niz
        Expr target = lVrednost();

        // Ako je sledeći token '=' → dodela
        if (match(TokenType.ASSIGN)) {
            Expr value = izraz();
            consume(TokenType.SEMICOLON, "Očekivan ';' nakon dodele");
            return new Stmt.Assign(target, value);
        }

        // Ako je sledeći token '(' → poziv funkcije (samo ExpressionStmt)
        if (target instanceof Expr.Ident || target instanceof Expr.Index) {
            if (match(TokenType.LPAREN)) {
                List<Expr> args = new ArrayList<>();
                if (!check(TokenType.RPAREN)) {
                    do {
                        args.add(izraz());
                    } while (match(TokenType.SEPARATOR_COMMA));
                }
                consume(TokenType.RPAREN, "Očekivano ')' nakon argumenata");
                consume(TokenType.SEMICOLON, "Očekivan ';' nakon poziva funkcije");

                Token callTok;
                if (target instanceof Expr.Ident) {
                    callTok = ((Expr.Ident) target).name;
                } else {
                    callTok = ((Expr.Index) target).name;
                }

                return new Stmt.ExpressionStmt(new Expr.Call(callTok, args));
            }
        }

        throw error(peek(), "Očekivano '=' ili '(' nakon identifikatora ili indeksa");
    }


    private Stmt.ExpressionStmt napisi() {
        consume(TokenType.LPAREN, "Očekivano '(' nakon 'napisi'");
        Expr argument = izraz();
        consume(TokenType.RPAREN, "Očekivano ')' nakon argumenta");
        consume(TokenType.SEMICOLON, "Očekivano ';' nakon 'napisi'");

        Token napisiToken = previous();
        Token fakeCallee = new Token(TokenType.IDENT, "napisi", null,
                napisiToken.line, napisiToken.colStart, napisiToken.colEnd);

        Expr.Call call = new Expr.Call(napisiToken, List.of(argument));

        return new Stmt.ExpressionStmt(call);
    }

    private Stmt.ExpressionStmt upisi() {
        consume(TokenType.LPAREN, "Očekivano '(' nakon 'upisi'");
        Token varName = consume(TokenType.IDENT, "Očekivano ime promenljive u 'upisi'");
        consume(TokenType.RPAREN, "Očekivano ')' nakon imena");
        consume(TokenType.SEMICOLON, "Očekivano ';' nakon 'upisi'");

        Token upisiToken = new Token(TokenType.IDENT, "upisi", null,
                varName.line, varName.colStart, varName.colEnd);

        Expr.Call call = new Expr.Call(upisiToken,List.of(new Expr.Ident(varName)));

        return new Stmt.ExpressionStmt(call);
    }

    private Stmt.If ifNaredba() {

        consume(TokenType.LPAREN, "Očekivano '(' posle 'ako'");
        Expr cond = izraz();
        consume(TokenType.RPAREN, "Očekivano ')' nakon uslova");

        consume(TokenType.LBRACE, "Očekivano '{' posle uslova u 'ako'");
        List<Stmt> thenStmts = new ArrayList<>();
        while (!check(TokenType.RBRACE) && !isAtEnd()) {
            thenStmts.add(naredba());
        }
        consume(TokenType.RBRACE, "Očekivano '}' nakon tela 'ako'");
        Stmt thenBranch = new Stmt.Block(thenStmts);

        Stmt elseBranch = null;
        if (match(TokenType.INACE)) {
            // OBAVEZNO {} posle inace
            consume(TokenType.LBRACE, "Očekivano '{' posle 'inace'");
            List<Stmt> elseStmts = new ArrayList<>();
            while (!check(TokenType.RBRACE) && !isAtEnd()) {
                elseStmts.add(naredba());
            }
            consume(TokenType.RBRACE, "Očekivano '}' nakon 'inace' bloka");
            elseBranch = new Stmt.Block(elseStmts);
        }

        return new Stmt.If(cond, thenBranch, elseBranch);
    }

    private Stmt.While whileNaredba() {

        consume(TokenType.LPAREN, "Očekivano '(' posle 'radi'");
        Expr cond = izraz();
        consume(TokenType.RPAREN, "Očekivano ')' nakon uslova");

        Stmt body = naredba();

        return new Stmt.While(cond, body);
    }

    private Stmt.Return vratiNaredba() {
        Expr value = izraz();
        consume(TokenType.SEMICOLON, "Očekivano ';' nakon 'vrati'");
        return new Stmt.Return(value);
    }

    private Stmt.Block blok() {

        consume(TokenType.LBRACE, "Očekivano '{'");
        List<Stmt> statements = new ArrayList<>();
        while (!check(TokenType.RBRACE) && !isAtEnd()) {
            statements.add(naredba());
        }
        consume(TokenType.RBRACE, "Očekivano '}'");
        return new Stmt.Block(statements);
    }

    // IZRAZI
    private Expr izraz() {
        return logIli();
    }

    private Expr logIli() {
        Expr expr = logI();
        while (match(TokenType.ILI)) {
            Token op = previous();
            Expr right = logI();
            expr = new Expr.Binary(expr, op, right);
        }
        return expr;
    }

    private Expr logI() {
        Expr expr = jednakost();
        while (match(TokenType.I)) {
            Token op = previous();
            Expr right = jednakost();
            expr = new Expr.Binary(expr, op, right);
        }
        return expr;
    }

    private Expr jednakost() {
        Expr expr = poredjenje();
        while (match(TokenType.EQ, TokenType.NEQ)) {
            Token op = previous();
            Expr right = poredjenje();
            expr = new Expr.Binary(expr, op, right);
        }
        return expr;
    }

    private Expr poredjenje() {
        Expr expr = zbir();
        while (match(TokenType.LT, TokenType.LE, TokenType.GT, TokenType.GE)) {
            Token op = previous();
            Expr right = zbir();
            expr = new Expr.Binary(expr, op, right);
        }
        return expr;
    }

    private Expr zbir() {
        Expr expr = faktor();
        while (match(TokenType.ADD, TokenType.SUBTRACT)) {
            Token op = previous();
            Expr right = faktor();
            expr = new Expr.Binary(expr, op, right);
        }
        return expr;
    }

    private Expr faktor() {
        Expr expr = unarni();
        while (match(TokenType.MULTIPLY, TokenType.DIVIDE, TokenType.PERCENT)) {
            Token op = previous();
            Expr right = unarni();
            expr = new Expr.Binary(expr, op, right);
        }
        return expr;
    }

    private Expr unarni() {
        if (match(TokenType.NE, TokenType.SUBTRACT)) {
            Token op = previous();
            Expr right = unarni();
            return new Expr.Unary(op, right);
        }
        return primarni();
    }

    private Expr primarni() {

        if (match(TokenType.INT_LIT, TokenType.CH_LIT, TokenType.STR_LIT, TokenType.BOOL_LIT)) {
            return new Expr.Literal(previous(), previous().literal);
        }


        if (match(TokenType.IDENT)) {
            Token name = previous();


            if (match(TokenType.LBRACKET)) {
                List<Expr> indices = new ArrayList<>();
                do {
                    indices.add(izraz());
                    consume(TokenType.RBRACKET, "Očekivano ']' nakon indeksa indeksa");
                } while (match(TokenType.LBRACKET));

                return new Expr.Index(name, indices);
            }


            if (match(TokenType.LPAREN)) {
                List<Expr> args = new ArrayList<>();
                if (!check(TokenType.RPAREN)) {
                    do {
                        args.add(izraz());
                    } while (match(TokenType.SEPARATOR_COMMA));
                }
                consume(TokenType.RPAREN, "Očekivano ')' nakon argumenata");


                Token callTok = new Token(TokenType.IDENT, name.lexeme + "(", null,
                        name.line, name.colStart, name.colEnd);


                return new Expr.Call(callTok, args);
            }


            return new Expr.Ident(name);
        }


        if (match(TokenType.LPAREN)) {
            Expr expr = izraz();
            consume(TokenType.RPAREN, "Očekivano ')' nakon izraza");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Očekivan izraz");
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

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw error(peek(), message);

    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private ParseError error(Token token, String message) {
        throw new ParseError(token, message);
    }
}


