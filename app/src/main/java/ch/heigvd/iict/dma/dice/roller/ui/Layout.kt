package ch.heigvd.iict.dma.dice.roller.ui

import RollsResult
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import ch.heigvd.iict.dma.dice.roller.opengl.MyGLSurfaceView

class Layout {

    enum class Tab {
        DICE_ROLLER, SETTINGS, THREE_D_VIEW
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainLayout(
        rollsResults: List<RollsResult>,
        onRollDice: (diceSize: Int, diceCount: Int) -> Unit,
        username: String = "",
        onUsernameChanged: (String) -> Unit = {},
        glSurfaceView: MyGLSurfaceView? = null
    ) {
        var selectedTab by remember { mutableStateOf(Tab.DICE_ROLLER) }
        if (username.isBlank()) DisplayInputForNickname(username, onUsernameChanged)
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Dice Roller") }
                )
            },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Casino, contentDescription = "Dice") },
                        label = { Text("Roll") },
                        selected = selectedTab == Tab.DICE_ROLLER,
                        onClick = { selectedTab = Tab.DICE_ROLLER }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") },
                        selected = selectedTab == Tab.SETTINGS,
                        onClick = { selectedTab = Tab.SETTINGS }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.ViewInAr, contentDescription = "3D View") },
                        label = { Text("3D View") },
                        selected = selectedTab == Tab.THREE_D_VIEW,
                        onClick = { selectedTab = Tab.THREE_D_VIEW }
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    when (selectedTab) {
                        Tab.DICE_ROLLER -> RollerTab(rollsResults, onRollDice)
                        Tab.SETTINGS -> SettingsTab(
                            username = username,
                            onUsernameChanged = onUsernameChanged,
                            rollsResults = rollsResults
                        )
                        Tab.THREE_D_VIEW -> ThreeDViewTab(glSurfaceView)
                    }
                }
            }
        }
    }

    @Composable
    fun RollerTab(
        rollsResults: List<RollsResult>,
        onRollDice: (diceSize: Int, diceCount: Int) -> Unit
    ) {
        var selectedDiceSize by remember { mutableStateOf(6) }
        var diceCount by remember { mutableStateOf(1) }

        val diceOptions = listOf(4, 6, 8, 10, 12, 20, 100)

        Column {
            Text("Select Dice Type:")
            DropdownMenuBox(
                selected = selectedDiceSize,
                options = diceOptions,
                onSelected = { selectedDiceSize = it }
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("Number of Dice: $diceCount")
            Slider(
                value = diceCount.toFloat(),
                onValueChange = { diceCount = it.toInt() },
                valueRange = 1f..10f,
                steps = 8
            )

            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { onRollDice(selectedDiceSize, diceCount) }) {
                Text("Roll $diceCount d$selectedDiceSize")
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Latest Roll:")
            if (rollsResults.isNotEmpty()) {
                val latestRoll = rollsResults.last()
                Text("User: ${latestRoll.username}", style = MaterialTheme.typography.bodyMedium)
                latestRoll.results.forEach { roll ->
                    Text(" - d${roll.diceSize}: ${roll.result}")
                }
            }
        }
    }

    @Composable
    fun SettingsTab(
        username: String,
        onUsernameChanged: (String) -> Unit,
        rollsResults: List<RollsResult>
    ) {
        var usernameState by remember { mutableStateOf(TextFieldValue(username)) }
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .fillMaxSize()
        ) {
            Text("Settings", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))

            Text("Your Username: $username")
            Button(onClick = {
                onUsernameChanged("")
            }) {
                Text("Change username")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Roll History", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            if (rollsResults.isEmpty()) {
                Text("No rolls yet")
            } else {
                Column {
                    rollsResults.forEachIndexed { index, rollsResult ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Roll Set ${index + 1} by ${rollsResult.username}:",
                                    style = MaterialTheme.typography.bodyMedium)
                                rollsResult.results.forEach { roll ->
                                    Text(" - d${roll.diceSize}: ${roll.result}")
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }

    @Composable
    fun ThreeDViewTab(glSurfaceView: MyGLSurfaceView?) {
        glSurfaceView?.let { surfaceView ->
            AndroidView(
                factory = { surfaceView },
                modifier = Modifier.fillMaxSize()
            )
        } ?: Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text("3D View not available")
        }
    }

    @Composable
    fun DropdownMenuBox(
        selected: Int,
        options: List<Int>,
        onSelected: (Int) -> Unit
    ) {
        var expanded by remember { mutableStateOf(false) }

        Box {
            Button(onClick = { expanded = true }) {
                Text("d$selected")
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { size ->
                    DropdownMenuItem(
                        text = { Text("d$size") },
                        onClick = {
                            onSelected(size)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
    @Composable
    fun DisplayInputForNickname(username: String,
                                onUsernameChanged: (String) -> Unit,
                                ) {
        var tempInput by remember { mutableStateOf("") }
        var nickname by remember { mutableStateOf(TextFieldValue(username)) }
        AlertDialog(
            onDismissRequest = { },
            confirmButton = {
                Button(onClick = {
                    if (tempInput.isNotBlank()) {
                        onUsernameChanged(tempInput)
                    }
                }) {
                    Text("Confirm")
                }
            },
            title = { Text("Enter your nickname") },
            text = {
                Column {
                    OutlinedTextField(
                        value = tempInput,
                        onValueChange = { tempInput = it },
                        label = { Text("Nickname") }
                    )
                }
            }
        )
    }
}