import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class Roll(val diceSize: Number, val diceNumber: Number )
//TODO add username to roll result
data class RollResult(val diceSize: Number, val result: Number)
data class RollsResult(val results: List<RollResult>)
class HistoricViewModel : ViewModel() {

    private val _history = MutableStateFlow<List<RollsResult>>(emptyList())
    val history: StateFlow<List<RollsResult>> = _history.asStateFlow()

    fun addRollsResult(result: RollsResult) {
        _history.value += result
    }

    fun clearHistory() {
        _history.value = emptyList()
    }
}