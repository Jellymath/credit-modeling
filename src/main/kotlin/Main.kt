import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random
import koma.*


fun main() {
    val bank = Bank()

    val clientGenerator = {
        Client(
            Random.nextDouble(Params.minClientRequest, Params.maxClientRequest),
            Random.nextDouble(Params.minClientReliability, Params.maxClientReliability)
        )
    }

    repeat(Params.timeUnits) {
        generateSequence(clientGenerator).take(Params.clientsPerTimeUnit).forEach(bank::makeDeal)
        bank.finishTimeUnit()
    }

    figure(1)
    plot(bank.rateHistory.toDoubleArray())

    figure(2)
    plot(bank.normalizedMoneyHistory.toDoubleArray())

}

object Params {
    const val idealRate = 1.10
    const val bankErrorRange = 0.1
    const val minClientReliability = 0.1
    const val maxClientReliability = 1.0
    const val minClientRequest = 1000.0
    const val maxClientRequest = 2000.0
    const val averageRequest = (minClientRequest + maxClientRequest) / 2
    const val clientsPerTimeUnit = 10000
    const val timeUnits = 1000
}

//fun expectedMoney(turn: Int) = Params.averageRequest * Params.clientsPerTimeUnit * pow(Params.idealRate, turn + 1)
fun expectedMoney(turn: Int) = Params.averageRequest * Params.clientsPerTimeUnit * ((Params.idealRate - 1) * (turn + 1) + 1)

class Bank {
    private var money: Double = Params.averageRequest * Params.clientsPerTimeUnit
    private var rate: Double = Params.idealRate
    val moneyHistory = mutableListOf<Double>()
    val normalizedMoneyHistory: List<Double>
        get() = moneyHistory.mapIndexed { index, d ->
            d / expectedMoney(index)
        }
    val rateHistory = mutableListOf<Double>()

    fun makeDeal(client: Client) {
        val requestedReturns = client.requestedMoney / client.estimatedReliability * rate
        money += client.getOutcome(requestedReturns)
    }

    fun finishTimeUnit() {
        val expectedMoney = expectedMoney(moneyHistory.size)
        println("ratio: ${expectedMoney / money}")
        println("expectedMoney = ${expectedMoney}")
        println("money = ${money}")
        moneyHistory += money
        rateHistory += rate
//        rate = (rate - 1) * expectedMoney / money + 1
//        rate = rate * expectedMoney / money
        rate = (Params.averageRequest - (money - expectedMoney) / Params.clientsPerTimeUnit) / Params.averageRequest
        println("rate = ${rate}")
    }
}

class Client(val requestedMoney: Double, private val realReliability: Double) {
    val estimatedReliability: Double by lazy {
        Random.nextDouble(
            max(Params.minClientReliability, realReliability - Params.bankErrorRange),
            min(Params.maxClientReliability, realReliability + Params.bankErrorRange)
        )
    }

    fun getOutcome(requestedReturns: Double) =
        if (Random.nextDouble() < realReliability) requestedReturns else -requestedMoney
}