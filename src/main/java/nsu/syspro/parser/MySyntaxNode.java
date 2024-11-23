package nsu.syspro.parser;

import syspro.tm.lexer.Keyword;
import syspro.tm.lexer.Symbol;
import syspro.tm.lexer.Token;
import syspro.tm.parser.AnySyntaxKind;
import syspro.tm.parser.SyntaxKind;
import syspro.tm.parser.SyntaxNode;

import java.util.List;

public class MySyntaxNode implements SyntaxNode {

    private final AnySyntaxKind kind;
    // null if kind is non-terminal
    private final Token relatedToken;
    // Grammar rule terminals or non-terminals containing from API (because they are properly parsed),
    // wrapped in SyntaxNode
    private final List<SyntaxNode> syntaxNodes;

    public MySyntaxNode(AnySyntaxKind kind, Token relatedToken, List<SyntaxNode> syntaxNodes) {
        assert relatedToken == null || isAPISyntaxKind(kind);
        this.kind = kind;
        this.relatedToken = relatedToken;
        this.syntaxNodes = syntaxNodes;
    }

    public MySyntaxNode(AnySyntaxKind kind, List<SyntaxNode> syntaxNodes) {
        this(kind, null, syntaxNodes);
    }

    public static boolean isAPISyntaxKind(AnySyntaxKind kind) {
        return kind instanceof SyntaxKind || kind instanceof Keyword || kind instanceof Symbol;
    }

    @Override
    public AnySyntaxKind kind() {
        return kind;
    }

    @Override
    public int slotCount() {
        return syntaxNodes.size();
    }

    @Override
    public SyntaxNode slot(int index) {
        if (syntaxNodes == null || index < 0 || index >= syntaxNodes.size()) return null;
        return syntaxNodes.get(index);
    }

    @Override
    public Token token() {
        return relatedToken;
    }
}