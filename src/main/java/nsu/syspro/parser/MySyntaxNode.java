package nsu.syspro.parser;

import syspro.tm.lexer.Token;
import syspro.tm.parser.AnySyntaxKind;
import syspro.tm.parser.SyntaxNode;

import java.util.List;

public class MySyntaxNode implements SyntaxNode {

    private final AnySyntaxKind kind;
    // null if kind is non-terminal
    Token relatedToken;
    // Grammar rule terminals or non-terminals,
    // wrapped in SyntaxNode
    List<SyntaxNode> syntaxNodes;

    public MySyntaxNode(AnySyntaxKind kind, Token relatedToken) {
        this.kind = kind;
        this.relatedToken = relatedToken;
    }

    public MySyntaxNode(AnySyntaxKind kind) {
        this(kind, null);
    }

    public void addChild(SyntaxNode child) {
        if (syntaxNodes == null) {
            syntaxNodes = new java.util.ArrayList<>();
        }
        syntaxNodes.add(child);
    }

    public void addChildren(List<SyntaxNode> children) {
        if (syntaxNodes == null) {
            syntaxNodes = new java.util.ArrayList<>();
        }
        if (children == null) return;
        syntaxNodes.addAll(children);
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