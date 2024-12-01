package nsu.syspro.parser.nonterms;

import syspro.tm.parser.AnySyntaxKind;
import syspro.tm.parser.SyntaxKind;

import java.util.HashMap;

public enum AdditionalSyntaxKind implements AnySyntaxKind {
    // Post processed
    TYPE_NAME,
    EXPRESSION,
    PRIMARY,

    // Post process removable
    NAME_GENERIC,
    MEMBER_BLOCK,
    INNER_MEMBER_BLOCK,
    MEMBER_DEF,
    STATEMENT_BLOCK,
    INNER_STATEMENT_BLOCK,
    STATEMENT,
    TYPE_PARAMS,
    COLON_TYPE_NAME,
    ASSIGN,
    PARAM_WITH_COMMA,
    TYPE_PARAM_WITH_COMMA,
    TYPE_NAME_WITH_AMPERSAND,
    ELSE_STATEMENT,
    ATOM,
    DOT_EXPRESSION,
    INDEX_EXPRESSION,
    EXPRESSION_WITH_COMMA,
    UNARY_EXPRESSION,
    EXPRESSION_HATCH,
    BINARY_EXPRESSION_TAIL,
    PARENTHESIZED_LIST_EXPRESSION,
    TYPE_NAME_COMMA,

    // Cast to SyntaxKind.SEPARATED_LIST and SyntaxKind.LIST
    SEPARATED_LIST_PARAM_COMMA,
    SEPARATED_LIST_TYPE_NAME_AMPERSAND,
    SEPARATED_LIST_TYPE_PARAM_COMMA,
    SEPARATED_LIST_TYPE_NAME_COMMA,
    SEPARATED_LIST_EXPRESSION_COMMA,
    LIST_MEMBER_DEF,
    LIST_STATEMENT,
    LIST_TERMINAL,
    LIST_TYPE_DEFINITION;

    @Override
    public boolean isTerminal() {
        return false;
    }

    boolean isRemovable(AdditionalSyntaxKind kind) {
        return kind.ordinal() >= NAME_GENERIC.ordinal() &&
                kind.ordinal() <= TYPE_NAME_COMMA.ordinal();
    }

    boolean isListNonTerminal(AdditionalSyntaxKind kind) {
        return kind.ordinal() >= SEPARATED_LIST_PARAM_COMMA.ordinal() &&
                kind.ordinal() <= LIST_TYPE_DEFINITION.ordinal();
    }

    public final HashMap<AnySyntaxKind, AnySyntaxKind> additionalListToApiList = new HashMap<>() {{
        put(SEPARATED_LIST_PARAM_COMMA, SyntaxKind.SEPARATED_LIST);
        put(SEPARATED_LIST_TYPE_NAME_AMPERSAND, SyntaxKind.SEPARATED_LIST);
        put(SEPARATED_LIST_TYPE_PARAM_COMMA, SyntaxKind.SEPARATED_LIST);
        put(SEPARATED_LIST_EXPRESSION_COMMA, SyntaxKind.SEPARATED_LIST);
        put(SEPARATED_LIST_TYPE_NAME_COMMA, SyntaxKind.SEPARATED_LIST);

        put(LIST_MEMBER_DEF, SyntaxKind.LIST);
        put(LIST_STATEMENT, SyntaxKind.LIST);
        put(LIST_TERMINAL, SyntaxKind.LIST);
        put(LIST_TYPE_DEFINITION, SyntaxKind.LIST);
    }};
}
