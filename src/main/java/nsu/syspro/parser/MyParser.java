package nsu.syspro.parser;

import nsu.syspro.lexer.MyLexer;
import nsu.syspro.parser.nonterms.ListNONTERM;
import nsu.syspro.parser.nonterms.OrNONTERM;
import nsu.syspro.parser.nonterms.QuestionNONTERM;
import syspro.tm.lexer.Token;
import syspro.tm.parser.*;

import java.util.ArrayList;
import java.util.List;

public class MyParser implements Parser {

    private int currentPosition = 0;

    boolean isTerminal(AnySyntaxKind kind) {
        return kind.isTerminal();
    }

    void calculateFirst(AnySyntaxKind kind, List<AnySyntaxKind> result) {
        if (kind instanceof ListNONTERM) {
            kind = ((ListNONTERM) kind).getExtendedKind();
            calculateFirst(kind, result);
            return;
        } else if (kind instanceof QuestionNONTERM) {
            kind = ((QuestionNONTERM) kind).getExtendedKind();
            calculateFirst(kind, result);
            return;
        } else if (kind instanceof OrNONTERM) {
            List<AnySyntaxKind> orTerms = ((OrNONTERM) kind).getPossibleKinds();
            for (AnySyntaxKind orTerm : orTerms) {
                calculateFirst(orTerm, result);
            }
            return;
        }


        if (isTerminal(kind)) {
            result.add(kind);
            return;
        }

        List<AnySyntaxKind> terms = Grammar.getRules().get(kind);

        int i = 0;
        while (i < terms.size() && (terms.get(i) instanceof ListNONTERM || terms.get(i) instanceof QuestionNONTERM)) {
            AnySyntaxKind extendedTerm = terms.get(i);

            if (extendedTerm instanceof ListNONTERM) {
                extendedTerm = ((ListNONTERM) extendedTerm).getExtendedKind();
            } else if (extendedTerm instanceof QuestionNONTERM) {
                extendedTerm = ((QuestionNONTERM) extendedTerm).getExtendedKind();
            }

            calculateFirst(extendedTerm, result);
            i++;
        }

        if (i == terms.size()) return;
        calculateFirst(terms.get(i), result);
    }

    boolean isGenerativeKind(AnySyntaxKind kind) {
        return kind instanceof OrNONTERM || kind instanceof QuestionNONTERM || kind instanceof ListNONTERM;
    }


    List<SyntaxNode> postProcessParsingTree(List<SyntaxNode> currentNodes) {
        if (currentNodes == null) return null;

        List<SyntaxNode> result = new ArrayList<>();
        for (SyntaxNode currentNode : currentNodes) {
            MySyntaxNode myCurrentNode = (MySyntaxNode) currentNode;

            if (!isGenerativeKind(currentNode.kind())) {
                result.add(currentNode);
                myCurrentNode.syntaxNodes = postProcessParsingTree(myCurrentNode.syntaxNodes);
            } else {
                List<SyntaxNode> children = postProcessParsingTree(myCurrentNode.syntaxNodes);
                if (children != null) {
                    result.addAll(postProcessParsingTree(myCurrentNode.syntaxNodes));
                }
            }
        }
        return result;
    }


    @Override
    public ParseResult parse(String code) {

        List<Token> tokens = new MyLexer().lex(code);

        ArrayList<Diagnostic> diagnostics = new ArrayList<>();
        ArrayList<TextSpan> invalidRanges = new ArrayList<>();
        MySyntaxNode root = new MySyntaxNode(SyntaxKind.SOURCE_TEXT);

        parseRecursive(tokens, diagnostics, invalidRanges, root);
        root.syntaxNodes = postProcessParsingTree(root.syntaxNodes);

        return new MyParseResult(root, invalidRanges, diagnostics);
    }


    void parseRecursive(List<Token> tokens, ArrayList<Diagnostic> diagnostics, ArrayList<TextSpan> invalidRanges, MySyntaxNode currentNode) {
        if (currentPosition >= tokens.size()) {
            return;
        }
        Token token = tokens.get(currentPosition);

        AnySyntaxKind tokenKind = token.toSyntaxKind();
        AnySyntaxKind currentKind = currentNode.kind();

        List<AnySyntaxKind> first = new ArrayList<>();
        calculateFirst(currentKind, first);

        if (!first.contains(tokenKind) &&
                (currentKind instanceof QuestionNONTERM || currentKind instanceof ListNONTERM)) {
            return;
        }

        if (!first.contains(tokenKind)) {
            // TODO: code duplication

            invalidRanges.add(token.fullSpan());

            DiagnosticInfo info = new DiagnosticInfo(new UnrecognisedToken(), new Object[]{token});
            diagnostics.add(new Diagnostic(info, token.fullSpan(), null));
            currentPosition++;
            return;
        } else if (isTerminal(currentKind) && matchSyntaxKind(token, currentKind)) {
            currentNode.relatedToken = token;
            currentPosition++;
            return;
        } else if (isTerminal(currentKind)) {
            // TODO: code duplication

            invalidRanges.add(token.fullSpan());

            DiagnosticInfo info = new DiagnosticInfo(new UnrecognisedToken(), new Object[]{token});
            diagnostics.add(new Diagnostic(info, token.fullSpan(), null));
            currentPosition++;
            return;
        }
        switch (currentKind) {
            case OrNONTERM _ -> {
                boolean result = parseOR(tokens, diagnostics, invalidRanges, currentNode);
                if (!result) {
                    // TODO: code duplication
                    invalidRanges.add(token.fullSpan());

                    DiagnosticInfo info = new DiagnosticInfo(new UnrecognisedToken(), new Object[]{token});
                    diagnostics.add(new Diagnostic(info, token.fullSpan(), null));
                    currentPosition++;
                }
            }
            case ListNONTERM _ -> parseList(tokens, diagnostics, invalidRanges, currentNode);
            case QuestionNONTERM _ -> parseQuestion(tokens, diagnostics, invalidRanges, currentNode);
            default -> {
                List<AnySyntaxKind> rule = Grammar.getRules().get(currentKind);

                for (AnySyntaxKind kind : rule) {
                    currentNode.addChild(new MySyntaxNode(kind));
                    parseRecursive(tokens, diagnostics, invalidRanges, (MySyntaxNode) currentNode.slot(currentNode.slotCount() - 1));
                }
            }
        }


    }

