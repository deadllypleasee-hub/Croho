package ru.chronos.history;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

/** Small vector illustrations, drawn locally at any screen density. */
final class GameArtView extends View {
    static final int HOURGLASS = -1, TROPHY = -2, MEDAL = -3;
    private static final int INK = 0xFF3A315C, GOLD = 0xFFFFCD55;
    private static final int PURPLE = 0xFF8463EC, TEAL = 0xFF35BCA7, CORAL = 0xFFFF987C;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final int type;

    GameArtView(Context context, int type) {
        super(context);
        this.type = type;
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float size = Math.min(getWidth(), getHeight());
        if (size <= 0) return;
        canvas.save();
        canvas.translate((getWidth() - size) / 2, (getHeight() - size) / 2);
        canvas.scale(size / 120f, size / 120f);
        if (type == HOURGLASS) hourglass(canvas);
        else if (type == TROPHY) trophy(canvas);
        else if (type == MEDAL) medal(canvas);
        else {
            circle(canvas, 61, 61, 43, 0x55FFFFFF);
            switch (type) {
                case 0: pyramids(canvas); break;
                case 1: temple(canvas); break;
                case 2: arch(canvas); break;
                case 3: castle(canvas); break;
                case 4: shield(canvas); break;
                case 5: ship(canvas); break;
                case 6: telescope(canvas); break;
                case 7: factory(canvas); break;
                case 8: globe(canvas); break;
                default: rocket(canvas); break;
            }
        }
        canvas.restore();
    }

