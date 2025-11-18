package parser;

import lexer.token.Token;

import java.util.List;
import java.util.List;

public abstract class Expr {
    public interface Visitor<R> {
        R visitLiteral(Literal e);
        R visitIdent(Ident e);
        R visitIndex(Index e);
        R visitGrouping(Grouping e);
        R visitCall(Call e);
        R visitUnary(Unary e);
        R visitBinary(Binary e);
    }

    public abstract <R> R accept(Visitor<R> v);

    public static final class Literal extends Expr {
        public final Token token;
        public final Object value;

        public Literal(Token token, Object value) {
            this.token = token;
            this.value = value;
        }

        @Override
        public <R> R accept(Visitor<R> v) {
            return v.visitLiteral(this);
        }
    }

    public static final class Ident extends Expr {
        public final Token name; // IDENT

        public Ident(Token name) {
            this.name = name;
        }

        @Override
        public <R> R accept(Visitor<R> v) {
            return v.visitIdent(this);
        }
    }

    public static final class Index extends Expr {
        public final Token name; // IDENT
        public final List<Expr> indices;

        public Index(Token name, List<Expr> indices) {
            this.name = name;
            this.indices = indices;
        }

        @Override
        public <R> R accept(Visitor<R> v) {
            return v.visitIndex(this);
        }
    }

    public static final class Grouping extends Expr {
        public final Expr inner;

        public Grouping(Expr inner) {
            this.inner = inner;
        }

        @Override
        public <R> R accept(Visitor<R> v) {
            return v.visitGrouping(this);
        }
    }

    public static final class Call extends Expr {

        public final Token callee;  // IDENT
        public final List<Expr> args;
        public Call(Token callee, List<Expr> args) {
            this.callee = callee; this.args = args;
        }

        @Override
        public <R> R accept(Visitor<R> v) {
            return v.visitCall(this);
        }
    }

    public static final class Unary extends Expr {
        public final Token operator;
        public final Expr right;

        public Unary(Token operator, Expr right) {
            this.operator = operator;
            this.right = right;
        }

        @Override
        public <R> R accept(Visitor<R> v) {
            return v.visitUnary(this);
        }
    }

    public static final class Binary extends Expr {
        public final Expr left;
        public final Token op;
        public final Expr right;

        public Binary(Expr left, Token op, Expr right) {
            this.left = left;
            this.op = op;
            this.right = right;
        }

        @Override
        public <R> R accept(Visitor<R> v) {
            return v.visitBinary(this);
        }
    }
}

