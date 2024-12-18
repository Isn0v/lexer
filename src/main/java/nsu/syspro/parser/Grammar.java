package nsu.syspro.parser;

import nsu.syspro.parser.nonterms.AdditionalSyntaxKind;
import nsu.syspro.parser.nonterms.ListNONTERM;
import nsu.syspro.parser.nonterms.OrNONTERM;
import nsu.syspro.parser.nonterms.QuestionNONTERM;
import syspro.tm.lexer.Keyword;
import syspro.tm.lexer.Symbol;
import syspro.tm.parser.AnySyntaxKind;
import syspro.tm.parser.SyntaxKind;

import java.util.HashMap;
import java.util.List;

public record Grammar() {


    public final static HashMap<AnySyntaxKind, List<AnySyntaxKind>> rules = new HashMap<>();

    static {

        // Root
        // SOURCE_TEXT := LIST_TYPE_DEFINITION
        rules.put(SyntaxKind.SOURCE_TEXT, List.of(
                new QuestionNONTERM(AdditionalSyntaxKind.LIST_TYPE_DEFINITION, true)
        ));

        // LIST_TYPE_DEFINITION := TYPE_DEFINITION*
        rules.put(AdditionalSyntaxKind.LIST_TYPE_DEFINITION, List.of(
                new ListNONTERM(SyntaxKind.TYPE_DEFINITION)
        ));
        // ----------------------------------------------------------------------------------------------------------------

        // Type name
        // TYPE_NAME := '?'? IDENTIFIER NAME_GENERIC?
        rules.put(AdditionalSyntaxKind.TYPE_NAME, List.of(
                new QuestionNONTERM(Symbol.QUESTION), SyntaxKind.IDENTIFIER,
                new QuestionNONTERM(AdditionalSyntaxKind.NAME_GENERIC)
        ));

        // NAME_GENERIC := '<' SEPARATED_LIST_TYPE_NAME_COMMA '>'
        rules.put(AdditionalSyntaxKind.NAME_GENERIC, List.of(
                Symbol.LESS_THAN, AdditionalSyntaxKind.SEPARATED_LIST_TYPE_NAME_COMMA, Symbol.GREATER_THAN
        ));


        // SEPARATED_LIST_TYPE_NAME_COMMA := TYPE_NAME TYPE_NAME_COMMA*
        rules.put(AdditionalSyntaxKind.SEPARATED_LIST_TYPE_NAME_COMMA, List.of(
                AdditionalSyntaxKind.TYPE_NAME,
                new ListNONTERM(AdditionalSyntaxKind.TYPE_NAME_COMMA)
        ));

        // TYPE_NAME_COMMA := ',' TYPE_NAME
        rules.put(AdditionalSyntaxKind.TYPE_NAME_COMMA, List.of(
                Symbol.COMMA, AdditionalSyntaxKind.TYPE_NAME
        ));
        // ----------------------------------------------------------------------------------------------------------------

        // Blocks
        // MEMBER_BLOCK := INNER_MEMBER_BLOCK?
        rules.put(AdditionalSyntaxKind.MEMBER_BLOCK,
                List.of(new QuestionNONTERM(AdditionalSyntaxKind.INNER_MEMBER_BLOCK)));
        // INNER_MEMBER_BLOCK := INDENT LIST_MEMBER_DEF DEDENT
        rules.put(AdditionalSyntaxKind.INNER_MEMBER_BLOCK, List.of(
                SyntaxKind.INDENT, AdditionalSyntaxKind.LIST_MEMBER_DEF, SyntaxKind.DEDENT
        ));

        // LIST_MEMBER_DEF := MEMBER_DEF MEMBER_DEF*
        rules.put(AdditionalSyntaxKind.LIST_MEMBER_DEF, List.of(
                AdditionalSyntaxKind.MEMBER_DEF,
                new ListNONTERM(AdditionalSyntaxKind.MEMBER_DEF)
        ));

        // STATEMENT_BLOCK := INNER_STATEMENT_BLOCK?
        rules.put(AdditionalSyntaxKind.STATEMENT_BLOCK,
                List.of(new QuestionNONTERM(AdditionalSyntaxKind.INNER_STATEMENT_BLOCK)));
        // INNER_STATEMENT_BLOCK := INDENT LIST_STATEMENT DEDENT
        rules.put(AdditionalSyntaxKind.INNER_STATEMENT_BLOCK, List.of(
                SyntaxKind.INDENT, AdditionalSyntaxKind.LIST_STATEMENT, SyntaxKind.DEDENT
        ));

        // LIST_STATEMENT := STATEMENT STATEMENT*
        rules.put(AdditionalSyntaxKind.LIST_STATEMENT, List.of(
                AdditionalSyntaxKind.STATEMENT,
                new ListNONTERM(AdditionalSyntaxKind.STATEMENT)
        ));

        // MEMBER_DEF := VARIABLE_DEF | FUNCTION_DEF
        rules.put(AdditionalSyntaxKind.MEMBER_DEF, List.of(
                new OrNONTERM(List.of(SyntaxKind.VARIABLE_DEFINITION, SyntaxKind.FUNCTION_DEFINITION))
        ));

        // STATEMENT := VARIABLE_DEFINITION_STATEMENT | ASSIGNMENT_STATEMENT | EXPRESSION_STATEMENT |
        // RETURN_STATEMENT | BREAK_STATEMENT | CONTINUE_STATEMENT | IF_STATEMENT | FOR_STATEMENT | WHILE_STATEMENT
        rules.put(AdditionalSyntaxKind.STATEMENT, List.of(
                new OrNONTERM(List.of(
                        SyntaxKind.VARIABLE_DEFINITION_STATEMENT,
                        SyntaxKind.ASSIGNMENT_STATEMENT,
                        SyntaxKind.EXPRESSION_STATEMENT,
                        SyntaxKind.RETURN_STATEMENT,
                        SyntaxKind.BREAK_STATEMENT,
                        SyntaxKind.CONTINUE_STATEMENT,
                        SyntaxKind.IF_STATEMENT,
                        SyntaxKind.FOR_STATEMENT,
                        SyntaxKind.WHILE_STATEMENT
                ))
        ));
        // ----------------------------------------------------------------------------------------------------------------

        // Definitions
        // TYPE_DEFINITION := (CLASS | OBJECT | INTERFACE) IDENTIFIER TYPE_PARAMS? TYPE_BOUND? MEMBER_BLOCK
        rules.put(SyntaxKind.TYPE_DEFINITION, List.of(
                new OrNONTERM(List.of(Keyword.CLASS, Keyword.OBJECT, Keyword.INTERFACE)),
                SyntaxKind.IDENTIFIER,
                new QuestionNONTERM(AdditionalSyntaxKind.TYPE_PARAMS),
                new QuestionNONTERM(SyntaxKind.TYPE_BOUND),
                AdditionalSyntaxKind.MEMBER_BLOCK
        ));

        // FUNCTION_DEFINITION := LIST_TERMINAL 'DEF'
        // (IDENTIFIER | 'THIS') '(' SEPARATED_LIST_PARAM_COMMA? ')' COLON_TYPE_NAME? STATEMENT_BLOCK
        rules.put(SyntaxKind.FUNCTION_DEFINITION, List.of(
                new QuestionNONTERM(AdditionalSyntaxKind.LIST_TERMINAL, true),
                Keyword.DEF,
                new OrNONTERM(List.of(SyntaxKind.IDENTIFIER, Keyword.THIS)),
                Symbol.OPEN_PAREN,
                new QuestionNONTERM(AdditionalSyntaxKind.SEPARATED_LIST_PARAM_COMMA),
                Symbol.CLOSE_PAREN,
                new QuestionNONTERM(AdditionalSyntaxKind.COLON_TYPE_NAME),
                AdditionalSyntaxKind.STATEMENT_BLOCK
        ));

        // LIST_TERMINAL := (ABSTRACT | VIRTUAL | OVERRIDE | NATIVE)*
        rules.put(AdditionalSyntaxKind.LIST_TERMINAL, List.of(
                new ListNONTERM(new OrNONTERM(List.of(Keyword.ABSTRACT, Keyword.VIRTUAL, Keyword.OVERRIDE, Keyword.NATIVE)))
        ));

        // SEPARATED_LIST_PARAM_COMMA := PARAMETER_DEFINITION PARAM_WITH_COMMA*
        rules.put(AdditionalSyntaxKind.SEPARATED_LIST_PARAM_COMMA, List.of(
                SyntaxKind.PARAMETER_DEFINITION,
                new ListNONTERM(AdditionalSyntaxKind.PARAM_WITH_COMMA)
        ));

        // VARIABLE_DEFINITION := ('VAR' | 'VAL') IDENTIFIER COLON_TYPE_NAME? ASSIGN?
        rules.put(SyntaxKind.VARIABLE_DEFINITION, List.of(
                new OrNONTERM(List.of(Keyword.VAR, Keyword.VAL)),
                SyntaxKind.IDENTIFIER,
                new QuestionNONTERM(AdditionalSyntaxKind.COLON_TYPE_NAME),
                new QuestionNONTERM(Symbol.EQUALS),
                new QuestionNONTERM(AdditionalSyntaxKind.ASSIGN)
        ));

        // TYPE_PARAMS := '<' SEPARATED_LIST_TYPE_PARAM_COMMA '>'
        rules.put(AdditionalSyntaxKind.TYPE_PARAMS, List.of(
                Symbol.LESS_THAN,
                AdditionalSyntaxKind.SEPARATED_LIST_TYPE_PARAM_COMMA,
                Symbol.GREATER_THAN
        ));

        // SEPARATED_LIST_TYPE_PARAM_COMMA := TYPE_PARAMETER_DEFINITION TYPE_PARAM_WITH_COMMA*
        rules.put(AdditionalSyntaxKind.SEPARATED_LIST_TYPE_PARAM_COMMA, List.of(
                SyntaxKind.TYPE_PARAMETER_DEFINITION,
                new ListNONTERM(AdditionalSyntaxKind.TYPE_PARAM_WITH_COMMA)
        ));

        // TYPE_PARAM_WITH_COMMA := ',' TYPE_PARAMETER_DEFINITION
        rules.put(AdditionalSyntaxKind.TYPE_PARAM_WITH_COMMA, List.of(
                Symbol.COMMA, SyntaxKind.TYPE_PARAMETER_DEFINITION
        ));

        // TYPE_PARAMETER_DEFINITION := IDENTIFIER TYPE_BOUND?
        rules.put(SyntaxKind.TYPE_PARAMETER_DEFINITION, List.of(
                SyntaxKind.IDENTIFIER,
                new QuestionNONTERM(SyntaxKind.TYPE_BOUND)
        ));

        // PARAMETER_DEFINITION := IDENTIFIER ':' TYPE_NAME
        rules.put(SyntaxKind.PARAMETER_DEFINITION, List.of(
                SyntaxKind.IDENTIFIER, Symbol.COLON, AdditionalSyntaxKind.TYPE_NAME
        ));

        // TYPE_BOUND := '<:' SEPARATED_LIST_TYPE_NAME_AMPERSAND
        rules.put(SyntaxKind.TYPE_BOUND, List.of(
                Symbol.BOUND, AdditionalSyntaxKind.SEPARATED_LIST_TYPE_NAME_AMPERSAND
        ));

        // SEPARATED_LIST_TYPE_NAME_AMPERSAND := TYPE_NAME TYPE_NAME_WITH_AMPERSAND*
        rules.put(AdditionalSyntaxKind.SEPARATED_LIST_TYPE_NAME_AMPERSAND, List.of(
                AdditionalSyntaxKind.TYPE_NAME,
                new ListNONTERM(AdditionalSyntaxKind.TYPE_NAME_WITH_AMPERSAND)
        ));

        // COLON_TYPE_NAME := ':' TYPE_NAME
        rules.put(AdditionalSyntaxKind.COLON_TYPE_NAME, List.of(
                Symbol.COLON, AdditionalSyntaxKind.TYPE_NAME
        ));

        // ASSIGN := '=' EXPRESSION
        rules.put(AdditionalSyntaxKind.ASSIGN, List.of(
                Symbol.EQUALS, AdditionalSyntaxKind.EXPRESSION
        ));

        // TYPE_NAME_WITH_AMPERSAND := '&' TYPE_NAME
        rules.put(AdditionalSyntaxKind.TYPE_NAME_WITH_AMPERSAND, List.of(
                Symbol.AMPERSAND, AdditionalSyntaxKind.TYPE_NAME
        ));

        // PARAM_WITH_COMMA := ',' PARAMETER_DEFINITION
        rules.put(AdditionalSyntaxKind.PARAM_WITH_COMMA, List.of(
                Symbol.COMMA, SyntaxKind.PARAMETER_DEFINITION
        ));
        // ----------------------------------------------------------------------------------------------------------------

        // Simple statements
        // VARIABLE_DEF_STATEMENT := VARIABLE_DEFINITION
        rules.put(SyntaxKind.VARIABLE_DEFINITION_STATEMENT, List.of(
                SyntaxKind.VARIABLE_DEFINITION
        ));

        // ASSIGNMENT_STATEMENT := PRIMARY '=' EXPRESSION
        rules.put(SyntaxKind.ASSIGNMENT_STATEMENT, List.of(
                AdditionalSyntaxKind.PRIMARY,
                Symbol.EQUALS,
                AdditionalSyntaxKind.EXPRESSION
        ));

        // EXPRESSION_STATEMENT := EXPRESSION
        rules.put(SyntaxKind.EXPRESSION_STATEMENT, List.of(
                AdditionalSyntaxKind.EXPRESSION
        ));
        //----------------------------------------------------------------------------------------------------------------

        // Stream management statements
        // RETURN_STATEMENT := 'RETURN' EXPRESSION?
        rules.put(SyntaxKind.RETURN_STATEMENT, List.of(
                Keyword.RETURN,
                new QuestionNONTERM(AdditionalSyntaxKind.EXPRESSION)
        ));

        // BREAK_STATEMENT := 'BREAK'
        rules.put(SyntaxKind.BREAK_STATEMENT, List.of(
                Keyword.BREAK
        ));

        // CONTINUE_STATEMENT := 'CONTINUE'
        rules.put(SyntaxKind.CONTINUE_STATEMENT, List.of(
                Keyword.CONTINUE
        ));
        // ----------------------------------------------------------------------------------------------------------------

        // Composite statements
        // IF_STATEMENT := 'IF' EXPRESSION STATEMENT_BLOCK ELSE_STATEMENT?
        rules.put(SyntaxKind.IF_STATEMENT, List.of(
                Keyword.IF,
                AdditionalSyntaxKind.EXPRESSION,
                AdditionalSyntaxKind.STATEMENT_BLOCK,
                new QuestionNONTERM(AdditionalSyntaxKind.ELSE_STATEMENT)
        ));

        // ELSE_STATEMENT := 'ELSE' STATEMENT_BLOCK
        rules.put(AdditionalSyntaxKind.ELSE_STATEMENT, List.of(
                Keyword.ELSE,
                AdditionalSyntaxKind.STATEMENT_BLOCK
        ));

        // WHILE_STATEMENT := 'WHILE' PRIMARY 'IN' EXPRESSION STATEMENT_BLOCK
        rules.put(SyntaxKind.WHILE_STATEMENT, List.of(
                Keyword.WHILE,
                AdditionalSyntaxKind.PRIMARY,
                Keyword.IN,
                AdditionalSyntaxKind.EXPRESSION,
                AdditionalSyntaxKind.STATEMENT_BLOCK
        ));

        // FOR_STATEMENT := 'FOR' PRIMARY 'IN' EXPRESSION STATEMENT_BLOCK
        rules.put(SyntaxKind.FOR_STATEMENT, List.of(
                Keyword.FOR,
                AdditionalSyntaxKind.PRIMARY,
                Keyword.IN,
                AdditionalSyntaxKind.EXPRESSION,
                AdditionalSyntaxKind.STATEMENT_BLOCK
        ));
        // ----------------------------------------------------------------------------------------------------------------

        // Primary
        // PRIMARY := ATOM ( DOT_EXPRESSION | PARENTHESIZED_LIST_EXPRESSION | INDEX_EXPRESSION )*
        rules.put(AdditionalSyntaxKind.PRIMARY, List.of(
                AdditionalSyntaxKind.ATOM,
                new ListNONTERM(
                        new OrNONTERM(List.of(
                                AdditionalSyntaxKind.DOT_EXPRESSION,
                                AdditionalSyntaxKind.PARENTHESIZED_LIST_EXPRESSION,
                                AdditionalSyntaxKind.INDEX_EXPRESSION))
                )
        ));

        // DOT_EXPRESSION := '.' IDENTIFIER
        rules.put(AdditionalSyntaxKind.DOT_EXPRESSION, List.of(
                Symbol.DOT,
                SyntaxKind.IDENTIFIER
        ));

        // PARENTHESIZED_LIST_EXPRESSION := '(' SEPARATED_LIST_EXPRESSION_COMMA? ')'
        rules.put(AdditionalSyntaxKind.PARENTHESIZED_LIST_EXPRESSION, List.of(
                Symbol.OPEN_PAREN,
                new QuestionNONTERM(AdditionalSyntaxKind.SEPARATED_LIST_EXPRESSION_COMMA),
                Symbol.CLOSE_PAREN
        ));

        // SEPARATED_LIST_EXPRESSION_COMMA := EXPRESSION EXPRESSION_WITH_COMMA*
        rules.put(AdditionalSyntaxKind.SEPARATED_LIST_EXPRESSION_COMMA, List.of(
                AdditionalSyntaxKind.EXPRESSION,
                new ListNONTERM(AdditionalSyntaxKind.EXPRESSION_WITH_COMMA)
        ));

        // INDEX_EXPRESSION := '[' EXPRESSION ']'
        rules.put(AdditionalSyntaxKind.INDEX_EXPRESSION, List.of(
                Symbol.OPEN_BRACKET,
                AdditionalSyntaxKind.EXPRESSION,
                Symbol.CLOSE_BRACKET
        ));

        // EXPRESSION_WITH_COMMA := ',' EXPRESSION
        rules.put(AdditionalSyntaxKind.EXPRESSION_WITH_COMMA, List.of(
                Symbol.COMMA,
                AdditionalSyntaxKind.EXPRESSION
        ));

        // ATOM := IDENTIFIER | THIS_EXPRESSION | SUPER_EXPRESSION | NULL_LITERAL_EXPRESSION |
        // TYPE_NAME | BOOLEAN |
        // STRING_LITERAL_EXPRESSION | RUNE_LITERAL_EXPRESSION | INTEGER_LITERAL_EXPRESSION | PARENTHESIZED_EXPRESSION
        rules.put(AdditionalSyntaxKind.ATOM, List.of(new OrNONTERM(List.of(
                SyntaxKind.IDENTIFIER,
                SyntaxKind.THIS_EXPRESSION,
                SyntaxKind.SUPER_EXPRESSION,
                SyntaxKind.NULL_LITERAL_EXPRESSION,
                AdditionalSyntaxKind.TYPE_NAME,
                SyntaxKind.BOOLEAN, // will be post processed to TRUE_LITERAL_EXPRESSION or FALSE_LITERAL_EXPRESSION
                SyntaxKind.STRING_LITERAL_EXPRESSION,
                SyntaxKind.RUNE_LITERAL_EXPRESSION,
                SyntaxKind.INTEGER_LITERAL_EXPRESSION,
                SyntaxKind.PARENTHESIZED_EXPRESSION
        ))));

        // THIS_EXPRESSION := 'THIS'
        rules.put(SyntaxKind.THIS_EXPRESSION, List.of(
                Keyword.THIS
        ));

        // SUPER_EXPRESSION := 'SUPER'
        rules.put(SyntaxKind.SUPER_EXPRESSION, List.of(
                Keyword.SUPER
        ));

        // NULL_LITERAL_EXPRESSION := 'NULL'
        rules.put(SyntaxKind.NULL_LITERAL_EXPRESSION, List.of(
                Keyword.NULL
        ));

        // STRING_LITERAL_EXPRESSION := STRING_LITERAL
        rules.put(SyntaxKind.STRING_LITERAL_EXPRESSION, List.of(
                SyntaxKind.STRING
        ));

        // RUNE_LITERAL_EXPRESSION := RUNE_LITERAL
        rules.put(SyntaxKind.RUNE_LITERAL_EXPRESSION, List.of(
                SyntaxKind.RUNE
        ));

        // INTEGER_LITERAL_EXPRESSION := INTEGER_LITERAL
        rules.put(SyntaxKind.INTEGER_LITERAL_EXPRESSION, List.of(
                SyntaxKind.INTEGER
        ));

        // PARENTHESIZED_EXPRESSION := '(' EXPRESSION ')'
        rules.put(SyntaxKind.PARENTHESIZED_EXPRESSION, List.of(
                Symbol.OPEN_PAREN,
                AdditionalSyntaxKind.EXPRESSION,
                Symbol.CLOSE_PAREN
        ));

        // ----------------------------------------------------------------------------------------------------------------

        // Expression
        // EXPRESSION := (PRIMARY | UNARY_EXPRESSION) EXPRESSION_HATCH?
        rules.put(AdditionalSyntaxKind.EXPRESSION, List.of(
                new OrNONTERM(List.of(
                        AdditionalSyntaxKind.PRIMARY,
                        AdditionalSyntaxKind.UNARY_EXPRESSION
                )),
                new QuestionNONTERM(AdditionalSyntaxKind.EXPRESSION_HATCH)
        ));

        // UNARY_EXPRESSION := (! | - | + | ~) EXPRESSION
        rules.put(AdditionalSyntaxKind.UNARY_EXPRESSION, List.of(
                new OrNONTERM(List.of(
                        Symbol.EXCLAMATION,
                        Symbol.MINUS,
                        Symbol.PLUS,
                        Symbol.TILDE
                )),
                AdditionalSyntaxKind.EXPRESSION
        ));

        // EXPRESSION_HATCH := BINARY_EXPRESSION_TAIL* | IS_EXPRESSION
        rules.put(AdditionalSyntaxKind.EXPRESSION_HATCH, List.of(
                new OrNONTERM(List.of(
                        new ListNONTERM(AdditionalSyntaxKind.BINARY_EXPRESSION_TAIL),
                        SyntaxKind.IS_EXPRESSION
                )),
                new QuestionNONTERM(SyntaxKind.IDENTIFIER)
        ));

        // BINARY_EXPRESSION_TAIL := ( '+' | '-' | '*' | '/' | '%' | '==' |
        // '!=' | '<' | '>' | '<=' | '>=' | '&&' | '||' | '&' | '|' | '^' | '<<' | '>>') EXPRESSION
        rules.put(AdditionalSyntaxKind.BINARY_EXPRESSION_TAIL, List.of(
                new OrNONTERM(List.of(
                        Symbol.PLUS,
                        Symbol.MINUS,
                        Symbol.ASTERISK,
                        Symbol.SLASH,
                        Symbol.PERCENT,
                        Symbol.EQUALS_EQUALS,
                        Symbol.EXCLAMATION_EQUALS,
                        Symbol.LESS_THAN,
                        Symbol.GREATER_THAN,
                        Symbol.LESS_THAN_EQUALS,
                        Symbol.GREATER_THAN_EQUALS,
                        Symbol.AMPERSAND_AMPERSAND,
                        Symbol.BAR_BAR,
                        Symbol.AMPERSAND,
                        Symbol.BAR,
                        Symbol.CARET,
                        Symbol.LESS_THAN_LESS_THAN,
                        Symbol.GREATER_THAN_GREATER_THAN
                )),
                AdditionalSyntaxKind.EXPRESSION
        ));

        // IS_EXPRESSION := 'IS' TYPE_NAME IDENTIFIER?
        rules.put(SyntaxKind.IS_EXPRESSION, List.of(
                Keyword.IS,
                AdditionalSyntaxKind.TYPE_NAME,
                new QuestionNONTERM(SyntaxKind.IDENTIFIER)
        ));
        // ----------------------------------------------------------------------------------------------------------------


    }


}
