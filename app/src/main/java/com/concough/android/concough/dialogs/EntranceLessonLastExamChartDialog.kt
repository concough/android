package com.concough.android.concough.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import com.concough.android.chartaccessory.ChartValueNumberFormatter
import com.concough.android.concough.R
import com.concough.android.extensions.timeAgoSinceDate
import com.concough.android.models.EntranceLessonExamModel
import com.concough.android.models.EntranceLessonExamModelHandler
import com.concough.android.models.EntranceLessonModelHandler
import com.concough.android.models.EntranceQuestionModel
import com.concough.android.singletons.FontCacheSingleton
import com.concough.android.singletons.FormatterSingleton
import com.concough.android.singletons.UserDefaultsSingleton
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.android.synthetic.main.dialog_entrance_lesson_last_exam_chart.*

/**
 * Created by abolfazl on 11/28/18.
 */
class EntranceLessonLastExamChartDialog: DialogFragment() {
    companion object {
        const val TAG = "EntranceLessonLastExamChartDialog"
    }

    private lateinit var localExamRecord: EntranceLessonExamModel
    private var questionAdapter: EntranceLessonExamAdapter? = null

    private lateinit var entranceUniqueId: String
    private lateinit var lessonTitle: String
    private var lessonOrder: Int = 0
    private var bookletOrder: Int = 0
    private lateinit var whoCalled: String

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        inflater?.let {
            return inflater.inflate(R.layout.dialog_entrance_lesson_last_exam_chart, container, false)
        }
        return null
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        DELLEChart_titleTextView.typeface = FontCacheSingleton.getInstance(activity.applicationContext).Light
        DELLEChart_lessonTitleTextView.typeface = FontCacheSingleton.getInstance(activity.applicationContext).Bold
        DELLEChart_lessonExamDateTextView.typeface = FontCacheSingleton.getInstance(activity.applicationContext).Regular
        DELLEChart_closeButton.typeface = FontCacheSingleton.getInstance(activity.applicationContext).Regular

        DELLEChart_resultPieChart.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
        DELLEChart_resultPieChart.transparentCircleRadius = 40f
        DELLEChart_resultPieChart.holeRadius = 40f
        DELLEChart_resultPieChart.setTransparentCircleColor(ContextCompat.getColor(context, android.R.color.transparent))
        DELLEChart_resultPieChart.setHoleColor(ContextCompat.getColor(context, android.R.color.transparent))

        DELLEChart_resultPieChart.setDescription("")
        DELLEChart_resultPieChart.legend.isEnabled = false

        DELLEChart_closeButton.setOnClickListener {
            this.dismiss()
        }

        this.setupDialog()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
//        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }

