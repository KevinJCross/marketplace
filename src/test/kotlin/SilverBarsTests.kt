package org.kware.silverbars

import org.junit.jupiter.api.Test
import org.tenkiv.physikal.core.gram
import org.tenkiv.physikal.core.kilo
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import tec.units.indriya.ComparableQuantity
import java.math.BigDecimal
import java.util.*
import javax.measure.quantity.Mass

enum class OrderType { BUY }

class Market {
    private var theOrder: Optional<Order> = Optional.empty()

    fun register(order: Order) {
        theOrder = Optional.of(order)
    }

    fun summary(): SummaryItem {
        val order = theOrder.get()
        return SummaryItem(order.quantity, order.price)
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
    @Test
    fun `user can register an order and see summary`() {
        val market = Market()
        market.register(Order(UserId("user1"), 9.2.kilo.gram, OrderType.BUY, 303.toBigDecimal()))
        expectThat(market.summary()).isEqualTo(SummaryItem(9.2.kilo.gram, 303.toBigDecimal()))
    }
}

data class SummaryItem(val quantity: ComparableQuantity<Mass>, val price: BigDecimal)