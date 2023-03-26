package com.example.gazege.ui.previews.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.gazege.ui.theme.GazegeTheme
import com.example.gazege.ui.widgets.Node
import com.example.gazege.ui.widgets.RecyclerTreeView

data class CuentaEHijos(
    val nombre: String,
    val valor: Int,
    val hijos: List<CuentaEHijos>
)


data class CuentaNode(
    override val content: CuentaEHijos,
    override val level: Int
) : Node<CuentaEHijos, CuentaNode> {
    override val children: List<CuentaNode>
        get() = content.hijos.map { CuentaNode(it, level + 1) }
}

@Preview(showBackground = true, heightDp = 400, widthDp = 200)
@Composable
fun RecyclerViewPreview() {
    val cuentas = listOf(
        CuentaEHijos("Efectivo", 10000, listOf()),
        CuentaEHijos(
            "Banco", 20000, listOf(
                CuentaEHijos(
                    "Banco 1", 5000, listOf(
                        CuentaEHijos("Banco 1.1", 6000, listOf()),
                        CuentaEHijos("Banco 1.2", 7000, listOf())
                    )
                ),
                CuentaEHijos("Banco 2", 6000, listOf())
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
                    CuentaNode(it, 0)
                }
            ) { node, scope ->
                Text(
                    node.content.nombre,
                    Modifier.clickable {
                        scope.toggleExpanded(node)
                    }
                )
            }
        }
    }
}