    override fun onResume() {
        dialog.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT)
        super.onResume()
    }

    public fun setVariables(entranceUniqueId: String, lessonTitle: String, lessonOrder: Int, bookletOrder: Int,
                            whoCalled: String = "", examRecord: EntranceLessonExamModel?) {
        this.entranceUniqueId = entranceUniqueId
        this.lessonTitle = lessonTitle
        this.lessonOrder = lessonOrder
        this.bookletOrder = bookletOrder
        this.whoCalled = whoCalled
        if (examRecord != null)
            this.localExamRecord = examRecord
    }

    private fun setupDialog() {

        val username = UserDefaultsSingleton.getInstance(activity.applicationContext).getUsername()
        DELLEChart_lessonTitleTextView.text = this.lessonTitle

        if (whoCalled != "ExamHistory") {
            val lastExam = EntranceLessonExamModelHandler.getLastExam(activity.applicationContext,
                    username!!, entranceUniqueId, lessonTitle, lessonOrder, bookletOrder)

            if (lastExam != null) {
                this.localExamRecord = lastExam
            } else {
                this.dismiss()
            }
        }

        DELLEChart_lessonExamDateTextView.text = this.localExamRecord.created.timeAgoSinceDate("fa", true)

        val lesson = EntranceLessonModelHandler.getOneLessonByTitleAndOrder(activity.applicationContext,
                username!!, this.entranceUniqueId, this.lessonTitle, this.lessonOrder)

        val parser = JsonParser()
        val answers = parser.parse(this.localExamRecord.examData)

        val layoutManager = LinearLayoutManager(context)
        DELLEChart_questionsRecycleView.layoutManager = layoutManager

        this.questionAdapter = EntranceLessonExamAdapter(this.context, listOf(), answers, this.whoCalled)
        DELLEChart_questionsRecycleView.adapter = this.questionAdapter

        if (lesson != null) {
            val questions = lesson.questions.toList()
            this.questionAdapter?.let {
                this.questionAdapter!!.setItems(questions)
                this.questionAdapter!!.notifyDataSetChanged()
            }
        } else {
            this.dismiss()
        }


        this.setupChart()
    }

    private fun setupChart() {
        val labels = arrayOf("درست", "نادرست", "بی جواب")
        val data = floatArrayOf(this.localExamRecord.trueAnswer.toFloat(),
                this.localExamRecord.falseAnswer.toFloat(),
                this.localExamRecord.noAnswer.toFloat())

        DELLEChart_resultPieChart.setCenterTextTypeface(FontCacheSingleton.getInstance(activity.applicationContext).Regular)
        DELLEChart_resultPieChart.setCenterTextColor(ContextCompat.getColor(context, R.color.colorBlack))
        DELLEChart_resultPieChart.setCenterTextSize(14f)
        DELLEChart_resultPieChart.centerText = "${FormatterSingleton.getInstance().DecimalNumberFormatter.format(this.localExamRecord.percentage)} %"

        val dataEntries = ArrayList<Entry>()

        for (i in labels.indices) {
            val dataEntry = Entry(data[i], i)
            dataEntries.add(dataEntry)
        }

        val chartDataSet = PieDataSet(dataEntries, "")
        chartDataSet.valueFormatter = ChartValueNumberFormatter()
        chartDataSet.selectionShift = 0.0f
        chartDataSet.valueTypeface = FontCacheSingleton.getInstance(activity.applicationContext).Regular
        chartDataSet.valueTextSize = 15f
        chartDataSet.valueTextColor = ContextCompat.getColor(context, R.color.colorWhite)
        chartDataSet.setColors(intArrayOf(R.color.colorConcoughGreen, R.color.colorConcoughRedLight, R.color.colorConcoughOrange), context)

        val chartData = PieData(labels, chartDataSet)
        DELLEChart_resultPieChart.data = chartData
        DELLEChart_resultPieChart.animateXY(1, 1)
    }

    private class EntranceLessonExamAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private var context: Context
        private var questions: List<EntranceQuestionModel>
        private var whoCalled: String = ""
        private var answers: JsonObject

        constructor(context: Context, questions: List<EntranceQuestionModel>, answers: JsonElement, whoCalled: String) {
            this.context = context
            this.questions = questions
            this.whoCalled = whoCalled
            this.answers = answers.asJsonObject
        }

        public fun setItems(questions: List<EntranceQuestionModel>) {
            this.questions = questions
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.item_entrance_lesson_exam_chart_dialog_q, parent, false)
            return QuestionAnswerHolder(view)
        }

        override fun getItemCount(): Int {
            return this.questions.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
            holder?.let {
                if (holder is QuestionAnswerHolder) {
                    val question = this.questions[position]

                    var state = 0
                    var answer = 0

                    if (this.answers.has("${question.number}")) {
                        val ans = this.answers.get("${question.number}").asInt
                        if (ans != 0) {
                            answer = ans
                            state = if (question.answer == ans) {
                                1
                            } else {
                                -1
                            }
                        }
                    }

                    holder.setupHolder(context, question, answer, state, this.whoCalled, position)
                }
            }
        }

        public class QuestionAnswerHolder: RecyclerView.ViewHolder {
            private var questionNumberTextView: TextView
            private var answer1TextView: TextView
            private var answer2TextView: TextView
            private var answer3TextView: TextView
            private var answer4TextView: TextView
            private var helpImageView: ImageView

            constructor(itemView: View): super(itemView) {
                this.questionNumberTextView = itemView.findViewById(R.id.itemELECDQuestion_questionTextView) as TextView
                this.answer1TextView = itemView.findViewById(R.id.itemELECDQuestion_answer1TextView) as TextView
                this.answer2TextView = itemView.findViewById(R.id.itemELECDQuestion_answer2TextView) as TextView
                this.answer3TextView = itemView.findViewById(R.id.itemELECDQuestion_answer3TextView) as TextView
                this.answer4TextView = itemView.findViewById(R.id.itemELECDQuestion_answer4TextView) as TextView
                this.helpImageView = itemView.findViewById(R.id.itemELECDQuestion_helpImageView) as ImageView

                this.questionNumberTextView.typeface = FontCacheSingleton.getInstance(itemView.context.applicationContext).Regular
                this.answer1TextView.typeface = FontCacheSingleton.getInstance(itemView.context.applicationContext).Regular
                this.answer2TextView.typeface = FontCacheSingleton.getInstance(itemView.context.applicationContext).Regular
                this.answer3TextView.typeface = FontCacheSingleton.getInstance(itemView.context.applicationContext).Regular
                this.answer4TextView.typeface = FontCacheSingleton.getInstance(itemView.context.applicationContext).Regular

                this.helpImageView.setColorFilter(ContextCompat.getColor(itemView.context, R.color.colorConcoughBlue))
                this.helpImageView.isClickable = true
            }

            public fun setupHolder(context: Context, question: EntranceQuestionModel,
                                   answer: Int, state: Int, from: String, position: Int) {
                this.resetAnswerButton(context)

                this.questionNumberTextView.text = FormatterSingleton.getInstance().NumberFormatter.format(question.number)

                if (state != 0) {
                    this.setAnswerButton(context, answer, state)
                }

                if (from != "ExamHistory") {
                    this.helpImageView.visibility = View.INVISIBLE
                }

                this.helpImageView.setOnClickListener {
                    // TODO: must implement
                }
            }

            private fun resetAnswerButton(context: Context) {
                val color = ContextCompat.getColor(context, R.color.colorConcoughGray2)
                val bgDrawable = ContextCompat.getDrawable(context, R.drawable.concough_border_radius_full_2gray_style)

                this.answer1TextView.setTextColor(color)
                this.answer2TextView.setTextColor(color)
                this.answer3TextView.setTextColor(color)
                this.answer4TextView.setTextColor(color)

                this.answer1TextView.background = bgDrawable
                this.answer2TextView.background = bgDrawable
                this.answer3TextView.background = bgDrawable
                this.answer4TextView.background = bgDrawable
            }

            private fun setAnswerButton(context: Context, index: Int, answerState: Int = 0) {
                val colorW = ContextCompat.getColor(context, R.color.colorWhite)
                var drawable = ContextCompat.getDrawable(context, R.drawable.concough_border_radius_full_2gray_style)

                if (answerState == 1) {
                    drawable = ContextCompat.getDrawable(context, R.drawable.concough_border_radius_full_greengray_style)
                } else if (answerState == -1) {
                    drawable = ContextCompat.getDrawable(context, R.drawable.concough_border_radius_full_redgray_style)
                }

                when (index) {
                    1 -> {
                        this.answer1TextView.background = drawable
                        this.answer1TextView.setTextColor(colorW)
                    }
                    2 -> {
                        this.answer2TextView.background = drawable
                        this.answer2TextView.setTextColor(colorW)
                    }
                    3 -> {
                        this.answer3TextView.background = drawable
                        this.answer3TextView.setTextColor(colorW)
                    }
                    4 -> {
                        this.answer4TextView.background = drawable
                        this.answer4TextView.setTextColor(colorW)
                    }
                }
            }
        }
    }
}