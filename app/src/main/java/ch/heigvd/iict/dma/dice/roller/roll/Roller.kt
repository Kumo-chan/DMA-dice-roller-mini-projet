package ch.heigvd.iict.dma.dice.roller.roll

import Roll
import RollResult
import RollsResult
import kotlin.random.Random

class Roller {
    fun roll(username: String, roll: Roll) : RollsResult {
        val rolls = List(roll.diceNumber.toInt()) {
            RollResult(
                diceSize = roll.diceSize,
                result = Random.nextInt(1, roll.diceSize.toInt())
            )
        }
        return RollsResult(username, rolls)
    }
}