package com.concough.android.concough.dialogs

import android.app.Dialog
import android.support.v4.app.DialogFragment
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.util.SortedList
import android.util.Base64
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.target.Target.SIZE_ORIGINAL
import com.concough.android.concough.R
import com.concough.android.models.EntranceQuestionModel
import com.concough.android.models.EntranceQuestionStarredModelHandler
import com.concough.android.models.UserLogModelHandler
import com.concough.android.settings.SECRET_KEY
import com.concough.android.singletons.FontCacheSingleton
import com.concough.android.singletons.FormatterSingleton
import com.concough.android.singletons.UserDefaultsSingleton
import com.concough.android.structures.LogTypeEnum
import com.concough.android.utils.MD5Digester
import com.concough.android.utils.questionAnswerToString
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.android.synthetic.main.dialog_entrance_show_perview.*
import org.cryptonode.jncryptor.AES256JNCryptor
import java.io.File
import java.io.FileInputStream
import java.util.*

/**
 * Created by abolfazl on 12/3/18.
 */
class EntranceShowPreviewDialog: DialogFragment() {
    companion object {
        const val TAG = "EntranceShowPreviewDialog"
    }

    private lateinit var question: EntranceQuestionModel
    private var starred: Boolean = false
    private var username: String = ""
    private var imageRepo: HashMap<String, ByteArray> = hashMapOf()

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        inflater?.let {
            return inflater.inflate(R.layout.dialog_entrance_show_perview, container, false)
        }
        return null
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.username = UserDefaultsSingleton.getInstance(activity.applicationContext).getUsername()!!

        DESPreview_questionNumberTextView.typeface = FontCacheSingleton.getInstance(activity.applicationContext).Light
        DESPreview_answerTextView.typeface = FontCacheSingleton.getInstance(activity.applicationContext).Regular
        DESPreview_closeButton.typeface = FontCacheSingleton.getInstance(activity.applicationContext).Regular

        DESPreview_closeButton.setOnClickListener {
            this.dismiss()
        }
        DESPreview_star.isClickable = true
        DESPreview_star.setOnClickListener {
            this.addStarQuestionId(this.question.uniqueId, this.question.number)
            this.changeStarState()
        }

