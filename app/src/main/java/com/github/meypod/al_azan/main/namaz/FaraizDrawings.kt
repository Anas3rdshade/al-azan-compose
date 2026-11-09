package com.github.meypod.al_azan.main.namaz

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Original line drawings for the Faraiz pages, drawn on a 100×100 grid. Women's poses face right. */
internal enum class Pic { None, Jug, Hands, Mouth, Nose, Face, Arm, HeadTop, Ears, Foot, Khilal, Body, DontMiss, Strike, Strike2, Takbeer, Qiyam, Ruku, Qawma, Sajda, Jalsa, Qada, Salam }

/** [hl] colours the part being washed or the pose itself: gold for farz, [ink] otherwise. */
internal fun DrawScope.drawPic(pic: Pic, ink: Color, hl: Color, gold: Color, water: Color, text: TextMeasurer) {
    val u = size.minDimension / 100f
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun sz(w: Float, h: Float) = Size(w * u, h * u)
    val thin = Stroke(2.2f * u, cap = StrokeCap.Round)
    val bold = Stroke(3.6f * u, cap = StrokeCap.Round)
    val dash = PathEffect.dashPathEffect(floatArrayOf(4f * u, 3f * u))
    fun line(a: Offset, b: Offset, c: Color = ink, w: Float = 2.2f, dashed: Boolean = false) =
        drawLine(c, a, b, w * u, StrokeCap.Round, if (dashed) dash else null)
    fun path(c: Color, w: Float, vararg pts: Pair<Float, Float>, close: Boolean = false, fill: Color? = null) {
        val path = Path().apply {
            moveTo(pts[0].first * u, pts[0].second * u)
            pts.drop(1).forEach { lineTo(it.first * u, it.second * u) }
            if (close) close()
        }
        fill?.let { drawPath(path, it) }
        drawPath(path, c, style = Stroke(w * u, cap = StrokeCap.Round))
    }
    fun drop(x: Float, y: Float) = path(water, 1.8f, x to y - 5, x + 3 to y, x to y + 3, x - 3 to y, close = true, fill = water.copy(alpha = 0.35f))
    fun label(s: String, x: Float, y: Float, c: Color) =
        drawText(text, s, p(x, y), TextStyle(color = c, fontSize = (11f * u / density).sp, fontWeight = FontWeight.Bold))

    fun headFront(face: Color, mouth: Color, nose: Color, ears: Color) {
        drawArc(ink, 180f, 180f, false, p(28f, 13f), sz(44f, 32f), style = thin) // hair line
        drawOval(face.copy(alpha = if (face == ink) 0f else 0.14f), p(30f, 26f), sz(40f, 54f))
        drawOval(ink, p(30f, 26f), sz(40f, 54f), style = thin)
        drawOval(ears, p(23f, 46f), sz(7f, 14f), style = if (ears == ink) thin else bold)
        drawOval(ears, p(70f, 46f), sz(7f, 14f), style = if (ears == ink) thin else bold)
        drawCircle(ink, 1.8f * u, p(42f, 47f)); drawCircle(ink, 1.8f * u, p(58f, 47f))
        path(nose, if (nose == ink) 2.2f else 3.6f, 50f to 49f, 46.5f to 60f, 51f to 61.5f)
        drawArc(mouth, 20f, 140f, false, p(43f, 63f), sz(14f, 8f), style = if (mouth == ink) thin else bold)
    }

    fun arm(limitX: Float, washFrom: Float) {
        drawRect(hl.copy(alpha = 0.16f), p(washFrom, 30f), sz(98f - washFrom, 34f))
        path(ink, 2.2f, 3f to 40f, 38f to 42f, 74f to 44f)
        path(ink, 2.2f, 3f to 60f, 34f to 60f, 39f to 63f, 44f to 59f, 74f to 55f) // elbow bump at x≈39
        path(ink, 2.2f, 74f to 44f, 80f to 36f, 87f to 38f) // thumb
        listOf(44f, 48.5f, 53f).forEach { y -> line(p(80f, y), p(96f, y + 1f)) }
        line(p(74f, 55f), p(84f, 56f)); line(p(84f, 56f), p(94f, 55f))
        line(p(limitX, 28f), p(limitX, 68f), hl, 2.6f, dashed = true)
    }

    fun body(c: Color, fill: Boolean) {
        drawCircle(c, 8f * u, p(50f, 15f), style = thin)
        path(
            c, 2.4f,
            45f to 23f, 31f to 29f, 24f to 58f, 29f to 59f, 35f to 37f, 36f to 62f, 38f to 93f, 47f to 93f, 50f to 65f,
            53f to 93f, 62f to 93f, 64f to 62f, 65f to 37f, 71f to 59f, 76f to 58f, 69f to 29f, 55f to 23f,
            close = true, fill = if (fill) c.copy(alpha = 0.12f) else null,
        )
    }

    fun palms(n: String?) {
        drawRect(water.copy(alpha = 0.25f), p(10f, 82f), sz(80f, 8f))
        line(p(8f, 82f), p(92f, 82f))
        listOf(18f, 34f, 50f, 66f, 82f).forEach { x -> line(p(x, 84f), p(x - 5f, 90f), water) }
        listOf(26f to 4f, 74f to -4f).forEach { (cx, tilt) ->
            drawRoundRect(ink, p(cx - 11f, 34f), sz(22f, 26f), androidx.compose.ui.geometry.CornerRadius(6f * u), style = thin)
            listOf(-7f, -2.5f, 2.5f, 7f).forEach { dx -> line(p(cx + dx, 34f), p(cx + dx + tilt * 0.3f, 18f)) }
            line(p(cx, 64f), p(cx, 76f), ink, 2f, dashed = true)
            path(ink, 2f, cx - 3f to 72f, cx to 77f, cx + 3f to 72f)
        }
        if (n != null) label(n, 46f, 2f, hl)
    }

    class Pose(
        val head: Offset, val face: Float, val shoulder: Offset, val elbow: Offset, val hand: Offset,
        val hip: Offset, val knee: Offset, val ankle: Offset, val toe: Offset,
    )
    fun o(x: Float, y: Float) = Offset(x, y)
    fun pose(q: Pose) {
        line(p(4f, 93.5f), p(96f, 93.5f), ink.copy(alpha = 0.35f), 1.6f)
        fun seg(a: Offset, b: Offset, w: Float) = line(p(a.x, a.y), p(b.x, b.y), hl, w)
        seg(q.hip, q.knee, 7f); seg(q.knee, q.ankle, 5.5f); seg(q.ankle, q.toe, 4f)
        seg(q.shoulder, q.hip, 8f)
        seg(q.shoulder, q.elbow, 4.4f); seg(q.elbow, q.hand, 4f)
        val r = 6.5f
        drawCircle(hl, r * u, p(q.head.x, q.head.y), style = thin)
        // scarf: covers the head except the face side
        drawArc(hl, q.face + 55f, 250f, false, p(q.head.x - r - 2.6f, q.head.y - r - 2.6f), sz(2 * (r + 2.6f), 2 * (r + 2.6f)), style = bold)
        seg(q.head + o(0f, r), q.shoulder, 4f)
    }
    val stand = Pose(o(50f, 12f), 0f, o(50f, 27f), o(55f, 43f), o(58f, 36f), o(50f, 56f), o(51f, 74f), o(50f, 90f), o(59f, 91f))
    val sit = Pose(o(43f, 45f), 0f, o(42f, 58f), o(49f, 73f), o(61f, 82f), o(40f, 86f), o(64f, 87f), o(42f, 91f), o(28f, 91f))

    when (pic) {
        Pic.None -> Unit
        Pic.Jug -> {
            drawOval(ink, p(26f, 42f), sz(40f, 42f), style = thin)
            path(ink, 2.2f, 38f to 43f, 38f to 30f, 54f to 30f, 54f to 43f)
            line(p(33f, 30f), p(59f, 30f))
            path(ink, 2.2f, 63f to 58f, 80f to 46f, 86f to 42f)
            drawArc(ink, 90f, 180f, false, p(16f, 46f), sz(16f, 24f), style = thin)
            drop(88f, 54f); drop(84f, 66f)
        }
        Pic.Hands -> arm(73f, 72f)
        Pic.Mouth -> { headFront(ink, hl, ink, ink); drop(64f, 72f) }
        Pic.Nose -> { headFront(ink, ink, hl, ink); drop(58f, 58f) }
        Pic.Face -> {
            headFront(hl, ink, ink, ink)
            drawOval(hl, p(21f, 23f), sz(58f, 63f), style = Stroke(2.6f * u, pathEffect = dash))
        }
        Pic.Ears -> { headFront(ink, ink, ink, hl); drop(18f, 44f); drop(82f, 44f) }
        Pic.Arm -> arm(33f, 33f)
        Pic.HeadTop -> {
            // seen from above, front (nose) at the bottom; one quarter is the farz minimum
            drawArc(hl.copy(alpha = 0.3f), 0f, 90f, true, p(16f, 16f), sz(68f, 68f))
            drawArc(hl, 0f, 90f, true, p(16f, 16f), sz(68f, 68f), style = bold)
            drawCircle(ink, 34f * u, p(50f, 50f), style = thin)
            line(p(16f, 50f), p(84f, 50f), ink, 1.6f, dashed = true)
            line(p(50f, 16f), p(50f, 84f), ink, 1.6f, dashed = true)
            drawArc(ink, 90f, 180f, false, p(10f, 42f), sz(10f, 16f), style = thin)
            drawArc(ink, -90f, 180f, false, p(80f, 42f), sz(10f, 16f), style = thin)
            path(ink, 2.2f, 45f to 83f, 50f to 92f, 55f to 83f)
            label("¼", 60f, 54f, hl)
        }
        Pic.Foot -> {
            drawRect(hl.copy(alpha = 0.16f), p(32f, 54f), sz(64f, 32f))
            line(p(40f, 6f), p(40f, 60f)); line(p(56f, 6f), p(56f, 60f))
            path(ink, 2.2f, 40f to 60f, 37f to 76f, 43f to 83f, 92f to 83f, 93f to 76f, 76f to 69f, 56f to 60f)
            drawCircle(ink, 3f * u, p(51f, 63f), style = thin)
            line(p(28f, 55f), p(68f, 55f), hl, 2.6f, dashed = true)
        }
        Pic.Khilal -> {
            drawRoundRect(ink, p(34f, 52f), sz(32f, 36f), androidx.compose.ui.geometry.CornerRadius(8f * u), style = thin)
            listOf(38f to 30f, 46f to 42f, 54f to 58f, 62f to 70f).forEach { (x, tx) -> line(p(x, 53f), p(tx, 16f), ink, 5f) }
            line(p(66f, 70f), p(84f, 58f), ink, 5f)
            drop(36f, 36f); drop(50f, 30f); drop(64f, 36f)
        }
        Pic.Body -> {
            body(hl, fill = true)
            label("1", 47f, 0f, hl); label("2", 20f, 18f, hl); label("3", 73f, 18f, hl) // her right shoulder is on the viewer's left
            drop(30f, 12f); drop(70f, 12f)
        }
        Pic.DontMiss -> {
            body(ink, fill = false)
            listOf(50f to 7f, 41f to 15f, 51f to 46f, 26f to 57f).forEachIndexed { i, (x, y) ->
                drawCircle(gold, 3.2f * u, p(x, y))
                label("${i + 1}", if (x < 50f) x - 14f else x + 4f, y - 7f, gold)
            }
            drawCircle(gold, 3.2f * u, p(59f, 15f)); drawCircle(gold, 2.4f * u, p(53f, 18.5f))
        }
        Pic.Strike -> palms("1")
        Pic.Strike2 -> palms("2")
        Pic.Takbeer -> pose(Pose(stand.head, 0f, stand.shoulder, o(57f, 37f), o(58f, 24f), stand.hip, stand.knee, stand.ankle, stand.toe))
        Pic.Qiyam -> pose(stand)
        Pic.Ruku -> pose(Pose(o(77f, 33f), 40f, o(66f, 40f), o(60f, 53f), o(53f, 65f), o(44f, 55f), o(47f, 73f), o(44f, 90f), o(53f, 91f)))
        Pic.Qawma -> pose(Pose(stand.head, 0f, stand.shoulder, o(51f, 43f), o(52f, 57f), stand.hip, stand.knee, stand.ankle, stand.toe))
        Pic.Sajda -> pose(Pose(o(71f, 85f), 90f, o(57f, 79f), o(62f, 90f), o(80f, 91f), o(31f, 76f), o(45f, 91f), o(24f, 90f), o(17f, 91f)))
        Pic.Jalsa, Pic.Qada -> pose(sit)
        Pic.Salam -> {
            pose(sit)
            drawArc(hl, 200f, 140f, false, p(33f, 26f), sz(22f, 12f), style = thin)
            path(hl, 2.2f, 52f to 26f, 55f to 30f, 51f to 32f)
        }
    }
}
