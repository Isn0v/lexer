package nsu.syspro.parser;

import nsu.syspro.lexer.MyLexer;
import nsu.syspro.parser.nonterms.AdditionalSyntaxKind;
import nsu.syspro.parser.nonterms.ListNONTERM;
import nsu.syspro.parser.nonterms.OrNONTERM;
import nsu.syspro.parser.nonterms.QuestionNONTERM;
import syspro.tm.lexer.BooleanLiteralToken;
import syspro.tm.lexer.Token;
import syspro.tm.parser.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MyParser implements Parser {

    private int currentPosition = 0;

    boolean isTerminal(AnySyntaxKind kind) {
        return kind.isTerminal();
    }

    void calculateFirst(AnySyntaxKind kind, List<AnySyntaxKind> result,
                        HashMap<AnySyntaxKind, List<AnySyntaxKind>> rules) {
        if (kind instanceof ListNONTERM) {
            kind = ((ListNONTERM) kind).getExtendedKind();
            calculateFirst(kind, result, rules);
            return;
        } else if (kind instanceof QuestionNONTERM) {
            kind = ((QuestionNONTERM) kind).getExtendedKind();
            calculateFirst(kind, result, rules);
            return;
        } else if (kind instanceof OrNONTERM) {
            List<AnySyntaxKind> orTerms = ((OrNONTERM) kind).getPossibleKinds();
            for (AnySyntaxKind orTerm : orTerms) {
                calculateFirst(orTerm, result, rules);
            }
            return;
        }


        if (isTerminal(kind)) {
            result.add(kind);
            return;
        }

        List<AnySyntaxKind> terms = rules.get(kind);

        int i = 0;
        while (i < terms.size() && (terms.get(i) instanceof ListNONTERM || terms.get(i) instanceof QuestionNONTERM)) {
            AnySyntaxKind extendedTerm = terms.get(i);

            if (extendedTerm instanceof ListNONTERM) {
                extendedTerm = ((ListNONTERM) extendedTerm).getExtendedKind();
            } else if (extendedTerm instanceof QuestionNONTERM) {
                extendedTerm = ((QuestionNONTERM) extendedTerm).getExtendedKind();
            }

            calculateFirst(extendedTerm, result, rules);
            i++;
        }

        if (i == terms.size()) return;
        calculateFirst(terms.get(i), result, rules);
    }

    boolean isGenerativeKind(AnySyntaxKind kind) {
        return kind instanceof OrNONTERM || kind instanceof QuestionNONTERM || kind instanceof ListNONTERM;
    }


    List<SyntaxNode> postProcessParsingTree(List<SyntaxNode> currentNodes) {
        if (currentNodes == null) return null;

        List<SyntaxNode> result = new ArrayList<>();
        for (SyntaxNode currentNode : currentNodes) {
            MySyntaxNode myCurrentNode = (MySyntaxNode) currentNode;
            AnySyntaxKind currentKind = currentNode.kind();

            if (isGenerativeKind(currentKind) ||
                    (currentKind instanceof AdditionalSyntaxKind && ((AdditionalSyntaxKind) currentKind).isRemovable())
            ) {
                List<SyntaxNode> children = postProcessParsingTree(myCurrentNode.syntaxNodes);
                if (children != null) {
                    result.addAll(children);
                }
            } else if (currentKind instanceof AdditionalSyntaxKind && ((AdditionalSyntaxKind) currentKind).isListNonTerminal()) {
                List<SyntaxNode> children = postProcessParsingTree(myCurrentNode.syntaxNodes);

                MySyntaxNode node = new MySyntaxNode(AdditionalSyntaxKind.additionalListToApiList.get(currentKind));
                node.addChildren(children);
                result.add(node);
            } else if (currentKind == SyntaxKind.BOOLEAN) {
                boolean value = ((BooleanLiteralToken) currentNode.token()).value;
                AnySyntaxKind boolean_literal = value ? SyntaxKind.TRUE_LITERAL_EXPRESSION : SyntaxKind.FALSE_LITERAL_EXPRESSION;
                MySyntaxNode node = new MySyntaxNode(boolean_literal);
                node.addChild(new MySyntaxNode(currentNode.kind(), currentNode.token()));
                result.add(node);
            }
            else if (currentKind == AdditionalSyntaxKind.PRIMARY){
                SyntaxNode atom = myCurrentNode.syntaxNodes.getFirst();
                ArrayList<SyntaxNode> processedAtom = (ArrayList<SyntaxNode>) postProcessParsingTree(List.of(atom));

                // always ListNONTERM according to Grammar
                MySyntaxNode listNonTerm = (MySyntaxNode) myCurrentNode.syntaxNodes.get(1);

                List<SyntaxNode> tail = listNonTerm.syntaxNodes;
                if (tail == null) {
                    result.addAll(processedAtom);
                    continue;
                }
                for (SyntaxNode node : tail) {
                    // node is always OrNONTERM according to Grammar, and always has single child,
                    // which is DOT_EXPRESSION, PARENTHESIZED_LIST_EXPRESSION or INDEX_EXPRESSION
                    assert node.slotCount() == 1 : "slotCount != 1";
                    MySyntaxNode myNode = (MySyntaxNode) node.slot(0);

                    AnySyntaxKind kindToExtend = switch (myNode.kind()){
                        case AdditionalSyntaxKind.DOT_EXPRESSION -> SyntaxKind.MEMBER_ACCESS_EXPRESSION;
                        case AdditionalSyntaxKind.PARENTHESIZED_LIST_EXPRESSION -> SyntaxKind.INVOCATION_EXPRESSION;
                        case AdditionalSyntaxKind.INDEX_EXPRESSION -> SyntaxKind.INDEX_EXPRESSION;
                        default -> throw new RuntimeException("Unknown node kind: " + myNode.kind());
                    };

                    List<SyntaxNode> processedNode = postProcessParsingTree(List.of(myNode));
                    processedAtom.addAll(processedNode);

                    MySyntaxNode extendedNode = new MySyntaxNode(kindToExtend);
                    extendedNode.addChildren(processedAtom);

                    processedAtom = new ArrayList<>(List.of(extendedNode));
                }
                result.add(processedAtom.getFirst());
            }
            else {
                result.add(currentNode);
                myCurrentNode.syntaxNodes = postProcessParsingTree(myCurrentNode.syntaxNodes);
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

        parseRecursive(tokens, diagnostics, invalidRanges, root, Grammar.rules);
        root.syntaxNodes = postProcessParsingTree(root.syntaxNodes);

        return new MyParseResult(root, invalidRanges, diagnostics);
    }


    void parseRecursive(List<Token> tokens, ArrayList<Diagnostic> diagnostics, ArrayList<TextSpan> invalidRanges,
                        MySyntaxNode currentNode, HashMap<AnySyntaxKind, List<AnySyntaxKind>> rules) {
        if (currentPosition >= tokens.size()) {
            return;
        }
        Token token = tokens.get(currentPosition);

        AnySyntaxKind tokenKind = token.toSyntaxKind();
        AnySyntaxKind currentKind = currentNode.kind();

        List<AnySyntaxKind> first = new ArrayList<>();
        calculateFirst(currentKind, first, rules);

        if (!first.contains(tokenKind) &&
                ((currentKind instanceof QuestionNONTERM && !((QuestionNONTERM) currentKind).saveInParsingTree)
                        || currentKind instanceof ListNONTERM)) {
            return;
        } else if (!first.contains(tokenKind) &&
                (currentKind instanceof QuestionNONTERM && ((QuestionNONTERM) currentKind).saveInParsingTree)) {
            currentNode.addChild(new MySyntaxNode(
                    ((QuestionNONTERM) currentKind).getExtendedKind())
            );
            return;

        } else if (!first.contains(tokenKind)) {
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
                boolean result = parseOR(tokens, diagnostics, invalidRanges, currentNode, rules);
                if (!result) {
                    // TODO: code duplication
                    invalidRanges.add(token.fullSpan());

                    DiagnosticInfo info = new DiagnosticInfo(new UnrecognisedToken(), new Object[]{token});
                    diagnostics.add(new Diagnostic(info, token.fullSpan(), null));
                    currentPosition++;
                }
            }
            case ListNONTERM _ -> parseList(tokens, diagnostics, invalidRanges, currentNode, rules);
            case QuestionNONTERM _ -> parseQuestion(tokens, diagnostics, invalidRanges, currentNode, rules);
            default -> {
                List<AnySyntaxKind> rule = rules.get(currentKind);

                for (AnySyntaxKind kind : rule) {
                    currentNode.addChild(new MySyntaxNode(kind));
                    parseRecursive(tokens, diagnostics, invalidRanges,
                            (MySyntaxNode) currentNode.slot(currentNode.slotCount() - 1),
                            rules);
                }
            }
        }


    }

    void parseQuestion(List<Token> tokens, ArrayList<Diagnostic> diagnostics,
                       ArrayList<TextSpan> invalidRanges, MySyntaxNode currentNode,
                       HashMap<AnySyntaxKind, List<AnySyntaxKind>> rules) {
        if (currentPosition >= tokens.size()) {
            return;
        }
        AnySyntaxKind tokenKind = tokens.get(currentPosition).toSyntaxKind();
        AnySyntaxKind currentKind = ((QuestionNONTERM) currentNode.kind()).getExtendedKind();

        List<AnySyntaxKind> first = new ArrayList<>();
        calculateFirst(currentKind, first, rules);

        if (isTerminal(currentKind) && matchSyntaxKind(tokens.get(currentPosition), currentKind)) {
            currentNode.addChild(new MySyntaxNode(currentKind, tokens.get(currentPosition++)));
        } else if (first.contains(tokenKind)) {
            currentNode.addChild(new MySyntaxNode(currentKind));
            parseRecursive(tokens, diagnostics, invalidRanges,
                    (MySyntaxNode) currentNode.slot(currentNode.slotCount() - 1),
                    rules);
        }
    }

    void parseList(List<Token> tokens, ArrayList<Diagnostic> diagnostics,
                   ArrayList<TextSpan> invalidRanges, MySyntaxNode currentNode,
                   HashMap<AnySyntaxKind, List<AnySyntaxKind>> rules) {
        boolean keepRecognising = true;
        while (keepRecognising) {
            keepRecognising = false;
            if (currentPosition >= tokens.size()) {
                break;
            }

            AnySyntaxKind tokenKind = tokens.get(currentPosition).toSyntaxKind();
            AnySyntaxKind currentKind = ((ListNONTERM) currentNode.kind()).getExtendedKind();

            List<AnySyntaxKind> first = new ArrayList<>();
            calculateFirst(currentKind, first, rules);

            if (isTerminal(currentKind) && matchSyntaxKind(tokens.get(currentPosition), currentKind)) {
                currentNode.addChild(new MySyntaxNode(currentKind, tokens.get(currentPosition++)));
                keepRecognising = true;
            } else if (first.contains(tokenKind)) {
                currentNode.addChild(new MySyntaxNode(currentKind));
                parseRecursive(tokens, diagnostics, invalidRanges,
                        (MySyntaxNode) currentNode.slot(currentNode.slotCount() - 1),
                        rules);
                keepRecognising = true;
            }
        }
    }


    boolean parseOR(List<Token> tokens, ArrayList<Diagnostic> diagnostics,
                    ArrayList<TextSpan> invalidRanges, MySyntaxNode currentNode,
                    HashMap<AnySyntaxKind, List<AnySyntaxKind>> rules) {
        if (currentPosition >= tokens.size()) {
            return false;
        }
        AnySyntaxKind tokenKind = tokens.get(currentPosition).toSyntaxKind();
        OrNONTERM currentKind = (OrNONTERM) currentNode.kind();

        for (AnySyntaxKind possibleKind : currentKind.getPossibleKinds()) {
            List<AnySyntaxKind> first = new ArrayList<>();
            calculateFirst(possibleKind, first, rules);

            if (isTerminal(possibleKind) && matchSyntaxKind(tokens.get(currentPosition), possibleKind)) {
                currentNode.addChild(new MySyntaxNode(possibleKind, tokens.get(currentPosition++)));
                return true;
            } else if (first.contains(tokenKind)) {
                currentNode.addChild(new MySyntaxNode(possibleKind));
                parseRecursive(tokens, diagnostics, invalidRanges,
                        (MySyntaxNode) currentNode.slot(currentNode.slotCount() - 1),
                        rules);
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
