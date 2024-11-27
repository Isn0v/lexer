package nsu.syspro.parser;

import nsu.syspro.lexer.MyLexer;
import nsu.syspro.parser.nonterms.*;
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
        if (kind == AdditionalSyntaxKind.TERMINAL || isTerminal(kind)) {
            result.add(kind);
            return;
        }

        List<AnySyntaxKind> terms = Grammar.getRules().get(kind);

        int i = 0;
        while (i < terms.size() &&
                (terms.get(i) instanceof ListNONTERM || terms.get(i) instanceof QuestionNONTERM)) {
            AnySyntaxKind extendedTerm = terms.get(i);

            if (extendedTerm instanceof ListNONTERM) {
                extendedTerm = ((ListNONTERM) extendedTerm).getExtendedKind();
            } else if (extendedTerm instanceof QuestionNONTERM) {
                extendedTerm = ((QuestionNONTERM) extendedTerm).getExtendedKind();
            }

            calculateFirst(extendedTerm, result);
            i++;
        }

        AnySyntaxKind lastTerm = terms.get(i);
        if (lastTerm instanceof OrNONTERM) {
            List<AnySyntaxKind> orTerms = ((OrNONTERM) lastTerm).getPossibleKinds();
            for (AnySyntaxKind orTerm : orTerms) {
                calculateFirst(orTerm, result);
            }
        } else {
            calculateFirst(lastTerm, result);
        }
    }


    @Override
    public ParseResult parse(String code) {

        List<Token> tokens = new MyLexer().lex(code);

        ArrayList<Diagnostic> diagnostics = new ArrayList<>();
        ArrayList<TextSpan> invalidRanges = new ArrayList<>();
        MySyntaxNode root = new MySyntaxNode(SyntaxKind.SOURCE_TEXT);

        parseRecursive(tokens, diagnostics, invalidRanges, root);

        return new MyParseResult(root, invalidRanges, diagnostics);
    }

    void parseRecursive(List<Token> tokens,
                        ArrayList<Diagnostic> diagnostics, ArrayList<TextSpan> invalidRanges,
                        MySyntaxNode currentNode) {
        Token token = tokens.get(currentPosition);
        AnySyntaxKind tokenKind = token.toSyntaxKind();
        AnySyntaxKind currentKind = currentNode.kind();

        List<AnySyntaxKind> first = new ArrayList<>();
        calculateFirst(currentKind, first);

        if (!first.contains(tokenKind)) {
            // TODO: code duplication

            invalidRanges.add(token.fullSpan());

            DiagnosticInfo info = new DiagnosticInfo(new UnrecognisedToken(), new Object[]{token});
            diagnostics.add(new Diagnostic(info, token.fullSpan(), null));
            currentPosition++;
        }

        if (isTerminal(currentKind) && matchSyntaxKind(token, currentKind)) {
            currentNode.relatedToken = token;
            currentPosition++;
            return;
        }
        switch (currentKind) {
            case OrNONTERM orNONTERM -> {
                boolean result = parseOR(tokens, diagnostics, invalidRanges, currentNode);
                if (!result) {
                    // TODO: code duplication
                    invalidRanges.add(token.fullSpan());

                    DiagnosticInfo info = new DiagnosticInfo(new UnrecognisedToken(), new Object[]{token});
                    diagnostics.add(new Diagnostic(info, token.fullSpan(), null));
                    currentPosition++;
                }
            }
            case SeparatedListNONTERM _ -> parseSeparatedList(tokens, diagnostics, invalidRanges, currentNode);
            case ListNONTERM _ -> parseList(tokens, diagnostics, invalidRanges, currentNode);
            case QuestionNONTERM _ -> parseQuestion(tokens, diagnostics, invalidRanges, currentNode);
            default -> {
                List<AnySyntaxKind> rule = Grammar.getRules().get(currentKind);

                for (AnySyntaxKind kind : rule) {
                    currentNode.addChild(new MySyntaxNode(kind));
                    parseRecursive(tokens, diagnostics, invalidRanges,
                            (MySyntaxNode) currentNode.slot(currentNode.slotCount() - 1));
                }
            }
        }


    }

    void parseSeparatedList(List<Token> tokens,
                            ArrayList<Diagnostic> diagnostics, ArrayList<TextSpan> invalidRanges,
                            MySyntaxNode currentNode) {
        AnySyntaxKind tokenKind = tokens.get(currentPosition).toSyntaxKind();

        AnySyntaxKind currentKind = ((SeparatedListNONTERM) currentNode.kind()).getExtendedKind();
        AnySyntaxKind separatorKind = ((SeparatedListNONTERM) currentNode.kind()).getSeparator();

        List<AnySyntaxKind> first = new ArrayList<>();
        calculateFirst(currentKind, first);

        boolean keepRecognising = true;
        int counter = 0;

        while (keepRecognising) {
            keepRecognising = false;
            if (counter % 2 == 0) {
                if (first.contains(tokenKind)) {
                    currentNode.addChild(new MySyntaxNode(currentKind));
                    parseRecursive(tokens, diagnostics, invalidRanges,
                            (MySyntaxNode) currentNode.slot(currentNode.slotCount() - 1));
                    keepRecognising = true;
                } else if (isTerminal(currentKind) && matchSyntaxKind(tokens.get(currentPosition), currentKind)) {
                    currentNode.relatedToken = tokens.get(currentPosition++);
                    keepRecognising = true;
                } else {
                    break;
                }
            } else if (isTerminal(tokenKind) && matchSyntaxKind(tokens.get(currentPosition), separatorKind)) {
                currentNode.relatedToken = tokens.get(currentPosition++);
                keepRecognising = true;
            } else {
                Token token = tokens.get(currentPosition);
                int tokenStart = token.start, length = token.end - tokenStart;

                TextSpan span = new TextSpan(tokenStart, length);
                invalidRanges.add(span);

                DiagnosticInfo info = new DiagnosticInfo(new WrongSeparator(), new Object[]{token});
                diagnostics.add(new Diagnostic(info, span, null));
                currentPosition++;
            }
            counter++;
        }
    }

    void parseQuestion(List<Token> tokens,
                       ArrayList<Diagnostic> diagnostics, ArrayList<TextSpan> invalidRanges,
                       MySyntaxNode currentNode) {
        AnySyntaxKind tokenKind = tokens.get(currentPosition).toSyntaxKind();
        AnySyntaxKind currentKind = ((QuestionNONTERM) currentNode.kind()).getExtendedKind();

        List<AnySyntaxKind> first = new ArrayList<>();
        calculateFirst(currentKind, first);

        if (first.contains(tokenKind)) {
            currentNode.addChild(new MySyntaxNode(currentKind));
            parseRecursive(tokens, diagnostics, invalidRanges,
                    (MySyntaxNode) currentNode.slot(currentNode.slotCount() - 1));
        } else if (isTerminal(currentKind) && matchSyntaxKind(tokens.get(currentPosition), currentKind)) {
            currentNode.relatedToken = tokens.get(currentPosition++);
        }
    }

    void parseList(List<Token> tokens,
                   ArrayList<Diagnostic> diagnostics, ArrayList<TextSpan> invalidRanges,
                   MySyntaxNode currentNode) {
        AnySyntaxKind tokenKind = tokens.get(currentPosition).toSyntaxKind();
        AnySyntaxKind currentKind = ((ListNONTERM) currentNode.kind()).getExtendedKind();

        List<AnySyntaxKind> first = new ArrayList<>();
        calculateFirst(currentKind, first);

        boolean keepRecognising = true;

        while (keepRecognising) {
            keepRecognising = false;

            if (first.contains(tokenKind)) {
                currentNode.addChild(new MySyntaxNode(currentKind));
                parseRecursive(tokens, diagnostics, invalidRanges,
                        (MySyntaxNode) currentNode.slot(currentNode.slotCount() - 1));
                keepRecognising = true;
            } else if (isTerminal(currentKind) && matchSyntaxKind(tokens.get(currentPosition), currentKind)) {
                currentNode.relatedToken = tokens.get(currentPosition++);
                keepRecognising = true;
            }
        }
    }


    boolean parseOR(List<Token> tokens,
                    ArrayList<Diagnostic> diagnostics, ArrayList<TextSpan> invalidRanges, MySyntaxNode currentNode) {
        AnySyntaxKind tokenKind = tokens.get(currentPosition).toSyntaxKind();
        OrNONTERM currentKind = (OrNONTERM) currentNode.kind();

        for (AnySyntaxKind possibleKind : currentKind.getPossibleKinds()) {
            List<AnySyntaxKind> first = new ArrayList<>();
            calculateFirst(possibleKind, first);

            if (isTerminal(possibleKind) && matchSyntaxKind(tokens.get(currentPosition), possibleKind)) {
                currentNode.relatedToken = tokens.get(currentPosition++);
                return true;
            } else if (first.contains(tokenKind)) {
                currentNode.addChild(new MySyntaxNode(possibleKind));
                parseRecursive(tokens, diagnostics, invalidRanges,
                        (MySyntaxNode) currentNode.slot(currentNode.slotCount() - 1));
                return true;
            }
        }
        return false;
    }

    boolean matchSyntaxKind(Token token, AnySyntaxKind currentKind) {

        return token.toSyntaxKind() == currentKind;
    }
}
