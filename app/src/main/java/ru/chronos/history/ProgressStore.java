package ru.chronos.history;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

final class ProgressStore {
    private final SharedPreferences prefs;
    ProgressStore(Context context) {
        prefs = context.getSharedPreferences("chronos_progress_v1", Context.MODE_PRIVATE);
    }

    Progress loadProgress() {
        Progress progress = new Progress();
        for (int level = -1; level < Catalog.LEVEL_COUNT; level++) {
            int score = prefs.getInt("best_" + level, -1);
            int max = level == -1 ? Catalog.EXAM_SIZE : Catalog.QUESTIONS_PER_LEVEL;
            if (score >= 0 && score <= max) progress.record(level, score);
        }
        return progress;
    }

    void saveProgress(Progress progress) {
        SharedPreferences.Editor editor = prefs.edit();
        for (int level = -1; level < Catalog.LEVEL_COUNT; level++) {
            editor.putInt("best_" + level, level == -1 ? progress.examBest() : progress.best(level));
        }
        editor.apply();
    }

    QuizSession loadSession() {
        String raw = prefs.getString("session", null);
        if (raw == null) return null;
        try {
            JSONObject data = new JSONObject(raw);
            int level = data.getInt("level");
            JSONArray ids = data.getJSONArray("questions");
            JSONArray choices = data.getJSONArray("answers");
            int expected = level == -1 ? Catalog.EXAM_SIZE : Catalog.QUESTIONS_PER_LEVEL;
            if (ids.length() != expected || choices.length() != expected) return null;
            List<Question> questions = new ArrayList<>();
            int[] answers = new int[expected];
            for (int i = 0; i < expected; i++) {
                int id = ids.getInt(i);
                if (level != -1 && id / Catalog.QUESTIONS_PER_LEVEL != level) return null;
                questions.add(Catalog.byId(id));
                answers[i] = choices.getInt(i);
            }
            return new QuizSession(level, questions, answers, data.getInt("position"));
        } catch (JSONException | IllegalArgumentException | IndexOutOfBoundsException ignored) {
            return null; // An incompatible or damaged attempt must never prevent opening the app.
        }
    }

    void saveSession(QuizSession session) {
        if (session == null) {
            prefs.edit().remove("session").apply();
            return;
        }
        try {
            JSONObject data = new JSONObject();
            JSONArray ids = new JSONArray();
            JSONArray answers = new JSONArray();
            for (Question question : session.questions) ids.put(question.id);
            for (int answer : session.answers()) answers.put(answer);
            data.put("level", session.level);
            data.put("position", session.position());
            data.put("questions", ids);
            data.put("answers", answers);
            prefs.edit().putString("session", data.toString()).apply();
        } catch (JSONException exception) {
            throw new IllegalStateException("Cannot save quiz", exception);
        }
    }

    boolean showingQuiz() { return prefs.getBoolean("showing_quiz", false); }
    void showingQuiz(boolean showing) { prefs.edit().putBoolean("showing_quiz", showing).apply(); }
    void reset() { prefs.edit().clear().apply(); }
}
