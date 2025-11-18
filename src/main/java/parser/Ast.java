package parser;

import lexer.token.Token;
import parser.Expr;
import parser.Stmt;

import java.util.ArrayList;
import java.util.List;

public final class Ast {

    // ============================================================
    // PROGRAM
    // ============================================================
    public static final class Program {
        public final List<TopItem> items;

        public Program(List<TopItem> items) {
            this.items = List.copyOf(items);
        }
    }

    // ============================================================
    // TOP LEVEL
    // ============================================================
    public interface TopItem {}

    public static final class TopVarDecl implements TopItem {
        public final Stmt.VarDecl decl;
        public TopVarDecl(Stmt.VarDecl decl) { this.decl = decl; }
    }

    public static final class TopStmt implements TopItem {
        public final Stmt stmt;
        public TopStmt(Stmt stmt) { this.stmt = stmt; }
    }

    // ============================================================
    // FUNKCIJA
    // ============================================================
    public static final class FuncDef implements TopItem {
        public final Token name;
        public final List<Param> params;
        public final Type returnType;
        public final List<Stmt> body;

        public FuncDef(Token name, List<Param> params, Type returnType, List<Stmt> body) {
            this.name = name;
            this.params = params;
            this.returnType = returnType;
            this.body = body;
        }
    }

    // ============================================================
    // PARAMETAR
    // ============================================================
    public static final class Param {
        public final Token name;
        public final Type type;

        public Param(Token name, Type type) {
            this.name = name;
            this.type = type;
        }
    }

    // ============================================================
    // TIP
    // ============================================================
    public static class Type {

        public enum Kind {
            INT, REAL, CHAR, STRING, BOOL, VOID, ARRAY
        }

        public final Kind kind;
        public final Token token;
        public final int rank;          // broj dimenzija niza
        public final List<Expr> dims;   // dimenzije (null = prazan [])

        // NOVI FULL KONSTRUKTOR
        public Type(Kind kind, Token token, int rank, List<Expr> dims) {
            this.kind = kind;
            this.token = token;
            this.rank = rank;
            this.dims = dims;
        }

        // BACKWARD KOMPATIBILNI KONSTRUKTOR
        public Type(Kind kind, Token token, int rank) {
            this(kind, token, rank, new ArrayList<>());
        }

        public boolean isArray() {
            return kind == Kind.ARRAY;
        }
    }
}
