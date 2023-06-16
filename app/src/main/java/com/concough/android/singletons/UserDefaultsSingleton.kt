package com.concough.android.singletons

import android.content.Context
import android.content.SharedPreferences
import com.concough.android.structures.ProfileStruct
import java.util.*

/**
 * Created by abolfazl on 7/5/17.
 */
class UserDefaultsSingleton {
    data class WalletStruct(var cash: Int, var updated: Date)

    private var prefs: SharedPreferences
    private var context: Context

    private constructor(context: Context) {
        this.context = context
        this.prefs = context.getSharedPreferences(FILENAME, Context.MODE_PRIVATE)
    }

    companion object Factory {
        val TAG: String = "UserDefaultsSingleton"
        val FILENAME = "profile"

        private var sharedInstance: UserDefaultsSingleton? = null

        @JvmStatic
        fun getInstance(context: Context): UserDefaultsSingleton {
            if (sharedInstance == null)
                sharedInstance = UserDefaultsSingleton(context)

            return sharedInstance!!
        }
    }

    private fun setValueAsString(key: String, value: String) {
        this.prefs.edit().putString(key, value).apply()
    }

    private fun getValueAsString(key: String): String? {
        return this.prefs.getString(key, null)
    }

    private fun setValueAsBoolean(key: String, value: Boolean) {
        this.prefs.edit().putBoolean(key, value).apply()
    }

    private fun getValueAsBoolean(key: String): Boolean {
        return this.prefs.getBoolean(key, false)
    }

    private fun setValueAsInt(key: String, value: Int) {
        this.prefs.edit().putInt(key, value).apply()
    }

    private fun getValueAsInt(key: String): Int? {
        return this.prefs.getInt(key, 0)
    }

    public fun clearAll(): Boolean {
        this.prefs.edit().clear().apply()
        return true
    }

    public fun hasProfile(): Boolean {
        if (this.prefs.contains("Profile.Created")) {
            return this.getValueAsBoolean("Profile.Created")
        }
        return false
    }

    public fun createProfile(firstname: String, lastname: String, grade: String, gradeString: String, gender: String, birthday: Date, modified: Date) {
        this.setValueAsString("Profile.Firstname", firstname)
        this.setValueAsString("Profile.Lastname", lastname)
        this.setValueAsString("Profile.Grade", grade)
        this.setValueAsString("Profile.GradeString", gradeString)
        this.setValueAsString("Profile.Gender", gender)
        this.setValueAsString("Profile.Birthday", FormatterSingleton.getInstance().UTCDateFormatter.format(birthday))
        this.setValueAsString("Profile.Modified", FormatterSingleton.getInstance().UTCDateFormatter.format(modified))
        this.setValueAsBoolean("Profile.Created", true)
    }


    public fun updateModified(modified: Date) {
        this.setValueAsString("Profile.Modified", FormatterSingleton.getInstance().UTCDateFormatter.format(modified))
    }

    public fun updateGrade(grade: String, gradeString: String, modified: Date) {
        this.setValueAsString("Profile.Grade", grade)
        this.setValueAsString("Profile.GradeString", gradeString)
        this.updateModified(modified)
    }

    public fun getUsername(): String? {
        return TokenHandlerSingleton.getInstance(this.context).getUsername()
    }

    public fun checkPassword(context: Context, password: String): Boolean {
        if (TokenHandlerSingleton.getInstance(context).getPassword() == password) {
            return true
        }
        return false
    }

    public fun getProfile(): ProfileStruct? {
        val firstname = this.getValueAsString("Profile.Firstname")
        val lastname = this.getValueAsString("Profile.Lastname")
        val grade = this.getValueAsString("Profile.Grade")
        val gradeString = this.getValueAsString("Profile.GradeString")
        val gender = this.getValueAsString("Profile.Gender")
        val birthday = this.getValueAsString("Profile.Birthday")
        val modified = this.getValueAsString("Profile.Modified")

        if (this.prefs.contains("Profile.Created") && this.getValueAsBoolean("Profile.Created")) {
            try {
                return ProfileStruct(firstname!!, lastname!!, grade!!, gradeString!!, gender!!, FormatterSingleton.getInstance().UTCDateFormatter.parse(birthday), FormatterSingleton.getInstance().UTCDateFormatter.parse(modified))
            } catch (exc: Exception) {
            }
        }
        return null
    }

    fun clearWallet() {
        this.prefs.edit().remove("Wallet.Created").apply()
        this.prefs.edit().remove("Wallet.Cash").apply()
        this.prefs.edit().remove("Wallet.Updated").apply()
    }

    fun hasWallet(): Boolean {
        if (this.prefs.contains("Wallet.Created")) {
            return this.getValueAsBoolean("Wallet.Created")
        }
        return false
    }

    fun getWalletInfo(): WalletStruct? {
        val cash = this.getValueAsInt("Wallet.Cash")
        val updated = this.getValueAsString("Wallet.Updated")

        if (cash != null && updated != null) {
            val up = FormatterSingleton.getInstance().UTCDateFormatter.parse(updated)
            return WalletStruct(cash, up)
        }

        return null
    }

    fun setWalletInfo(cash: Int, updated: String) {
        this.setValueAsInt("Wallet.Cash", cash)
        this.setValueAsString("Wallet.Updated", updated)
        this.setValueAsBoolean("Wallet.Created", true)
    }
}