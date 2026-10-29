package ru.chronos.history;

import org.junit.Test;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import static org.junit.Assert.*;

public class QuizTest {
    @Test public void catalogHasTenCompleteChaptersAndUniqueQuestions() {
        HashSet<Integer> ids = new HashSet<>();
        for (int level = 0; level < 10; level++) {
            assertEquals(5, Catalog.level(level).size());
            for (Question question : Catalog.level(level)) {
                assertTrue(ids.add(question.id));
                assertFalse(question.text.isEmpty());
                assertFalse(question.explanation.isEmpty());
                assertEquals(4, new HashSet<>(question.options).size());
            }
        }
        assertEquals(50, ids.size());
    }

    @Test public void examSamplesTwoDistinctQuestionsFromEveryChapter() {
        for (int seed = 0; seed < 100; seed++) {
            List<Question> questions = Catalog.exam(new Random(seed));
            int[] count = new int[10];
            HashSet<Integer> ids = new HashSet<>();
            for (Question question : questions) {
                assertTrue(ids.add(question.id));
                count[question.id / 5]++;
            }
            assertEquals(20, questions.size());
            for (int total : count) assertEquals(2, total);
        }
    }

    @Test public void answerCannotBeCountedTwiceOrSkipped() {
        QuizSession quiz = new QuizSession(0, Catalog.level(0));
        assertThrows(IllegalStateException.class, quiz::next);
        assertThrows(IllegalArgumentException.class, () -> quiz.answer(4));
        quiz.answer(quiz.current().correct);
        assertEquals(1, quiz.score());
        assertThrows(IllegalStateException.class, () -> quiz.answer(0));
        assertEquals(1, quiz.score());
        quiz.next();
        assertEquals(-1, quiz.selected());
    }

    @Test public void restoredAttemptKeepsFeedbackAndScoreWithoutSharingMutableArrays() {
        QuizSession quiz = new QuizSession(0, Catalog.level(0));
        quiz.answer(quiz.current().correct);
        quiz.next();
        quiz.answer((quiz.current().correct + 1) % 4);
        int[] saved = quiz.answers();
        QuizSession restored = new QuizSession(0, quiz.questions, saved, quiz.position());
        saved[0] = -1;
        restored.answers()[0] = -1;
        assertEquals(1, restored.score());
        assertEquals(quiz.selected(), restored.selected());
        assertEquals(1, restored.position());
        assertThrows(IllegalArgumentException.class,
                () -> new QuizSession(0, Catalog.level(0), new int[] {-1, -1, -1, -1, -1}, 2));
    }

    @Test public void eightyPercentPassesOnlyAfterFinishing() {
        QuizSession quiz = new QuizSession(0, Catalog.level(0));
        for (int i = 0; i < 5; i++) {
            quiz.answer(i == 0 ? (quiz.current().correct + 1) % 4 : quiz.current().correct);
            assertFalse(quiz.passed());
            quiz.next();
        }
        assertEquals(4, quiz.score());
        assertTrue(quiz.passed());
        assertThrows(IllegalStateException.class, quiz::next);
    }

    @Test public void failedRetryCannotEraseProgressAndExamNeedsEveryChapter() {
        Progress progress = new Progress();
        assertTrue(progress.unlocked(0));
        assertFalse(progress.unlocked(1));
        progress.record(0, 3);
        assertFalse(progress.unlocked(1));
        for (int level = 0; level < 10; level++) {
            assertFalse(progress.examUnlocked());
            progress.record(level, 4);
        }
        progress.record(0, 1);
        assertEquals(4, progress.best(0));
        assertEquals(10, progress.completed());
        assertTrue(progress.examUnlocked());
        progress.record(-1, 18);
        progress.record(-1, 9);
        assertEquals(18, progress.examBest());
        assertThrows(IllegalArgumentException.class, () -> progress.record(-1, 21));
    }

    @Test public void finalExamNeedsAtLeastSixteenCorrectAnswers() {
        for (int correctAnswers : new int[] {15, 16}) {
            QuizSession exam = new QuizSession(-1, Catalog.exam(new Random(42)));
            for (int i = 0; i < 20; i++) {
                int correct = exam.current().correct;
                exam.answer(i < correctAnswers ? correct : (correct + 1) % 4);
                exam.next();
            }
            assertEquals(correctAnswers, exam.score());
            assertEquals(correctAnswers == 16, exam.passed());
        }
    }
}
