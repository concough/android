package com.concough.android.concough

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.baoyz.widget.PullRefreshLayout
import com.concough.android.chartaccessory.ChartValueNumberFormatter
import com.concough.android.chartaccessory.DecimalChartValueNumberFormatter
import com.concough.android.concough.dialogs.EntranceLessonLastExamChartDialog
import com.concough.android.concough.dialogs.EntranceShowInfoDialog
import com.concough.android.extensions.diffInHourMinSec
import com.concough.android.extensions.timeAgoSinceDate
import com.concough.android.models.EntranceLessonExamModel
import com.concough.android.models.EntranceLessonExamModelHandler
import com.concough.android.models.EntranceModelHandler
import com.concough.android.singletons.FontCacheSingleton
import com.concough.android.singletons.FormatterSingleton
import com.concough.android.singletons.UserDefaultsSingleton
import com.concough.android.structures.EntranceQuestionAnswerState
import com.concough.android.structures.EntranceStruct
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.YAxisValueFormatter
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_entrance_lesson_exam_history.*
import java.util.*
import kotlin.collections.ArrayList

class EntranceLessonExamHistoryActivity : TopNavigationActivity() {

    companion object {
        const val TAG = "EntranceLessonExamHistoryActivity"
        const val ENTRANCE_UNIQUE_ID_KEY = "entranceUniqueId"
        const val LESSON_TITLE_KEY = "lessonTitle"
        const val LESSON_ORDER_KEY = "lessonOrder"
        const val BOOKLET_ORDER_KEY = "bookletOrder"
        const val LESSON_EXAM_DURATION = "lessonExamDuration"
        const val LESSON_QUESTION_COUNT = "lessonQuestionCount"

        @JvmStatic
        public fun getIntent(packageContext: Context, entranceUnqiueId: String, lessonTitle: String,
                             lessonOrder: Int, bookletOrder: Int, lessonExamDuration: Int, lessonQuestionCount: Int): Intent {
            val i = Intent(packageContext, EntranceLessonExamHistoryActivity::class.java)
            i.putExtra(ENTRANCE_UNIQUE_ID_KEY, entranceUnqiueId)
            i.putExtra(LESSON_TITLE_KEY, lessonTitle)
            i.putExtra(LESSON_QUESTION_COUNT, lessonQuestionCount)
            i.putExtra(LESSON_ORDER_KEY, lessonOrder)
            i.putExtra(BOOKLET_ORDER_KEY, bookletOrder)
            i.putExtra(LESSON_EXAM_DURATION, lessonExamDuration)

            return i
        }
    }

    private lateinit var entranceUniqueId: String
    private var entrance: EntranceStruct? = null
    private lateinit var lessonTitle: String
    private var lessonOrder: Int = -1
    private var bookletOrder: Int = -1
    private var lessonExamDuration: Int = 0
    private var lessonQuestionCount: Int = 0

