package com.concough.android.concough.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.view.Window
import com.concough.android.concough.R
import com.concough.android.concough.interfaces.EntranceShowCommentDelegate
import com.concough.android.general.AlertClass
import com.concough.android.singletons.FontCacheSingleton
import com.concough.android.singletons.FormatterSingleton
import com.concough.android.structures.EntranceCommentType
import com.concough.android.utils.hideKeyboard
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.dialog_entrance_show_new_comment.*

/**
 * Created by abolfazl on 11/17/18.
 */
class EntranceShowNewCommentDialog(context: Context): Dialog(context) {
    companion object {
        const val TAG: String = "EntranceShowNewCommentDialog"
        const val MAX_CHARACTER_COUNT = 255
    }

    private var commentType: EntranceCommentType = EntranceCommentType.TEXT
    public var listener: EntranceShowCommentDelegate? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_entrance_show_new_comment)
        window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)

        DESNewComment_headerTitleTextView.typeface = FontCacheSingleton.getInstance(context.applicationContext).Light
        DESNewComment_questionTextView.typeface = FontCacheSingleton.getInstance(context.applicationContext).Regular
        DESNewComment_newEditText.typeface = FontCacheSingleton.getInstance(context.applicationContext).Light
        DESNewComment_charactersCountTextView.typeface = FontCacheSingleton.getInstance(context.applicationContext).Light
        DESNewComment_saveButton.typeface = FontCacheSingleton.getInstance(context.applicationContext).Bold
        DESNewComment_cancelButton.typeface = FontCacheSingleton.getInstance(context.applicationContext).Regular
        DESNewComment_newCommentErrorTextView.typeface = FontCacheSingleton.getInstance(context.applicationContext).Regular

        DESNewComment_newCommentErrorTextView.visibility = View.GONE
        DESNewComment_newEditText.onFocusChangeListener = View.OnFocusChangeListener { view, b ->
            if(!b) {
                hideKeyboard(view)
            }
        }

        DESNewComment_cancelButton.setOnClickListener {
            this@EntranceShowNewCommentDialog.dismiss()
        }

        DESNewComment_newEditText.addTextChangedListener(object: TextWatcher{
            override fun afterTextChanged(p0: Editable?) {
                if (p0 != null) {
                    val allowChars = MAX_CHARACTER_COUNT - p0.length
                    DESNewComment_charactersCountTextView.text = "${FormatterSingleton.getInstance().NumberFormatter.format(allowChars)!!} کاراکتر"
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

        })
    }

    fun setupDialog(questionUniqueId: String, questionNo: Int, position: Int) {
        DESNewComment_questionTextView.text = "سوال ${FormatterSingleton.getInstance().NumberFormatter.format(questionNo)}"

        val allowChars = MAX_CHARACTER_COUNT - this.DESNewComment_newEditText.text.length
        DESNewComment_charactersCountTextView.text = "${FormatterSingleton.getInstance().NumberFormatter.format(allowChars)!!} کاراکتر"

        DESNewComment_saveButton.setOnClickListener {
            if (DESNewComment_newEditText.length() > 0) {
                hideKeyboard(DESNewComment_container)
                this@EntranceShowNewCommentDialog.disableSubmitButton(true)

                if (this@EntranceShowNewCommentDialog.listener != null) {
                    if (this@EntranceShowNewCommentDialog.commentType == EntranceCommentType.TEXT) {
                        val eData = JsonObject()
                        eData.addProperty("text", DESNewComment_newEditText.text.toString())
                        eData.addProperty("questionNo", questionNo)

                        val result = this@EntranceShowNewCommentDialog.listener!!.addTextComment(
                                questionUniqueId,
                                questionNo,
                                position,
                                eData)

                        if (result) {
                            this@EntranceShowNewCommentDialog.dismiss()
                        } else {
                            this@EntranceShowNewCommentDialog.disableSubmitButton(false)
                            DESNewComment_newCommentErrorTextView.setTextColor(ContextCompat.getColor(context, R.color.colorConcoughRed))
                            DESNewComment_newCommentErrorTextView.text = "خطا! لطفا دوباره سعی نمایید."
                            DESNewComment_newCommentErrorTextView.visibility = View.VISIBLE
                        }
                    } else {
                        this@EntranceShowNewCommentDialog.disableSubmitButton(false)
                    }
                } else {
                    this@EntranceShowNewCommentDialog.disableSubmitButton(false)
                }
            } else {
                val s = AlertClass.convertMessage("Form", "EmptyFields")
                DESNewComment_newCommentErrorTextView.setTextColor(ContextCompat.getColor(context, R.color.colorConcoughRed))
                DESNewComment_newCommentErrorTextView.text = s.message
                DESNewComment_newCommentErrorTextView.visibility = View.VISIBLE
            }
        }
    }

    private fun disableSubmitButton(state: Boolean) {
        if (state) {
            DESNewComment_saveButton.isEnabled = false
            DESNewComment_saveButton.setBackgroundColor(ContextCompat.getColor(context, R.color.colorConcoughGray2))
        } else {
            DESNewComment_saveButton.isEnabled = true
            DESNewComment_saveButton.setBackgroundColor(ContextCompat.getColor(context, R.color.colorConcoughBlue))
        }
    }
}