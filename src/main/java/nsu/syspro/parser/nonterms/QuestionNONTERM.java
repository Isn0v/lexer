package nsu.syspro.parser.nonterms;

import syspro.tm.parser.AnySyntaxKind;

public class QuestionNONTERM implements AnySyntaxKind {
    final AnySyntaxKind extendedKind;
    public final boolean saveInParsingTree;

    public QuestionNONTERM(AnySyntaxKind extendedKind, boolean saveInParsingTree) {
        this.extendedKind = extendedKind;
        this.saveInParsingTree = saveInParsingTree;
    }

    public QuestionNONTERM(AnySyntaxKind extendedKind) {
        this(extendedKind, false);
    }

    public AnySyntaxKind getExtendedKind() {
        return extendedKind;
    }

    @Override
    public boolean isTerminal() {
        return false;
    }
}
