import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random
import koma.*
import org.nield.kotlinstatistics.median
import org.nield.kotlinstatistics.percentile
import org.nield.kotlinstatistics.variance

fun main() {
    val clientGenerator = {
        Client(
            Random.nextDouble(Params.minClientRequest, Params.maxClientRequest),
            Random.nextDouble(Params.minClientReliability, Params.maxClientReliability)
        )
    }

    repeat(Params.timeUnits) {
        generateSequence(clientGenerator).take(Params.clientsPerTimeUnit).forEach(Bank::makeDeal)
        Bank.finishTimeUnit()
    }

    with(Bank.rateHistory) {
        println("Rate median is ${median()}")
        println("Rate variance is ${variance()}")
        println("Rate 5th percentile is ${percentile(5.0)}")
        println("Rate 95th percentile is ${percentile(95.0)}")
        println("Rate minimum is ${min()}")
        println("Rate maximum is ${max()}")

        figure(1)
        plot(toDoubleArray())
        xlabel("Time Units Passed")
        ylabel("Bank rate")
    }
    println()

    with(Bank.normalizedMoneyHistory) {
        println("Normalized money median is ${median()}")
        println("Normalized money variance is ${variance()}")
        println("Normalized money 5th percentile is ${percentile(5.0)}")
        println("Normalized money 95th percentile is ${percentile(95.0)}")
        println("Normalized money minimum is ${min()}")
        println("Normalized money maximum is ${max()}")

        figure(2)
        plot(toDoubleArray())
        xlabel("Time Units Passed")
        ylabel("Normalized money")
    }
}

object Params {
    const val idealRate = 1.10
    const val bankErrorRange = 0.1
    const val minClientReliability = 0.5
    const val maxClientReliability = 1.0
    const val minClientRequest = 1000.0
    const val maxClientRequest = 2000.0
    const val clientsPerTimeUnit = 10000
    const val timeUnits = 100
    const val averageRequest = (minClientRequest + maxClientRequest) / 2
    const val startMoney = Params.averageRequest * Params.clientsPerTimeUnit
}

fun expectedMoney(turn: Int) =
    Params.startMoney * ((Params.idealRate - 1) * (turn + 1) + 1)

object Bank {
    private var money: Double = Params.startMoney
    private var rate: Double = Params.idealRate
    private val moneyHistory = mutableListOf<Double>()
    val normalizedMoneyHistory: List<Double>
        get() = moneyHistory.mapIndexed { index, d ->
            d / expectedMoney(index)
        }
    val rateHistory = mutableListOf<Double>()

    fun makeDeal(client: Client) {
        val requestedReturns = (client.requestedMoney / client.estimatedReliability) * rate
        money += client.getOutcome(requestedReturns)
    }

    fun finishTimeUnit() {
        val expectedMoney = expectedMoney(moneyHistory.size + 1)
        moneyHistory += money
        rateHistory += rate
        rate = (Params.averageRequest - (money - expectedMoney) / Params.clientsPerTimeUnit) / Params.averageRequest
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
        if (Random.nextDouble() < realReliability) requestedReturns - requestedMoney else -requestedMoney
}