@file:OptIn(ExperimentalFoundationApi::class)

package me.tomasan7.opinet.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp

@Composable
fun Tooltipped(
    tooltip: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
)
{
    TooltipArea(
        tooltip = {
            Surface(
                modifier = Modifier.shadow(4.dp),
                color = MaterialTheme.colorScheme.onBackground,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = tooltip,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.padding(10.dp)
                )
            }
        },
        modifier = modifier,
        content = content
    )
}
