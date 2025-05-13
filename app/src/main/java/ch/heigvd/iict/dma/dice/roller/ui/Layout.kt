package ch.heigvd.iict.dma.dice.roller.ui

import RollsResult
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class Layout {

    @Composable
    fun MainLayout(
        rollsResults: List<RollsResult>,
        onRollDice: (diceSize: Int, diceCount: Int) -> Unit
    ) {
        var selectedDiceSize by remember { mutableStateOf(6) }
        var diceCount by remember { mutableStateOf(1) }

        val diceOptions = listOf(4, 6, 8, 10, 12, 20, 100)

        Column(modifier = Modifier.padding(16.dp)) {
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
            Text("History:")
            rollsResults.forEachIndexed { index, rollsResult ->
                Text("Roll Set ${index + 1}:")
                rollsResult.results.forEach { roll ->
                    Text(" - d${roll.diceSize}: ${roll.result}")
                }
                Spacer(modifier = Modifier.height(8.dp))
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