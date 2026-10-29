package ru.chronos.history;

/** Best scores survive retries; the next chapter opens after four correct answers. */
public final class Progress {
    private final int[] best = new int[Catalog.LEVEL_COUNT];
    private int examBest = -1;

    public Progress() { java.util.Arrays.fill(best, -1); }
    public int best(int level) { return best[level]; }
    public int examBest() { return examBest; }
    public void record(int level, int score) {
        int maximum = level == -1 ? Catalog.EXAM_SIZE : Catalog.QUESTIONS_PER_LEVEL;
        if (level < -1 || level >= best.length || score < 0 || score > maximum) {
            throw new IllegalArgumentException("Invalid score");
        }
        if (level == -1) examBest = Math.max(examBest, score);
        else best[level] = Math.max(best[level], score);
    }
    public boolean unlocked(int level) { return level == 0 || best[level - 1] >= 4; }
    public int completed() {
        int count = 0;
        for (int score : best) if (score >= 4) count++;
        return count;
    }
    public boolean examUnlocked() { return completed() == Catalog.LEVEL_COUNT; }
}
