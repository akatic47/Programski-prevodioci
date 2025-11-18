package parser;

import lexer.token.Token;

import java.util.List;

import java.util.List;

public abstract class Stmt {
    public interface Visitor<R> {
        R visitVarDecl(VarDecl stmt);
        R visitAssign(Assign stmt);
        R visitIf(If stmt);
        R visitWhile(While stmt);
        R visitReturn(Return stmt);
        R visitBlock(Block stmt);
        R visitExprStmt(ExpressionStmt stmt);
        R visitEmpty(Empty stmt);

        R visitFunDecl(FunDecl stmt);
    }



    public abstract <R> R accept(Visitor<R> v);

    // Deklaracija promenljive
    public static final class VarDecl extends Stmt {
        public final String type;   // broj, realan, slovo, tekst, pogodak, niz
        public final Token name;    // IDENT
        public final Expr initializer; // može biti null

        public VarDecl(String type, Token name, Expr initializer) {
            this.type = type;
            this.name = name;
            this.initializer = initializer;
        }

        @Override
        public <R> R accept(Visitor<R> v) {
            return v.visitVarDecl(this);
        }
    }

    // Dodela vrednosti promenljivoj ili nizu
    public static final class Assign extends Stmt {
        public final Expr target; // VariableExpr ili Index
        public final Expr value;

        public Assign(Expr target, Expr value) {
            this.target = target;
            this.value = value;
        }

        @Override
        public <R> R accept(Visitor<R> v) {
            return v.visitAssign(this);
        }
    }

    // If naredba
    public static final class If extends Stmt {
        public final Expr condition;
        public final Stmt thenBranch;
        public final Stmt elseBranch; // može biti null

        public If(Expr condition, Stmt thenBranch, Stmt elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }

        @Override
        public <R> R accept(Visitor<R> v) {
            return v.visitIf(this);
        }
    }

    // While / radi petlja
    public static final class While extends Stmt {
        public final Expr condition;
        public final Stmt body;

        public While(Expr condition, Stmt body) {
            this.condition = condition;
            this.body = body;
        }

        @Override
        public <R> R accept(Visitor<R> v) {
            return v.visitWhile(this);
        }
    }

    // Return / vrati naredba
    public static final class Return extends Stmt {
        public final Expr value;

        public Return(Expr value) {
            this.value = value;
        }

        @Override
        public <R> R accept(Visitor<R> v) {
            return v.visitReturn(this);
        }
    }

    // Blok naredbi { stmt; stmt; ... }
    public static final class Block extends Stmt {
        public final List<Stmt> statements;

        public Block(List<Stmt> statements) {
            this.statements = statements;
        }

        @Override
        public <R> R accept(Visitor<R> v) {
            return v.visitBlock(this);
        }
    }

    // Izraz kao naredba (npr. poziv funkcije, napisi/upisi)
    public static final class ExpressionStmt extends Stmt {
        public final Expr expression;

        public ExpressionStmt(Expr expression) {
            this.expression = expression;
        }

        @Override
        public <R> R accept(Visitor<R> v) {
            return v.visitExprStmt(this);
        }
    }

    //funkcije

    public static final class FunDecl extends Stmt {
        public final Token tip;
        public final Token ime;
        public final List<Param> parametri;
        public final Block telo;

        public FunDecl(Token tip, Token ime, List<Param> parametri, Block telo) {
            this.tip = tip;
            this.ime = ime;
            this.parametri = parametri;
            this.telo = telo;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitFunDecl(this);
        }

        public static class Param {
            public final Token tip;      // originalni token tipa (broj / niz / ...)
            public final Token ime;      // ime parametra
            public final parser.Ast.Type parsedType;  // PARSIRANI TIP (sa dimenzijama!)

            public Param(Token tip, Token ime, parser.Ast.Type parsedType) {
                this.tip = tip;
                this.ime = ime;
                this.parsedType = parsedType;
            }
        }
    }



    // Prazna naredba ;
    public static final class Empty extends Stmt {
        @Override
        public <R> R accept(Visitor<R> v) {
            return v.visitEmpty(this);
        }
    }
}
