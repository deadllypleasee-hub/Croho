package ru.chronos.history;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends Activity {
    private static final int BG = Color.rgb(16, 24, 33);
    private static final int PANEL = Color.rgb(26, 37, 49);
    private static final int GOLD = Color.rgb(231, 191, 117);
    private static final int INK = Color.rgb(239, 242, 245);
    private static final int MUTED = Color.rgb(170, 185, 199);
    private static final int GREEN = Color.rgb(131, 216, 172);
    private static final int RED = Color.rgb(255, 165, 158);
    private ProgressStore store;
    private Progress progress;
    private QuizSession session;
    private boolean showingQuiz;
    private LinearLayout content;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        store = new ProgressStore(this);
        progress = store.loadProgress();
        session = store.loadSession();
        showingQuiz = store.showingQuiz() && session != null;
        if (showingQuiz) renderQuiz(); else renderHome();
    }

    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }

    private void page() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);
        root.setOnApplyWindowInsetsListener((view, insets) -> {
            if (android.os.Build.VERSION.SDK_INT >= 30) {
                android.graphics.Insets bars = insets.getInsets(
                        WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
                view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            } else {
                view.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(),
                        insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            }
            return insets;
        });
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(24), dp(24), dp(24), dp(32));
        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, -1));
        setContentView(root);
        root.requestApplyInsets();
    }

    private GradientDrawable background(int color, int border) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(16));
        if (border != 0) drawable.setStroke(dp(1), border);
        return drawable;
    }

    private TextView label(LinearLayout parent, String value, int size, int color, boolean bold) {
        TextView text = new TextView(this);
        text.setText(value);
        text.setTextSize(size);
        text.setTextColor(color);
        text.setLineSpacing(dp(3), 1f);
        if (bold) text.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        parent.addView(text, new LinearLayout.LayoutParams(-1, -2));
        return text;
    }

    private void gap(LinearLayout parent, int height) {
        View space = new View(this);
        parent.addView(space, new LinearLayout.LayoutParams(1, dp(height)));
    }

    private Button button(LinearLayout parent, String text, boolean primary, Runnable action) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(16);
        button.setAllCaps(false);
        button.setTextColor(primary ? BG : INK);
        button.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        button.setBackground(background(primary ? GOLD : PANEL, primary ? 0 : Color.rgb(63, 79, 94)));
        button.setPadding(dp(16), dp(14), dp(16), dp(14));
        button.setMinHeight(dp(54));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.topMargin = dp(12);
        parent.addView(button, params);
        button.setOnClickListener(view -> action.run());
        return button;
    }

    private LinearLayout card() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(20), dp(20), dp(20), dp(20));
        card.setBackground(background(PANEL, 0));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.topMargin = dp(14);
        content.addView(card, params);
        return card;
    }

    private void progressBar(LinearLayout parent, int value, int max) {
        ProgressBar bar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        bar.setMax(max);
        bar.setProgress(value);
        bar.setProgressTintList(android.content.res.ColorStateList.valueOf(GOLD));
        bar.setProgressBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(50, 66, 81)));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp(8));
        params.topMargin = dp(12);
        params.bottomMargin = dp(12);
        parent.addView(bar, params);
        bar.setContentDescription("Прогресс: " + value + " из " + max);
    }

    private void renderHome() {
        showingQuiz = false;
        store.showingQuiz(false);
        page();
        label(content, "C H R O N O S", 15, GOLD, true);
        gap(content, 24);
        label(content, "У истории есть\nпродолжение. Ваше.", 32, INK, true);
        gap(content, 12);
        label(content, "От первых городов до космической эры.\n10 глав всемирной истории — в вашем темпе.", 16, MUTED, false);

        LinearLayout summary = card();
        label(summary, "ВАШ ПУТЬ", 12, GOLD, true);
        gap(summary, 8);
        label(summary, progress.completed() + " / 10 глав пройдено", 24, INK, true);
        progressBar(summary, progress.completed(), Catalog.LEVEL_COUNT);
        label(summary, "5 вопросов в главе · проходной балл 4 из 5", 14, MUTED, false);
        if (progress.examBest() >= 16) {
            gap(summary, 8);
            label(summary, "Экзамен сдан · " + progress.examBest() + " / 20", 16, GREEN, true);
        }
        if (session != null && !session.finished()) {
            button(content, "Продолжить · " + (session.level == -1 ? "экзамен" : "глава " + (session.level + 1)), true,
                    () -> { showingQuiz = true; store.showingQuiz(true); renderQuiz(); });
        } else {
            int next = 0;
            while (next < Catalog.LEVEL_COUNT - 1 && progress.best(next) >= 4) next++;
            final int nextLevel = next;
            if (progress.examUnlocked()) button(content, "К финальному экзамену", true, () -> requestStart(-1));
            else button(content, progress.completed() == 0 ? "Начать путешествие" : "Следующая глава", true,
                    () -> requestStart(nextLevel));
        }
        gap(content, 30);
        label(content, "Лента времени", 24, INK, true);
        gap(content, 4);
        label(content, "Каждая пройденная глава открывает следующую.", 14, MUTED, false);

        for (int level = 0; level < Catalog.LEVEL_COUNT; level++) {
            final int selectedLevel = level;
            boolean unlocked = progress.unlocked(level);
            int best = progress.best(level);
            LinearLayout chapter = card();
            String status = !unlocked ? "ЗАКРЫТО" : best >= 4 ? "ПРОЙДЕНО" : best >= 0 ? "ПОПРОБУЙТЕ ЕЩЁ" : "ДОСТУПНО";
            label(chapter, String.format(Locale.ROOT, "%02d  /  %s", level + 1, status), 12,
                    !unlocked ? MUTED : best >= 4 ? GREEN : GOLD, true);
            gap(chapter, 8);
            label(chapter, Catalog.TITLES[level], 21, unlocked ? INK : MUTED, true);
            gap(chapter, 4);
            label(chapter, Catalog.ERAS[level], 14, MUTED, false);
            if (best >= 0) {
                gap(chapter, 8);
                label(chapter, "Лучший результат: " + best + " / 5", 14, best >= 4 ? GREEN : MUTED, false);
            }
            if (unlocked) {
                Button start = button(chapter, best >= 0 ? "Пройти снова" : "Открыть главу", false,
                        () -> requestStart(selectedLevel));
                start.setContentDescription((best >= 0 ? "Повторить главу " : "Начать главу ") + (level + 1) + ": " + Catalog.TITLES[level]);
            } else {
                gap(chapter, 12);
                label(chapter, "Сначала наберите 4 из 5 в главе " + level + ".", 14, MUTED, false);
            }
        }
        LinearLayout exam = card();
        label(exam, "ФИНАЛЬНЫЙ ЭКЗАМЕН", 12, GOLD, true);
        gap(exam, 8);
        label(exam, "Вся история. Один вызов.", 23, INK, true);
        gap(exam, 8);
        label(exam, "20 вопросов: по два из каждой главы.\nДля сдачи нужно 16 правильных ответов.", 15, MUTED, false);
        if (progress.examUnlocked()) button(exam, "Начать экзамен", true, () -> requestStart(-1));
        else {
            gap(exam, 12);
            label(exam, "Откроется после прохождения всех 10 глав.", 14, MUTED, false);
        }
        gap(content, 28);
        label(content, "Работает офлайн. Прогресс хранится на этом устройстве.", 13, MUTED, false);
        gap(content, 12);
        label(content, getString(R.string.developer_credit), 14, GOLD, false);
        button(content, "Сбросить прогресс", false, () -> new AlertDialog.Builder(this)
                .setTitle("Сбросить весь прогресс?")
                .setMessage("Результаты глав, экзамена и текущая попытка будут удалены с устройства.")
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Сбросить", (dialog, which) -> {
                    store.reset(); progress = new Progress(); session = null; renderHome();
                }).show());
    }

    private void requestStart(int level) {
        if ((level == -1 && !progress.examUnlocked()) || (level >= 0 && !progress.unlocked(level))) return;
        if (session != null && !session.finished()) {
            new AlertDialog.Builder(this).setTitle("Начать новую попытку?")
                    .setMessage("Текущая незавершённая попытка будет заменена. Лучшие результаты сохранятся.")
                    .setNegativeButton("Отмена", null)
                    .setPositiveButton("Начать", (dialog, which) -> start(level)).show();
        } else start(level);
    }

    private void start(int level) {
        List<Question> questions = level == -1 ? Catalog.exam(new Random()) : new ArrayList<>(Catalog.level(level));
        session = new QuizSession(level, questions);
        showingQuiz = true;
        store.saveSession(session);
        store.showingQuiz(true);
        renderQuiz();
    }

    private void renderQuiz() {
        if (session.finished()) { renderResult(); return; }
        page();
        button(content, "‹  К ленте времени", false, this::renderHome);
        gap(content, 24);
        label(content, session.level == -1 ? "ФИНАЛЬНЫЙ ЭКЗАМЕН" : "ГЛАВА " + (session.level + 1), 12, GOLD, true);
        gap(content, 8);
        label(content, session.level == -1 ? "Всемирная история" : Catalog.TITLES[session.level], 24, INK, true);
        gap(content, 16);
        label(content, "Вопрос " + (session.position() + 1) + " из " + session.questions.size(), 14, MUTED, false);
        progressBar(content, session.position(), session.questions.size());
        final Question question = session.current();
        TextView prompt = label(content, question.text, 25, INK, true);
        prompt.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        gap(content, 12);
        int selected = session.selected();
        for (int choice = 0; choice < question.options.size(); choice++) {
            final int selectedChoice = choice;
            boolean correct = selected != -1 && choice == question.correct;
            boolean wrong = choice == selected && selected != question.correct;
            String prefix = correct ? "✓  " : wrong ? "×  " : (char) ('A' + choice) + "  ";
            Button answer = button(content, prefix + question.options.get(choice), false, () -> {
                if (session.selected() != -1) return;
                session.answer(selectedChoice);
                store.saveSession(session);
                renderQuiz();
            });
            answer.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            answer.setTag("answer_" + choice);
            if (selected != -1) answer.setOnClickListener(null);
            if (correct || wrong) {
                int color = correct ? GREEN : RED;
                answer.setTextColor(color);
                answer.setBackground(background(PANEL, color));
            }
            answer.setContentDescription(question.options.get(choice)
                    + (correct ? ". Правильный ответ" : wrong ? ". Ваш ответ, неверно" : ""));
        }
        if (selected != -1) {
            LinearLayout feedback = card();
            label(feedback, selected == question.correct ? "Верно. Так и было." : "Теперь вы знаете больше.",
                    18, selected == question.correct ? GREEN : RED, true);
            gap(feedback, 8);
            label(feedback, question.explanation, 16, INK, false);
            Button next = button(content, session.position() == session.questions.size() - 1 ? "Показать результат" : "Следующий вопрос", true,
                    () -> {
                        if (session.finished() || session.selected() == -1) return;
                        session.next();
                        store.saveSession(session);
                        renderQuiz();
                    });
            next.setTag("next");
        } else {
            gap(content, 20);
            label(content, "Выберите один ответ. После ответа появится пояснение.", 14, MUTED, false);
        }
    }

    private void renderResult() {
        progress.record(session.level, session.score());
        store.saveProgress(progress);
        page();
        boolean passed = session.passed();
        boolean exam = session.level == -1;
        label(content, exam ? "ФИНАЛЬНЫЙ ЭКЗАМЕН" : "ГЛАВА " + (session.level + 1) + " ЗАВЕРШЕНА", 12, GOLD, true);
        gap(content, 24);
        label(content, passed ? (exam ? "История покорена." : "Ещё одна эпоха\nпозади.") : "Знания растут\nс каждой попыткой.", 32, INK, true);
        LinearLayout result = card();
        label(result, session.score() + " / " + session.questions.size(), 48, passed ? GREEN : GOLD, true);
        gap(result, 8);
        label(result, "правильных ответов", 16, MUTED, false);
        progressBar(result, session.score(), session.questions.size());
        label(result, passed ? (exam ? "Экзамен сдан. Отличная работа!" : "Глава пройдена. Следующий этап открыт.")
                : "Для прохождения нужно " + (exam ? "16 из 20" : "4 из 5") + ". Попробуйте ещё раз.", 16, INK, false);
        if (passed && !exam) {
            int next = session.level + 1;
            button(content, next == Catalog.LEVEL_COUNT ? "К финальному экзамену" : "Следующая глава", true,
                    () -> requestStart(next == Catalog.LEVEL_COUNT ? -1 : next));
        }
        button(content, "Пройти ещё раз", !passed, () -> requestStart(session.level));
        button(content, "К ленте времени", false, this::renderHome);
        gap(content, 28);
        label(content, "Разбор ответов", 24, INK, true);
        int[] answers = session.answers();
        for (int i = 0; i < session.questions.size(); i++) {
            Question question = session.questions.get(i);
            boolean correct = answers[i] == question.correct;
            LinearLayout review = card();
            label(review, (i + 1) + ". " + question.text, 18, INK, true);
            gap(review, 10);
            label(review, "Ваш ответ: " + question.options.get(answers[i]), 15, correct ? GREEN : RED, false);
            if (!correct) {
                gap(review, 6);
                label(review, "Правильный: " + question.options.get(question.correct), 15, GREEN, false);
            }
            gap(review, 10);
            label(review, question.explanation, 15, MUTED, false);
        }
    }

    @Override public void onBackPressed() {
        if (showingQuiz) renderHome(); else super.onBackPressed();
    }
}
