package me.tomasan7.opinet.isolationlevels

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.singleWindowApplication
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import me.tomasan7.opinet.config.FileConfigProvider
import me.tomasan7.opinet.config.IsolationLevel
import me.tomasan7.opinet.ui.component.FilledDropDownSelector
import me.tomasan7.opinet.ui.theme.AppTheme
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction

class IsolationLevels
{
    val config = FileConfigProvider("opinet.conf").getConfig()
    val accountName = "Bob"
    val database = config.database.let { dbConf ->
        Database.connect(
            url = dbConf.url,
            user = dbConf.user,
            password = dbConf.password.value,
        )
    }
    private val dirtyReadsTab = DirtyReadsTab(accountName)
    private val dirtyWritesTab = DirtyWritesTab(accountName)

    private var _isolationLevel = mutableStateOf(config.isolationLevel)
    var isolationLevel: IsolationLevel
        get() = _isolationLevel.value
        set(value) {
            TransactionManager.manager.defaultIsolationLevel = value.id
            _isolationLevel.value = value
        }

    init
    {
        isolationLevel = config.isolationLevel
        TransactionManager.defaultDatabase = database
        transaction {
            SchemaUtils.drop(AccountTable)
            SchemaUtils.create(AccountTable)
            AccountTable.insert {
                it[name] = accountName
                it[balance] = 100f
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    fun start() = singleWindowApplication(
        title = "IsolationLevels"
    ) {
        AppTheme {
            TabNavigator(DirtyReadsTab(accountName)) { navigator ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    Column {
                        FilledDropDownSelector(
                            label = "Isolation level",
                            items = IsolationLevel.entries,
                            selectedItem = isolationLevel,
                            onChange = { isolationLevel = it },
                            modifier = Modifier.padding(16.dp)
                        )

                        SecondaryTabRow(
                            selectedTabIndex = navigator.current.options.index.toInt()
                        ) {
                            Tab(
                                selected = navigator.current == dirtyReadsTab,
                                onClick = { navigator.current = dirtyReadsTab },
                                text = { Text("Dirty reads") }
                            )
                            Tab(
                                selected = navigator.current == dirtyWritesTab,
                                onClick = { navigator.current = dirtyWritesTab },
                                text = { Text("Dirty writes") }
                            )
                        }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                        ) {
                            CurrentTab()
                        }
                    }
                }
            }
        }
    }
}
