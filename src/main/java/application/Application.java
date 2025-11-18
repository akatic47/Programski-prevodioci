package application;


import lexer.Lexer;
import lexer.token.Token;
import lexer.token.TokenFormatter;
import parser.Ast;
import parser.JsonAstPrinter;
import parser.ParseError;
import parser.ParserAST;

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

            ParserAST parser = new ParserAST(tokens);

            Ast.Program program = parser.parseProgram();

            // 4. JSON ispis AST-a
            JsonAstPrinter printer = new JsonAstPrinter();
             System.out.println(printer.print(program));

            //  System.out.println(output);

        } catch (ParseError e) {
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