    private void fill(int color) { paint.setStyle(Paint.Style.FILL); paint.setColor(color); paint.setShader(null); }
    private void circle(Canvas c, float x, float y, float r, int color) {
        fill(color); c.drawCircle(x, y, r, paint);
    }
    private void rect(Canvas c, float l, float t, float r, float b, float radius, int color) {
        fill(color); c.drawRoundRect(l, t, r, b, radius, radius, paint);
    }
    private void line(Canvas c, float x, float y, float x2, float y2, float width, int color) {
        paint.setStyle(Paint.Style.STROKE); paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(color); paint.setStrokeWidth(width); c.drawLine(x, y, x2, y2, paint);
    }
    private void poly(Canvas c, int color, float... points) {
        Path path = new Path(); path.moveTo(points[0], points[1]);
        for (int i = 2; i < points.length; i += 2) path.lineTo(points[i], points[i + 1]);
        path.close(); fill(color); c.drawPath(path, paint);
    }
    private void star(Canvas c, float x, float y, float radius, int color) {
        float[] points = new float[20];
        for (int i = 0; i < 10; i++) {
            double angle = -Math.PI / 2 + i * Math.PI / 5;
            float r = i % 2 == 0 ? radius : radius * 0.46f;
            points[i * 2] = x + (float) Math.cos(angle) * r;
            points[i * 2 + 1] = y + (float) Math.sin(angle) * r;
        }
        poly(c, color, points);
    }
    private void spark(Canvas c, float x, float y, int color) {
        poly(c, color, x, y - 7, x + 2, y - 2, x + 7, y, x + 2, y + 2,
                x, y + 7, x - 2, y + 2, x - 7, y, x - 2, y - 2);
    }
    private void hourglass(Canvas c) {
        circle(c, 60, 60, 48, 0x227B5DE6);
        spark(c, 18, 35, GOLD); spark(c, 100, 76, 0xFFBFF090);
        circle(c, 97, 26, 4, CORAL); circle(c, 24, 95, 3, 0xFF8FDCD7);
        c.save(); c.rotate(12, 60, 60);
        poly(c, INK, 35, 23, 85, 23, 85, 37, 67, 59, 85, 82, 85, 99,
                35, 99, 35, 82, 53, 59, 35, 37);
        poly(c, 0xFFE3F9F5, 41, 30, 79, 30, 79, 36, 60, 57, 41, 36);
        poly(c, 0xFFE3F9F5, 41, 92, 41, 82, 60, 63, 79, 82, 79, 92);
        poly(c, GOLD, 44, 38, 76, 38, 60, 54);
        poly(c, GOLD, 43, 90, 60, 73, 77, 90);
        line(c, 60, 58, 60, 71, 3, GOLD);
        rect(c, 30, 19, 90, 29, 5, 0xFFBDA7FF);
        rect(c, 30, 94, 90, 104, 5, 0xFFBDA7FF);
        c.restore();
    }
    private void trophy(Canvas c) {
        circle(c, 60, 58, 44, 0xFFFFEDBE);
        spark(c, 17, 31, CORAL); spark(c, 101, 42, PURPLE);
        circle(c, 104, 80, 3, TEAL); circle(c, 22, 87, 4, PURPLE);
        rect(c, 19, 29, 43, 62, 12, 0xFFE6A72C);
        rect(c, 77, 29, 101, 62, 12, 0xFFE6A72C);
        rect(c, 25, 35, 37, 55, 6, 0xFFFFEDBE);
        rect(c, 83, 35, 95, 55, 6, 0xFFFFEDBE);
        rect(c, 35, 23, 85, 74, 20, GOLD);
        rect(c, 35, 21, 85, 35, 5, 0xFFFFD96E);
        star(c, 60, 48, 15, 0xFFFFF9E7);
        rect(c, 55, 70, 65, 91, 3, 0xFFE6A72C);
        rect(c, 40, 89, 80, 99, 5, PURPLE);
        rect(c, 35, 97, 85, 104, 3, INK);
    }
    private void medal(Canvas c) {
        poly(c, PURPLE, 37, 51, 57, 57, 48, 108, 33, 94, 20, 99);
        poly(c, 0xFF9A7EF3, 63, 57, 83, 51, 100, 99, 87, 94, 72, 108);
        circle(c, 60, 45, 36, 0xFFE8AD37);
        circle(c, 60, 42, 31, GOLD);
        circle(c, 60, 42, 24, 0xFFFFDE83);
        star(c, 60, 42, 19, 0xFFFFFAE7);
    }
    private void pyramids(Canvas c) {
        circle(c, 88, 29, 13, 0xFFFFF3C6);
        poly(c, 0xFFE3A844, 10, 94, 48, 31, 85, 94);
        poly(c, 0xFFFFE3A0, 10, 94, 48, 31, 48, 94);
        poly(c, 0xFFE3A844, 60, 94, 85, 50, 111, 94);
        poly(c, 0xFFFFEBBD, 60, 94, 85, 50, 85, 94);
        rect(c, 10, 94, 111, 99, 2, 0xFFBF8B45);
    }
    private void temple(Canvas c) {
        poly(c, INK, 12, 44, 60, 18, 108, 44);
        poly(c, Color.WHITE, 23, 40, 60, 22, 97, 40);
        for (int x = 25; x <= 85; x += 20) {
            rect(c, x, 47, x + 11, 87, 3, Color.WHITE);
            rect(c, x - 2, 46, x + 13, 51, 2, 0xFF8AA8BF);
        }
        rect(c, 16, 88, 104, 96, 3, INK);
        rect(c, 11, 97, 109, 103, 3, Color.WHITE);
    }
    private void arch(Canvas c) {
        rect(c, 20, 29, 100, 97, 6, CORAL);
        rect(c, 41, 48, 79, 107, 19, 0xFFB26F65);
        rect(c, 47, 54, 73, 107, 13, 0xFFFFE7DD);
        rect(c, 15, 24, 105, 34, 3, INK);
        for (int y = 46; y < 88; y += 14) {
            line(c, 23, y, 35, y, 3, 0xFFFFD2C3);
            line(c, 85, y, 97, y, 3, 0xFFFFD2C3);
        }
        rect(c, 14, 95, 106, 102, 3, INK);
    }
    private void castle(Canvas c) {
        rect(c, 36, 47, 85, 98, 3, 0xFFAE96F3);
        rect(c, 18, 34, 40, 98, 3, PURPLE);
        rect(c, 81, 34, 103, 98, 3, PURPLE);
        for (int x : new int[] {18, 32, 81, 95}) rect(c, x, 25, x + 8, 42, 1, PURPLE);
        rect(c, 51, 74, 70, 101, 9, INK);
        rect(c, 26, 53, 33, 65, 3, 0xFFFFE9A8);
        rect(c, 89, 53, 96, 65, 3, 0xFFFFE9A8);
        line(c, 61, 47, 61, 17, 3, INK);
        poly(c, CORAL, 63, 17, 82, 25, 63, 31);
        rect(c, 13, 97, 108, 103, 3, INK);
    }
    private void shield(Canvas c) {
        poly(c, INK, 60, 15, 99, 31, 93, 74, 79, 94, 60, 107, 41, 94, 27, 74, 21, 31);
        poly(c, TEAL, 60, 23, 91, 37, 85, 72, 73, 88, 60, 98, 47, 88, 35, 72, 29, 37);
        poly(c, 0xFF79D9BA, 60, 23, 60, 98, 47, 88, 35, 72, 29, 37);
        star(c, 60, 57, 22, 0xFFFFE5A4);
    }
    private void ship(Canvas c) {
        poly(c, INK, 15, 80, 106, 80, 92, 99, 32, 99);
        line(c, 59, 23, 59, 80, 4, INK);
        poly(c, Color.WHITE, 52, 28, 52, 73, 20, 73);
        poly(c, CORAL, 65, 29, 65, 73, 100, 73);
        poly(c, GOLD, 59, 16, 79, 23, 59, 29);
        line(c, 10, 104, 111, 104, 5, 0xFF66BFD5);
        line(c, 22, 113, 95, 113, 3, 0xFF8DD0E0);
    }
    private void telescope(Canvas c) {
        line(c, 62, 66, 39, 103, 6, INK);
        line(c, 62, 66, 88, 103, 6, INK);
        line(c, 62, 65, 62, 106, 5, INK);
        c.save(); c.rotate(-28, 60, 48);
        rect(c, 21, 38, 91, 62, 6, PURPLE);
        rect(c, 18, 37, 29, 63, 3, INK);
        rect(c, 86, 32, 102, 68, 4, CORAL);
        rect(c, 95, 35, 106, 65, 4, 0xFFDDF7F8);
        c.restore();
        spark(c, 26, 23, GOLD);
    }
    private void factory(Canvas c) {
        rect(c, 82, 28, 96, 93, 3, INK);
        circle(c, 89, 17, 6, 0xFFBBAAEA); circle(c, 101, 10, 5, 0xFFD5C9F9);
        poly(c, PURPLE, 17, 94, 17, 60, 42, 43, 42, 59, 67, 43, 67, 60, 99, 60, 99, 94);
        for (int x = 27; x < 91; x += 20) rect(c, x, 73, x + 10, 84, 2, GOLD);
        rect(c, 12, 94, 106, 101, 3, INK);
    }
    private void globe(Canvas c) {
        line(c, 60, 89, 60, 103, 6, INK);
        rect(c, 36, 101, 84, 107, 3, INK);
        circle(c, 60, 53, 35, 0xFF65BEE4);
        poly(c, 0xFF91E0BC, 36, 30, 49, 23, 61, 28, 55, 40, 67, 47, 57, 57, 44, 50, 45, 40, 34, 40);
        poly(c, 0xFF91E0BC, 64, 60, 80, 53, 91, 60, 81, 77, 68, 84, 62, 74);
        paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(4); paint.setColor(INK);
        c.drawArc(17, 10, 103, 96, -60, 250, false, paint);
        spark(c, 99, 22, GOLD);
    }
    private void rocket(Canvas c) {
        c.save(); c.rotate(26, 60, 59);
        poly(c, GOLD, 48, 88, 60, 116, 72, 88);
        poly(c, CORAL, 50, 85, 60, 105, 70, 85);
        poly(c, PURPLE, 44, 64, 27, 90, 48, 85);
        poly(c, PURPLE, 76, 64, 93, 90, 72, 85);
        rect(c, 44, 35, 76, 89, 14, 0xFFFFFAF5);
        poly(c, CORAL, 44, 40, 60, 14, 76, 40);
        circle(c, 60, 53, 12, INK); circle(c, 60, 53, 8, 0xFF8DDBEF);
        rect(c, 48, 83, 72, 91, 3, INK);
        c.restore();
        spark(c, 98, 25, GOLD); circle(c, 23, 45, 3, PURPLE);
    }
}
