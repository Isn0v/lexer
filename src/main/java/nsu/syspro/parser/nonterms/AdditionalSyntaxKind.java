package nsu.syspro.parser.nonterms;

import syspro.tm.parser.AnySyntaxKind;

public enum AdditionalSyntaxKind implements AnySyntaxKind {
    NAME_EXPRESSION, TYPE_NAME, NAME_GENERIC, MEMBER_BLOCK, INNER_MEMBER_BLOCK, MEMBER_DEF,
    STATEMENT_BLOCK, INNER_STATEMENT_BLOCK, STATEMENT, TYPE_PARAMS, PARAM, COLON_TYPE_NAME,
    ASSIGN, PARAM_WITH_COMMA, TYPE_PARAM, TYPE_PARAM_WITH_COMMA, TYPE_NAME_WITH_AMPERSAND, EXPRESSION, PRIMARY, ELSE_STATEMENT, ATOM, DOT_EXPRESSION, PARENTHESIZED_EXPRESSION, INDEX_EXPRESSION, EXPRESSION_WITH_COMMA, UNARY_EXPRESSION, EXPRESSION_HATCH, BINARY_EXPRESSION_TAIL;

    @Override
    public boolean isTerminal() {
        return false;
    }
}
