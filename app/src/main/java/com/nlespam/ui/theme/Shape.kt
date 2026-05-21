package com.nlespam.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.star
import androidx.graphics.shapes.toPath

// Expressive shapes — larger radii for emphasis, rounded for accessible feel
val NleSpamShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
)

// Wrapper to create a stable shape from graphics-shapes
class PolygonShape(private val polygon: RoundedPolygon) : Shape {
    private val matrix = Matrix()
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val bounds = polygon.calculateBounds()
        val boundsWidth = bounds[2] - bounds[0]
        val boundsHeight = bounds[3] - bounds[1]
        
        matrix.reset()
        // Determine scale to fit the exact Layout container size
        val scaleX = size.width / boundsWidth
        val scaleY = size.height / boundsHeight
        matrix.scale(scaleX, scaleY)
        // Translate to origin
        matrix.translate(-bounds[0], -bounds[1])
        
        val composePath = polygon.toPath().asComposePath()
        composePath.transform(matrix)
        
        return Outline.Generic(composePath)
    }
}

// Official 9-sided "cookie" / scalloped shape using Material 3 Expressive shapes
val CookieShape = PolygonShape(
    RoundedPolygon.star(
        numVerticesPerRadius = 9,
        innerRadius = 0.8f,
        rounding = CornerRounding(radius = 0.2f)
    )
)
