package com.yes.flashcamera.presentation.ui

import android.graphics.Rect
import kotlin.math.sqrt


object Geometry {
    fun vectorBetween(from: Point, to: Point): Vector {
        return Vector(
            to.x - from.x,
            to.y - from.y,
            to.z - from.z
        )
    }

    fun intersects(sphere: Sphere, ray: Ray): Boolean {
        return distanceBetween(sphere.center, ray) < sphere.radius
    }
    fun intersects(rect:Rectangle, ray: Ray): Boolean {
        val plane = Plane(
            Point(0f, 0f, 0f),
            Vector(0f, 0f, 1f)
        )
        val intersectionPoint = Geometry.intersectionPoint(ray, plane)
        return (intersectionPoint.x<rect.center.x+rect.width/2
                &&
                intersectionPoint.x>rect.center.x-rect.width/2
                &&
                intersectionPoint.y<rect.center.y+rect.height/2
                &&
                intersectionPoint.y>rect.center.y-rect.height/2)

    }

    // http://mathworld.wolfram.com/Point-LineDistance3-Dimensional.html
    // Note that this formula treats Ray as if it extended infinitely past
    // either point.
    private fun distanceBetween(point: Point, ray: Ray): Float {
        val p1ToPoint = vectorBetween(ray.point, point)
        val p2ToPoint = vectorBetween(ray.point.translate(ray.vector), point)

        // The length of the cross product gives the area of an imaginary
        // parallelogram having the two vectors as sides. A parallelogram can be
        // thought of as consisting of two triangles, so this is the same as
        // twice the area of the triangle defined by the two vectors.
        // http://en.wikipedia.org/wiki/Cross_product#Geometric_meaning
        val areaOfTriangleTimesTwo = p1ToPoint.crossProduct(p2ToPoint).length()
        val lengthOfBase = ray.vector.length()

        // The area of a triangle is also equal to (base * height) / 2. In
        // other words, the height is equal to (area * 2) / base. The height
        // of this triangle is the distance from the point to the ray.
        val distanceFromPointToRay = areaOfTriangleTimesTwo / lengthOfBase
        return distanceFromPointToRay
    }

    // http://en.wikipedia.org/wiki/Line-plane_intersection
    // This also treats rays as if they were infinite. It will return a
    // point full of NaNs if there is no intersection point.
    fun intersectionPoint(ray: Ray, plane: Plane): Point {
        val rayToPlaneVector = vectorBetween(ray.point, plane.point)

        val scaleFactor = (rayToPlaneVector.dotProduct(plane.normal)
                / ray.vector.dotProduct(plane.normal))

        val intersectionPoint = ray.point.translate(ray.vector.scale(scaleFactor))
        return intersectionPoint
    }

    class Point(val x: Float, val y: Float, val z: Float) {
        fun translateY(distance: Float): Point {
            return Point(x, y + distance, z)
        }

        fun translate(vector: Vector): Point {
            return Point(
                x + vector.x,
                y + vector.y,
                z + vector.z
            )
        }
    }

    class Vector(val x: Float, val y: Float, val z: Float) {
        fun length(): Float {
            return sqrt(
                x * x + y * y + z * z
            )
        }

        // http://en.wikipedia.org/wiki/Cross_product
        fun crossProduct(other: Vector): Vector {
            return Vector(
                (y * other.z) - (z * other.y),
                (z * other.x) - (x * other.z),
                (x * other.y) - (y * other.x)
            )
        }

        // http://en.wikipedia.org/wiki/Dot_product
        fun dotProduct(other: Vector): Float {
            return x * other.x + y * other.y + z * other.z
        }

        fun scale(f: Float): Vector {
            return Vector(
                x * f,
                y * f,
                z * f
            )
        }
    }

    class Ray(val point: Point, val vector: Vector)

    // TODO: Re-use shared stuff in classes as an exercise
    class Circle(val center: Point, val radius: Float) {
        fun scale(scale: Float): Circle {
            return Circle(center, radius * scale)
        }
    }
    interface Figure
    class Cylinder(val center: Point, val radius: Float, val height: Float)

    class Sphere(val center: Point, val radius: Float)

    class Plane(val point: Point, val normal: Vector)
    class Rectangle(val center: Point, val width: Float,val height: Float):Figure
}