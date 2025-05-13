package ch.heigvd.iict.dma.dice.roller.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.Text

class Layout {

    @Composable
    fun MessageCard(name: String = "Toto") {
        Text(text = "Hello $name!")
    }
    @Composable
    @Preview
    fun Preview() {
        MessageCard("Test")
    }

}