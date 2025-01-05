package me.tomasan7.opinet.isolationlevels

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun TransactionProgressIndicator(inProgress: Boolean)
{
    Canvas(Modifier.size(10.dp)) {
        drawCircle(
            color = if (inProgress) Color.Green else Color.Red
        )
    }
}
