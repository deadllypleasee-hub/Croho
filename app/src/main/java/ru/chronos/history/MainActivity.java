package ru.chronos.history;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.view.animation.DecelerateInterpolator;
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
    private static final int BG = 0xFFF6F7FC, INK = 0xFF252948, MUTED = 0xFF666E87;
    private static final int PURPLE = 0xFF694CDF, BORDER = 0xFFE4E7F2;
    private static final int GREEN = 0xFF12745E, RED = 0xFFAB3940, LIME = 0xFFD3F67C;
    private static final int[] EPOCH_COLORS = {
        0xFFFFD46C, 0xFFB5E5F1, 0xFFFFBAA6, 0xFFD4C9FF, 0xFFBCEBD6,
        0xFFB8E4FF, 0xFFFFDDA6, 0xFFD5D0FD, 0xFFC1E4F7, 0xFFF5C3E0
    };
    private ProgressStore store;
    private Progress progress;
    private QuizSession session;
    private boolean showingQuiz;
    private LinearLayout content;
    private int cardSequence;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(android.os.Build.VERSION.SDK_INT >= 26 ? BG : INK);
        int flags = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        if (android.os.Build.VERSION.SDK_INT >= 26) flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        getWindow().getDecorView().setSystemUiVisibility(flags);
        store = new ProgressStore(this);
        progress = store.loadProgress();
        session = store.loadSession();
        showingQuiz = store.showingQuiz() && session != null;
        if (showingQuiz) renderQuiz(); else renderHome();
    }

    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
    private LinearLayout column() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        return layout;
    }
    private LinearLayout row(LinearLayout parent) {
        LinearLayout layout = new LinearLayout(this);
        layout.setGravity(Gravity.CENTER_VERTICAL);
        parent.addView(layout, new LinearLayout.LayoutParams(-1, -2));
        return layout;
    }
    private void page() {
        cardSequence = 0;
        LinearLayout root = column();
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
        scroll.setClipToPadding(false);
        scroll.setVerticalScrollBarEnabled(false);
        content = column();
        content.setPadding(dp(20), dp(16), dp(20), dp(28));
        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, -1));
        setContentView(root);
        root.requestApplyInsets();
    }
    private GradientDrawable background(int color, int border, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radius));
        if (border != 0) drawable.setStroke(dp(1), border);
        return drawable;
    }
    private TextView label(LinearLayout parent, String value, int size, int color, boolean bold) {
        TextView text = new TextView(this);
        text.setText(value);
        text.setTextSize(size);
        text.setTextColor(color);
        text.setLineSpacing(dp(2), 1f);
        text.setIncludeFontPadding(false);
        if (bold) text.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        parent.addView(text, new LinearLayout.LayoutParams(-1, -2));
        return text;
    }
    private void gap(LinearLayout parent, int height) {
        parent.addView(new View(this), new LinearLayout.LayoutParams(1, dp(height)));
    }
    private boolean motionEnabled() {
        if (android.os.Build.VERSION.SDK_INT >= 26) return ValueAnimator.areAnimatorsEnabled();
        return Settings.Global.getFloat(getContentResolver(), Settings.Global.ANIMATOR_DURATION_SCALE, 1f) != 0;
    }
    private void enter(View view, long delay) {
        if (!motionEnabled()) return;
        view.setAlpha(0f);
        view.setTranslationY(dp(12));
        view.animate().alpha(1f).translationY(0).setStartDelay(delay).setDuration(260)
                .setInterpolator(new DecelerateInterpolator()).start();
    }
    private Button button(LinearLayout parent, String text, boolean primary, Runnable action) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(16);
        button.setAllCaps(false);
        button.setTextColor(primary ? Color.WHITE : PURPLE);
        button.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        button.setBackground(background(primary ? PURPLE : Color.WHITE, primary ? 0 : BORDER, 17));
        button.setPadding(dp(16), dp(14), dp(16), dp(14));
        button.setMinHeight(dp(54));
        button.setStateListAnimator(null);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.topMargin = dp(12);
        parent.addView(button, params);
        button.setOnClickListener(view -> action.run());
        return button;
    }
    private LinearLayout card(int color) {
        LinearLayout card = column();
        card.setPadding(dp(20), dp(20), dp(20), dp(20));
        card.setBackground(background(color, color == Color.WHITE ? BORDER : 0, 24));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.topMargin = dp(14);
        params.bottomMargin = dp(2);
        content.addView(card, params);
        enter(card, Math.min(cardSequence++ * 30L, 150L));
        return card;
    }
    private void pill(LinearLayout parent, String value, int color, int fill) {
        TextView badge = label(parent, value, 12, color, true);
        badge.setPadding(dp(10), dp(6), dp(10), dp(6));
        badge.setBackground(background(fill, 0, 12));
        badge.setLayoutParams(new LinearLayout.LayoutParams(-2, -2));
    }
    private void progressBar(LinearLayout parent, int value, int max, int fill, int track) {
        ProgressBar bar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        bar.setMax(max);
        bar.setProgress(value);
        bar.setProgressTintList(ColorStateList.valueOf(fill));
        bar.setProgressBackgroundTintList(ColorStateList.valueOf(track));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp(8));
        params.topMargin = dp(12);
        params.bottomMargin = dp(10);
        parent.addView(bar, params);
        bar.setContentDescription("Прогресс: " + value + " из " + max);
    }
    private GameArtView art(LinearLayout parent, int type, int size) {
        GameArtView art = new GameArtView(this, type);
        parent.addView(art, new LinearLayout.LayoutParams(dp(size), dp(size)));
        return art;
    }
    private void brand() {
        LinearLayout brand = row(content);
        art(brand, GameArtView.HOURGLASS, 40);
        LinearLayout title = column();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, -2, 1f);
        params.leftMargin = dp(9);
        brand.addView(title, params);
        label(title, "CHRONOS", 21, INK, true).setLetterSpacing(0.09f);
        pill(brand, "ОФЛАЙН", GREEN, 0xFFE4F5EE);
    }

    private void renderHome() {
        showingQuiz = false;
        store.showingQuiz(false);
        page();
        brand();
        gap(content, 24);
        label(content, "История —\nтвоя игра!", 34, INK, true);
        gap(content, 9);
        label(content, "Открывай эпохи. Собирай награды.", 15, MUTED, false);
        LinearLayout hero = card(PURPLE);
        LinearLayout heroRow = row(hero);
        LinearLayout heroText = column();
        heroRow.addView(heroText, new LinearLayout.LayoutParams(0, -2, 1f));
        label(heroText, "ТВОЁ ПУТЕШЕСТВИЕ", 11, 0xFFE4DEFF, true).setLetterSpacing(0.06f);
        gap(heroText, 12);
        label(heroText, progress.completed() + " / 10 эпох", 30, Color.WHITE, true);
        gap(heroText, 6);
        label(heroText, progress.completed() == 0 ? "Приключение начинается!" : "Каждая глава — новое открытие", 13, Color.WHITE, false);
        art(heroRow, GameArtView.HOURGLASS, 88);
        progressBar(hero, progress.completed(), Catalog.LEVEL_COUNT, LIME, 0xFF8871E8);
        label(hero, "5 вопросов · 4 верных ответа для победы", 12, Color.WHITE, false);
        Button continueButton;
        if (session != null && !session.finished()) {
            continueButton = button(hero, "Продолжить · " + (session.level == -1 ? "экзамен" : "глава " + (session.level + 1)), true,
                    () -> { showingQuiz = true; store.showingQuiz(true); renderQuiz(); });
        } else {
            int next = 0;
            while (next < Catalog.LEVEL_COUNT - 1 && progress.best(next) >= 4) next++;
            final int nextLevel = next;
            if (progress.examUnlocked()) continueButton = button(hero, "К финальному экзамену", true, () -> requestStart(-1));
            else continueButton = button(hero, progress.completed() == 0 ? "Начать путешествие" : "Следующая глава", true,
                    () -> requestStart(nextLevel));
        }
        continueButton.setBackground(background(LIME, 0, 16));
        continueButton.setTextColor(INK);
        gap(content, 24);
        label(content, "Твои награды", 22, INK, true);
        gap(content, 12);
        LinearLayout rewards = row(content);
        reward(rewards, "Первый шаг", 1, progress.completed() >= 1, 0xFFFFE5AE);
        reward(rewards, "Знаток эпох", 3, progress.completed() >= 3, 0xFFDBD1FF);
        reward(rewards, "Хранитель\nистории", 10, progress.completed() == 10, 0xFFC7EEDF);
        gap(content, 28);
        label(content, "Карта эпох", 24, INK, true);
        gap(content, 6);
        label(content, "Пройди главу, чтобы открыть следующую.", 14, MUTED, false);
        for (int level = 0; level < Catalog.LEVEL_COUNT; level++) chapter(level);
        LinearLayout exam = card(0xFFFFE7DF);
        LinearLayout examHeader = row(exam);
        LinearLayout examTitle = column();
        examHeader.addView(examTitle, new LinearLayout.LayoutParams(0, -2, 1f));
        pill(examTitle, progress.examBest() >= 16 ? "ЭКЗАМЕН СДАН" : "ФИНАЛЬНЫЙ ВЫЗОВ", INK, 0xFFFFD0BE);
        gap(examTitle, 10);
        label(examTitle, "Вся история.\nОдин вызов.", 25, INK, true);
        art(examHeader, GameArtView.TROPHY, 76);
        gap(exam, 12);
        label(exam, "20 вопросов из всех эпох. Для сдачи нужно 16 правильных ответов.", 15, INK, false);
        if (progress.examBest() >= 0) {
            gap(exam, 10);
            label(exam, "Твой рекорд: " + progress.examBest() + " / 20", 15, GREEN, true);
        }
        if (progress.examUnlocked()) button(exam, "Начать экзамен", true, () -> requestStart(-1));
        else { gap(exam, 14); pill(exam, "После прохождения 10 глав", MUTED, 0xFFFFF5F0); }
        gap(content, 28);
        label(content, getString(R.string.developer_credit), 13, INK, true);
        gap(content, 8);
        label(content, "Прогресс хранится на этом устройстве. Играть можно без интернета.", 12, MUTED, false);
        button(content, "Сбросить прогресс", false, () -> new AlertDialog.Builder(this)
                .setTitle("Сбросить весь прогресс?")
                .setMessage("Результаты, награды и текущая попытка будут удалены с устройства.")
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Сбросить", (dialog, which) -> {
                    store.reset(); progress = new Progress(); session = null; renderHome();
                }).show());
    }
    private void reward(LinearLayout parent, String title, int required, boolean earned, int color) {
        LinearLayout reward = column();
        reward.setGravity(Gravity.CENTER_HORIZONTAL);
        reward.setPadding(dp(7), dp(12), dp(7), dp(12));
        reward.setBackground(background(earned ? color : 0xFFECEDF5, 0, 20));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, -1, 1f);
        if (parent.getChildCount() > 0) params.leftMargin = dp(8);
        parent.addView(reward, params);
        GameArtView medal = art(reward, GameArtView.MEDAL, 48);
        if (!earned) medal.setAlpha(0.38f);
        gap(reward, 6);
        label(reward, title, 12, earned ? INK : MUTED, true).setGravity(Gravity.CENTER);
        gap(reward, 5);
        label(reward, earned ? "Получена" : required + (required == 1 ? " глава" : required == 3 ? " главы" : " глав"), 10, earned ? GREEN : MUTED, false).setGravity(Gravity.CENTER);
        reward.setContentDescription(title.replace('\n', ' ') + (earned ? ", награда получена" : ", пройди глав: " + required));
        reward.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
    }
    private void chapter(int level) {
        boolean unlocked = progress.unlocked(level);
        int best = progress.best(level);
        LinearLayout chapter = card(Color.WHITE);
        LinearLayout header = row(chapter);
        GameArtView icon = art(header, level, 66);
        icon.setBackground(background(unlocked ? EPOCH_COLORS[level] : 0xFFECEDF5, 0, 20));
        if (!unlocked) icon.setAlpha(0.55f);
        LinearLayout text = column();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, -2, 1f);
        params.leftMargin = dp(14);
        header.addView(text, params);
        label(text, String.format(Locale.ROOT, "ГЛАВА %02d", level + 1), 10, best >= 4 ? GREEN : MUTED, true).setLetterSpacing(0.06f);
        gap(text, 6);
        label(text, Catalog.TITLES[level], 19, unlocked ? INK : MUTED, true);
        gap(text, 5);
        label(text, Catalog.ERAS[level], 12, MUTED, false);
        gap(chapter, 14);
        if (unlocked) {
            pill(chapter, best >= 4 ? "✓ Пройдено · " + best + " / 5" : best >= 0 ? "Твой рекорд: " + best + " / 5" : "Новая эпоха · 5 вопросов",
                    best >= 4 ? GREEN : PURPLE, best >= 4 ? 0xFFE4F5EE : 0xFFF0ECFF);
            Button start = button(chapter, best >= 0 ? "Пройти снова" : "Открыть главу", false, () -> requestStart(level));
            start.setContentDescription((best >= 0 ? "Повторить главу " : "Начать главу ") + (level + 1) + ": " + Catalog.TITLES[level]);
        } else pill(chapter, "Закрыто · сначала глава " + level, MUTED, 0xFFF1F2F7);
    }

    private void requestStart(int level) {
        if ((level == -1 && !progress.examUnlocked()) || (level >= 0 && !progress.unlocked(level))) return;
        if (session != null && !session.finished()) {
            new AlertDialog.Builder(this).setTitle("Начать новую попытку?")
                    .setMessage("Текущая попытка будет заменена. Лучшие результаты и награды сохранятся.")
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
        gap(content, 22);
        LinearLayout top = row(content);
        LinearLayout topText = column();
        top.addView(topText, new LinearLayout.LayoutParams(0, -2, 1f));
        pill(topText, session.level == -1 ? "ФИНАЛЬНЫЙ ЭКЗАМЕН" : "ГЛАВА " + (session.level + 1), PURPLE, 0xFFECE6FF);
        gap(topText, 9);
        label(topText, session.level == -1 ? "Всемирная история" : Catalog.TITLES[session.level], 20, INK, true);
        art(top, session.level == -1 ? GameArtView.TROPHY : session.level, 56);
        gap(content, 20);
        label(content, "Вопрос " + (session.position() + 1) + " из " + session.questions.size() + "     ·     Верно: " + session.score(), 13, MUTED, true);
        progressBar(content, session.position(), session.questions.size(), PURPLE, BORDER);
        final Question question = session.current();
        LinearLayout promptCard = card(Color.WHITE);
        label(promptCard, question.text, 23, INK, true).setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        gap(content, 4);
        int selected = session.selected();
        for (int choice = 0; choice < question.options.size(); choice++) {
            final int selectedChoice = choice;
            boolean correct = selected != -1 && choice == question.correct;
            boolean wrong = choice == selected && selected != question.correct;
            String prefix = correct ? "✓  " : wrong ? "×  " : (char) ('A' + choice) + "   ";
            Button answer = button(content, prefix + question.options.get(choice), false, () -> {
                if (session.selected() != -1) return;
                session.answer(selectedChoice);
                store.saveSession(session);
                renderQuiz();
            });
            answer.setTextColor(INK);
            answer.setTextSize(15);
            answer.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            answer.setTag("answer_" + choice);
            if (selected != -1) answer.setOnClickListener(null);
            if (correct || wrong) {
                int color = correct ? GREEN : RED;
                answer.setTextColor(color);
                answer.setBackground(background(correct ? 0xFFE0F6EB : 0xFFFFE9E6, color, 17));
            }
            answer.setContentDescription(question.options.get(choice)
                    + (correct ? ". Правильный ответ" : wrong ? ". Ваш ответ, неверно" : ""));
        }
        if (selected != -1) {
            LinearLayout feedback = card(selected == question.correct ? 0xFFE0F6EB : 0xFFFFE9E6);
            label(feedback, selected == question.correct ? "Отлично! Всё верно." : "Ещё одно открытие!", 19,
                    selected == question.correct ? GREEN : RED, true);
            gap(feedback, 9);
            label(feedback, question.explanation, 15, INK, false);
            Button next = button(content, session.position() == session.questions.size() - 1 ? "Показать результат" : "Следующий вопрос", true, () -> {
                if (session.finished() || session.selected() == -1) return;
                session.next(); store.saveSession(session); renderQuiz();
            });
            next.setTag("next");
            next.post(() -> next.requestRectangleOnScreen(
                    new android.graphics.Rect(0, 0, next.getWidth(), next.getHeight()), !motionEnabled()));
        } else {
            gap(content, 18);
            label(content, "Выбери один ответ — и узнай историю за ним.", 13, MUTED, false);
        }
    }

    private void renderResult() {
        progress.record(session.level, session.score());
        store.saveProgress(progress);
        page();
        boolean passed = session.passed(), exam = session.level == -1;
        gap(content, 8);
        LinearLayout celebration = column();
        celebration.setGravity(Gravity.CENTER_HORIZONTAL);
        content.addView(celebration, new LinearLayout.LayoutParams(-1, -2));
        GameArtView trophy = art(celebration, passed ? GameArtView.TROPHY : GameArtView.HOURGLASS, 112);
        if (motionEnabled()) {
            trophy.setScaleX(0.8f); trophy.setScaleY(0.8f); trophy.setRotation(-10);
            trophy.animate().scaleX(1f).scaleY(1f).rotation(0).setDuration(420)
                    .setInterpolator(new android.view.animation.OvershootInterpolator(1.1f)).start();
        }
        gap(celebration, 12);
        label(celebration, passed ? (exam ? "История покорена!" : "Эпоха пройдена!") : "Каждая попытка —\nновое открытие", 30, INK, true).setGravity(Gravity.CENTER);
        gap(celebration, 9);
        label(celebration, exam ? "ФИНАЛЬНЫЙ ЭКЗАМЕН" : "ГЛАВА " + (session.level + 1) + " ЗАВЕРШЕНА", 11, MUTED, true).setGravity(Gravity.CENTER);
        LinearLayout result = card(Color.WHITE);
        label(result, session.score() + " / " + session.questions.size(), 50, passed ? PURPLE : INK, true).setGravity(Gravity.CENTER);
        gap(result, 4);
        label(result, "правильных ответов", 14, MUTED, false).setGravity(Gravity.CENTER);
        progressBar(result, session.score(), session.questions.size(), passed ? 0xFF21B992 : 0xFFFFB96C, BORDER);
        label(result, passed ? (exam ? "Экзамен сдан. Отличная работа!" : "Глава пройдена. Следующий этап открыт.")
                : "Для прохождения нужно " + (exam ? "16 из 20" : "4 из 5") + ". Попробуй ещё раз!", 15, INK, false).setGravity(Gravity.CENTER);
        if (passed && !exam) {
            gap(result, 12);
            pill(result, "✓ Ещё одна эпоха в твоей коллекции", GREEN, 0xFFE4F5EE);
            int completed = progress.completed();
            if (completed == 1 || completed == 3 || completed == 10) {
                gap(result, 9);
                label(result, "Награда: " + (completed == 1 ? "Первый шаг" : completed == 3 ? "Знаток эпох" : "Хранитель истории"), 14, PURPLE, true);
            }
        }
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
            LinearLayout review = card(Color.WHITE);
            pill(review, (correct ? "✓  " : "×  ") + "ВОПРОС " + (i + 1), correct ? GREEN : RED, correct ? 0xFFE4F5EE : 0xFFFFE9E6);
            gap(review, 12);
            label(review, question.text, 18, INK, true);
            gap(review, 12);
            label(review, "Твой ответ: " + question.options.get(answers[i]), 14, correct ? GREEN : RED, true);
            if (!correct) {
                gap(review, 6);
                label(review, "Правильный: " + question.options.get(question.correct), 14, GREEN, false);
            }
            gap(review, 12);
            label(review, question.explanation, 14, MUTED, false);
        }
    }
    @Override public void onBackPressed() {
        if (showingQuiz) renderHome(); else super.onBackPressed();
    }
}
