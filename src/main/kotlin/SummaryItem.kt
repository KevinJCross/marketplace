package org.kware.silverbars
import tec.units.indriya.ComparableQuantity
import java.math.BigDecimal
import javax.measure.quantity.Mass

data class SummaryItem(val quantity: ComparableQuantity<Mass>, val price: BigDecimal)