    private lateinit var examList: RealmResults<EntranceLessonExamModel>
    private var entranceLessonExamHistoryAdapter: EntranceLessonExamHistoryAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entrance_lesson_exam_history)

        this.entranceUniqueId = intent.getStringExtra(ENTRANCE_UNIQUE_ID_KEY)
        this.lessonTitle = intent.getStringExtra(LESSON_TITLE_KEY)
        this.lessonOrder = intent.getIntExtra(LESSON_ORDER_KEY, -1)
        this.bookletOrder = intent.getIntExtra(BOOKLET_ORDER_KEY, -1)
        this.lessonExamDuration = intent.getIntExtra(LESSON_EXAM_DURATION, 0)
        this.lessonQuestionCount = intent.getIntExtra(LESSON_QUESTION_COUNT, 0)

        val username = UserDefaultsSingleton.getInstance(this.applicationContext).getUsername()!!
        val count = EntranceLessonExamModelHandler.getExamCount(applicationContext, username,
                this.entranceUniqueId, this.lessonTitle, this.lessonOrder, this.bookletOrder)

        if (count <= 0) {
            this.finish()
        }

        this.actionBarSet(FormatterSingleton.getInstance().NumberFormatter.format(count) + " سنجش")

        this.examList = EntranceLessonExamModelHandler.getAllExam(applicationContext, username,
                this.entranceUniqueId, this.lessonTitle, this.lessonOrder, this.bookletOrder)

        EntranceLessonExamHistoryA_recycleView.setHasFixedSize(false)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        EntranceLessonExamHistoryA_recycleView.layoutManager = layoutManager

        this.entranceLessonExamHistoryAdapter = EntranceLessonExamHistoryAdapter(this, this.examList)
        EntranceLessonExamHistoryA_recycleView.adapter = this.entranceLessonExamHistoryAdapter

        EntranceLessonExamHistoryA_swipeRefreshLayout.setColorSchemeColors(Color.TRANSPARENT, Color.GRAY, Color.GRAY, Color.GRAY)
        EntranceLessonExamHistoryA_swipeRefreshLayout.setRefreshStyle(PullRefreshLayout.STYLE_MATERIAL)
        EntranceLessonExamHistoryA_swipeRefreshLayout.setOnRefreshListener(PullRefreshLayout.OnRefreshListener {
            // start refresh
            this@EntranceLessonExamHistoryActivity.examList = EntranceLessonExamModelHandler.getAllExam(applicationContext, username,
                    this.entranceUniqueId, this.lessonTitle, this.lessonOrder, this.bookletOrder)
            this@EntranceLessonExamHistoryActivity.entranceLessonExamHistoryAdapter?.setItems(this.examList)
            this@EntranceLessonExamHistoryActivity.entranceLessonExamHistoryAdapter?.notifyDataSetChanged()
            EntranceLessonExamHistoryA_swipeRefreshLayout.setRefreshing(false)
        })

    }

    private fun actionBarSet(title: String) {
        val buttonDetailArrayList = ArrayList<ButtonDetail>()

        var buttonDetail = ButtonDetail()
        buttonDetail.imageSource = R.drawable.info_icon
        buttonDetailArrayList.add(buttonDetail)

        super.createActionBar(title, true, buttonDetailArrayList)

        super.clickEventInterface = object : TopNavigationActivity.OnClickEventInterface {
            override fun OnButtonClicked(id: Int) {
                when (id) {
                    R.drawable.info_icon -> {
                        // make entrance struct
                        if (this@EntranceLessonExamHistoryActivity.entrance == null) {
                            val username = UserDefaultsSingleton.getInstance(applicationContext).getUsername()!!
                            val entranceDB = EntranceModelHandler.getByUsernameAndId(this@EntranceLessonExamHistoryActivity,
                                    username, this@EntranceLessonExamHistoryActivity.entranceUniqueId)

                            entranceDB?.let {

                                val extraStr = entranceDB.extraData
                                var extraData: JsonElement? = null
                                if (extraStr != null && "" != extraStr) {
                                    extraData = try {
                                        JsonParser().parse(extraStr)
                                    } catch (exc: Exception) {
                                        JsonParser().parse("[]")
                                    }
                                }

                                val e = EntranceStruct()
                                e.entranceUniqueId = entranceDB.uniqueId
                                e.entranceLastPublished = entranceDB.lastPublished
                                e.entranceDuration = entranceDB.duration
                                e.entranceMonth = entranceDB.month
                                e.entranceYear = entranceDB.year
                                e.entranceBookletCounts = entranceDB.bookletsCount
                                e.entranceExtraData = extraData
                                e.entranceSetId = entranceDB.setId
                                e.entranceSetTitle = entranceDB.set
                                e.entranceGroupTitle = entranceDB.group
                                e.entranceOrgTitle = entranceDB.organization
                                e.entranceTypeTitle = entranceDB.type

                                this@EntranceLessonExamHistoryActivity.entrance = e
                            } ?: run {
                                this.OnBackClicked()
                            }
                        }

                        val showInfoDialog = EntranceShowInfoDialog(this@EntranceLessonExamHistoryActivity)
                        showInfoDialog.setCancelable(true)
                        showInfoDialog.setCanceledOnTouchOutside(true)
                        showInfoDialog.listener = null
                        showInfoDialog.show()
                        showInfoDialog.setupDialog(this@EntranceLessonExamHistoryActivity.entrance!!,
                                0, EntranceQuestionAnswerState.None, "LessonExamHistory",
                                this@EntranceLessonExamHistoryActivity.lessonQuestionCount, 0,
                                this@EntranceLessonExamHistoryActivity.lessonTitle,
                                this@EntranceLessonExamHistoryActivity.lessonExamDuration)
                    }
                }
            }

            override fun OnBackClicked() {
                onBackPressed()
            }

            override fun OnTitleClicked() {
            }
        }
    }

    private enum class EntranceLessonExamHistoryHolderType private constructor(val value: Int) {
        LESSON_EXAM_HISTORY_CHART(1),
        LESSON_EXAM_HISTORY_HEADER(2),
        LESSON_EXAM_HISTORY_ITEM(3)
    }

    private class EntranceLessonExamHistoryAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private var context: Context
        private var examList: RealmResults<EntranceLessonExamModel>

        constructor(context: Context, examList: RealmResults<EntranceLessonExamModel>) {
            this.context = context
            this.examList = examList
        }

        public fun setItems(examList: RealmResults<EntranceLessonExamModel>) {
            this.examList = examList
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            if (viewType == EntranceLessonExamHistoryHolderType.LESSON_EXAM_HISTORY_HEADER.value) {
                val view = LayoutInflater.from(this.context).inflate(R.layout.item_favorite_header, parent, false)
                return HeaderViewHeader(view)
            } else if (viewType == EntranceLessonExamHistoryHolderType.LESSON_EXAM_HISTORY_CHART.value) {
                val view = LayoutInflater.from(this.context).inflate(R.layout.cc_entrance_lesson_exam_history_chart, parent, false)
                return HistoryChartViewHolder(view)
            } else {
                val view = LayoutInflater.from(this.context).inflate(R.layout.cc_entrance_lesson_exam_history_item, parent, false)
                return HistoryItemViewHolder(view)
            }
        }

        override fun getItemCount(): Int {
            if (this.examList.size > 0)
                return this.examList.count() + 2
            return 0
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is HeaderViewHeader) {
                holder.setupHolder("تمام سنجش ها")
            } else if (holder is HistoryItemViewHolder) {
                val item = this.examList[position - 2]
                if (item != null) {
                    holder.setupHolder(item.percentage, item.trueAnswer, item.falseAnswer, item.noAnswer,
                            item.created, item.startedDate, item.finishedDate)
                    holder.itemView.setOnClickListener {
                        val dialog = EntranceLessonLastExamChartDialog()
                        dialog.isCancelable = false
                        dialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.zhycan_dialog_fullscreen)
                        dialog.setVariables( (context as EntranceLessonExamHistoryActivity).entranceUniqueId,
                                (context as EntranceLessonExamHistoryActivity).lessonTitle,
                                (context as EntranceLessonExamHistoryActivity).lessonOrder,
                                (context as EntranceLessonExamHistoryActivity).bookletOrder,
                                "ExamHistory", item)

                        dialog.show((context as EntranceLessonExamHistoryActivity).supportFragmentManager, "EntranceLessonExamDialog")
                    }
                }
            } else if (holder is HistoryChartViewHolder) {
                holder.setupHolder(context, this.examList)
            }
        }

        override fun getItemViewType(position: Int): Int {
            if (position == 0) {
                return EntranceLessonExamHistoryHolderType.LESSON_EXAM_HISTORY_CHART.value
            } else if (position == 1) {
                return EntranceLessonExamHistoryHolderType.LESSON_EXAM_HISTORY_HEADER.value
            }
            return EntranceLessonExamHistoryHolderType.LESSON_EXAM_HISTORY_ITEM.value
        }

        // MARK: Holders
        internal inner class HeaderViewHeader(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val titleTextView: TextView = itemView.findViewById(R.id.itemFEH_titleTextView) as TextView

            init {
                this.titleTextView.typeface = FontCacheSingleton.getInstance(itemView.context.applicationContext).Regular
            }

            fun setupHolder(title: String) {
                this.titleTextView.text = title
            }
        }

        internal inner class HistoryItemViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
            private val percentageTextView: TextView = itemView.findViewById(R.id.itemELEHItem_percentageTextView) as TextView
            private val dateTextView: TextView = itemView.findViewById(R.id.itemELEHItem_dateTextView) as TextView
            private val timeElapsedTextView: TextView = itemView.findViewById(R.id.itemELEHItem_timeElapsedTextView) as TextView
            private val trueAnswerTextView: TextView = itemView.findViewById(R.id.itemELEHItem_trueAnswerTextView) as TextView
            private val falseAnswerTextView: TextView = itemView.findViewById(R.id.itemELEHItem_falseAnswerTextView) as TextView
            private val noAnswerTextView: TextView = itemView.findViewById(R.id.itemELEHItem_noAnswerTextView) as TextView

            init {
                this.percentageTextView.typeface = FontCacheSingleton.getInstance(itemView.context.applicationContext).Bold
                this.dateTextView.typeface = FontCacheSingleton.getInstance(itemView.context.applicationContext).Light
                this.timeElapsedTextView.typeface = FontCacheSingleton.getInstance(itemView.context.applicationContext).Regular
                this.trueAnswerTextView.typeface = FontCacheSingleton.getInstance(itemView.context.applicationContext).Bold
                this.falseAnswerTextView.typeface = FontCacheSingleton.getInstance(itemView.context.applicationContext).Bold
                this.noAnswerTextView.typeface = FontCacheSingleton.getInstance(itemView.context.applicationContext).Bold
            }

            fun setupHolder(percentage: Double, trueAnswer: Int, falseAnswer: Int, noAnswer: Int,
                            examDate: Date, started: Date, finished: Date) {
                this.percentageTextView.text = FormatterSingleton.getInstance().DecimalNumberFormatter.format(percentage * 100) + " %"
                this.trueAnswerTextView.text = FormatterSingleton.getInstance().NumberFormatter.format(trueAnswer)
                this.falseAnswerTextView.text = FormatterSingleton.getInstance().NumberFormatter.format(falseAnswer)
                this.noAnswerTextView.text = FormatterSingleton.getInstance().NumberFormatter.format(noAnswer)
                this.dateTextView.text = examDate.timeAgoSinceDate("fa", true)

                val d = started.diffInHourMinSec(finished)
                var s = ""
                if (d[0] > 0) {
                    s += FormatterSingleton.getInstance().NumberFormatter.format(d[0]) + " : "
                }

                var m = FormatterSingleton.getInstance().NumberFormatter.format(d[1])
                if (m.length == 1) {
                    m = "۰$m"
                }
                s += m + " : "

                var se = FormatterSingleton.getInstance().NumberFormatter.format(d[2])
                if (se.length == 1) {
                    se = "۰$se"
                }
                s += se

                this.timeElapsedTextView.text = s

            }
        }

        internal inner class HistoryChartViewHolder: RecyclerView.ViewHolder {
            private val recycleView: RecyclerView
            private var adapter: EntranceLessonExamHistoryChartAdapter? = null

            constructor(itemView: View): super(itemView) {
                this.recycleView = itemView.findViewById(R.id.itemELEHChart_recycleView) as RecyclerView

                this.recycleView.setHasFixedSize(true)
                val layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
                this.recycleView.layoutManager = layoutManager
                this.adapter = EntranceLessonExamHistoryChartAdapter(itemView.context, null)
                this.recycleView.adapter = this.adapter
            }

            fun setupHolder(context: Context, result: RealmResults<EntranceLessonExamModel>?) {
                this.adapter?.setItems(result)
            }
        }
    }

    private enum class EntranceLessonExamHistoryChartHolderType(val value: Int) {
        LESSON_EXAM_HISTORY_CHART_1(1),
        LESSON_EXAM_HISTORY_CHART_2(2)
    }

    public class EntranceLessonExamHistoryChartAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private var context: Context
        private var result: RealmResults<EntranceLessonExamModel>?

        constructor(context: Context, result: RealmResults<EntranceLessonExamModel>?) {
            this.context = context
            this.result = result
        }

        public fun setItems(result: RealmResults<EntranceLessonExamModel>?) {
            this.result = result
            this.notifyDataSetChanged()
        }

        // MARK: Methods
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            if (viewType == EntranceLessonExamHistoryChartHolderType.LESSON_EXAM_HISTORY_CHART_1.value) {
                val view = LayoutInflater.from(this.context).inflate(R.layout.cc_entrance_lesson_exam_history_chart_bar, parent, false)

                parent?.let {
                    val layoutParams = view.layoutParams
                    layoutParams.width = (parent.width * 0.85).toInt()
                    view.layoutParams = layoutParams
                }

                return EntranceLessonExamHistoryChart1ViewHolder(view)
            } else {
                val view = LayoutInflater.from(this.context).inflate(R.layout.cc_entrance_lesson_exam_history_chart_line, parent, false)

                parent?.let {
                    val layoutParams = view.layoutParams
                    layoutParams.width = (parent.width * 0.85).toInt()
                    view.layoutParams = layoutParams
                }

                return EntranceLessonExamHistoryChart2ViewHolder(view)
            }
        }

        override fun getItemCount(): Int {
            if (this.result != null)
                return 2

            return 0
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is EntranceLessonExamHistoryChart1ViewHolder) {
                val labels = ArrayList<String>()
                val data  = ArrayList<ArrayList<Float>>()

                var i = 0
                for (item in this.result!!) {
                    labels.add(FormatterSingleton.getInstance().NumberFormatter.format(i + 1))
                    data.add(arrayListOf(item.trueAnswer.toFloat(), item.falseAnswer.toFloat(), item.noAnswer.toFloat()))

                    i++
                    if (i == 10)
                        break
                }

                data.reverse()
                holder.setupHolder(labels, data)
            } else if (holder is EntranceLessonExamHistoryChart2ViewHolder) {
                val labels = ArrayList<String>()
                val data  = ArrayList<Float>()

                var i = 0
                for (item in this.result!!) {
                    labels.add(FormatterSingleton.getInstance().NumberFormatter.format(i + 1))
                    data.add(item.percentage.toFloat() * 100)

                    i++
                    if (i == 10)
                        break
                }
                data.reverse()
                holder.setupHolder(labels, data)
            }
        }

        override fun getItemViewType(position: Int): Int {
            return when (position) {
                0 -> EntranceLessonExamHistoryChartHolderType.LESSON_EXAM_HISTORY_CHART_2.value
                1 -> EntranceLessonExamHistoryChartHolderType.LESSON_EXAM_HISTORY_CHART_1.value
                else -> 50
            }
        }

        internal inner class EntranceLessonExamHistoryChart1ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val chartView: BarChart = itemView.findViewById(R.id.itemELEHC1_barChart) as BarChart

            init {
                this.chartView.setDescription("")
                this.chartView.animateXY(2000, 2000)
                this.chartView.setDrawGridBackground(false)
                this.chartView.setGridBackgroundColor(ContextCompat.getColor(itemView.context, android.R.color.transparent))
                this.chartView.setDrawBorders(false)
                this.chartView.getAxis(YAxis.AxisDependency.LEFT).setDrawAxisLine(false)
                this.chartView.getAxis(YAxis.AxisDependency.RIGHT).setDrawAxisLine(false)
                this.chartView.getAxis(YAxis.AxisDependency.LEFT).setDrawGridLines(false)
                this.chartView.getAxis(YAxis.AxisDependency.RIGHT).setDrawGridLines(false)
                this.chartView.getAxis(YAxis.AxisDependency.RIGHT).setDrawLabels(false)
                this.chartView.getAxis(YAxis.AxisDependency.LEFT).typeface = FontCacheSingleton.getInstance(itemView.context.applicationContext).Light
                this.chartView.getAxis(YAxis.AxisDependency.LEFT).textSize = 10.0f
                this.chartView.getAxis(YAxis.AxisDependency.LEFT).valueFormatter = YAxisValueFormatter { value, yAxis ->
                    FormatterSingleton.getInstance().NumberFormatter.format(value)
                }
                this.chartView.xAxis.position = XAxis.XAxisPosition.BOTTOM
                this.chartView.xAxis.setDrawGridLines(false)
                this.chartView.xAxis.typeface = FontCacheSingleton.getInstance(itemView.context.applicationContext).Light
                this.chartView.xAxis.textSize = 10.0f
                this.chartView.legend.typeface = FontCacheSingleton.getInstance(itemView.context.applicationContext).Light
                this.chartView.legend.textSize = 10.0f
                this.chartView.setScaleEnabled(false)
                this.chartView.isHighlightPerTapEnabled = false
                this.chartView.isHighlightPerDragEnabled = false
                this.chartView.setDrawValueAboveBar(false)
                //this.chartView.clearValues()
            }

            fun setupHolder(dataPoints: ArrayList<String>, values: ArrayList<ArrayList<Float>>) {
                this.chartView.data = this.setChart(dataPoints, values)

            }

            private fun setChart(dataPoints: ArrayList<String>, values: ArrayList<ArrayList<Float>>): BarData {
                val dataEntries = ArrayList<BarEntry>()

                for (i in dataPoints.indices) {
                    val entry = BarEntry(values[i].toFloatArray(), i)
                    dataEntries.add(entry)
                }

                val chartDataSet = BarDataSet(dataEntries, "۱۰ سنجش اخیر")
                chartDataSet.stackLabels = arrayOf("درست", "نادرست", "بی جواب")
                chartDataSet.valueFormatter = ChartValueNumberFormatter()
                chartDataSet.valueTypeface = FontCacheSingleton.getInstance(itemView.context.applicationContext).Regular
                chartDataSet.valueTextColor = ContextCompat.getColor(context, R.color.colorWhite)
                chartDataSet.valueTextSize = 12.0f
                chartDataSet.setColors(intArrayOf(R.color.colorConcoughGreen, R.color.colorConcoughRedLight, R.color.colorConcoughOrange), context)

                return BarData(dataPoints, chartDataSet)
            }
        }

        internal inner class EntranceLessonExamHistoryChart2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val chartView: LineChart = itemView.findViewById(R.id.itemELEHC_lineChart) as LineChart

            init {
                this.chartView.setDescription("")
                this.chartView.animateXY(2000, 2000)
                this.chartView.setDrawGridBackground(false)
                this.chartView.setGridBackgroundColor(ContextCompat.getColor(itemView.context, android.R.color.transparent))
                this.chartView.setDrawBorders(false)
                this.chartView.getAxis(YAxis.AxisDependency.LEFT).setDrawAxisLine(false)
                this.chartView.getAxis(YAxis.AxisDependency.RIGHT).setDrawAxisLine(false)
                this.chartView.getAxis(YAxis.AxisDependency.LEFT).setDrawGridLines(false)
                this.chartView.getAxis(YAxis.AxisDependency.RIGHT).setDrawGridLines(false)
                this.chartView.getAxis(YAxis.AxisDependency.RIGHT).setDrawLabels(false)
                this.chartView.getAxis(YAxis.AxisDependency.LEFT).typeface = FontCacheSingleton.getInstance(itemView.context.applicationContext).Light
                this.chartView.getAxis(YAxis.AxisDependency.LEFT).textSize = 10.0f
                this.chartView.getAxis(YAxis.AxisDependency.LEFT).valueFormatter = YAxisValueFormatter { value, yAxis ->
                    FormatterSingleton.getInstance().DecimalNumberFormatter.format(value)
                }
                this.chartView.xAxis.position = XAxis.XAxisPosition.BOTTOM
                this.chartView.xAxis.setDrawGridLines(false)
                this.chartView.xAxis.typeface = FontCacheSingleton.getInstance(itemView.context.applicationContext).Light
                this.chartView.xAxis.textSize = 10.0f
                this.chartView.xAxis.setAxisMaxValue(100.0f)
                this.chartView.legend.typeface = FontCacheSingleton.getInstance(itemView.context.applicationContext).Light
                this.chartView.legend.textSize = 12.0f
                this.chartView.setScaleEnabled(false)
                this.chartView.isHighlightPerTapEnabled = false
                this.chartView.isHighlightPerDragEnabled = false
                this.chartView.isHighlightPerDragEnabled = false
                //this.chartView.clearValues()
            }

            fun setupHolder(dataPoints: ArrayList<String>, values: ArrayList<Float>) {
                val sum = values.sum()
                var average = 0.0f
                if (sum != 0.0f) {
                    average = sum / values.count()
                }

                if (values.count() > 1) {
                    val limit = LimitLine(average, "میانگین")
                    limit.lineColor = ContextCompat.getColor(context, R.color.colorConcoughBlueLight)
                    limit.enableDashedLine(10.0f, 10.0f, 10.0f)
                    limit.labelPosition = LimitLine.LimitLabelPosition.LEFT_BOTTOM
                    limit.typeface = FontCacheSingleton.getInstance(context.applicationContext).Light
                    limit.textSize = 10.0f
                    limit.lineWidth = 2.0f

                    this.chartView.getAxis(YAxis.AxisDependency.LEFT).removeAllLimitLines()
                    this.chartView.getAxis(YAxis.AxisDependency.LEFT).addLimitLine(limit)
                    this.chartView.getAxis(YAxis.AxisDependency.LEFT).setDrawLimitLinesBehindData(true)
                }

                this.chartView.data = this.setChart(dataPoints, values)

            }

            private fun setChart(dataPoints: ArrayList<String>, values: ArrayList<Float>): LineData {
                val dataEntries = ArrayList<Entry>()

                for (i in dataPoints.indices) {
                    val entry = Entry(values[i], i)
                    dataEntries.add(entry)
                }

                val chartDataSet = LineDataSet(dataEntries, "درصد ۱۰ سنجش اخیر")
                chartDataSet.valueFormatter = ChartValueNumberFormatter()
                chartDataSet.valueTypeface = FontCacheSingleton.getInstance(itemView.context.applicationContext).Regular
                chartDataSet.valueFormatter = DecimalChartValueNumberFormatter()
                chartDataSet.valueTextSize = 10.0f
                chartDataSet.setColors(intArrayOf(R.color.colorConcoughBlue), context)
                chartDataSet.fillColor = ContextCompat.getColor(context, R.color.colorConcoughBlueLight)
                chartDataSet.setDrawFilled(true)
                chartDataSet.circleRadius = 3.0f
                chartDataSet.setCircleColors(intArrayOf(R.color.colorConcoughBlue), context)

                return LineData(dataPoints, chartDataSet)
            }
        }
    }
}
