package com.example.gazege.ui.previews.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gazege.ui.theme.GazegeTheme
import com.example.gazege.ui.widgets.Node
import com.example.gazege.ui.widgets.RecyclerTreeView

data class CuentaEHijos(
    val nombre: String,
    val valor: Int,
    val owner: String,
    val hijos: List<CuentaEHijos>
)


data class CuentaNode(
    override val content: CuentaEHijos,
    override val group: String?,
    override val level: Int
) : Node<CuentaEHijos, CuentaNode> {
    override val children: List<CuentaNode>
        get() = content.hijos.map { CuentaNode(it, group, level + 1) }
}

@Preview(showBackground = true, heightDp = 400, widthDp = 200)
@Composable
fun RecyclerViewPreview() {
    val cuentas = listOf(
        CuentaEHijos("Efectivo", 10000, "A", listOf()),
        CuentaEHijos(
            "Banco", 20000, "B", listOf(
                CuentaEHijos(
                    "Banco 1", 5000, "B", listOf(
                        CuentaEHijos("Banco 1.1", 6000, "B", listOf()),
                        CuentaEHijos("Banco 1.2", 7000, "C", listOf())
                    )
                ),
                CuentaEHijos("Banco 2", 6000, "B", listOf())
            )
        )
    )
    GazegeTheme {
        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
        ) {
            RecyclerTreeView(
                cuentas.map {
                    CuentaNode(it, it.owner, 0)
                }
            ) { node, scope ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(node.level.dp * 8))
                    if (node.children.isNotEmpty()) {
                        IconButton(onClick = { scope.toggleExpanded(node) }) {
                            if (scope.isExpanded(node)) {
                                Icon(
                                    painter = rememberVectorPainter(image = Icons.Default.KeyboardArrowDown),
                                    contentDescription = "Expand"
                                )
                            } else {
                                Icon(
                                    painter = rememberVectorPainter(image = Icons.Default.KeyboardArrowRight),
                                    contentDescription = "Collapse"
                                )
                            }
                        }
                    }
                    Text(
                        node.content.nombre
                    )
                }
            }
        }
    }
}