package nsu.syspro;

import syspro.tm.Tasks;

public class Main {
    public static void main(String[] args) {
        Tasks.Lexer.registerSolution(new MyLexer());
    }
}
