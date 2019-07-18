package org.kware.silverbars

import org.junit.jupiter.api.Test
import org.tenkiv.physikal.core.gram
import org.tenkiv.physikal.core.kilo
import org.tenkiv.physikal.core.plus
import strikt.api.expectThat
import strikt.assertions.containsExactly
import tec.units.indriya.ComparableQuantity
import java.lang.ref.WeakReference
import java.math.BigDecimal
import javax.measure.quantity.Mass

// Ive added the comparators to the Type because there will always be 1:1 and require a sort order
enum class OrderType(val sortOrder: Comparator<BigDecimal>) {
    BUY(Comparator<BigDecimal> { a, b -> a.compareTo(b) }),
    SELL(Comparator<BigDecimal> { a, b -> b.compareTo(a) })
}

class Market {
    private val theOrders: MutableList<Order> = mutableListOf()

    fun register(order: Order): OrderReference {
        theOrders.add(order)
        return OrderReference(WeakReference(order), WeakReference(this))
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

    private fun summarise(type: OrderType, list: List<Order>): List<SummaryItem> {
        return list
            .groupBy { it.price }
            .toSortedMap(type.sortOrder)
            .map { SummaryItem(it.value.map { it.quantity }.reduce { a, b -> a + b }, it.key) }
    }

    class OrderReference(private val orderRef: WeakReference<Order>, private val market: WeakReference<Market>) {
        fun cancel() = orderRef.get()?.let { order -> market.get()?.run { remove(order) } }
    }

    private fun remove(order: Order) = theOrders.remove(order)
}

data class SummaryItem(val quantity: ComparableQuantity<Mass>, val price: BigDecimal)

data class Order(
    val userId: UserId,

    // JSR quantity types used to avoid using conversions kg->g lb->kg ect.
    // and add type safety i.e 3.kilo.meter will not compile.
    val quantity: ComparableQuantity<Mass>,
    val type: OrderType,
    val price: BigDecimal
)

// DDD class to avoid aliasing string so it actually is type safe
data class UserId(val id: String)

fun Order.toSummary() = SummaryItem(this.quantity, this.price)

class SilverBarsTests {
    private val userId = UserId("user1")

    private val buyOrder1 = Order(userId, 9.2.kilo.gram, OrderType.BUY, 303.toBigDecimal())
    private val buyOrder2 = Order(userId, 9.2.kilo.gram, OrderType.BUY, 304.toBigDecimal())

    private val sellOrder1 = Order(userId, 9.2.kilo.gram, OrderType.SELL, 303.toBigDecimal())
    private val sellOrder2 = Order(userId, 9.2.kilo.gram, OrderType.SELL, 304.toBigDecimal())
    private val sellOrder3 = Order(userId, 2.kilo.gram, OrderType.SELL, 305.toBigDecimal())

    @Test
    fun `user can register an order and see summary`() {
        val market = Market()
        market.register(buyOrder1)

        expectThat(market.summary()[OrderType.BUY].orEmpty())
            .containsExactly(SummaryItem(9.2.kilo.gram, 303.toBigDecimal()))
    }

    @Test
    fun `user can register 2 buy orders of different prices and see them in the summary items in the correct order`() {
        val market = with(Market()) {
            register(buyOrder2)
            register(buyOrder1)
            this
        }

        expectThat(market.summary()[OrderType.BUY].orEmpty())
            .containsExactly(
                buyOrder1.toSummary(),
                buyOrder2.toSummary()
            )
    }

    @Test
    fun `user can register 2 sell orders of different prices and see them in the summary in the correct order`() {
        val market = with(Market()) {
            register(sellOrder1)
            register(sellOrder2)
            this
        }

        expectThat(market.summary()[OrderType.SELL].orEmpty())
            .containsExactly(
                sellOrder2.toSummary(),
                sellOrder1.toSummary()
            )
    }

    @Test
    fun `user can register 3 sell orders and 2 orders of same price and see them merged in the summary`() {
        val market = Market()
        market.register(sellOrder1)
        market.register(sellOrder2)
        market.register(sellOrder2)

        expectThat(market.summary()[OrderType.SELL].orEmpty())
            .containsExactly(
                SummaryItem(18.4.kilo.gram, 304.toBigDecimal()),
                sellOrder1.toSummary()
            )
    }

    @Test
    fun `user can specify the same price with different precisions and it will merge`() {
        val market = with(Market()) {
            register(Order(userId, 2.kilo.gram, OrderType.BUY, BigDecimal(0.00500)))
            register(Order(userId, 2.kilo.gram, OrderType.BUY, BigDecimal(0.005)))
            this
        }
        expectThat(market.summary()[OrderType.BUY].orEmpty())
            .containsExactly(
                SummaryItem(4.kilo.gram, BigDecimal(0.005))
            )
    }

    @Test
    fun `the user can cancel an order`() {
        val market = with(Market()) {
            register(sellOrder3)
            register(sellOrder1)
            this
        }
        val orderToCancel = market.register(sellOrder2)

        orderToCancel.cancel()

        expectThat(market.summary()[OrderType.SELL].orEmpty())
            .containsExactly(
                sellOrder3.toSummary(),
                sellOrder1.toSummary()
            )
    }
}
