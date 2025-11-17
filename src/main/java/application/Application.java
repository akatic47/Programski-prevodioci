package application;

import lexer.Lexer;
import lexer.token.Token;
import lexer.token.TokenFormatter;
import parser.AstNode;
import parser.JsonPrinter;
import parser.ParseException;
import parser.Parser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Application {

    public static void main(String[] args) {

        if (args.length != 1) {
            System.err.println("Usage: java application.Application <source-file>");
            System.exit(64);
        }

        try {
            // 1. učitaj fajl
            String code = Files.readString(Path.of(args[0]));

            // 2. lexer
            Lexer lexer = new Lexer(code);
            List<Token> tokens = lexer.scanTokens();

            // DEBUG: ispisi sve tokene
            System.out.println(TokenFormatter.formatList(tokens));


            // 3. parser
            Parser parser = new Parser(tokens);
            AstNode ast = parser.parse();

            // 4. JSON ispis AST-a
            JsonPrinter printer = new JsonPrinter();
            String output = printer.print(ast);

            System.out.println(output);

        } catch (ParseException e) {
            System.err.println("Sintaksna greška: " + e.getMessage());
            System.exit(65);

        } catch (RuntimeException e) {
            System.err.println("Leksička greška: " + e.getMessage());
            System.exit(66);

        } catch (Exception e) {
            System.err.println("Greška: " + e.getMessage());
            System.exit(1);
        }
    }
}
