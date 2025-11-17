package parser;

import java.util.List;

public class JsonPrinter {

    private final StringBuilder out = new StringBuilder();
    private int indent = 0;

    public String print(AstNode node) {
        out.setLength(0);     // reset
        indent = 0;
        writeNode(node);
        return out.toString();
    }

    private void write(String s) {
        out.append(s);
    }

    private void newline() {
        out.append("\n");
        out.append("  ".repeat(indent));
    }

    private void writeNode(AstNode node) {
        write("{");
        indent++;

        // id
        newline();
        write("\"id\": " + node.id + ",");

        // kind
        newline();
        write("\"kind\": \"" + node.kind + "\",");

        // token info (optional)
        newline();
        if (node.token != null)
            write("\"token\": \"" + escape(node.token.lexeme) + "\",");
        else
            write("\"token\": null,");

        // children
        newline();
        write("\"children\": [");

        indent++;
        List<AstNode> kids = node.children;

        for (int i = 0; i < kids.size(); i++) {
            newline();
            writeNode(kids.get(i));
            if (i < kids.size() - 1) write(",");
        }

        indent--;
        if (!kids.isEmpty()) newline();
        write("]");

        indent--;
        newline();
        write("}");
    }

    private String escape(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}
