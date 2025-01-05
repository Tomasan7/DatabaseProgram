package me.tomasan7.opinet.isolationlevels

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions

class DirtyWritesTab(
    private val accountName: String
) : Tab
{
    override val options: TabOptions
        @Composable
        get() {
            return remember {
                TabOptions(
                    index = 1u,
                    title = "Dirty Writes"
                )
            }
        }

    @Composable
    override fun Content()
    {
        Text("Dirty Writes")
    }
}
