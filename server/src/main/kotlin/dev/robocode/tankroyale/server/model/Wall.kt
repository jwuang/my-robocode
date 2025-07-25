package dev.robocode.tankroyale.server.model

import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt


/**
 * Defines a wall instance.
 * @param id Unique id of this wall.
 * @param x x coordinate of the center of this wall.
 * @param y y coordinate of the center of this wall.
 * @param width Width of this wall.
 * @param height Height of this wall.
 * @param rotation Rotation of this wall, default to 0.
 * @param color Color of this wall, default to black.
 */
data class Wall(
    val id: Int,
    var x: Double,
    var y: Double,
    val width: Double,
    val height: Double,
    var rotation: Double = 0.0,
    val color: Color? = Color.from("#000000")
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Wall) return false
        return id == other.id // 根据 id 判断相等性
    }
    override fun hashCode(): Int = id
    // 矩形外接圆半径（对角线的一半）
    val boundsRadius: Double = sqrt((width / 2).pow(2) + (height / 2).pow(2))

    // 判断线段是否与旋转后的矩形相交
    fun intersects(line: Line): Boolean {
        val p1 = translateAndRotatePoint(line.start)
        val p2 = translateAndRotatePoint(line.end)

        val halfWidth = width / 2
        val halfHeight = height / 2
        val rectLeft = -halfWidth
        val rectRight = halfWidth
        val rectTop = -halfHeight
        val rectBottom = halfHeight

        // 检查端点是否在矩形内
        if (isPointInsideAxisAlignedRect(p1, rectLeft, rectRight, rectTop, rectBottom) ||
            isPointInsideAxisAlignedRect(p2, rectLeft, rectRight, rectTop, rectBottom)) {
            return true
        }

        // 检查线段是否与四边相交
        val edges = listOf(
            Line(rectLeft, rectTop, rectLeft, rectBottom),   // 左边
            Line(rectRight, rectTop, rectRight, rectBottom), // 右边
            Line(rectLeft, rectTop, rectRight, rectTop),     // 上边
            Line(rectLeft, rectBottom, rectRight, rectBottom) // 下边
        )

        val localLine = Line(p1, p2)
        return edges.any { edge ->
            isLineIntersectingLine(localLine, edge)
        }
    }

    // 判断圆是否与旋转后的矩形相交
    fun intersectsCircle(center: Point, radius: Double): Boolean {
        val localCenter = translateAndRotatePoint(center)

        val halfWidth = width / 2
        val halfHeight = height / 2

        val closestX = localCenter.x.coerceIn(-halfWidth, halfWidth)
        val closestY = localCenter.y.coerceIn(-halfHeight, halfHeight)

        val dx = localCenter.x - closestX
        val dy = localCenter.y - closestY
        return dx * dx + dy * dy <= radius * radius
    }

    // 获取墙体的中心坐标
    fun position() = Point(x, y)

    private fun translateAndRotatePoint(point: Point): Point {
        val translatedX = point.x - x
        val translatedY = point.y - y

        val rad = Math.toRadians(-rotation)
        val cos = cos(rad)
        val sin = sin(rad)

        return Point(
            translatedX * cos - translatedY * sin,
            translatedX * sin + translatedY * cos
        )
    }

    private fun isPointInsideAxisAlignedRect(
        point: Point,
        left: Double,
        right: Double,
        top: Double,
        bottom: Double
    ): Boolean {
        return point.x in left..right && point.y in top..bottom
    }
}