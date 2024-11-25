package nsu.syspro.parser;

import syspro.tm.parser.ErrorCode;

public class UnrecognisedToken implements ErrorCode {
    @Override
    public String name() {
        return "UnrecognisedToken";
    }
}
