package ru.chronos.history;

import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class AppFlowTest {
    private Context context;

    @Before public void resetProgress() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        new ProgressStore(context).reset();
    }

    private static View text(View root, String value) {
        if (root instanceof TextView && value.contentEquals(((TextView) root).getText())) return root;
        if (root instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) root;
            for (int i = 0; i < group.getChildCount(); i++) {
                View found = text(group.getChildAt(i), value);
                if (found != null) return found;
            }
        }
        return null;
    }

    private static void click(MainActivity activity, String value) {
        View view = text(activity.findViewById(android.R.id.content), value);
        assertNotNull("Missing button: " + value, view);
        assertTrue(view.performClick());
    }

    @Test public void systemBackKeepsAnswerAndHomeStateAcrossRelaunch() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                click(activity, "Начать путешествие");
                activity.findViewById(android.R.id.content)
                        .findViewWithTag("answer_" + Catalog.byId(0).correct).performClick();
            });
            InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            scenario.onActivity(activity -> {
                assertNotNull(text(activity.findViewById(android.R.id.content), "CHRONOS"));
                assertNotNull(text(activity.findViewById(android.R.id.content), "Продолжить · глава 1"));
                assertFalse(new ProgressStore(activity).showingQuiz());
            });
        }
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                assertNotNull(text(activity.findViewById(android.R.id.content), "CHRONOS"));
                click(activity, "Продолжить · глава 1");
                QuizSession restored = new ProgressStore(activity).loadSession();
                assertNotNull(restored);
                assertEquals(0, restored.position());
                assertEquals(Catalog.byId(0).correct, restored.selected());
                assertEquals(1, restored.score());
                activity.findViewById(android.R.id.content).findViewWithTag("next").performClick();
            });
            scenario.recreate();
            scenario.onActivity(activity -> {
                QuizSession restored = new ProgressStore(activity).loadSession();
                assertNotNull(restored);
                assertEquals(1, restored.position());
                assertEquals(-1, restored.selected());
                assertEquals(1, restored.score());
                assertNull(activity.findViewById(android.R.id.content).findViewWithTag("next"));
            });
        }
    }

    @Test public void attemptSurvivesRecreationAndRelaunchAndUnlocksNextChapter() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                click(activity, "Начать путешествие");
                activity.findViewById(android.R.id.content).findViewWithTag("answer_" + Catalog.byId(0).correct).performClick();
            });
            scenario.recreate();
            scenario.onActivity(activity -> {
                assertNotNull(activity.findViewById(android.R.id.content).findViewWithTag("next"));
                QuizSession saved = new ProgressStore(activity).loadSession();
                assertNotNull(saved);
                assertEquals(1, saved.score());
                // A second tap on the same answered option must leave the score unchanged.
                activity.findViewById(android.R.id.content).findViewWithTag("answer_" + Catalog.byId(0).correct).performClick();
                assertEquals(1, new ProgressStore(activity).loadSession().score());
            });
        }
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                activity.findViewById(android.R.id.content).findViewWithTag("next").performClick();
                for (int i = 1; i < 5; i++) {
                    int answer = Catalog.byId(i).correct;
                    if (i == 4) answer = (answer + 1) % 4;
                    activity.findViewById(android.R.id.content).findViewWithTag("answer_" + answer).performClick();
                    activity.findViewById(android.R.id.content).findViewWithTag("next").performClick();
                }
                assertNotNull(text(activity.findViewById(android.R.id.content), "4 / 5"));
                Progress progress = new ProgressStore(activity).loadProgress();
                assertEquals(4, progress.best(0));
                assertTrue(progress.unlocked(1));
                assertFalse(progress.examUnlocked());
                click(activity, "Следующая глава");
                assertEquals(1, new ProgressStore(activity).loadSession().level);
            });
        }
    }

    @Test public void finalExamCanBeCompletedAndReviewed() {
        ProgressStore store = new ProgressStore(context);
        Progress progress = new Progress();
        for (int level = 0; level < 10; level++) progress.record(level, 5);
        store.saveProgress(progress);
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                click(activity, "К финальному экзамену");
                for (int i = 0; i < 20; i++) {
                    QuizSession quiz = new ProgressStore(activity).loadSession();
                    assertNotNull(quiz);
                    activity.findViewById(android.R.id.content).findViewWithTag("answer_" + quiz.current().correct).performClick();
                    activity.findViewById(android.R.id.content).findViewWithTag("next").performClick();
                }
                assertNotNull(text(activity.findViewById(android.R.id.content), "Экзамен сдан. Отличная работа!"));
                assertNotNull(text(activity.findViewById(android.R.id.content), "Разбор ответов"));
                assertEquals(20, new ProgressStore(activity).loadProgress().examBest());
            });
        }
    }
}