        this.setupDialog()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onResume() {
        dialog.window!!.setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE)
        dialog.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT)
        super.onResume()
    }

    public fun setVariables(questionModel: EntranceQuestionModel) {
        this.question = questionModel
    }

    private fun setupDialog() {
        DESPreview_questionNumberTextView.text = "سوال ${FormatterSingleton.getInstance().NumberFormatter.format(this.question.number)}"
        DESPreview_answerTextView.text = "گزینه ${questionAnswerToString(this.question.answer)} درست است"

        val starQ = EntranceQuestionStarredModelHandler.get(activity.applicationContext,
                this.username, this.question.entrance.uniqueId, this.question.uniqueId)
        if (starQ != null) {
            this.starred = true
            this.changeStarState()
        }

        this.loadImages(this.question.images)
        this.insertImage(this.question.images)
    }

    private fun changeStarState() {
        if (this.starred) {
            DESPreview_star.setImageResource(R.drawable.bookmark_filled)
            DESPreview_star.setColorFilter(ContextCompat.getColor(activity.applicationContext, R.color.colorConcoughRedLight))
        } else {
            DESPreview_star.setImageResource(R.drawable.bookmark_empty)
            DESPreview_star.setColorFilter(ContextCompat.getColor(activity.applicationContext, R.color.colorConcoughGray2))
        }
    }

    private fun addStarQuestionId(questionId: String, questionNumber: Int) {
        if (this.starred) {
            val id = this.question.entrance.uniqueId
            if (id != null) {
                if (EntranceQuestionStarredModelHandler.remove(activity.applicationContext, username, id, questionId)) {
                    val eData = JsonObject()
                    eData.addProperty("uniqueId", id)
                    eData.addProperty("questionNo", questionNumber)
                    this.createLog(LogTypeEnum.EntranceQuestionUnStar.title, eData)

                    this.starred = !this.starred
                }
            }
        } else {
            val id = this.question.entrance.uniqueId
            if (id != null) {
                if (EntranceQuestionStarredModelHandler.add(activity.applicationContext, username, id, questionId)) {
                    val eData = JsonObject()
                    eData.addProperty("uniqueId", id)
                    eData.addProperty("questionNo", questionNumber)
                    this.createLog(LogTypeEnum.EntranceQuestionStar.title, eData)

                    this.starred = !this.starred
                }
            }
        }
    }

    private fun createLog(logType: String, extraData: JsonObject) {
        val uniqueId = UUID.randomUUID().toString()
        val created = Date()
        val calendar = Calendar.getInstance()
        calendar.timeZone = TimeZone.getTimeZone("UTC")
        calendar.time = created
        val createdUtc = calendar.time

        try {
            UserLogModelHandler.add(activity.applicationContext, username, uniqueId, createdUtc, logType, extraData)
        } catch (exc: Exception) {
        }
    }

    private fun loadImages(imageString: String) {
        val jsonObjects = ArrayList<JsonObject>()
        val jsonArray = JsonParser().parse(imageString).asJsonArray

        for (item in jsonArray) {
            jsonObjects.add(item.asJsonObject)
        }

        Collections.sort(jsonObjects, object : SortedList.Callback<JsonObject>() {
            override fun compare(o1: JsonObject, o2: JsonObject): Int {

                return if (o1.get("order").asInt > o2.get("order").asInt) {
                    1
                } else if (o1.get("order").asInt < o2.get("order").asInt) {
                    -1
                } else {
                    0
                }
            }

            override fun onChanged(position: Int, count: Int) {

            }

            override fun areContentsTheSame(oldItem: JsonObject, newItem: JsonObject): Boolean {
                return false
            }

            override fun areItemsTheSame(item1: JsonObject, item2: JsonObject): Boolean {
                return false
            }

            override fun onInserted(position: Int, count: Int) {

            }

            override fun onRemoved(position: Int, count: Int) {

            }

            override fun onMoved(fromPosition: Int, toPosition: Int) {

            }
        })

        val hashStr = "$username:$SECRET_KEY"
        val hashKey = MD5Digester.digest(hashStr)

        val entranceUniqueId = this.question.entrance.uniqueId
        var finalPath = username + "_" + entranceUniqueId
        val f = File(activity.filesDir, finalPath)
        if (!f.exists()) {
            finalPath = entranceUniqueId
        }

        for (item in jsonObjects) {
            val imageId = item.asJsonObject.get("unique_key").asString
            if (!this.imageRepo.containsKey(imageId)) {
                val filePath = "$finalPath/$imageId"

                val file = File(activity.filesDir, filePath)
                if (file.exists()) {
                    try {
                        var buffer: ByteArray? = ByteArray(file.length().toInt())
                        val input = FileInputStream(file)

                        input.read(buffer!!)

                        val decoded = Base64.decode(buffer, Base64.DEFAULT)
                        var i: ByteArray? = AES256JNCryptor(1023).decryptData(decoded, hashKey.toCharArray())

                        i?.let {
                            this.imageRepo.put(imageId, i!!)
                        }

                        i = null
                        buffer = null
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }
            }
        }
    }

    private fun insertImage(imageString: String) {
        DESPreview_img1.setImageDrawable(null)
        DESPreview_img2.setImageDrawable(null)
        DESPreview_img3.setImageDrawable(null)

        DESPreview_imgPreLoad.visibility = View.VISIBLE

        val jsonObjects = ArrayList<JsonObject>()
        val jsonArray = JsonParser().parse(imageString).asJsonArray

        for (item in jsonArray) {
            jsonObjects.add(item.asJsonObject)
        }

        Collections.sort(jsonObjects, object : SortedList.Callback<JsonObject>() {
            override fun compare(o1: JsonObject, o2: JsonObject): Int {

                return if (o1.get("order").asInt > o2.get("order").asInt) {
                    1
                } else if (o1.get("order").asInt < o2.get("order").asInt) {
                    -1
                } else {
                    0
                }
            }

            override fun onChanged(position: Int, count: Int) {

            }

            override fun areContentsTheSame(oldItem: JsonObject, newItem: JsonObject): Boolean {
                return false
            }

            override fun areItemsTheSame(item1: JsonObject, item2: JsonObject): Boolean {
                return false
            }

            override fun onInserted(position: Int, count: Int) {

            }

            override fun onRemoved(position: Int, count: Int) {

            }

            override fun onMoved(fromPosition: Int, toPosition: Int) {

            }
        })

        val localBitmaps = ArrayList<ByteArray>()
        for (item in jsonObjects) {
            val imageId = item.asJsonObject.get("unique_key").asString
            if (this.imageRepo.containsKey(imageId))
                localBitmaps.add(this.imageRepo[imageId]!!)
        }

        if (localBitmaps.size >= 1) {
            try {
                Glide.with(this)
                        .load(localBitmaps[0])
                        .listener(object : RequestListener<ByteArray, GlideDrawable> {
                            override fun onException(e: Exception, model: ByteArray, target: Target<GlideDrawable>, isFirstResource: Boolean): Boolean {
                                return false
                            }

                            override fun onResourceReady(resource: GlideDrawable, model: ByteArray, target: Target<GlideDrawable>, isFromMemoryCache: Boolean, isFirstResource: Boolean): Boolean {
                                DESPreview_imgPreLoad.visibility = View.GONE
                                DESPreview_img1.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT)
                                DESPreview_img1.scaleType = ImageView.ScaleType.FIT_CENTER
                                DESPreview_img1.visibility = View.VISIBLE
                                DESPreview_img1.adjustViewBounds = true

                                return false
                            }

                        })
                        .override(SIZE_ORIGINAL, SIZE_ORIGINAL)
                        .crossFade()
                        .fitCenter()
                        .into(DESPreview_img1)

                DESPreview_img1.visibility = View.VISIBLE
                DESPreview_img1.adjustViewBounds = true


            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        if (localBitmaps.size >= 2) {
            try {
                Glide.with(this)
                        .load(localBitmaps[1])
                        .listener(object : RequestListener<ByteArray, GlideDrawable> {
                            override fun onException(e: Exception, model: ByteArray, target: Target<GlideDrawable>, isFirstResource: Boolean): Boolean {
                                return false
                            }

                            override fun onResourceReady(resource: GlideDrawable, model: ByteArray, target: Target<GlideDrawable>, isFromMemoryCache: Boolean, isFirstResource: Boolean): Boolean {
                                DESPreview_imgPreLoad.visibility = View.GONE
                                DESPreview_img2.scaleType = ImageView.ScaleType.FIT_CENTER
                                DESPreview_img2.visibility = View.VISIBLE
                                DESPreview_img2.adjustViewBounds = true

                                return false
                            }

                        })

                        .override(SIZE_ORIGINAL, SIZE_ORIGINAL)
                        .crossFade()
                        .fitCenter()

                        .into(DESPreview_img2)

                DESPreview_img2.scaleType = ImageView.ScaleType.FIT_CENTER
                DESPreview_img2.visibility = View.VISIBLE
                DESPreview_img2.adjustViewBounds = true


            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        if (localBitmaps.size >= 3) {
            try {

                Glide.with(this)
                        .load(localBitmaps[2])
                        .listener(object : RequestListener<ByteArray, GlideDrawable> {
                            override fun onException(e: Exception, model: ByteArray, target: Target<GlideDrawable>, isFirstResource: Boolean): Boolean {
                                return false
                            }

                            override fun onResourceReady(resource: GlideDrawable, model: ByteArray, target: Target<GlideDrawable>, isFromMemoryCache: Boolean, isFirstResource: Boolean): Boolean {
                                DESPreview_imgPreLoad.visibility = View.GONE
                                DESPreview_img3.scaleType = ImageView.ScaleType.FIT_CENTER
                                DESPreview_img3.visibility = View.VISIBLE
                                DESPreview_img3.adjustViewBounds = true

                                return false
                            }

                        })

                        .override(SIZE_ORIGINAL, SIZE_ORIGINAL)
                        .crossFade()
                        .fitCenter()
                        .into(DESPreview_img3)

                DESPreview_img3.scaleType = ImageView.ScaleType.FIT_CENTER
                DESPreview_img3.visibility = View.VISIBLE
                DESPreview_img3.adjustViewBounds = true

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        localBitmaps.clear()
    }
}