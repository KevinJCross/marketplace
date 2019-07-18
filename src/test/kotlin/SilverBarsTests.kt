package org.kware.silverbars

import org.junit.jupiter.api.Test
import org.tenkiv.physikal.core.gram
import org.tenkiv.physikal.core.kilo
import tec.units.indriya.ComparableQuantity
import java.math.BigDecimal
import javax.measure.quantity.Mass

enum class OrderType { BUY }

class Market {
    fun register(order: Order) {}
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
    fun `user can register an order`() {
        Market().register(Order(UserId("user1"), 9.2.kilo.gram, OrderType.BUY, 303.toBigDecimal()))
    }
}
