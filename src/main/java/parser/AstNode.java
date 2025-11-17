package parser;

import lexer.token.Token;

import java.util.ArrayList;
import java.util.List;

public final class AstNode {

    private static int NEXT_ID = 1;

    public final int id;
    public final String kind;        // tip čvora, npr "Function", "VarDecl", "BinaryExpr"
    public final Token token;        // opcioni token (npr. za identifikator)
    public final List<AstNode> children;

    private AstNode(String kind, Token token, List<AstNode> children) {
        this.id = NEXT_ID++;
        this.kind = kind;
        this.token = token;
        this.children = children;
    }

    // === FABRIKE ZA KREIRANJE ČVOROVA ===

    public static AstNode node(String kind, Token token, AstNode... kids) {
        List<AstNode> list = new ArrayList<>();
        for (AstNode k : kids) {
            if (k != null) list.add(k);
        }
        return new AstNode(kind, token, list);
    }

    public static AstNode list(String kind, List<AstNode> items) {
        return new AstNode(kind, null, items);
    }

    public static AstNode leaf(String kind, Token token) {
        return new AstNode(kind, token, new ArrayList<>());
    }
}
