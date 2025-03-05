package com.fncompany.randomfootballteam.commons.face_detection

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import com.fncompany.randomfootballteam.commons.camerax.GraphicOverlay
import com.google.mlkit.vision.face.Face

class FaceContourGraphic(
    overlay: GraphicOverlay,
    private val face: Face,
    private val imageRect: Rect
) : GraphicOverlay.Graphic(overlay) {

    private val facePositionPaint: Paint
    private val idPaint: Paint
    private val boxPaint: Paint

    init {
        val selectedColor = Color.WHITE

        facePositionPaint = Paint()
        facePositionPaint.color = selectedColor

        idPaint = Paint()
        idPaint.color = selectedColor

        boxPaint = Paint()
        boxPaint.color = selectedColor
        boxPaint.style = Paint.Style.STROKE
        boxPaint.strokeWidth = BOX_STROKE_WIDTH
    }

    override fun draw(canvas: Canvas?) {
        // Adicione um deslocamento para cima no retângulo
        val offsetAboveHead = 50.0f // Ajuste esse valor conforme necessário

        // Ajusta o cálculo do retângulo para considerar a rotação da imagem
        val rect = calculateRect(
            imageRect.height().toFloat(),
            imageRect.width().toFloat(),
            face.boundingBox
        ).apply {
            val scaleFactor = 1.0f // Fator de escala para diminuir o tamanho do retângulo
            val width = right - left
            val height = bottom - top
            left += width * (1 - scaleFactor) / 2
            right -= width * (1 - scaleFactor) / 2
            top = top - height * scaleFactor - offsetAboveHead // Aplica o deslocamento maior
            bottom = bottom - height * scaleFactor - offsetAboveHead
        }

        canvas?.drawRect(rect, idPaint)
    }

    companion object {
        private const val BOX_STROKE_WIDTH = 5.0f
    }
}