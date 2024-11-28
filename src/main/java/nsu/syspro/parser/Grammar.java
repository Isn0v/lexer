package nsu.syspro.parser;

import nsu.syspro.parser.nonterms.*;
import syspro.tm.lexer.Keyword;
import syspro.tm.lexer.Symbol;
import syspro.tm.parser.AnySyntaxKind;
import syspro.tm.parser.SyntaxKind;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public record Grammar() {

    private final static HashMap<AnySyntaxKind, List<AnySyntaxKind>> rules = new HashMap<>();

    static {
        // Non-terminals
        rules.put(SyntaxKind.SOURCE_TEXT, List.of(new ListNONTERM(SyntaxKind.TYPE_DEFINITION)));
        rules.put(SyntaxKind.TYPE_BOUND, List.of(Symbol.BOUND, new SeparatedListNONTERM(AdditionalSyntaxKind.NAME_EXPRESSION, Symbol.AMPERSAND)));

        // Definitions
        // TYPE_DEFINITION := Terminal IDENTIFIER LESS_THAN? SEPARATED_LIST[TYPE_PARAMETER_DEFINITION, COMMA]?
        //                    GREATER_THAN? TYPE_BOUND? INDENT? LIST[Definition]? DEDENT?
        rules.put(SyntaxKind.TYPE_DEFINITION, List.of(
                new ListNONTERM(new OrNONTERM(List.of(Keyword.CLASS, Keyword.OBJECT, Keyword.INTERFACE))),
                SyntaxKind.IDENTIFIER,
                new QuestionNONTERM(Symbol.LESS_THAN),
                new QuestionNONTERM(new SeparatedListNONTERM(SyntaxKind.TYPE_PARAMETER_DEFINITION, Symbol.COMMA)),
                new QuestionNONTERM(Symbol.GREATER_THAN),
                new QuestionNONTERM(SyntaxKind.TYPE_BOUND),
                new QuestionNONTERM(SyntaxKind.INDENT),
                new QuestionNONTERM(new ListNONTERM(AdditionalSyntaxKind.DEFINITION)),
                new QuestionNONTERM(SyntaxKind.DEDENT)));
        // FUNCTION_DEFINITION := LIST[Terminal] DEF Terminal OPEN_PAREN SEPARATED_LIST[PARAMETER_DEFINITION, COMMA]?
        //                        CLOSE_PAREN COLON? NameExpression? INDENT? LIST[Statement]? DEDENT?
        rules.put(SyntaxKind.FUNCTION_DEFINITION,
                List.of(
                        new ListNONTERM(
                                new OrNONTERM(List.of(Keyword.ABSTRACT, Keyword.OVERRIDE,
                                        Keyword.NATIVE, Keyword.VIRTUAL))),
                        Keyword.DEF,
                        AdditionalSyntaxKind.TERMINAL,
                        Symbol.OPEN_PAREN,
                        new QuestionNONTERM(new SeparatedListNONTERM(SyntaxKind.PARAMETER_DEFINITION, Symbol.COMMA)),
                        Symbol.CLOSE_PAREN,
                        new QuestionNONTERM(Symbol.COLON),
                        new QuestionNONTERM(AdditionalSyntaxKind.NAME_EXPRESSION),
                        new QuestionNONTERM(SyntaxKind.INDENT),
                        new QuestionNONTERM(new ListNONTERM(AdditionalSyntaxKind.STATEMENT)),
                        new QuestionNONTERM(SyntaxKind.DEDENT)));
        // VARIABLE_DEFINITION := (VAR | VAL) IDENTIFIER COLON? NameExpression? EQUALS? Expression?
        rules.put(SyntaxKind.VARIABLE_DEFINITION,
                List.of(AdditionalSyntaxKind.VAR_OR_VAL,
                        SyntaxKind.IDENTIFIER,
                        new QuestionNONTERM(Symbol.COLON),
                        new QuestionNONTERM(AdditionalSyntaxKind.NAME_EXPRESSION),
                        new QuestionNONTERM(Symbol.EQUALS),
                        new QuestionNONTERM(AdditionalSyntaxKind.EXPRESSION)));
        // TYPE_PARAMETER_DEFINITION := IDENTIFIER TYPE_BOUND?
        rules.put(SyntaxKind.TYPE_PARAMETER_DEFINITION,
                List.of(SyntaxKind.IDENTIFIER,
                        new QuestionNONTERM(SyntaxKind.TYPE_BOUND)));
        // PARAMETER_DEFINITION := IDENTIFIER COLON NameExpression
        rules.put(SyntaxKind.PARAMETER_DEFINITION,
                List.of(SyntaxKind.IDENTIFIER, Symbol.COLON,
                        AdditionalSyntaxKind.NAME_EXPRESSION));

        // Statements
        // VARIABLE_DEFINITION_STATEMENT := VARIABLE_DEFINITION
        rules.put(SyntaxKind.VARIABLE_DEFINITION_STATEMENT, List.of(SyntaxKind.VARIABLE_DEFINITION));
        //    ASSIGNMENT_STATEMENT := Primary EQUALS Expression
        rules.put(SyntaxKind.ASSIGNMENT_STATEMENT,
                List.of(AdditionalSyntaxKind.PRIMARY,
                        Symbol.EQUALS,
                        AdditionalSyntaxKind.EXPRESSION));
        // EXPRESSION_STATEMENT := Expression
        rules.put(SyntaxKind.EXPRESSION_STATEMENT, List.of(AdditionalSyntaxKind.EXPRESSION));
        // RETURN_STATEMENT := RETURN Expression?
        rules.put(SyntaxKind.RETURN_STATEMENT,
                List.of(Keyword.RETURN, new QuestionNONTERM(AdditionalSyntaxKind.EXPRESSION)));
        // BREAK_STATEMENT := BREAK
        rules.put(SyntaxKind.BREAK_STATEMENT, List.of(Keyword.BREAK));
        // CONTINUE_STATEMENT := CONTINUE
        rules.put(SyntaxKind.CONTINUE_STATEMENT, List.of(Keyword.CONTINUE));
        // IF_STATEMENT := IF Expression INDENT? LIST[Statement]? DEDENT? ELSE? INDENT? LIST[Statement]? DEDENT?
        rules.put(SyntaxKind.IF_STATEMENT,
                List.of(Keyword.IF,
                        AdditionalSyntaxKind.EXPRESSION,
                        new QuestionNONTERM(SyntaxKind.INDENT),
                        new QuestionNONTERM(new ListNONTERM(AdditionalSyntaxKind.STATEMENT)),
                        new QuestionNONTERM(SyntaxKind.DEDENT),
                        new QuestionNONTERM(Keyword.ELSE),
                        new QuestionNONTERM(SyntaxKind.INDENT),
                        new QuestionNONTERM(new ListNONTERM(AdditionalSyntaxKind.STATEMENT)),
                        new QuestionNONTERM(SyntaxKind.DEDENT)));
        // WHILE_STATEMENT := WHILE Expression INDENT? LIST[Statement]? DEDENT?
        rules.put(SyntaxKind.WHILE_STATEMENT,
                List.of(Keyword.WHILE,
                        AdditionalSyntaxKind.EXPRESSION,
                        new QuestionNONTERM(SyntaxKind.INDENT),
                        new QuestionNONTERM(new ListNONTERM(AdditionalSyntaxKind.STATEMENT)),
                        new QuestionNONTERM(SyntaxKind.DEDENT)));
        // FOR_STATEMENT := FOR Primary IN Expression INDENT? LIST[Statement]? DEDENT?
        rules.put(SyntaxKind.FOR_STATEMENT,
                List.of(Keyword.FOR,
                        AdditionalSyntaxKind.PRIMARY,
                        Keyword.IN,
                        AdditionalSyntaxKind.EXPRESSION,
                        new QuestionNONTERM(SyntaxKind.INDENT),
                        new QuestionNONTERM(new ListNONTERM(AdditionalSyntaxKind.STATEMENT)),
                        new QuestionNONTERM(SyntaxKind.DEDENT)));


        // Array containing needed expressions
        List<AnySyntaxKind> KINDS = new LinkedList<>(List.of(
                SyntaxKind.LOGICAL_AND_EXPRESSION,
                SyntaxKind.LOGICAL_OR_EXPRESSION,
                SyntaxKind.LOGICAL_NOT_EXPRESSION,
                SyntaxKind.EQUALS_EXPRESSION,
                SyntaxKind.NOT_EQUALS_EXPRESSION,
                SyntaxKind.LESS_THAN_EXPRESSION,
                SyntaxKind.LESS_THAN_OR_EQUAL_EXPRESSION,
                SyntaxKind.GREATER_THAN_EXPRESSION,
                SyntaxKind.GREATER_THAN_OR_EQUAL_EXPRESSION,
                SyntaxKind.IS_EXPRESSION,
                SyntaxKind.BITWISE_AND_EXPRESSION,
                SyntaxKind.BITWISE_OR_EXPRESSION,
                SyntaxKind.BITWISE_EXCLUSIVE_OR_EXPRESSION,
                SyntaxKind.BITWISE_LEFT_SHIFT_EXPRESSION,
                SyntaxKind.BITWISE_RIGHT_SHIFT_EXPRESSION,
                SyntaxKind.ADD_EXPRESSION,
                SyntaxKind.SUBTRACT_EXPRESSION,
                SyntaxKind.MULTIPLY_EXPRESSION,
                SyntaxKind.DIVIDE_EXPRESSION,
                SyntaxKind.MODULO_EXPRESSION,
                SyntaxKind.UNARY_PLUS_EXPRESSION,
                SyntaxKind.UNARY_MINUS_EXPRESSION,
                SyntaxKind.BITWISE_NOT_EXPRESSION,
                AdditionalSyntaxKind.PRIMARY
        ));
        // Expressions
        // TODO----------------------------------------------------------------------------------
        // LOGICAL_AND_EXPRESSION := Expression AMPERSAND_AMPERSAND Expression
//        rules.put(SyntaxKind.LOGICAL_AND_EXPRESSION,
//                List.of(AdditionalSyntaxKind.EXPRESSION, Symbol.AMPERSAND_AMPERSAND, AdditionalSyntaxKind.EXPRESSION));

        // After removing left recursion
        KINDS.removeFirst();
        assert KINDS.getFirst() == SyntaxKind.LOGICAL_OR_EXPRESSION;
        // LOGICAL_AND_EXPRESSION := LOGICAL_AND_EXPRESSION_REDUCED LOGICAL_AND_EXPRESSION_HATCH
        rules.put(SyntaxKind.LOGICAL_AND_EXPRESSION,
                List.of(AdditionalSyntaxKind.LOGICAL_AND_EXPRESSION_REDUCED,
                        AdditionalSyntaxKind.LOGICAL_AND_EXPRESSION_HATCH));
        // LOGICAL_AND_EXPRESSION_REDUCED := OR[KINDS / LOGICAL_AND_EXPRESSION]
        rules.put(AdditionalSyntaxKind.LOGICAL_AND_EXPRESSION_REDUCED, List.of(new OrNONTERM(KINDS)));
        // LOGICAL_AND_EXPRESSION_HATCH := AMPERSAND_AMPERSAND LOGICAL_AND_EXPRESSION_REDUCED LOGICAL_AND_EXPRESSION_HATCH?
        rules.put(AdditionalSyntaxKind.LOGICAL_AND_EXPRESSION_HATCH,
                List.of(Symbol.AMPERSAND_AMPERSAND, AdditionalSyntaxKind.LOGICAL_AND_EXPRESSION_REDUCED,
                        new QuestionNONTERM(AdditionalSyntaxKind.LOGICAL_AND_EXPRESSION_HATCH)));
        // TODO----------------------------------------------------------------------------------
        // LOGICAL_OR_EXPRESSION := Expression BAR_BAR Expression
//        rules.put(SyntaxKind.LOGICAL_OR_EXPRESSION,
//                List.of(AdditionalSyntaxKind.EXPRESSION, Symbol.BAR_BAR, AdditionalSyntaxKind.EXPRESSION));

        // After removing left recursion
        KINDS.removeFirst();
        assert KINDS.getFirst() == SyntaxKind.LOGICAL_NOT_EXPRESSION;
        // LOGICAL_OR_EXPRESSION := LOGICAL_OR_EXPRESSION_REDUCED LOGICAL_OR_EXPRESSION_HATCH
        rules.put(SyntaxKind.LOGICAL_OR_EXPRESSION,
                List.of(AdditionalSyntaxKind.LOGICAL_OR_EXPRESSION_REDUCED,
                        AdditionalSyntaxKind.LOGICAL_OR_EXPRESSION_HATCH));
        // LOGICAL_OR_EXPRESSION_REDUCED := OR[KINDS / LOGICAL_OR_EXPRESSION]
        rules.put(AdditionalSyntaxKind.LOGICAL_OR_EXPRESSION_REDUCED, List.of(new OrNONTERM(KINDS)));
        // LOGICAL_OR_EXPRESSION_HATCH := BAR_BAR LOGICAL_OR_EXPRESSION_REDUCED LOGICAL_OR_EXPRESSION_HATCH?
        rules.put(AdditionalSyntaxKind.LOGICAL_OR_EXPRESSION_HATCH,
                List.of(Symbol.BAR_BAR, AdditionalSyntaxKind.LOGICAL_OR_EXPRESSION_REDUCED,
                        new QuestionNONTERM(AdditionalSyntaxKind.LOGICAL_OR_EXPRESSION_HATCH)));
        // TODO----------------------------------------------------------------------------------
        // LOGICAL_NOT_EXPRESSION := EXCLAMATION Expression
//        rules.put(SyntaxKind.LOGICAL_NOT_EXPRESSION, List.of(Symbol.EXCLAMATION, AdditionalSyntaxKind.EXPRESSION));

        // After removing left recursion
        // LOGICAL_NOT_EXPRESSION := EXCLAMATION LOGICAL_NOT_EXPRESSION_REDUCED
        rules.put(SyntaxKind.LOGICAL_NOT_EXPRESSION,
                List.of(Symbol.EXCLAMATION, AdditionalSyntaxKind.LOGICAL_NOT_EXPRESSION_REDUCED));
        // LOGICAL_NOT_EXPRESSION_REDUCED := OR[KINDS / LOGICAL_NOT_EXPRESSION]
        rules.put(AdditionalSyntaxKind.LOGICAL_NOT_EXPRESSION_REDUCED, List.of(new OrNONTERM(KINDS)));

        KINDS.removeFirst();
        // TODO----------------------------------------------------------------------------------
        // EQUALS_EXPRESSION := Expression EQUALS_EQUALS Expression
//        rules.put(SyntaxKind.EQUALS_EXPRESSION,
//                List.of(AdditionalSyntaxKind.EXPRESSION, Symbol.EQUALS_EQUALS, AdditionalSyntaxKind.EXPRESSION));

        // After removing left recursion
        KINDS.removeFirst();
        assert KINDS.getFirst() == SyntaxKind.NOT_EQUALS_EXPRESSION;
        // EQUALS_EXPRESSION := EQUALS_EXPRESSION_REDUCED EQUALS_EXPRESSION_HATCH
        rules.put(SyntaxKind.EQUALS_EXPRESSION,
                List.of(AdditionalSyntaxKind.EQUALS_EXPRESSION_REDUCED,
                        AdditionalSyntaxKind.EQUALS_EXPRESSION_HATCH));
        // EQUALS_EXPRESSION_REDUCED := OR[KINDS / EQUALS_EXPRESSION]
        rules.put(AdditionalSyntaxKind.EQUALS_EXPRESSION_REDUCED, List.of(new OrNONTERM(KINDS)));
        // EQUALS_EXPRESSION_HATCH := EQUALS_EQUALS EQUALS_EXPRESSION_REDUCED EQUALS_EXPRESSION_HATCH?
        rules.put(AdditionalSyntaxKind.EQUALS_EXPRESSION_HATCH,
                List.of(Symbol.EQUALS_EQUALS, AdditionalSyntaxKind.EQUALS_EXPRESSION_REDUCED,
                        new QuestionNONTERM(AdditionalSyntaxKind.EQUALS_EXPRESSION_HATCH)));
        // TODO----------------------------------------------------------------------------------
        // NOT_EQUALS_EXPRESSION := Expression EXCLAMATION_EQUALS Expression
//        rules.put(SyntaxKind.NOT_EQUALS_EXPRESSION,
//                List.of(AdditionalSyntaxKind.EXPRESSION, Symbol.EXCLAMATION_EQUALS, AdditionalSyntaxKind.EXPRESSION));

        // After removing left recursion
        KINDS.removeFirst();
        assert KINDS.getFirst() == SyntaxKind.LESS_THAN_EXPRESSION;
        // NOT_EQUALS_EXPRESSION := NOT_EQUALS_EXPRESSION_REDUCED NOT_EQUALS_EXPRESSION_HATCH
        rules.put(SyntaxKind.NOT_EQUALS_EXPRESSION,
                List.of(AdditionalSyntaxKind.NOT_EQUALS_EXPRESSION_REDUCED,
                        AdditionalSyntaxKind.NOT_EQUALS_EXPRESSION_HATCH));
        // NOT_EQUALS_EXPRESSION_REDUCED := OR[KINDS / NOT_EQUALS_EXPRESSION]
        rules.put(AdditionalSyntaxKind.NOT_EQUALS_EXPRESSION_REDUCED, List.of(new OrNONTERM(KINDS)));
        // NOT_EQUALS_EXPRESSION_HATCH := EXCLAMATION_EQUALS NOT_EQUALS_EXPRESSION_REDUCED NOT_EQUALS_EXPRESSION_HATCH?
        rules.put(AdditionalSyntaxKind.NOT_EQUALS_EXPRESSION_HATCH,
                List.of(Symbol.EXCLAMATION_EQUALS, AdditionalSyntaxKind.NOT_EQUALS_EXPRESSION_REDUCED,
                        new QuestionNONTERM(AdditionalSyntaxKind.NOT_EQUALS_EXPRESSION_HATCH)));
        // TODO----------------------------------------------------------------------------------
        // LESS_THAN_EXPRESSION := Expression LESS_THAN Expression
//        rules.put(SyntaxKind.LESS_THAN_EXPRESSION,
//                List.of(AdditionalSyntaxKind.EXPRESSION, Symbol.LESS_THAN, AdditionalSyntaxKind.EXPRESSION));

        // After removing left recursion
        KINDS.removeFirst();
        assert KINDS.getFirst() == SyntaxKind.LESS_THAN_OR_EQUAL_EXPRESSION;
        // LESS_THAN_EXPRESSION := LESS_THAN_EXPRESSION_REDUCED LESS_THAN_EXPRESSION_HATCH
        rules.put(SyntaxKind.LESS_THAN_EXPRESSION,
                List.of(AdditionalSyntaxKind.LESS_THAN_EXPRESSION_REDUCED,
                        AdditionalSyntaxKind.LESS_THAN_EXPRESSION_HATCH));
        // LESS_THAN_EXPRESSION_REDUCED := OR[KINDS / LESS_THAN_EXPRESSION]
        rules.put(AdditionalSyntaxKind.LESS_THAN_EXPRESSION_REDUCED, List.of(new OrNONTERM(KINDS)));
        // LESS_THAN_EXPRESSION_HATCH := LESS_THAN LESS_THAN_EXPRESSION_REDUCED LESS_THAN_EXPRESSION_HATCH?
        rules.put(AdditionalSyntaxKind.LESS_THAN_EXPRESSION_HATCH,
                List.of(Symbol.LESS_THAN, AdditionalSyntaxKind.LESS_THAN_EXPRESSION_REDUCED,
                        new QuestionNONTERM(AdditionalSyntaxKind.LESS_THAN_EXPRESSION_HATCH)));
        // TODO----------------------------------------------------------------------------------
        // LESS_THAN_OR_EQUAL_EXPRESSION := Expression LESS_THAN_EQUALS Expression
//        rules.put(SyntaxKind.LESS_THAN_OR_EQUAL_EXPRESSION,
//                List.of(AdditionalSyntaxKind.EXPRESSION, Symbol.LESS_THAN_EQUALS, AdditionalSyntaxKind.EXPRESSION));

        // After removing left recursion
        KINDS.removeFirst();
        assert KINDS.getFirst() == SyntaxKind.GREATER_THAN_EXPRESSION;
        // LESS_THAN_OR_EQUAL_EXPRESSION := LESS_THAN_OR_EQUAL_EXPRESSION_REDUCED LESS_THAN_OR_EQUAL_EXPRESSION_HATCH
        rules.put(SyntaxKind.LESS_THAN_OR_EQUAL_EXPRESSION,
                List.of(AdditionalSyntaxKind.LESS_THAN_OR_EQUAL_EXPRESSION_REDUCED,
                        AdditionalSyntaxKind.LESS_THAN_OR_EQUAL_EXPRESSION_HATCH));
        // LESS_THAN_OR_EQUAL_EXPRESSION_REDUCED := OR[KINDS / LESS_THAN_OR_EQUAL_EXPRESSION]
        rules.put(AdditionalSyntaxKind.LESS_THAN_OR_EQUAL_EXPRESSION_REDUCED, List.of(new OrNONTERM(KINDS)));
        // LESS_THAN_OR_EQUAL_EXPRESSION_HATCH := LESS_THAN_EQUALS LESS_THAN_OR_EQUAL_EXPRESSION_REDUCED
        // LESS_THAN_OR_EQUAL_EXPRESSION_HATCH?
        rules.put(AdditionalSyntaxKind.LESS_THAN_OR_EQUAL_EXPRESSION_HATCH,
                List.of(Symbol.LESS_THAN_EQUALS, AdditionalSyntaxKind.LESS_THAN_OR_EQUAL_EXPRESSION_REDUCED,
                        new QuestionNONTERM(AdditionalSyntaxKind.LESS_THAN_OR_EQUAL_EXPRESSION_HATCH)));
        // TODO----------------------------------------------------------------------------------
        // GREATER_THAN_EXPRESSION := Expression GREATER_THAN Expression
//        rules.put(SyntaxKind.GREATER_THAN_EXPRESSION,
//                List.of(AdditionalSyntaxKind.EXPRESSION, Symbol.GREATER_THAN, AdditionalSyntaxKind.EXPRESSION));

        // After removing left recursion
        KINDS.removeFirst();
        assert KINDS.getFirst() == SyntaxKind.GREATER_THAN_OR_EQUAL_EXPRESSION;
        // GREATER_THAN_EXPRESSION := GREATER_THAN_EXPRESSION_REDUCED GREATER_THAN_EXPRESSION_HATCH
        rules.put(SyntaxKind.GREATER_THAN_EXPRESSION,
                List.of(AdditionalSyntaxKind.GREATER_THAN_EXPRESSION_REDUCED,
                        AdditionalSyntaxKind.GREATER_THAN_EXPRESSION_HATCH));
        // GREATER_THAN_EXPRESSION_REDUCED := OR[KINDS / GREATER_THAN_EXPRESSION]
        rules.put(AdditionalSyntaxKind.GREATER_THAN_EXPRESSION_REDUCED, List.of(new OrNONTERM(KINDS)));
        // GREATER_THAN_EXPRESSION_HATCH := GREATER_THAN GREATER_THAN_EXPRESSION_REDUCED GREATER_THAN_EXPRESSION_HATCH?
        rules.put(AdditionalSyntaxKind.GREATER_THAN_EXPRESSION_HATCH,
                List.of(Symbol.GREATER_THAN, AdditionalSyntaxKind.GREATER_THAN_EXPRESSION_REDUCED,
                        new QuestionNONTERM(AdditionalSyntaxKind.GREATER_THAN_EXPRESSION_HATCH)));
        // TODO----------------------------------------------------------------------------------
        // GREATER_THAN_OR_EQUAL_EXPRESSION := Expression GREATER_THAN_EQUALS Expression
//        rules.put(SyntaxKind.GREATER_THAN_OR_EQUAL_EXPRESSION,
//                List.of(AdditionalSyntaxKind.EXPRESSION, Symbol.GREATER_THAN_EQUALS, AdditionalSyntaxKind.EXPRESSION));

        // After removing left recursion
        KINDS.removeFirst();
        assert KINDS.getFirst() == SyntaxKind.IS_EXPRESSION;
        // GREATER_THAN_OR_EQUAL_EXPRESSION := GREATER_THAN_OR_EQUAL_EXPRESSION_REDUCED GREATER_THAN_OR_EQUAL_EXPRESSION_HATCH
        rules.put(SyntaxKind.GREATER_THAN_OR_EQUAL_EXPRESSION,
                List.of(AdditionalSyntaxKind.GREATER_THAN_OR_EQUAL_EXPRESSION_REDUCED,
                        AdditionalSyntaxKind.GREATER_THAN_OR_EQUAL_EXPRESSION_HATCH));
        // GREATER_THAN_OR_EQUAL_EXPRESSION_REDUCED := OR[KINDS / GREATER_THAN_OR_EQUAL_EXPRESSION]
        rules.put(AdditionalSyntaxKind.GREATER_THAN_OR_EQUAL_EXPRESSION_REDUCED, List.of(new OrNONTERM(KINDS)));
        // GREATER_THAN_OR_EQUAL_EXPRESSION_HATCH := GREATER_THAN_EQUALS GREATER_THAN_OR_EQUAL_EXPRESSION_REDUCED
        // GREATER_THAN_OR_EQUAL_EXPRESSION_HATCH?
        rules.put(AdditionalSyntaxKind.GREATER_THAN_OR_EQUAL_EXPRESSION_HATCH,
                List.of(Symbol.GREATER_THAN_EQUALS, AdditionalSyntaxKind.GREATER_THAN_OR_EQUAL_EXPRESSION_REDUCED,
                        new QuestionNONTERM(AdditionalSyntaxKind.GREATER_THAN_OR_EQUAL_EXPRESSION_HATCH)));
        // TODO----------------------------------------------------------------------------------
        // IS_EXPRESSION := Expression IS NameExpression IDENTIFIER?
//        rules.put(SyntaxKind.IS_EXPRESSION,
//                List.of(AdditionalSyntaxKind.EXPRESSION, Keyword.IS, AdditionalSyntaxKind.NAME_EXPRESSION,
//                        new QuestionNONTERM(SyntaxKind.IDENTIFIER)));

        // After removing left recursion
        KINDS.removeFirst();
        assert KINDS.getFirst() == SyntaxKind.BITWISE_AND_EXPRESSION;
        // IS_EXPRESSION := IS_EXPRESSION_REDUCED IS NameExpression IDENTIFIER?
        rules.put(SyntaxKind.IS_EXPRESSION,
                List.of(AdditionalSyntaxKind.IS_EXPRESSION_REDUCED, Keyword.IS, AdditionalSyntaxKind.NAME_EXPRESSION,
                        new QuestionNONTERM(SyntaxKind.IDENTIFIER)));
        // IS_EXPRESSION_REDUCED := OR[KINDS / IS_EXPRESSION]
        rules.put(AdditionalSyntaxKind.IS_EXPRESSION_REDUCED, List.of(new OrNONTERM(KINDS)));
        // TODO----------------------------------------------------------------------------------
        // BITWISE_AND_EXPRESSION := Expression AMPERSAND Expression
//        rules.put(SyntaxKind.BITWISE_AND_EXPRESSION,
//                List.of(AdditionalSyntaxKind.EXPRESSION, Symbol.AMPERSAND, AdditionalSyntaxKind.EXPRESSION));

        // After removing left recursion
        KINDS.removeFirst();
        assert KINDS.getFirst() == SyntaxKind.BITWISE_OR_EXPRESSION;
        // BITWISE_AND_EXPRESSION := BITWISE_AND_EXPRESSION_REDUCED BITWISE_AND_EXPRESSION_HATCH
        rules.put(SyntaxKind.BITWISE_AND_EXPRESSION,
                List.of(AdditionalSyntaxKind.BITWISE_AND_EXPRESSION_REDUCED,
                        AdditionalSyntaxKind.BITWISE_AND_EXPRESSION_HATCH));
        // BITWISE_AND_EXPRESSION_REDUCED := OR[KINDS / BITWISE_AND_EXPRESSION]
        rules.put(AdditionalSyntaxKind.BITWISE_AND_EXPRESSION_REDUCED, List.of(new OrNONTERM(KINDS)));
        // BITWISE_AND_EXPRESSION_HATCH := AMPERSAND BITWISE_AND_EXPRESSION_REDUCED BITWISE_AND_EXPRESSION_HATCH?
        rules.put(AdditionalSyntaxKind.BITWISE_AND_EXPRESSION_HATCH,
                List.of(Symbol.AMPERSAND, AdditionalSyntaxKind.BITWISE_AND_EXPRESSION_REDUCED,
                        new QuestionNONTERM(AdditionalSyntaxKind.BITWISE_AND_EXPRESSION_HATCH)));
        // TODO----------------------------------------------------------------------------------
        // BITWISE_OR_EXPRESSION := Expression BAR Expression
//        rules.put(SyntaxKind.BITWISE_OR_EXPRESSION,
//                List.of(AdditionalSyntaxKind.EXPRESSION, Symbol.BAR, AdditionalSyntaxKind.EXPRESSION));

        // After removing left recursion
        KINDS.removeFirst();
        assert KINDS.getFirst() == SyntaxKind.BITWISE_EXCLUSIVE_OR_EXPRESSION;
        // BITWISE_OR_EXPRESSION := BITWISE_OR_EXPRESSION_REDUCED BITWISE_OR_EXPRESSION_HATCH
        rules.put(SyntaxKind.BITWISE_OR_EXPRESSION,
                List.of(AdditionalSyntaxKind.BITWISE_OR_EXPRESSION_REDUCED,
                        AdditionalSyntaxKind.BITWISE_OR_EXPRESSION_HATCH));
        // BITWISE_OR_EXPRESSION_REDUCED := OR[KINDS / BITWISE_OR_EXPRESSION]
        rules.put(AdditionalSyntaxKind.BITWISE_OR_EXPRESSION_REDUCED, List.of(new OrNONTERM(KINDS)));
        // BITWISE_OR_EXPRESSION_HATCH := BAR BITWISE_OR_EXPRESSION_REDUCED BITWISE_OR_EXPRESSION_HATCH?
        rules.put(AdditionalSyntaxKind.BITWISE_OR_EXPRESSION_HATCH,
                List.of(Symbol.BAR, AdditionalSyntaxKind.BITWISE_OR_EXPRESSION_REDUCED, new QuestionNONTERM(
                        AdditionalSyntaxKind.BITWISE_OR_EXPRESSION_HATCH)));
        // TODO----------------------------------------------------------------------------------
        // BITWISE_EXCLUSIVE_OR_EXPRESSION := Expression CARET Expression
//        rules.put(SyntaxKind.BITWISE_EXCLUSIVE_OR_EXPRESSION,
//                List.of(AdditionalSyntaxKind.EXPRESSION, Symbol.CARET, AdditionalSyntaxKind.EXPRESSION));

        // After removing left recursion
        KINDS.removeFirst();
        assert KINDS.getFirst() == SyntaxKind.BITWISE_LEFT_SHIFT_EXPRESSION;
        // BITWISE_EXCLUSIVE_OR_EXPRESSION := BITWISE_EXCLUSIVE_OR_EXPRESSION_REDUCED BITWISE_EXCLUSIVE_OR_EXPRESSION_HATCH
        rules.put(SyntaxKind.BITWISE_EXCLUSIVE_OR_EXPRESSION,
                List.of(AdditionalSyntaxKind.BITWISE_EXCLUSIVE_OR_EXPRESSION_REDUCED,
                        AdditionalSyntaxKind.BITWISE_EXCLUSIVE_OR_EXPRESSION_HATCH));
        // BITWISE_EXCLUSIVE_OR_EXPRESSION_REDUCED := OR[KINDS / BITWISE_EXCLUSIVE_OR_EXPRESSION]
        rules.put(AdditionalSyntaxKind.BITWISE_EXCLUSIVE_OR_EXPRESSION_REDUCED, List.of(new OrNONTERM(KINDS)));
        // BITWISE_EXCLUSIVE_OR_EXPRESSION_HATCH := CARET BITWISE_EXCLUSIVE_OR_EXPRESSION_REDUCED
        // BITWISE_EXCLUSIVE_OR_EXPRESSION_HATCH?
        rules.put(AdditionalSyntaxKind.BITWISE_EXCLUSIVE_OR_EXPRESSION_HATCH,
                List.of(Symbol.CARET, AdditionalSyntaxKind.BITWISE_EXCLUSIVE_OR_EXPRESSION_REDUCED,
                        new QuestionNONTERM(AdditionalSyntaxKind.BITWISE_EXCLUSIVE_OR_EXPRESSION_HATCH)));

        // TODO----------------------------------------------------------------------------------
        // BITWISE_LEFT_SHIFT_EXPRESSION := Expression LESS_THAN_LESS_THAN Expression
//        rules.put(SyntaxKind.BITWISE_LEFT_SHIFT_EXPRESSION,
//                List.of(AdditionalSyntaxKind.EXPRESSION, Symbol.LESS_THAN_LESS_THAN, AdditionalSyntaxKind.EXPRESSION));

        // After removing left recursion
        KINDS.removeFirst();
        assert KINDS.getFirst() == SyntaxKind.BITWISE_RIGHT_SHIFT_EXPRESSION;
        // BITWISE_LEFT_SHIFT_EXPRESSION := BITWISE_LEFT_SHIFT_EXPRESSION_REDUCED BITWISE_LEFT_SHIFT_EXPRESSION_HATCH
        rules.put(SyntaxKind.BITWISE_LEFT_SHIFT_EXPRESSION,
                List.of(AdditionalSyntaxKind.BITWISE_LEFT_SHIFT_EXPRESSION_REDUCED,
                        AdditionalSyntaxKind.BITWISE_LEFT_SHIFT_EXPRESSION_HATCH));
        // BITWISE_LEFT_SHIFT_EXPRESSION_REDUCED := OR[KINDS / BITWISE_LEFT_SHIFT_EXPRESSION]
        rules.put(AdditionalSyntaxKind.BITWISE_LEFT_SHIFT_EXPRESSION_REDUCED, List.of(new OrNONTERM(KINDS)));
        // BITWISE_LEFT_SHIFT_EXPRESSION_HATCH := LESS_THAN_LESS_THAN BITWISE_LEFT_SHIFT_EXPRESSION_REDUCED
        // BITWISE_LEFT_SHIFT_EXPRESSION_HATCH?
        rules.put(AdditionalSyntaxKind.BITWISE_LEFT_SHIFT_EXPRESSION_HATCH,
                List.of(Symbol.LESS_THAN_LESS_THAN, AdditionalSyntaxKind.BITWISE_LEFT_SHIFT_EXPRESSION_REDUCED,
                        new QuestionNONTERM(AdditionalSyntaxKind.BITWISE_LEFT_SHIFT_EXPRESSION_HATCH)));

        // TODO----------------------------------------------------------------------------------
        // BITWISE_RIGHT_SHIFT_EXPRESSION := Expression GREATER_THAN_GREATER_THAN Expression
//        rules.put(SyntaxKind.BITWISE_RIGHT_SHIFT_EXPRESSION,
//                List.of(AdditionalSyntaxKind.EXPRESSION, Symbol.GREATER_THAN_GREATER_THAN,
//                        AdditionalSyntaxKind.EXPRESSION));

        // After removing left recursion
        KINDS.removeFirst();
        assert KINDS.getFirst() == SyntaxKind.ADD_EXPRESSION;
        // BITWISE_RIGHT_SHIFT_EXPRESSION := BITWISE_RIGHT_SHIFT_EXPRESSION_REDUCED BITWISE_RIGHT_SHIFT_EXPRESSION_HATCH
        rules.put(SyntaxKind.BITWISE_RIGHT_SHIFT_EXPRESSION,
                List.of(AdditionalSyntaxKind.BITWISE_RIGHT_SHIFT_EXPRESSION_REDUCED,
                        AdditionalSyntaxKind.BITWISE_RIGHT_SHIFT_EXPRESSION_HATCH));
        // BITWISE_RIGHT_SHIFT_EXPRESSION_REDUCED := OR[KINDS / BITWISE_RIGHT_SHIFT_EXPRESSION]
        rules.put(AdditionalSyntaxKind.BITWISE_RIGHT_SHIFT_EXPRESSION_REDUCED, List.of(new OrNONTERM(KINDS)));
        // BITWISE_RIGHT_SHIFT_EXPRESSION_HATCH := GREATER_THAN_GREATER_THAN BITWISE_RIGHT_SHIFT_EXPRESSION_REDUCED
        // BITWISE_RIGHT_SHIFT_EXPRESSION_HATCH?
        rules.put(AdditionalSyntaxKind.BITWISE_RIGHT_SHIFT_EXPRESSION_HATCH,
                List.of(Symbol.GREATER_THAN_GREATER_THAN, AdditionalSyntaxKind.BITWISE_RIGHT_SHIFT_EXPRESSION_REDUCED,
                        new QuestionNONTERM(AdditionalSyntaxKind.BITWISE_RIGHT_SHIFT_EXPRESSION_HATCH)));
        // TODO----------------------------------------------------------------------------------
        // ADD_EXPRESSION := Expression PLUS Expression
//        rules.put(SyntaxKind.ADD_EXPRESSION,
//                List.of(AdditionalSyntaxKind.EXPRESSION, Symbol.PLUS, AdditionalSyntaxKind.EXPRESSION));

        // After removing left recursion
        KINDS.removeFirst();
        assert KINDS.getFirst() == SyntaxKind.SUBTRACT_EXPRESSION;
        // ADD_EXPRESSION := ADD_EXPRESSION_REDUCED ADD_EXPRESSION_HATCH
        rules.put(SyntaxKind.ADD_EXPRESSION,
                List.of(AdditionalSyntaxKind.ADD_EXPRESSION_REDUCED, AdditionalSyntaxKind.ADD_EXPRESSION_HATCH));
        // ADD_EXPRESSION_REDUCED := OR[KINDS / ADD_EXPRESSION]
        rules.put(AdditionalSyntaxKind.ADD_EXPRESSION_REDUCED, List.of(new OrNONTERM(KINDS)));
        // ADD_EXPRESSION_HATCH := PLUS ADD_EXPRESSION_REDUCED
        // ADD_EXPRESSION_HATCH?
        rules.put(AdditionalSyntaxKind.ADD_EXPRESSION_HATCH,
                List.of(Symbol.PLUS, AdditionalSyntaxKind.ADD_EXPRESSION_REDUCED,
                        new QuestionNONTERM(AdditionalSyntaxKind.ADD_EXPRESSION_HATCH)));
        // TODO----------------------------------------------------------------------------------
        // SUBTRACT_EXPRESSION := Expression MINUS Expression
//        rules.put(SyntaxKind.SUBTRACT_EXPRESSION,
//                List.of(AdditionalSyntaxKind.EXPRESSION, Symbol.MINUS, AdditionalSyntaxKind.EXPRESSION));

        // After removing left recursion
        KINDS.removeFirst();
        assert KINDS.getFirst() == SyntaxKind.MULTIPLY_EXPRESSION;
        // SUBTRACT_EXPRESSION := SUBTRACT_EXPRESSION_REDUCED SUBTRACT_EXPRESSION_HATCH
        rules.put(SyntaxKind.SUBTRACT_EXPRESSION,
                List.of(AdditionalSyntaxKind.SUBTRACT_EXPRESSION_REDUCED, AdditionalSyntaxKind.SUBTRACT_EXPRESSION_HATCH));
        // SUBTRACT_EXPRESSION_REDUCED := OR[KINDS / SUBTRACT_EXPRESSION]
        rules.put(AdditionalSyntaxKind.SUBTRACT_EXPRESSION_REDUCED, List.of(new OrNONTERM(KINDS)));
        // SUBTRACT_EXPRESSION_HATCH := MINUS SUBTRACT_EXPRESSION_REDUCED
        // SUBTRACT_EXPRESSION_HATCH?
        rules.put(AdditionalSyntaxKind.SUBTRACT_EXPRESSION_HATCH,
                List.of(Symbol.MINUS, AdditionalSyntaxKind.SUBTRACT_EXPRESSION_REDUCED,
                        new QuestionNONTERM(AdditionalSyntaxKind.SUBTRACT_EXPRESSION_HATCH)));
        // TODO----------------------------------------------------------------------------------
        // MULTIPLY_EXPRESSION := Expression ASTERISK Expression
//        rules.put(SyntaxKind.MULTIPLY_EXPRESSION,
//                List.of(AdditionalSyntaxKind.EXPRESSION, Symbol.ASTERISK, AdditionalSyntaxKind.EXPRESSION));

        // After removing left recursion
        KINDS.removeFirst();
        assert KINDS.getFirst() == SyntaxKind.DIVIDE_EXPRESSION;
        // MULTIPLY_EXPRESSION := MULTIPLY_EXPRESSION_REDUCED MULTIPLY_EXPRESSION_HATCH
        rules.put(SyntaxKind.MULTIPLY_EXPRESSION,
                List.of(AdditionalSyntaxKind.MULTIPLY_EXPRESSION_REDUCED, AdditionalSyntaxKind.MULTIPLY_EXPRESSION_HATCH));
        // MULTIPLY_EXPRESSION_REDUCED := OR[KINDS / MULTIPLY_EXPRESSION]
        rules.put(AdditionalSyntaxKind.MULTIPLY_EXPRESSION_REDUCED, List.of(new OrNONTERM(KINDS)));
        // MULTIPLY_EXPRESSION_HATCH := ASTERISK MULTIPLY_EXPRESSION_REDUCED
        // MULTIPLY_EXPRESSION_HATCH?
        rules.put(AdditionalSyntaxKind.MULTIPLY_EXPRESSION_HATCH,
                List.of(Symbol.ASTERISK, AdditionalSyntaxKind.MULTIPLY_EXPRESSION_REDUCED,
                        new QuestionNONTERM(AdditionalSyntaxKind.MULTIPLY_EXPRESSION_HATCH)));

        // TODO----------------------------------------------------------------------------------
        // DIVIDE_EXPRESSION := Expression SLASH Expression
        rules.put(SyntaxKind.DIVIDE_EXPRESSION,
                List.of(AdditionalSyntaxKind.EXPRESSION, Symbol.SLASH, AdditionalSyntaxKind.EXPRESSION));

        // After removing left recursion
        KINDS.removeFirst();
        assert KINDS.getFirst() == SyntaxKind.MODULO_EXPRESSION;
        // DIVIDE_EXPRESSION := DIVIDE_EXPRESSION_REDUCED DIVIDE_EXPRESSION_HATCH
        rules.put(SyntaxKind.DIVIDE_EXPRESSION,
                List.of(AdditionalSyntaxKind.DIVIDE_EXPRESSION_REDUCED, AdditionalSyntaxKind.DIVIDE_EXPRESSION_HATCH));
        // DIVIDE_EXPRESSION_REDUCED := OR[KINDS / DIVIDE_EXPRESSION]
        rules.put(AdditionalSyntaxKind.DIVIDE_EXPRESSION_REDUCED, List.of(new OrNONTERM(KINDS)));
        // DIVIDE_EXPRESSION_HATCH := SLASH DIVIDE_EXPRESSION_REDUCED
        // DIVIDE_EXPRESSION_HATCH?
        rules.put(AdditionalSyntaxKind.DIVIDE_EXPRESSION_HATCH,
                List.of(Symbol.SLASH, AdditionalSyntaxKind.DIVIDE_EXPRESSION_REDUCED,
                        new QuestionNONTERM(AdditionalSyntaxKind.DIVIDE_EXPRESSION_HATCH)));
        // TODO----------------------------------------------------------------------------------
        // MODULO_EXPRESSION := Expression PERCENT Expression
//        rules.put(SyntaxKind.MODULO_EXPRESSION,
//                List.of(AdditionalSyntaxKind.EXPRESSION, Symbol.PERCENT, AdditionalSyntaxKind.EXPRESSION));

        // After removing left recursion
        KINDS.removeFirst();
        assert KINDS.getFirst() == SyntaxKind.UNARY_PLUS_EXPRESSION;
        // MODULO_EXPRESSION := MODULO_EXPRESSION_REDUCED MODULO_EXPRESSION_HATCH
        rules.put(SyntaxKind.MODULO_EXPRESSION,
                List.of(AdditionalSyntaxKind.MODULO_EXPRESSION_REDUCED, AdditionalSyntaxKind.MODULO_EXPRESSION_HATCH));
        // MODULO_EXPRESSION_REDUCED := OR[KINDS / MODULO_EXPRESSION]
        rules.put(AdditionalSyntaxKind.MODULO_EXPRESSION_REDUCED, List.of(new OrNONTERM(KINDS)));
        // MODULO_EXPRESSION_HATCH := PERCENT MODULO_EXPRESSION_REDUCED
        // MODULO_EXPRESSION_HATCH?
        rules.put(AdditionalSyntaxKind.MODULO_EXPRESSION_HATCH,
                List.of(Symbol.PERCENT, AdditionalSyntaxKind.MODULO_EXPRESSION_REDUCED,
                        new QuestionNONTERM(AdditionalSyntaxKind.MODULO_EXPRESSION_HATCH)));
        // TODO----------------------------------------------------------------------------------
        // TODO: Probably need reduced expressions
        // UNARY_PLUS_EXPRESSION := PLUS Expression
        rules.put(SyntaxKind.UNARY_PLUS_EXPRESSION, List.of(Symbol.PLUS, AdditionalSyntaxKind.EXPRESSION));
        // UNARY_MINUS_EXPRESSION := MINUS Expression
        rules.put(SyntaxKind.UNARY_MINUS_EXPRESSION, List.of(Symbol.MINUS, AdditionalSyntaxKind.EXPRESSION));
        // BITWISE_NOT_EXPRESSION := TILDE Expression
        rules.put(SyntaxKind.BITWISE_NOT_EXPRESSION, List.of(Symbol.TILDE, AdditionalSyntaxKind.EXPRESSION));

        // Primary:
        // SIMPLE_PRIMARY := THIS_EXPRESSION |
        // SUPER_EXPRESSION | NULL_LITERAL_EXPRESSION | TRUE_LITERAL_EXPRESSION |
        // FALSE_LITERAL_EXPRESSION | STRING_LITERAL_EXPRESSION | RUNE_LITERAL_EXPRESSION |
        // INTEGER_LITERAL_EXPRESSION | PARENTHESIZED_EXPRESSION
        rules.put(AdditionalSyntaxKind.SIMPLE_PRIMARY,
                List.of(SyntaxKind.THIS_EXPRESSION, SyntaxKind.SUPER_EXPRESSION,
                        SyntaxKind.NULL_LITERAL_EXPRESSION,
                        SyntaxKind.TRUE_LITERAL_EXPRESSION,
                        SyntaxKind.FALSE_LITERAL_EXPRESSION,
                        SyntaxKind.STRING_LITERAL_EXPRESSION,
                        SyntaxKind.RUNE_LITERAL_EXPRESSION,
                        SyntaxKind.INTEGER_LITERAL_EXPRESSION,
                        SyntaxKind.PARENTHESIZED_EXPRESSION));


        // TODO--------------------------------------------------------------------------------------
        // MEMBER_ACCESS_EXPRESSION := Primary DOT IDENTIFIER
//        rules.put(SyntaxKind.MEMBER_ACCESS_EXPRESSION,
//                List.of(AdditionalSyntaxKind.PRIMARY, Symbol.DOT, SyntaxKind.IDENTIFIER));

        // After removing left recursion
        // MEMBER_ACCESS_EXPRESSION := PRIMARY_WITHOUT_MEMBER_ACCESS_EXPRESSION MEMBER_ACCESS_EXPRESSION_HATCH
        rules.put(SyntaxKind.MEMBER_ACCESS_EXPRESSION,
                List.of(AdditionalSyntaxKind.PRIMARY_WITHOUT_MEMBER_ACCESS_EXPRESSION,
                        AdditionalSyntaxKind.MEMBER_ACCESS_EXPRESSION_HATCH));
        // PRIMARY_WITHOUT_MEMBER_ACCESS_EXPRESSION := SIMPLE_PRIMARY | INVOCATION_EXPRESSION | INDEX_EXPRESSION
        rules.put(AdditionalSyntaxKind.PRIMARY_WITHOUT_MEMBER_ACCESS_EXPRESSION,
                List.of(new OrNONTERM(List.of(AdditionalSyntaxKind.SIMPLE_PRIMARY,
                        SyntaxKind.INVOCATION_EXPRESSION,
                        SyntaxKind.INDEX_EXPRESSION))));
        // MEMBER_ACCESS_EXPRESSION_HATCH := DOT IDENTIFIER MEMBER_ACCESS_EXPRESSION_HATCH?
        rules.put(AdditionalSyntaxKind.MEMBER_ACCESS_EXPRESSION_HATCH,
                List.of(Symbol.DOT, SyntaxKind.IDENTIFIER,
                        new QuestionNONTERM(AdditionalSyntaxKind.MEMBER_ACCESS_EXPRESSION_HATCH)));
        // TODO--------------------------------------------------------------------------------------
        // INVOCATION_EXPRESSION := Primary OPEN_PAREN SEPARATED_LIST[Expression, COMMA] CLOSE_PAREN
//        rules.put(SyntaxKind.INVOCATION_EXPRESSION,
//                List.of(AdditionalSyntaxKind.PRIMARY, Symbol.OPEN_PAREN,
//                        new SeparatedListNONTERM(AdditionalSyntaxKind.EXPRESSION, Symbol.COMMA),
//                        Symbol.CLOSE_PAREN));

        // After removing left recursion
        // INVOCATION_EXPRESSION := PRIMARY_WITHOUT_INVOCATION_EXPRESSION INVOCATION_EXPRESSION_HATCH
        rules.put(SyntaxKind.INVOCATION_EXPRESSION,
                List.of(AdditionalSyntaxKind.PRIMARY_WITHOUT_INVOCATION_EXPRESSION,
                        AdditionalSyntaxKind.INVOCATION_EXPRESSION_HATCH));
        // PRIMARY_WITHOUT_INVOCATION_EXPRESSION := SIMPLE_PRIMARY | INDEX_EXPRESSION
        rules.put(AdditionalSyntaxKind.PRIMARY_WITHOUT_INVOCATION_EXPRESSION,
                List.of(new OrNONTERM(List.of(AdditionalSyntaxKind.SIMPLE_PRIMARY, SyntaxKind.INDEX_EXPRESSION))));
        // INVOCATION_EXPRESSION_HATCH := OPEN_PAREN SEPARATED_LIST[Expression, COMMA] CLOSE_PAREN
        //  INVOCATION_EXPRESSION_HATCH?
        rules.put(AdditionalSyntaxKind.INVOCATION_EXPRESSION_HATCH,
                List.of(Symbol.OPEN_PAREN,
                        new SeparatedListNONTERM(AdditionalSyntaxKind.EXPRESSION, Symbol.COMMA),
                        new QuestionNONTERM(AdditionalSyntaxKind.INVOCATION_EXPRESSION_HATCH)));
        // TODO--------------------------------------------------------------------------------------
        // INDEX_EXPRESSION := Primary OPEN_BRACKET Expression CLOSE_BRACKET
//        rules.put(SyntaxKind.INDEX_EXPRESSION,
//                List.of(AdditionalSyntaxKind.PRIMARY, Symbol.OPEN_BRACKET, AdditionalSyntaxKind.EXPRESSION,
//                        Symbol.CLOSE_BRACKET));

        // After removing left recursion
        // INDEX_EXPRESSION := PRIMARY_WITHOUT_INDEX_EXPRESSION INDEX_EXPRESSION_HATCH
        rules.put(SyntaxKind.INDEX_EXPRESSION,
                List.of(AdditionalSyntaxKind.PRIMARY_WITHOUT_INDEX_EXPRESSION,
                        AdditionalSyntaxKind.INDEX_EXPRESSION_HATCH));
        // PRIMARY_WITHOUT_INDEX_EXPRESSION := SIMPLE_PRIMARY
        rules.put(AdditionalSyntaxKind.PRIMARY_WITHOUT_INDEX_EXPRESSION,
                List.of(AdditionalSyntaxKind.SIMPLE_PRIMARY));
        // INDEX_EXPRESSION_HATCH := OPEN_BRACKET Expression CLOSE_BRACKET INDEX_EXPRESSION_HATCH?
        rules.put(AdditionalSyntaxKind.INDEX_EXPRESSION_HATCH,
                List.of(Symbol.OPEN_BRACKET, AdditionalSyntaxKind.EXPRESSION, Symbol.CLOSE_BRACKET,
                        new QuestionNONTERM(AdditionalSyntaxKind.INDEX_EXPRESSION_HATCH)));
        // TODO--------------------------------------------------------------------------------------
        // THIS_EXPRESSION := THIS
        rules.put(SyntaxKind.THIS_EXPRESSION, List.of(Keyword.THIS));
        // SUPER_EXPRESSION := SUPER
        rules.put(SyntaxKind.SUPER_EXPRESSION, List.of(Keyword.SUPER));
        // NULL_LITERAL_EXPRESSION := NULL
        rules.put(SyntaxKind.NULL_LITERAL_EXPRESSION, List.of(Keyword.NULL));
        // TRUE_LITERAL_EXPRESSION := BOOLEAN
        // TODO: Only SyntaxKind.BOOLEAN?
        rules.put(SyntaxKind.TRUE_LITERAL_EXPRESSION, List.of(SyntaxKind.BOOLEAN));
        // FALSE_LITERAL_EXPRESSION := BOOLEAN
        rules.put(SyntaxKind.FALSE_LITERAL_EXPRESSION, List.of(SyntaxKind.BOOLEAN));
        // STRING_LITERAL_EXPRESSION := STRING
        rules.put(SyntaxKind.STRING_LITERAL_EXPRESSION, List.of(SyntaxKind.STRING));
        // RUNE_LITERAL_EXPRESSION := RUNE
        rules.put(SyntaxKind.RUNE_LITERAL_EXPRESSION, List.of(SyntaxKind.RUNE));
        // INTEGER_LITERAL_EXPRESSION := INTEGER
        rules.put(SyntaxKind.INTEGER_LITERAL_EXPRESSION, List.of(SyntaxKind.INTEGER));
        // PARENTHESIZED_EXPRESSION := OPEN_PAREN Expression CLOSE_PAREN
        rules.put(SyntaxKind.PARENTHESIZED_EXPRESSION,
                List.of(Symbol.OPEN_PAREN, AdditionalSyntaxKind.EXPRESSION, Symbol.CLOSE_PAREN));


        // Additional rules
        // VAL_OR_VAR := VAR | VAL
        rules.put(AdditionalSyntaxKind.VAR_OR_VAL, List.of(new OrNONTERM(List.of(Keyword.VAR, Keyword.VAL))));
        // NAME_EXPRESSION := IDENTIFIER_NAME_EXPRESSION | OPTION_NAME_EXPRESSION | GENERIC_NAME_EXPRESSION
        rules.put(AdditionalSyntaxKind.NAME_EXPRESSION,
                List.of(new OrNONTERM(List.of(SyntaxKind.IDENTIFIER_NAME_EXPRESSION,
                        SyntaxKind.OPTION_NAME_EXPRESSION,
                        SyntaxKind.GENERIC_NAME_EXPRESSION))));
        // TERMINAL := BAD | INDENT | DEDENT | IDENTIFIER | BOOLEAN | INTEGER | RUNE | STRING |
        //             SYMBOL.VALUES | KEYWORD.VALUES
        List<AnySyntaxKind> terms = new ArrayList<>(List.of(SyntaxKind.BAD,
                SyntaxKind.INDENT,
                SyntaxKind.DEDENT,
                SyntaxKind.IDENTIFIER,
                SyntaxKind.BOOLEAN,
                SyntaxKind.INTEGER,
                SyntaxKind.RUNE,
                SyntaxKind.STRING));
        terms.addAll(List.of(Keyword.values()));
        terms.addAll(List.of(Symbol.values()));
        rules.put(AdditionalSyntaxKind.TERMINAL, List.of(new OrNONTERM(terms)));
        // DEFINITION := TYPE_DEFINITION | FUNCTION_DEFINITION | VARIABLE_DEFINITION | TYPE_PARAMETER_DEFINITION |
        //               PARAMETER_DEFINITION
        rules.put(AdditionalSyntaxKind.DEFINITION,
                List.of(new OrNONTERM(List.of(SyntaxKind.TYPE_DEFINITION,
                        SyntaxKind.FUNCTION_DEFINITION,
                        SyntaxKind.VARIABLE_DEFINITION,
                        SyntaxKind.TYPE_PARAMETER_DEFINITION,
                        SyntaxKind.PARAMETER_DEFINITION))));
        // STATEMENT := VARIABLE_DEFINITION_STATEMENT | ASSIGNMENT_STATEMENT | EXPRESSION_STATEMENT |
        //              RETURN_STATEMENT | BREAK_STATEMENT | CONTINUE_STATEMENT | IF_STATEMENT |
        //              WHILE_STATEMENT | FOR_STATEMENT
        rules.put(AdditionalSyntaxKind.STATEMENT,
                List.of(new OrNONTERM(List.of(SyntaxKind.VARIABLE_DEFINITION_STATEMENT,
                        SyntaxKind.ASSIGNMENT_STATEMENT,
                        SyntaxKind.EXPRESSION_STATEMENT,
                        SyntaxKind.RETURN_STATEMENT,
                        SyntaxKind.BREAK_STATEMENT,
                        SyntaxKind.CONTINUE_STATEMENT,
                        SyntaxKind.IF_STATEMENT,
                        SyntaxKind.WHILE_STATEMENT,
                        SyntaxKind.FOR_STATEMENT))));
        // EXPRESSION := LOGICAL_AND_EXPRESSION | LOGICAL_OR_EXPRESSION | LOGICAL_NOT_EXPRESSION |
        //               EQUALS_EXPRESSION | NOT_EQUALS_EXPRESSION | LESS_THAN_EXPRESSION |
        //               LESS_THAN_OR_EQUAL_EXPRESSION |
        //               GREATER_THAN_EXPRESSION | GREATER_THAN_OR_EQUAL_EXPRESSION | IS_EXPRESSION |
        //               BITWISE_AND_EXPRESSION |
        //               BITWISE_OR_EXPRESSION | BITWISE_EXCLUSIVE_OR_EXPRESSION | BITWISE_LEFT_SHIFT_EXPRESSION |
        //               BITWISE_RIGHT_SHIFT_EXPRESSION | ADD_EXPRESSION | SUBTRACT_EXPRESSION | MULTIPLY_EXPRESSION |
        //               DIVIDE_EXPRESSION | MODULO_EXPRESSION | UNARY_PLUS_EXPRESSION |
        //               UNARY_MINUS_EXPRESSION | BITWISE_NOT_EXPRESSION | NAME_EXPRESSION | PRIMARY
        rules.put(AdditionalSyntaxKind.EXPRESSION,
                List.of(new OrNONTERM(List.of(AdditionalSyntaxKind.NAME_EXPRESSION,
                        AdditionalSyntaxKind.PRIMARY,
                        SyntaxKind.LOGICAL_AND_EXPRESSION,
                        SyntaxKind.LOGICAL_OR_EXPRESSION,
                        SyntaxKind.LOGICAL_NOT_EXPRESSION,
                        SyntaxKind.EQUALS_EXPRESSION,
                        SyntaxKind.NOT_EQUALS_EXPRESSION,
                        SyntaxKind.LESS_THAN_EXPRESSION,
                        SyntaxKind.LESS_THAN_OR_EQUAL_EXPRESSION,
                        SyntaxKind.GREATER_THAN_EXPRESSION,
                        SyntaxKind.GREATER_THAN_OR_EQUAL_EXPRESSION,
                        SyntaxKind.IS_EXPRESSION,
                        SyntaxKind.BITWISE_AND_EXPRESSION,
                        SyntaxKind.BITWISE_OR_EXPRESSION,
                        SyntaxKind.BITWISE_EXCLUSIVE_OR_EXPRESSION,
                        SyntaxKind.BITWISE_LEFT_SHIFT_EXPRESSION,
                        SyntaxKind.BITWISE_RIGHT_SHIFT_EXPRESSION,
                        SyntaxKind.ADD_EXPRESSION,
                        SyntaxKind.SUBTRACT_EXPRESSION,
                        SyntaxKind.MULTIPLY_EXPRESSION,
                        SyntaxKind.DIVIDE_EXPRESSION,
                        SyntaxKind.MODULO_EXPRESSION,
                        SyntaxKind.UNARY_PLUS_EXPRESSION,
                        SyntaxKind.UNARY_MINUS_EXPRESSION,
                        SyntaxKind.BITWISE_NOT_EXPRESSION))));
        // PRIMARY := MEMBER_ACCESS_EXPRESSION | INVOCATION_EXPRESSION | INDEX_EXPRESSION | SIMPLE_PRIMARY
        rules.put(AdditionalSyntaxKind.PRIMARY,
                List.of(new OrNONTERM(List.of(SyntaxKind.MEMBER_ACCESS_EXPRESSION,
                        SyntaxKind.INVOCATION_EXPRESSION,
                        SyntaxKind.INDEX_EXPRESSION,
                        AdditionalSyntaxKind.SIMPLE_PRIMARY))));

        // NameExpression

        // IDENTIFIER_NAME_EXPRESSION := IDENTIFIER
        rules.put(SyntaxKind.IDENTIFIER_NAME_EXPRESSION,
                List.of(SyntaxKind.IDENTIFIER));
        // OPTION_NAME_EXPRESSION := QUESTION NameExpression
        rules.put(SyntaxKind.OPTION_NAME_EXPRESSION,
                List.of(Symbol.QUESTION,
                        AdditionalSyntaxKind.NAME_EXPRESSION));
        // GENERIC_NAME_EXPRESSION := IDENTIFIER LESS_THAN SEPARATED_LIST[NameExpression, COMMA] GREATER_THAN
        rules.put(SyntaxKind.GENERIC_NAME_EXPRESSION,
                List.of(SyntaxKind.IDENTIFIER,
                        Symbol.LESS_THAN,
                        new SeparatedListNONTERM(AdditionalSyntaxKind.NAME_EXPRESSION,
                                Symbol.COMMA),
                        Symbol.GREATER_THAN));
    }

    public static HashMap<AnySyntaxKind, List<AnySyntaxKind>> getRules() {
        return rules;
    }
}
