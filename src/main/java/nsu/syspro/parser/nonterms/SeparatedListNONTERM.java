package nsu.syspro.parser.nonterms;

import syspro.tm.parser.AnySyntaxKind;
import syspro.tm.parser.SyntaxKind;

public class SeparatedListNONTERM extends ListNONTERM {
    private final AnySyntaxKind separator;

    public SeparatedListNONTERM(AnySyntaxKind extendedKind, AnySyntaxKind separator) {
        super(false, extendedKind);
        this.separator = separator;
    }

    public AnySyntaxKind getSeparator() {
        return separator;
    }

    @Override
    public boolean matchSyntaxKind(AnySyntaxKind kind) {
        if ((kind == extendedKind && accumulatedLength % 2 == 0)
                || (kind == separator && accumulatedLength % 2 == 1)) {
            accumulatedLength++;
            return true;
        }
        return false;
    }

    public AnySyntaxKind kind() {
        return SyntaxKind.SEPARATED_LIST;
    }
}
