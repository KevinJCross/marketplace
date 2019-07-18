package org.kware.silverbars
import tec.units.indriya.ComparableQuantity
import java.math.BigDecimal
import javax.measure.quantity.Mass

data class Order(
    val userId: UserId,

    // JSR quantity types used to avoid using conversions kg->g lb->kg ect.
    // and add type safety i.e 3.kilo.meter will not compile.
    val quantity: ComparableQuantity<Mass>,
    val type: OrderType,
    val price: BigDecimal
)