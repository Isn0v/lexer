package nsu.syspro.parser;

import syspro.tm.parser.Diagnostic;
import syspro.tm.parser.ParseResult;
import syspro.tm.parser.SyntaxNode;
import syspro.tm.parser.TextSpan;

import java.util.ArrayList;
import java.util.Collection;

public class MyParseResult implements ParseResult {

    private final SyntaxNode root;
    ArrayList<TextSpan> invalidRanges;
    ArrayList<Diagnostic> diagnostics;

    public MyParseResult(SyntaxNode root, ArrayList<TextSpan> invalidRanges, ArrayList<Diagnostic> diagnostics) {
        this.root = root;
        this.invalidRanges = invalidRanges;
        this.diagnostics = diagnostics;
    }

    @Override
    public SyntaxNode root() {
        return root;
    }

    @Override
    public Collection<TextSpan> invalidRanges() {
        return invalidRanges;
    }

    @Override
    public Collection<Diagnostic> diagnostics() {
        return diagnostics;
    }
}
