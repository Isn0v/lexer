package nsu.syspro.parser.nonterms;

import syspro.tm.parser.AnySyntaxKind;

import java.util.List;

public class OrNONTERM implements AnySyntaxKind {
    final List<AnySyntaxKind> possibleKinds;

    public OrNONTERM(List<AnySyntaxKind> possibleKinds) {
        this.possibleKinds = possibleKinds;
    }

    public List<AnySyntaxKind> getPossibleKinds() {
        return possibleKinds;
    }
}
