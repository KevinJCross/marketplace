package org.kware.silverbars

import org.junit.jupiter.api.Test
import org.tenkiv.physikal.core.gram
import org.tenkiv.physikal.core.kilo
import strikt.api.expectThat
import strikt.assertions.containsExactly
import tec.units.indriya.ComparableQuantity
import java.math.BigDecimal
import javax.measure.quantity.Mass

enum class OrderType(val sortOrder: (List<Order>) -> List<Order>) {
    BUY({ it.sortedBy { order -> order.price } }),
    SELL({ it.sortedByDescending { order -> order.price } })
}

class Market {
    private val theOrders: MutableList<Order> = mutableListOf()

    fun register(order: Order) {
        theOrders.add(order)
    }

    // could have used a few different mechanisms represent BUY and SELL in the summary.
    // I chose the map indexed by OrderType since this would easily extend to other types with the least amount of effort
    // other options considered were:
    // - type parameter on summary
    // - 2 different summary methods
    // - a summary object with buy list and a sell list.
    fun summary(): Map<OrderType, List<SummaryItem>> {
        return theOrders
            .asSequence()
            .groupBy { it.type }
            .map { entry -> Pair(entry.key, summarise(entry.key, entry.value)) }
            .toMap()
    }

    private fun summarise(type: OrderType, list: List<Order>): List<SummaryItem> =
        type.sortOrder(list).map { SummaryItem.of(it) }.toList()
}

data class SummaryItem(val quantity: ComparableQuantity<Mass>, val price: BigDecimal) {
    companion object {
        fun of(order: Order): SummaryItem = SummaryItem(order.quantity, order.price)
    }
}

data class Order(
    val userId: UserId,

    // JSR quantity types used to avoid using conversions kg->g lb->kg ect.
    // and add type safety i.e 3.kilo.meter will not compile.
    val quantity: ComparableQuantity<Mass>,

    val type: OrderType,

    // bigDecimal is being used to avoid double or float rounding errors.
    // Issues about precision equals will need to be tested i.e. 0.10 != 0.1
    val price: BigDecimal
)

// DDD class to avoid aliasing string so it actually is type safe
data class UserId(val id: String)

class SilverBarsTests {
    private val buyOrder1 = Order(UserId("user1"), 9.2.kilo.gram, OrderType.BUY, 303.toBigDecimal())
    private val buyOrder2 = Order(UserId("user1"), 9.2.kilo.gram, OrderType.BUY, 304.toBigDecimal())

    private val sellOrder1 = Order(UserId("user1"), 9.2.kilo.gram, OrderType.SELL, 303.toBigDecimal())
    private val sellOrder2 = Order(UserId("user1"), 9.2.kilo.gram, OrderType.SELL, 304.toBigDecimal())

    @Test
    fun `user can register an order and see summary`() {
        val market = Market()
        market.register(buyOrder1)
        expectThat(market.summary()[OrderType.BUY].orEmpty())
            .containsExactly(SummaryItem(9.2.kilo.gram, 303.toBigDecimal()))
    }

    @Test
    fun `user can register 2 buy orders of different prices and see them in the summary items in the correct order`() {
        val market = Market()
        market.register(buyOrder2)
        market.register(buyOrder1)

        expectThat(market.summary()[OrderType.BUY].orEmpty())
            .containsExactly(
                SummaryItem(9.2.kilo.gram, 303.toBigDecimal()),
                SummaryItem(9.2.kilo.gram, 304.toBigDecimal())
            )
    }

    @Test
    fun `user can register 2 sell orders of different prices and see them in the summary in the correct order`() {
        val market = Market()
        market.register(sellOrder1)
        market.register(sellOrder2)

        expectThat(market.summary()[OrderType.SELL].orEmpty())
            .containsExactly(
                SummaryItem(9.2.kilo.gram, 304.toBigDecimal()),
                SummaryItem(9.2.kilo.gram, 303.toBigDecimal())
            )
    }
}
