package ch.heigvd.iict.dma.dice.roller.ui

import RollsResult
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

class Layout {

    enum class Tab {
        DICE_ROLLER, SETTINGS
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainLayout(
        rollsResults: List<RollsResult>,
        onRollDice: (diceSize: Int, diceCount: Int) -> Unit,
        username: String = "User",
        onUsernameChanged: (String) -> Unit = {}
    ) {
        var selectedTab by remember { mutableStateOf(Tab.DICE_ROLLER) }

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
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                when (selectedTab) {
                    Tab.DICE_ROLLER -> RollerTab(rollsResults, onRollDice)
                    Tab.SETTINGS -> SettingsTab(
                        username = username,
                        onUsernameChanged = onUsernameChanged,
                        rollsResults = rollsResults
                    )
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

        Column {
            Text("Settings", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))

            Text("Your Username:")
            TextField(
                value = usernameState,
                onValueChange = {
                    usernameState = it
                    onUsernameChanged(it.text)
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

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
                                Text("Roll Set ${index + 1}:")
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
}