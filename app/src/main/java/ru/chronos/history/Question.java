package ru.chronos.history;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class Question {
    public final int id;
    public final String text;
    public final List<String> options;
    public final int correct;
    public final String explanation;

    Question(int id, String text, int correct, String explanation, String... options) {
        if (options.length != 4 || correct < 0 || correct >= options.length) {
            throw new IllegalArgumentException("A question needs four options and a valid answer");
        }
        this.id = id;
        this.text = text;
        this.options = Collections.unmodifiableList(Arrays.asList(options.clone()));
        this.correct = correct;
        this.explanation = explanation;
    }
}
