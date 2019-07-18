package org.kware.silverbars
import org.tenkiv.physikal.core.plus
import java.lang.ref.WeakReference

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