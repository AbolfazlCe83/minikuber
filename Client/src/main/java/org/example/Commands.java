package org.example;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum Commands {
    CREATE_TASK("create task --name=(?<taskName>\\S+)"),
    CREATE_SPECIAL_TASK("create task --name=(?<taskName>\\S+) --node=(?<workerId>\\S+)"),
    GET_TASKS("get tasks"),
    DELETE_TASK("delete task --name=(?<taskName>\\S+)"),
    GET_NODES("get nodes"),
    CORDON("cordon node (?<workerName>\\S+)");
    private final String regex;

    private Commands(String regex) {
        this.regex = regex;
    }


    public static Matcher getMatcher(String input, Commands command) {
        Matcher matcher = Pattern.compile(command.regex).matcher(input);
        return matcher.matches() ? matcher : null;
    }
}
