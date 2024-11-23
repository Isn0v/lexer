package nsu.syspro;


import nsu.syspro.lexer.MyLexer;
import syspro.tm.Tasks;

public class Main {
    public static void main(String[] args) {
        Tasks.Lexer.registerSolution(new MyLexer());
//        Grammatic.getRules().get(SyntaxKind.SOURCE_TEXT).forEach(System.out::println);
    }
}
