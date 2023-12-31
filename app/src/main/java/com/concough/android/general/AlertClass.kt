package com.concough.android.general

import android.content.Context
import android.graphics.Color
import com.google.android.material.snackbar.Snackbar
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.concough.android.concough.R
import com.concough.android.singletons.FontCacheSingleton
import com.concough.android.vendor.progressHUD.AnnularView
import com.concough.android.vendor.progressHUD.KProgressHUD
import com.concough.android.vendor.progressHUD.SpinView
import com.concough.android.vendor.zhikanalertdialog.Effectstype
import com.concough.android.vendor.zhikanalertdialog.ZhycanNiftyDialogBuilder
import java.util.concurrent.locks.Lock
import android.content.ComponentName
import android.app.ActivityManager


/**
 * Created by abolfazl on 7/3/17.
 */

class AlertClass {

    data class Message(var title: String, var message: String, var showMsg: Boolean)

    companion object {
        var hideLoadingLock: Int = 9

        @JvmStatic
        fun convertMessage(messageType: String, messageSubType: String): Message {
            var showMessage: Boolean = true
            var title: String = ""
            var message: String = ""

            when (messageType) {
                "SystemError" -> {
                    when (messageSubType) {
                        "LowMemory" -> {
                            title = "خطای دانلود"; message = "حافظه دستگاه شما پر می باشد"
                        }
                        else -> showMessage = false
                    }
                }
                "Contacts" -> {
                    when (messageSubType) {
                        "Denied" -> {
                            title = "خطا"; message = "لطفا اجازه دسترسی به لیست مخاطبین را از طریق تنظیمات گوشی صادر نمایید"
                        }
                        "FetchError" -> {
                            title = "خطا"; message = "خطا در بارگذاری مخاطبین"
                        }
                        else -> showMessage = false
                    }
                }
                "Form" -> {
                    when (messageSubType) {
                        "EmptyFields" -> {
                            title = "خطا"; message = "لطفا همه فیلدها را پر نمایید"
                        }
                        "NotSameFields" -> {
                            title = "خطا"; message = "مقادیر وارد شده باید یکسان باشند"
                        }
                        "OldPasswordNotCorrect" -> {
                            title = "خطا"; message = "گذرواژه فعلی نادرست است"
                        }
                        "CodeWrong" -> {
                            title = "خطا"; message = "کد وارد شده صحیح نمی باشد"
                        }
                        "PhoneVerifyWrong" -> {
                            title = "خطا"; message = "فرمت شماره همراه اشتباه است"
                        }
                        else -> showMessage = false
                    }
                }
                "ActionResult" -> {
                    when (messageSubType) {
                        "ResendCodeSuccess" -> {
                            title = "پیغام"; message = "کد فعالسازی مجددا ارسال گردید"
                        }
                        "DownloadFailed" -> {
                            title = "پیغام"; message = "دانلود با خطا مواجه گردید"
                        }
                        "PurchasedSuccess" -> {
                            title = "پیغام"; message = "خرید با موفقیت انجام گردید. جهت دانلود به بخش کتابخانه من مراجعه نمایید"
                        }
                        "DownloadSuccess" -> {
                            title = "پیغام"; message = "دانلود با موفقیت انجام گردید"
                        }
                        "BasketDeleteSuccess" -> {
                            title = "پیغام"; message = "از سبد کالا با موفقیت حذف گردید"
                        }
                        "BugReportedSuccess" -> {
                            title = "پیغام"; message = "خطای گزارش شده با موفقیت ثبت گردید"
                        }
                        "DownloadStarted" -> {
                            title = "پیغام"; message = "دانلود شروع شده است"
                        }
                        "QuestionStarred" -> {
                            title = "پیغام"; message = "✮" + " اضافه شد"
                        }
                        "QuestionUnStarred" -> {
                            title = "پیغام"; message = "✩" + " حذف شد"
                        }
                        "InviteSuccess" -> {
                            title = "پیغام"; message = "دعوتنامه ها با موفقیت ارسال گردید"
                        }
                        "ChangePasswordSuccess" -> {
                            title = "پیغام"; message = "گذرواژه شما با موفقیت تغییر یافت"
                        }
                        "FreeMemorySuccess" -> {
                            title = "پیغام"; message = "داده های شما با موفقیت پاک گردید"
                        }
                        else -> showMessage = false
                    }
                }
                "ExamAction" -> {
                    when (messageSubType) {
                        "FinishEntranceExam" -> {
                            title = "پیغام"; message = "آیا از اتمام سنجش مطمئن هستید؟"
                        }
                        "FinishEntranceExamResult" -> {
                            title = "پیغام"; message = "محاسبه نتیجه سنجش با انصراف از سنجش؟\n\nدر صورت انصراف سابقه ای ثبت نخواهد شد."
                        }
                        "LessonExamHistoryNotAvail" -> {
                            title = "پیغام"; message = "شما هنوز هیچ سنجشی ندارید."
                        }
                        else -> showMessage = false
                    }
                }
                "ErrorResult" -> {
                    when (messageSubType) {
                        "RemoteDBError" -> {
                            title = "خطا"; message = "دسترسی به پایگاه داده مقدور نیست"
                        }
                        "BadData" -> {
                            title = "خطا"; message = "لطفا دوباره سعی نمایید"
                        }
                        "ExpiredCode" -> {
                            title = "خطا"; message = "کد ارسالی نامعتبر است. لطفا درخواست کد مجدد نمایید"
                        }
                        "MultiRecord" -> {
                            title = "خطا"; message = "اطلاعات نامشخص است"
                        }
                        "EmptyArray" -> {
                            title = "خطا"; message = "اطلاعات برای نمایش ناموجود است"
                        }
                        "UnsupportedVersion" -> {
                            title = "خطا"; message = "این قابلیت در این نسخه پشتیبانی نمی شود. به نسخه جدیدتر بروز رسانی نمایید."
                        }
                        "ProductNotExist" -> {
                            title = "خطا"; message = "چنین محصولی موجود نمی باشد."
                        }
                        else -> showMessage = false
                    }
                }
                "EntranceResult" -> {
                    when (messageSubType) {
                        "EntranceNotExist" -> {
                            title = "خطا"; message = "آزمون درخواستی موجود نمی باشد"
                        }
                        "EntranceStarredNotExist" -> {
                            title = "خطا"; message = "سوال نشان شده ای وجود ندارد"
                        }
                        else -> showMessage = false
                    }
                }
                "BasketResult" -> {
                    when (messageSubType) {
                        "SaleNotExist" -> {
                            title = "خطا"; message = "چنین خریدی موجود نیست"
                        }
                        "DuplicateSale" -> {
                            title = "خطا"; message = "این خرید قبلا ثبت شده است"
                        }
                        "EmptyBasket" -> {
                            title = "خطا"; message = "سبد خرید شما خالی است"
                        }
                        "PaymentProviderError" -> {
                            title = "خطا"; message = "خطا در اتصال به بانک"
                        }
                        "NotPaymnetRecord" -> {
                            title = "خطا"; message = "پرداختی ثبت نشده است"
                        }
                        "CheckoutPending" -> {
                            title = "خطا"; message = "وضعیت سبد خرید قبلی شما در حالت معلق است، آن را نهایی نمایید"
                        }
                        "CheckoutError" -> {
                            title = "خطا"; message = "پرداخت نا موفق بود! در که صورتی که مبلغ از حساب شما کسر شده است حداکثر تا یک روز کاری به حسابتان باز خواهد گشت"
                        }
                        "MustCheckoutLast" -> {
                            title = "خطا"; message = "ابتدا پرداخت قبلی خود را نهایی نمایید"
                        }
                        else -> showMessage = false
                    }
                }
                "WalletResult" -> {
                    when (messageSubType) {
                        "WalletNotEnoughCash" -> {
                            title = "خطا"; message = "موجودی کافی نیست"
                        }
                        else -> showMessage = false
                    }
                }
                "AuthProfile" -> {
                    when (messageSubType) {
                        "ExistUsername" -> {
                            title = "خطا"; message = "این شماره همراه قبلا انتخاب شده است"
                        }
                        "UserNotExist" -> {
                            title = "خطا"; message = "لطفا ابتدا ثبت نام کنید"
                        }
                        "PreAuthNotExist" -> {
                            title = "خطا"; message = "لطفا مجددا تقاضای کد نمایید"
                        }
                        "MismatchPassword" -> {
                            title = "خطا"; message = "هر دو فیلد گذرواژه باید یکی باشند"
                        }
                        "PassCannotChange" -> {
                            title = "خطا"; message = "امکان تغییر گذرواژه وجود ندارد"
                        }
                        "SMSSendError" -> {
                            title = "خطا"; message = "ارسال پیامک با خطا مواجه شد، مجددا سعی نمایید"
                        }
                        "CallSendError" -> {
                            title = "خطا"; message = "تماس صوتی با خطا مواجه، مجددا سعی نمایید"
                        }
                        "ExceedToday" -> {
                            title = "خطا"; message = "ظرفیت ارسال پیامک امروز شما به پایان رسید"
                        }
                        "ExceedCallToday" -> {
                            title = "خطا"; message = "لطفا فردا سعی نمایید. ظرفیت امروز شما برای ارسال پیامک یا تماس تلفنی به پایان رسید."
                        }
                        else -> showMessage = false
                    }
                }
                "HTTPError" -> {
                    when (messageSubType) {
                        "BadRequest", "UnAuthorized" -> {
                            title = "خطای دسترسی"; message = "اطلاعات وارد شده صحیح نمی باشد."
                        }
                        "ForbiddenAccess" -> {
                            title = "خطای دسترسی"; message = "دسترسی غیر مجاز"
                        }
                        "Unknown", "NetworkError" -> {
                            title = "خطای دسترسی"; message = "برقراری ارتباط با سرور مقدور نیست"
                        }
                        "NotFound" -> {
                            title = "خطای دسترسی"; message = "آدرس نامعتبر است"
                        }
                        else -> showMessage = false
                    }
                }
                "NetworkError" -> {
                    when (messageSubType) {
                        "NoInternetAccess" -> {
                            title = "خطای اینترنت"; message = "لطفا اینترنت خود را فعال نمایید"
                        }
                        "HostUnreachable" -> {
                            title = "خطای اینترنت"; message = "در حال حاضر کنکوق پاسخگو نمیباشد"
                        }
                        "UnKnown" -> {
                            title = "خطای اینترنت"; message = "اشکال در شبکه"
                        }
                        "Timeout" -> {
                            title = "خطای اینترنت"; message = "اشکال در شبکه"
                        }
                        else -> showMessage = false
                    }
                }
                "DownloadError" -> {
                    when (messageSubType) {
                        "DownloadInProgress" -> {
                            title = "خطای دانلود"; message = "امکان دانلود همزمان وجود ندارد"
                        }
                        else -> showMessage = false
                    }
                }
                "DeviceInfoError" -> {
                    when (messageSubType) {
                        "AnotherDevice" -> {
                            title = "خطا"; message = "اکانت شما توسط دستگاه دیگری در حال استفاده می باشد، در صورتی که به دستگاه فعال دسترسی دارید گزینه 'قفل دستگاه' در تنظیمات کنکوق را فشار دهید و در غیر اینصورت از دکمه بازیایی گذرواژه همین دستگاه استفاده نمایید"
                        }
                        "DeviceNotRegistered" -> {
                            title = "خطا"; message = "دستگاه شما با این اکانت ثبت نشده است"
                        }
                        else -> showMessage = false
                    }
                }
                "DeviceAction" -> {
                    when (messageSubType) {
                        "UpdateApp" -> {
                            title = "نسخه جدید نرم افزار"; message = "نسخه %s منتشر شده است"
                        }
                        else -> showMessage = false
                    }
                }
                "EntranceShowAction" -> {
                    when (messageSubType) {
                        "DeleteComment" -> {
                            title = "پیغام"; message = "آیا از حذف یادداشت مطمئن هستید؟"
                        }
                        else -> showMessage = false
                    }
                }
                else -> showMessage = false


            }

            return Message(title, message, showMessage)
        }

        @JvmStatic
        fun showLoadingMessage(context: Context): KProgressHUD {
//            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
//            val cn = am.getRunningTasks(1)[0].topActivity


            synchronized(AlertClass.hideLoadingLock) {
                val v = SpinView(context, context.resources.getColor(R.color.colorConcoughBlue))

                val hud = KProgressHUD.create(context)
                hud.setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                hud.setDimAmount(0.35F)
                hud.setCustomView(v)
                hud.setLabel("حوصله نمایید ...", context.resources.getColor(android.R.color.black))
                hud.setLabelFont(FontCacheSingleton.getInstance(context.applicationContext!!).Light)
                hud.setBackgroundColor(context.resources.getColor(android.R.color.white))

//                if (!hud.isShowing)
//                    hud.show()
                return hud
            }
        }

        @JvmStatic
        fun showUpdatingMessage(context: Context, maxProgress: Int): KProgressHUD {
            val v = AnnularView(context, context.resources.getColor(R.color.colorConcoughBlue))

            val hud = KProgressHUD.create(context)
            hud.setStyle(KProgressHUD.Style.ANNULAR_DETERMINATE)
            hud.setMaxProgress(maxProgress)
            hud.setDimAmount(0.35F)
            hud.setCustomView(v)
            hud.setLabel("به روز رسانی ...", context.resources.getColor(android.R.color.black))
            hud.setLabelFont(FontCacheSingleton.getInstance(context.applicationContext!!).Light)
            hud.setBackgroundColor(context.resources.getColor(android.R.color.white))
            return hud
        }

        @JvmStatic
        fun progressUpdatingMessage(progressHUD: KProgressHUD?, progress: Int) {
            progressHUD?.setProgress(progress)
        }

        @JvmStatic
        fun hideLoadingMessage(progressHUD: KProgressHUD?) {
//            synchronized(AlertClass.hideLoadingLock) {
            if (progressHUD != null) {
                progressHUD.dismiss()
            }
//            }
        }

        @JvmStatic
        fun showMakeExamMessage(context: Context): KProgressHUD {
            val v = SpinView(context, context.resources.getColor(R.color.colorConcoughRed))

            val hud = KProgressHUD.create(context)
            hud.setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
            hud.setDimAmount(0.35F)
            hud.setCustomView(v)
            hud.setLabel("آماده سازی سنجش ...", context.resources.getColor(android.R.color.black))
            hud.setLabelFont(FontCacheSingleton.getInstance(context.applicationContext!!).Light)
            hud.setBackgroundColor(context.resources.getColor(android.R.color.white))
            return hud
        }

        @JvmStatic
        fun showAlertMessage(context: Context, messageType: String, messageSubType: String, type: String, completion: (() -> Unit)?) {
            val dialogBuilder = ZhycanNiftyDialogBuilder(context, R.style.zhycan_dialog_untran)

            val (title, message, showMessasge) = convertMessage(messageType, messageSubType)

            if (showMessasge) {
                dialogBuilder
                        .withTitle(title)
                        .withMessage(message)
                        .withDuration(500)
                        .withMessageType(type)
                        .withEffect(Effectstype.Slidetop)
                        .withButton1Text("متوجه شدم")
                        .withTypeface(FontCacheSingleton.getInstance(context.applicationContext!!).Regular)
                        .setMessageTypeface(FontCacheSingleton.getInstance(context.applicationContext!!).Light)
                        .isCancelableOnTouchOutside(false)
                        .setButton1Click(View.OnClickListener {
                            dialogBuilder.dismiss()
                            if (completion != null) {
                                completion()
                            }
                        })
                        .show()
            }
        }

        @JvmStatic
        fun showAlertMessageCustom(context: Context, title: String, message: String, yesButtonTitle: String, noButtonTitle: String, completion: (() -> Unit)?) {
            val dialogBuilder = ZhycanNiftyDialogBuilder(context, R.style.zhycan_dialog_untran)

            dialogBuilder
                    .withTitle(title)
                    .withMessage(message)
                    .withDuration(500)
                    .withMessageType("error")
                    .withEffect(Effectstype.Slidetop)
                    .withButton1Text(yesButtonTitle)
                    .withButton2Text(noButtonTitle)
                    .withTypeface(FontCacheSingleton.getInstance(context.applicationContext!!).Regular)
                    .setMessageTypeface(FontCacheSingleton.getInstance(context.applicationContext!!).Light)
                    .isCancelableOnTouchOutside(false)
                    .setButton1Click(View.OnClickListener {
                        dialogBuilder.dismiss()
                        if (completion != null) {
                            completion()
                        }
                    })
                    .setButton2Click(View.OnClickListener {
                        dialogBuilder.dismiss()
                    })
                    .show()
        }

        @JvmStatic
        fun showAlertMessageCustom(context: Context, title: String, message: String, yesButtonTitle: String, noButtonTitle: String, completion: (() -> Unit)?, noCompletion: (() -> Unit)?) {
            val dialogBuilder = ZhycanNiftyDialogBuilder(context, R.style.zhycan_dialog_untran)

            dialogBuilder
                    .withTitle(title)
                    .withMessage(message)
                    .withDuration(500)
                    .withMessageType("error")
                    .withEffect(Effectstype.Slidetop)
                    .withButton1Text(yesButtonTitle)
                    .withButton2Text(noButtonTitle)
                    .withTypeface(FontCacheSingleton.getInstance(context.applicationContext!!).Regular)
                    .setMessageTypeface(FontCacheSingleton.getInstance(context.applicationContext!!).Light)
                    .isCancelableOnTouchOutside(false)
                    .setButton1Click(View.OnClickListener {
                        dialogBuilder.dismiss()
                        if (completion != null) {
                            completion()
                        }
                    })
                    .setButton2Click(View.OnClickListener {
                        dialogBuilder.dismiss()
                        if (noCompletion != null) {
                            noCompletion()
                        }
                    })
                    .show()
        }

        @JvmStatic
        fun showSucceessMessageCustom(context: Context, title: String, message: String, yesButtonTitle: String, noButtonTitle: String, completion: (() -> Unit)?, noCompletion: (() -> Unit)?) {
            val dialogBuilder = ZhycanNiftyDialogBuilder(context, R.style.zhycan_dialog_untran)

            dialogBuilder
                    .withTitle(title)
                    .withMessage(message)
                    .withDuration(500)
                    .withMessageType("success")
                    .withEffect(Effectstype.Slidetop)
                    .withButton1Text(yesButtonTitle)
                    .withButton2Text(noButtonTitle)
                    .withTypeface(FontCacheSingleton.getInstance(context.applicationContext!!).Regular)
                    .setMessageTypeface(FontCacheSingleton.getInstance(context.applicationContext!!).Light)
                    .isCancelableOnTouchOutside(false)
                    .setButton1Click(View.OnClickListener {
                        dialogBuilder.dismiss()
                        if (completion != null) {
                            completion()
                        }
                    })
                    .setButton2Click(View.OnClickListener {
                        dialogBuilder.dismiss()
                        if (noCompletion != null) {
                            noCompletion()
                        }
                    })
                    .show()
        }

        @JvmStatic
        fun showTopMessage(context: Context, view: View, messageType: String, messageSubType: String, type: String, completion: (() -> Unit)?) {
            val (_, message, showMessasge) = convertMessage(messageType, messageSubType)

            if (showMessasge) {
                val snack = Snackbar.make(view, "", Snackbar.LENGTH_LONG)
                val layout = snack.getView() as Snackbar.SnackbarLayout

                when (type) {
                    "success" -> layout.setBackgroundColor(context.resources.getColor(R.color.colorConcoughGreen))
                    "error" -> layout.setBackgroundColor(context.resources.getColor(R.color.colorConcoughRedLight))
                    "warning" -> layout.setBackgroundColor(context.resources.getColor(R.color.colorConcoughYellow))
                    else -> layout.setBackgroundColor(context.resources.getColor(R.color.colorConcoughGray4))
                }
                val textView = layout.findViewById(com.google.android.material.R.id.snackbar_text) as TextView
                textView.visibility = View.INVISIBLE

                // Inflate our custom view
                val snackView = LayoutInflater.from(context).inflate(R.layout.cc_snackbar_item, null)
                val params = layout.layoutParams as FrameLayout.LayoutParams
                params.gravity = Gravity.TOP
                layout.setLayoutParams(params)

                val textViewTop = snackView.findViewById(R.id.snackbar_text) as TextView
                textViewTop.setText(message)
                textViewTop.setTextColor(Color.WHITE)
                textViewTop.setTypeface(FontCacheSingleton.getInstance(context.applicationContext).Light)
                when (type) {
                    "success", "error", "warning" -> {
                    }
                    else -> textViewTop.setTextColor(context.resources.getColor(android.R.color.black))
                }

                layout.addView(snackView, 0)

                snack.show()
            }
        }
    }

}