    void parseQuestion(List<Token> tokens, ArrayList<Diagnostic> diagnostics, ArrayList<TextSpan> invalidRanges, MySyntaxNode currentNode) {
        if (currentPosition >= tokens.size()) {
            return;
        }
        AnySyntaxKind tokenKind = tokens.get(currentPosition).toSyntaxKind();
        AnySyntaxKind currentKind = ((QuestionNONTERM) currentNode.kind()).getExtendedKind();

        List<AnySyntaxKind> first = new ArrayList<>();
        calculateFirst(currentKind, first);

        if (isTerminal(currentKind) && matchSyntaxKind(tokens.get(currentPosition), currentKind)) {
            currentNode.addChild(new MySyntaxNode(currentKind, tokens.get(currentPosition++)));
        } else if (first.contains(tokenKind)) {
            currentNode.addChild(new MySyntaxNode(currentKind));
            parseRecursive(tokens, diagnostics, invalidRanges, (MySyntaxNode) currentNode.slot(currentNode.slotCount() - 1));
        }
    }

    void parseList(List<Token> tokens, ArrayList<Diagnostic> diagnostics, ArrayList<TextSpan> invalidRanges, MySyntaxNode currentNode) {
        boolean keepRecognising = true;
        while (keepRecognising) {
            keepRecognising = false;
            if (currentPosition >= tokens.size()) {
                break;
            }

            AnySyntaxKind tokenKind = tokens.get(currentPosition).toSyntaxKind();
            AnySyntaxKind currentKind = ((ListNONTERM) currentNode.kind()).getExtendedKind();

            List<AnySyntaxKind> first = new ArrayList<>();
            calculateFirst(currentKind, first);

            if (isTerminal(currentKind) && matchSyntaxKind(tokens.get(currentPosition), currentKind)) {
                currentNode.addChild(new MySyntaxNode(currentKind, tokens.get(currentPosition++)));
                keepRecognising = true;
            } else if (first.contains(tokenKind)) {
                currentNode.addChild(new MySyntaxNode(currentKind));
                parseRecursive(tokens, diagnostics, invalidRanges, (MySyntaxNode) currentNode.slot(currentNode.slotCount() - 1));
                keepRecognising = true;
            }
        }
    }


    boolean parseOR(List<Token> tokens, ArrayList<Diagnostic> diagnostics, ArrayList<TextSpan> invalidRanges, MySyntaxNode currentNode) {
        if (currentPosition >= tokens.size()) {
            return false;
        }
        AnySyntaxKind tokenKind = tokens.get(currentPosition).toSyntaxKind();
        OrNONTERM currentKind = (OrNONTERM) currentNode.kind();

        for (AnySyntaxKind possibleKind : currentKind.getPossibleKinds()) {
            List<AnySyntaxKind> first = new ArrayList<>();
            calculateFirst(possibleKind, first);

            if (isTerminal(possibleKind) && matchSyntaxKind(tokens.get(currentPosition), possibleKind)) {
                currentNode.addChild(new MySyntaxNode(possibleKind, tokens.get(currentPosition++)));
                return true;
            } else if (first.contains(tokenKind)) {
                currentNode.addChild(new MySyntaxNode(possibleKind));
                parseRecursive(tokens, diagnostics, invalidRanges, (MySyntaxNode) currentNode.slot(currentNode.slotCount() - 1));
                return true;
            }
        }
        return false;
    }

    boolean matchSyntaxKind(Token token, AnySyntaxKind currentKind) {
        return switch (currentKind) {
            case QuestionNONTERM questionNONTERM -> questionNONTERM.getExtendedKind() == token.toSyntaxKind();
            case ListNONTERM listNONTERM -> listNONTERM.getExtendedKind() == token.toSyntaxKind();
            default -> token.toSyntaxKind() == currentKind;
        };
    }
}
