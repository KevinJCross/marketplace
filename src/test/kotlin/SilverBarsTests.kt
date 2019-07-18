package org.kware.silverbars

import org.junit.jupiter.api.Test
import org.tenkiv.physikal.core.gram
import org.tenkiv.physikal.core.kilo
import strikt.api.expectThat
import strikt.assertions.containsExactly
import tec.units.indriya.ComparableQuantity
import java.math.BigDecimal
import javax.measure.quantity.Mass

enum class OrderType { BUY }

class Market {
    private val theOrders: MutableList<Order> = mutableListOf()

    fun register(order: Order) {
        theOrders.add(order)
    }

    fun summary(): List<SummaryItem> {
        return theOrders.asSequence()
            .map { SummaryItem(it.quantity, it.price) }
            .toList()
            .sortedBy { it.price }
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

    @Test
    fun `user can register an order and see summary`() {
        val market = Market()
        market.register(buyOrder1)
        expectThat(market.summary()).containsExactly(SummaryItem(9.2.kilo.gram, 303.toBigDecimal()))
    }

    @Test
    fun `user can register 2 buy orders of different prices and see them in the summary items in the correct order`() {
        val market = Market()
        market.register(buyOrder2)
        market.register(buyOrder1)

        expectThat(market.summary()).containsExactly(
            SummaryItem(9.2.kilo.gram, 303.toBigDecimal()),
            SummaryItem(9.2.kilo.gram, 304.toBigDecimal())
        )
    }
}

data class SummaryItem(val quantity: ComparableQuantity<Mass>, val price: BigDecimal)