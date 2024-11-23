package nsu.syspro.parser.nonterms;

import syspro.tm.parser.AnySyntaxKind;

public class QuestionNONTERM implements AnySyntaxKind {
    final AnySyntaxKind extendedKind;

    public QuestionNONTERM(AnySyntaxKind extendedKind) {
        this.extendedKind = extendedKind;
    }

    public AnySyntaxKind getExtendedKind() {
        return extendedKind;
    }
}
