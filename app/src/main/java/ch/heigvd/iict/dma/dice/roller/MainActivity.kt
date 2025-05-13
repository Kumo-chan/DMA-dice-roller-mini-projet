package ch.heigvd.iict.dma.dice.roller

import HistoricViewModel
import Roll
import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import ch.heigvd.iict.dma.dice.roller.roll.Roller
import ch.heigvd.iict.dma.dice.roller.ui.Layout


class MainActivity : ComponentActivity() {

    private val layout = Layout()

    private val roller = Roller()

    private val viewModel: HistoricViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val history by viewModel.history.collectAsState()
            layout.MainLayout(rollsResults = history,
                onRollDice = { diceSize, diceCount ->
                viewModel.addRollsResult(roller.roll(Roll(diceSize, diceCount)))
            })
        }
        // we request permissions
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestBlePermissionLauncher.launch(arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT))
        }
        else {
            requestBlePermissionLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN))
        }



    }

    override fun onPause() {
        super.onPause()
    }
    private val requestBlePermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->

        val isBLEGranted =  if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            permissions.getOrDefault(Manifest.permission.BLUETOOTH_SCAN, false) &&
                    permissions.getOrDefault(Manifest.permission.BLUETOOTH_CONNECT, false)
        else
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) &&
                    permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) &&
                    permissions.getOrDefault(Manifest.permission.BLUETOOTH, false) &&
                    permissions.getOrDefault(Manifest.permission.BLUETOOTH_ADMIN, false)

    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

}