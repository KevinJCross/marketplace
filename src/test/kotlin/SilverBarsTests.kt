package org.kware

import org.junit.jupiter.api.Test
import org.kware.silverbars.*
import org.tenkiv.physikal.core.gram
import org.tenkiv.physikal.core.kilo
import strikt.api.expectThat
import strikt.assertions.containsExactly
import java.math.BigDecimal

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
