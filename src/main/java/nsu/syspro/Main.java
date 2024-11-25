package nsu.syspro;


import nsu.syspro.lexer.MyLexer;

public class Main {
    public static void main(String[] args) {
        String code = """
                class Indent1\r
                    def notMultipleOf2(): Boolean\r
                        return true""";

        new MyLexer().lex(code).forEach(System.out::println);
//        Tasks.Lexer.registerSolution(new MyLexer());
//        Grammatic.getRules().get(SyntaxKind.SOURCE_TEXT).forEach(System.out::println);
    }
}
