package nsu.syspro.parser;

import syspro.tm.parser.Diagnostic;
import syspro.tm.parser.ParseResult;
import syspro.tm.parser.SyntaxNode;
import syspro.tm.parser.TextSpan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MyParseResult implements ParseResult {

    private final SyntaxNode root;
    ArrayList<TextSpan> invalidRanges = new ArrayList<>();
    ArrayList<Diagnostic> diagnostics = new ArrayList<>();

    public MyParseResult(SyntaxNode root) {
        this.root = root;
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
