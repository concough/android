package com.concough.android.concough.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.view.GestureDetectorCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.TypedValue
import android.view.*
import android.widget.TextView
import com.concough.android.concough.R
import com.concough.android.concough.interfaces.EntranceShowCommentDelegate
import com.concough.android.extensions.timeAgoSinceDate
import com.concough.android.models.EntranceQuestionCommentModel
import com.concough.android.models.EntranceQuestionCommentModelHandler
import com.concough.android.singletons.FontCacheSingleton
import com.concough.android.singletons.FormatterSingleton
import com.concough.android.singletons.UserDefaultsSingleton
import com.concough.android.utils.timesAgoTranslate
import com.google.gson.JsonParser
import kotlinx.android.synthetic.main.dialog_entrance_show_all_comments.*
import android.view.MotionEvent
import android.view.GestureDetector
import com.concough.android.general.AlertClass


/**
 * Created by abolfazl on 11/17/18.
 */
class EntranceShowAllCommentsDialog(context: Context): Dialog(context) {
    companion object {
        const val TAG = "EntranceShowAllCommentsDialog"
    }

    public var listener: EntranceShowCommentDelegate? = null
    private var commentsAdapter: EntranceShowAllCommentsAdapter? = null
    private var questionId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_entrance_show_all_comments)
        window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
        window!!.setBackgroundDrawableResource(android.R.color.transparent)

        DESAllComments_titleTextView.typeface = FontCacheSingleton.getInstance(context.applicationContext).Light
        DESAllComments_closeButton.typeface = FontCacheSingleton.getInstance(context.applicationContext).Bold

        val layoutManager = LinearLayoutManager(context)
        DESAllComments_recycleView.layoutManager = layoutManager

        this.commentsAdapter = EntranceShowAllCommentsAdapter(context, arrayListOf())
        DESAllComments_recycleView.adapter = this.commentsAdapter

        DESAllComments_closeButton.setOnClickListener {
            this.dismiss()
        }
    }

    public fun setupDialog(questionId: String, entranceUuniqueId: String, questionNo: Int, position: Int) {
        this.questionId = questionId
        DESAllComments_titleTextView.text = context.getString(R.string.allCommentsHeaderTitle) + " " +
                FormatterSingleton.getInstance().NumberFormatter.format(questionNo)

        val swipeHandler = object : SwipeToDeleteCallback(context) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int) {
                viewHolder?.let {
                    val msg = AlertClass.convertMessage("EntranceShowAction", "DeleteComment")
                    AlertClass.showAlertMessageCustom(context, msg.title,
                            msg.message, "بله", "خیر") {
                        if (viewHolder is EntranceShowAllCommentsAdapter.TextCommentHolder) {
                            val comment = commentsAdapter!!.getItem(viewHolder.adapterPosition)
                            if (comment != null) {
                                if (this@EntranceShowAllCommentsDialog.listener != null) {
                                    val res = this@EntranceShowAllCommentsDialog.listener!!.deleteComment(questionId,
                                            questionNo, comment.uniqueId, position)

                                    if (res) {
                                        this@EntranceShowAllCommentsDialog.commentsAdapter!!.removeItem(viewHolder.adapterPosition)

                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(DESAllComments_recycleView)

        this.importComments(entranceUuniqueId, questionId)
    }

    private fun importComments(entranceUuniqueId: String, questionId: String) {
        val username = UserDefaultsSingleton.getInstance(context.applicationContext).getUsername()
        val comments = EntranceQuestionCommentModelHandler.getAllComments(context.applicationContext,
                entranceUuniqueId, username!!, questionId)

        this.commentsAdapter?.let {
            this.commentsAdapter!!.setItems(questionId, ArrayList(comments))
        }
    }

    abstract class SwipeToDeleteCallback(context: Context): ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
        private val deleteIcon = ContextCompat.getDrawable(context, R.drawable.waste)
        private val intrinsicWidth = context.resources.displayMetrics.density * 20
        private val intrinsicHeight = context.resources.displayMetrics.density * 20
        private val background = ColorDrawable()
        private val backgroundColor = Color.RED
        private val clearPaint = Paint().apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        }

        override fun onMove(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?, target: RecyclerView.ViewHolder?): Boolean {
            return false
        }

        override fun onChildDraw(c: Canvas?, recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
            viewHolder?.let {
                val itemView = viewHolder.itemView
                val itemHeight = itemView.bottom - itemView.top
                val isCanceled = dX == 0f && !isCurrentlyActive

                if (isCanceled) {
                    clearCanvas(c, itemView.left - dX, itemView.top.toFloat(),
                            itemView.left.toFloat(), itemView.bottom.toFloat())
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    return
                }

                // Draw the red delete button
                background.color = backgroundColor
                background.setBounds(itemView.left, itemView.top, itemView.left + dX.toInt(), itemView.bottom)
                background.draw(c)

                val deleteIconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
                val deleteIconMargin = (itemHeight - intrinsicHeight) / 2
                val deleteIconLeft = itemView.left + deleteIconMargin
                val deleteIconRight = itemView.left + deleteIconMargin + intrinsicWidth
                val deleteIconBottom = deleteIconTop + intrinsicHeight

                deleteIcon.setBounds(deleteIconLeft.toInt(), deleteIconTop.toInt(),
                        deleteIconRight.toInt(), deleteIconBottom.toInt())
                deleteIcon.setColorFilter(ContextCompat.getColor(itemView.context, R.color.colorWhite), PorterDuff.Mode.SRC_IN)
                deleteIcon.draw(c)

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }

        private fun clearCanvas(c: Canvas?, left: Float, top: Float, right: Float, bottom: Float) {
            c?.drawRect(left, top, right, bottom, clearPaint)
        }
    }

    private enum class EntranceShowAllCommentsTypes(code: Int) {
        TEXT_COMMENT(1)
    }

    private class EntranceShowAllCommentsAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private lateinit var questionId: String
        private var comments: ArrayList<EntranceQuestionCommentModel>
        private var context: Context

        constructor(context: Context, comments: ArrayList<EntranceQuestionCommentModel>) {
            this.context = context
            this.comments = comments
        }

        public fun setItems(questionId: String, comments: ArrayList<EntranceQuestionCommentModel>) {
            this.questionId = questionId
            this.comments = comments
        }

        public fun getItem(position: Int): EntranceQuestionCommentModel? {
            if (position < this.comments.size) {
                return this.comments[position]
            }
            return null
        }

        public fun removeItem(position: Int) {
            this.comments.removeAt(position)
            this.notifyItemRemoved(position)
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder? {
            if (viewType == EntranceShowAllCommentsTypes.TEXT_COMMENT.ordinal) {
                val view = LayoutInflater.from(context).inflate(R.layout.item_entrance_show_all_comments_dialog_cm, parent, false)
                return TextCommentHolder(view)
            }

            return null
        }

        override fun getItemCount(): Int {
            return this.comments.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
            holder?.let {
                if (holder is TextCommentHolder) {
                    val comment = this.comments[position]
                    holder.setupHolder(questionId, comment, position)
                }
            }
        }

        override fun getItemViewType(position: Int): Int {
            return EntranceShowAllCommentsTypes.TEXT_COMMENT.ordinal
        }

        override fun getItemId(position: Int): Long {
            return super.getItemId(position)
        }

        public class TextCommentHolder: RecyclerView.ViewHolder {
            private var commentTextView: TextView
            private var commentDateTextView: TextView

            constructor(itemView: View) : super(itemView) {
                this.commentTextView = itemView.findViewById(R.id.itemESACDialog_commentTextView) as TextView
                this.commentDateTextView = itemView.findViewById(R.id.itemESACDialog_commentDateTextView) as TextView

                this.commentTextView.typeface = FontCacheSingleton.getInstance(itemView.context.applicationContext).Regular
                this.commentDateTextView.typeface = FontCacheSingleton.getInstance(itemView.context.applicationContext).Light
            }

            fun setupHolder(questionId: String, comment: EntranceQuestionCommentModel, position: Int) {
                val data = JsonParser().parse(comment.commentData)
                val text = data.asJsonObject.get("text").asString

                this.commentTextView.text = text
                this.commentDateTextView.text = comment.created.timeAgoSinceDate("fa", true)

//                val gestureDetector = GestureDetectorCompat(itemView.context,
//                        object : GestureDetector.SimpleOnGestureListener() {
//                            override fun onLongPress(e: MotionEvent) {
//                            }
//                        })
//
//                this.itemView.setOnTouchListener { v, event -> gestureDetector.onTouchEvent(event) }
            }

            private fun removeComment(questionId: String, comment: EntranceQuestionCommentModel) {

            }
        }
    }
}