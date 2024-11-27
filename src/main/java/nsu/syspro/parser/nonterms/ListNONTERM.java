package nsu.syspro.parser.nonterms;

import syspro.tm.parser.AnySyntaxKind;
import syspro.tm.parser.SyntaxKind;

public class ListNONTERM implements AnySyntaxKind {
    final boolean allowEmpty;
    final AnySyntaxKind extendedKind;
    int accumulatedLength;

    public ListNONTERM(boolean allowEmpty, AnySyntaxKind extendedKind) {
        this.allowEmpty = allowEmpty;
        this.extendedKind = extendedKind;
        this.accumulatedLength = 0;
    }

    public ListNONTERM(AnySyntaxKind extendedKind) {
        this(true, extendedKind);
    }

    public AnySyntaxKind getExtendedKind() {
        return extendedKind;
    }

    public int getAccumulatedLength() {
        return accumulatedLength;
    }

    public boolean isAllowEmpty() {
        return allowEmpty;
    }

    public boolean matchSyntaxKind(AnySyntaxKind kind) {
        if (kind == extendedKind) {
            accumulatedLength++;
            return true;
        }
        return false;
    }

    public AnySyntaxKind kind() {
        return SyntaxKind.LIST;
    }

    @Override
    public boolean isTerminal() {
        return false;
    }
}
