package nsu.syspro.parser;

import syspro.tm.parser.ErrorCode;

public class WrongSeparator implements ErrorCode {
    @Override
    public String name() {
        return "WrongSeparator";
    }
}
