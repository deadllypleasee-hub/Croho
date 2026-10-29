package ru.chronos.history;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/** A quiz is advanced explicitly after feedback; answers cannot be changed or counted twice. */
public final class QuizSession {
    public final int level; // -1 is the final exam.
    public final List<Question> questions;
    private final int[] answers;
    private int position;

    public QuizSession(int level, List<Question> questions) {
        this(level, questions, emptyAnswers(questions.size()), 0);
    }

    public QuizSession(int level, List<Question> questions, int[] answers, int position) {
        if (level < -1 || level >= Catalog.LEVEL_COUNT || questions.isEmpty()
                || answers.length != questions.size() || position < 0 || position > questions.size()) {
            throw new IllegalArgumentException("Invalid quiz state");
        }
        HashSet<Integer> ids = new HashSet<>();
        for (int i = 0; i < questions.size(); i++) {
            if (!ids.add(questions.get(i).id) || answers[i] < -1 || answers[i] > 3
                    || (i < position && answers[i] == -1) || (i > position && answers[i] != -1)) {
                throw new IllegalArgumentException("Invalid answers");
            }
        }
        this.level = level;
        this.questions = Collections.unmodifiableList(new ArrayList<>(questions));
        this.answers = answers.clone();
        this.position = position;
    }

    private static int[] emptyAnswers(int size) {
        int[] answers = new int[size];
        java.util.Arrays.fill(answers, -1);
        return answers;
    }

    public int position() { return position; }
    public boolean finished() { return position == questions.size(); }
    public Question current() {
        if (finished()) throw new IllegalStateException("Quiz is finished");
        return questions.get(position);
    }
    public int selected() { return finished() ? -1 : answers[position]; }
    public int[] answers() { return answers.clone(); }

    public void answer(int choice) {
        if (finished() || selected() != -1) throw new IllegalStateException("Already answered");
        if (choice < 0 || choice >= current().options.size()) throw new IllegalArgumentException("Invalid choice");
        answers[position] = choice;
    }

    public void next() {
        if (finished() || selected() == -1) throw new IllegalStateException("Answer first");
        position++;
    }

    public int score() {
        int score = 0;
        for (int i = 0; i < answers.length; i++) {
            if (answers[i] == questions.get(i).correct) score++;
        }
        return score;
    }

    public boolean passed() { return finished() && score() * 5 >= questions.size() * 4; }
}
