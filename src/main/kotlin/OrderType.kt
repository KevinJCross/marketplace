package org.kware.silverbars
import java.math.BigDecimal

// Ive added the comparators to the Type because there will always be 1:1 and require a sort order
enum class OrderType(val sortOrder: Comparator<BigDecimal>) {
    BUY(Comparator<BigDecimal> { a, b -> a.compareTo(b) }),
    SELL(Comparator<BigDecimal> { a, b -> b.compareTo(a) })
}