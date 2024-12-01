package nsu.syspro;


import nsu.syspro.lexer.MyLexer;
import nsu.syspro.parser.MyParser;
import nsu.syspro.parser.nonterms.AdditionalSyntaxKind;
import syspro.tm.Tasks;
import syspro.tm.WebServer;
import syspro.tm.parser.SyntaxKind;

public class Main {
    public static void main(String[] args) {
        String code = """
                class Indent1\r
                    def notMultipleOf2(): Boolean\r
                        return true""";

        MyParser parser = new MyParser();
        parser.parse(code);
//        Tasks.Parser.registerSolution(parser);
//        WebServer.start();
//        WebServer.waitForWebServerExit();
    }
}
