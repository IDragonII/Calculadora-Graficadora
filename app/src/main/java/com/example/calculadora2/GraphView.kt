package com.example.calculadora2

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import net.objecthunter.exp4j.ExpressionBuilder
import kotlin.math.*

class GraphView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint().apply {
        color = Color.BLUE
        strokeWidth = 5f
    }

    private var function: String = "x^2" // Función por defecto

    // Parámetros de zoom y desplazamiento
    private var scaleFactor = 1f
    private var offsetX = 0f
    private var offsetY = 0f

    private val scaleGestureDetector = ScaleGestureDetector(context, ScaleListener())

    fun setFunction(func: String) {
        function = func
        invalidate() // Redibuja el gráfico cuando se cambia la función
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawAxes(canvas)
        drawGraph(canvas)
    }

    // Método para dibujar los ejes y las marcas con números
    private fun drawAxes(canvas: Canvas) {
        val width = width.toFloat()
        val height = height.toFloat()

        val scaleX = 0.05f * scaleFactor // Escala en el eje X
        val scaleY = 50f * scaleFactor   // Escala en el eje Y

        paint.color = Color.GRAY
        canvas.drawLine(0f, height / 2 + offsetY, width, height / 2 + offsetY, paint) // Eje X
        canvas.drawLine(width / 2 + offsetX, 0f, width / 2 + offsetX, height, paint) // Eje Y

        paint.textSize = 30f
        paint.strokeWidth = 2f

        // Determinar la distancia entre marcas en función del nivel de zoom
        val interval = 100 * scaleFactor

        // Dibujar marcas en el eje X
        for (i in -10..10) {
            val xPos = width / 2 + i * interval + offsetX
            if (xPos in 0f..width) {
                canvas.drawLine(xPos, height / 2 - 10 + offsetY, xPos, height / 2 + 10 + offsetY, paint) // Línea de la marca
                canvas.drawText(i.toString(), xPos - 15, height / 2 + 40 + offsetY, paint) // Texto de la marca
            }
        }

        // Dibujar marcas en el eje Y
        for (i in -10..10) {
            val yPos = height / 2 - i * interval + offsetY
            if (yPos in 0f..height) {
                canvas.drawLine(width / 2 - 10 + offsetX, yPos, width / 2 + 10 + offsetX, yPos, paint) // Línea de la marca
                if (i != 0) { // No dibujar el "0" en el eje Y para evitar solaparlo con el eje X
                    canvas.drawText(i.toString(), width / 2 + 20 + offsetX, yPos + 10, paint) // Texto de la marca
                }
            }
        }
    }

    private fun drawGraph(canvas: Canvas) {
        val width = width.toFloat()
        val height = height.toFloat()

        val scaleX = 0.05f * scaleFactor // Escala en el eje X
        val scaleY = 50f * scaleFactor   // Escala en el eje Y

        for (xPixel in 0 until width.toInt()) {
            val x = (xPixel * scaleX - (width / 2) * scaleX) + offsetX // Ajusta el valor de x para centrarlo
            val y = evaluateFunction(x.toFloat()) // Evaluamos la función con el valor de x

            // Dibujar el punto solo si y está dentro de la vista
            if (y.isFinite()) {
                canvas.drawPoint(xPixel.toFloat(), height / 2 - y * scaleY + offsetY, paint)
            }
        }
    }

    private fun evaluateFunction(x: Float): Float {
        return try {
            val expr = ExpressionBuilder(function)
                .variable("x")
                .build()
            expr.setVariable("x", x.toDouble())
            expr.evaluate().toFloat()
        } catch (e: Exception) {
            0f // Retorna 0 en caso de error
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleGestureDetector.onTouchEvent(event)
        return true
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleFactor *= detector.scaleFactor
            scaleFactor = maxOf(0.1f, minOf(scaleFactor, 5.0f)) // Limitar el zoom
            invalidate() // Redibuja el gráfico
            return true
        }
    }
}
