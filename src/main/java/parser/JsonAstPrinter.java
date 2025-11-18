package parser;


import java.util.List;

public final class JsonAstPrinter implements Expr.Visitor<String>, Stmt.Visitor<String> {

    public String print(Ast.Program program) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"type\": \"program\",\n");
        sb.append("  \"explicitProgram\": false,\n");
        sb.append("  \"items\": [\n");

        boolean first = true;
        for (Ast.TopItem item : program.items) {
            if (!first) sb.append(",\n");
            sb.append("    ").append(printTopItem(item));
            first = false;
        }
        sb.append("\n  ]\n}");
        return sb.toString();
    }

    private String printTopItem(Ast.TopItem item) {
        StringBuilder sb = new StringBuilder();
        if (item instanceof Ast.FuncDef f) {
            sb.append("{\n");
            sb.append("      \"kind\": \"funcDef\",\n");
            sb.append("      \"name\": \"").append(f.name.lexeme).append("\",\n");
            sb.append("      \"returnType\": { \"base\": \"").append(typeToString(f.returnType)).append("\", \"rank\": ").append(f.returnType.rank).append(" },\n");
            sb.append("      \"params\": [\n");
            for (int i = 0; i < f.params.size(); i++) {
                Ast.Param p = f.params.get(i);
                sb.append("        { \"name\": \"").append(p.name.lexeme).append("\", \"type\": { \"base\": \"").append(typeToString(p.type)).append("\", \"rank\": ").append(p.type.rank).append(" } }");
                if (i < f.params.size() - 1) sb.append(",");
                sb.append("\n");
            }
            sb.append("      ],\n");
            sb.append("      \"body\": [\n");
            for (int i = 0; i < f.body.size(); i++) {
                if (i > 0) sb.append(",\n");
                sb.append("        ").append(f.body.get(i).accept(this));
            }
            sb.append("\n      ]\n");
            sb.append("    }");
        }
        else if (item instanceof Ast.TopStmt ts) {
            sb.append("{\n");
            sb.append("      \"kind\": \"topStmt\",\n");
            sb.append("      \"stmt\": ").append(ts.stmt.accept(this)).append("\n");
            sb.append("    }");
        }
        return sb.toString();
    }

    private String typeToString(Ast.Type t) {
        return switch (t.kind) {
            case INT -> "broj";
            case REAL -> "realan";
            case CHAR -> "slovo";
            case STRING -> "tekst";
            case BOOL -> "pogodak";
            case ARRAY -> "niz";
            case VOID -> "void";
        };
    }

    @Override public String visitLiteral(Expr.Literal e) {
        return "{ \"type\": \"literal\", \"value\": " + (e.value == null ? "null" : "\"" + e.value + "\"") + " }";
    }

    @Override public String visitIdent(Expr.Ident e) {
        return "{ \"type\": \"ident\", \"name\": \"" + e.name.lexeme + "\" }";
    }

    @Override public String visitIndex(Expr.Index e) {
        StringBuilder sb = new StringBuilder();
        sb.append("{ \"type\": \"index\", \"name\": \"").append(e.name.lexeme).append("\", \"indices\": [");
        for (int i = 0; i < e.indices.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(e.indices.get(i).accept(this));
        }
        sb.append("] }");
        return sb.toString();
    }

    @Override public String visitGrouping(Expr.Grouping e) {
        return "{ \"type\": \"group\", \"expr\": " + e.inner.accept(this) + " }";
    }

    @Override public String visitCall(Expr.Call e) {
        StringBuilder sb = new StringBuilder();
        sb.append("{ \"type\": \"call\", \"name\": \"").append(e.callee.lexeme).append("\", \"args\": [");
        for (int i = 0; i < e.args.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(e.args.get(i).accept(this));
        }
        sb.append("] }");
        return sb.toString();
    }

    @Override public String visitUnary(Expr.Unary e) {
        return "{ \"type\": \"unary\", \"op\": \"" + e.operator.lexeme + "\", \"right\": " + e.right.accept(this) + " }";
    }

    @Override public String visitBinary(Expr.Binary e) {
        return "{ \"type\": \"binary\", \"op\": \"" + e.op.lexeme + "\", \"left\": " + e.left.accept(this) + ", \"right\": " + e.right.accept(this) + " }";
    }

    // ====================== Stmt ======================
    @Override public String visitVarDecl(Stmt.VarDecl s) {
        String init = s.initializer != null ? ", \"init\": " + s.initializer.accept(this) : "";
        return "{ \"stmt\": \"varDecl\", \"type\": \"" + s.type + "\", \"names\": [\"" + s.name.lexeme + "\"]" + init + " }";
    }

    @Override public String visitAssign(Stmt.Assign s) {
        return "{ \"stmt\": \"assign\", \"left\": " + s.target.accept(this) + ", \"value\": " + s.value.accept(this) + " }";
    }

    @Override public String visitIf(Stmt.If s) {
        StringBuilder sb = new StringBuilder();
        sb.append("{ \"stmt\": \"begin_if\", \"if\": { \"cond\": ").append(s.condition.accept(this)).append(", \"block\": [");
        List<Stmt> thenStmts = ((Stmt.Block)s.thenBranch).statements;
        for (int i = 0; i < thenStmts.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(thenStmts.get(i).accept(this));
        }
        sb.append("] }");
        if (s.elseBranch != null) {
            sb.append(", \"else\": [");
            List<Stmt> elseStmts = ((Stmt.Block)s.elseBranch).statements;
            for (int i = 0; i < elseStmts.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(elseStmts.get(i).accept(this));
            }
            sb.append("]");
        }
        sb.append(" }");
        return sb.toString();
    }

    @Override public String visitWhile(Stmt.While s) {
        StringBuilder sb = new StringBuilder();
        sb.append("{ \"stmt\": \"while\", \"cond\": ").append(s.condition.accept(this)).append(", \"body\": [");
        List<Stmt> body = ((Stmt.Block)s.body).statements;
        for (int i = 0; i < body.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(body.get(i).accept(this));
        }
        sb.append("] }");
        return sb.toString();
    }

    @Override public String visitReturn(Stmt.Return s) {
        return "{ \"stmt\": \"return\", \"expr\": " + s.value.accept(this) + " }";
    }

    @Override public String visitExprStmt(Stmt.ExpressionStmt s) {
        return s.expression.accept(this);
    }

    @Override public String visitBlock(Stmt.Block s) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < s.statements.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(s.statements.get(i).accept(this));
        }
        sb.append("]");
        return sb.toString();
    }



    @Override public String visitEmpty(Stmt.Empty s) {
        return "{ \"stmt\": \"empty\" }";
    }

    @Override public String visitFunDecl(Stmt.FunDecl s) {
        return "{ \"stmt\": \"funDecl\" }"; // ne koristi se u JSON-u
    }
}