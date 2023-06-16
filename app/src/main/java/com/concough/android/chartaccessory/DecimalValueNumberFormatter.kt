package com.concough.android.chartaccessory

import com.concough.android.singletons.FormatterSingleton
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ViewPortHandler
import java.text.NumberFormat

/**
 * Created by abolfazl on 11/24/18.
 */
class DecimalChartValueNumberFormatter : ValueFormatter {
    private var formatter: NumberFormat = FormatterSingleton.getInstance().DecimalNumberFormatter

    override fun getFormattedValue(value: Float, entry: Entry?, dataSetIndex: Int, viewPortHandler: ViewPortHandler?): String {
        return formatter.format(value)
    }

}