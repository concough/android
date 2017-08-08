package com.concough.android.models

import android.content.Context
import com.concough.android.singletons.BasketSingleton
import com.concough.android.singletons.RealmSingleton
import io.realm.RealmQuery
import io.realm.RealmResults
import io.realm.Sort
import java.util.*

/**
 * Created by abolfazl on 7/12/17.
 */
class PurchasedModelHandler {
    companion object Factory {
        val TAG: String = "PurchasedModelHandler"

        @JvmStatic
        fun add(context: Context, id: Int, username: String, isDownloaded: Boolean, downloadTimes: Int,
                isImageDownloaded: Boolean, purchasedType: String, purchaseUniqueId: String,
                created: Date) : Boolean {

            val purchased = PurchasedModel()
            purchased.created = created
            purchased.id = id
            purchased.isDownloaded = isDownloaded
            purchased.productType = purchasedType
            purchased.productUniqueId = purchaseUniqueId
            purchased.username = username
            purchased.isImageDownloaded = isImageDownloaded
            purchased.downloadTimes = downloadTimes

            try {
                RealmSingleton.getInstance(context).DefaultRealm.executeTransaction {
                    RealmSingleton.getInstance(context).DefaultRealm.copyToRealmOrUpdate(purchased)
                }
//                RealmSingleton.getInstance(context).DefaultRealm.beginTransaction()
//                RealmSingleton.getInstance(context).DefaultRealm.commitTransaction()
                return true
            } catch (exc: Exception) {
                RealmSingleton.getInstance(context).DefaultRealm.cancelTransaction()
            }

            return false
        }

        @JvmStatic
        fun removeById(context: Context,  username: String, id: Int) : Boolean {

            val purchased = RealmSingleton.getInstance(context).DefaultRealm
                    .where(PurchasedModel::class.java).equalTo("username", username).equalTo("id", id).findFirst()

            if (purchased != null) {
                try {
                    RealmSingleton.getInstance(context).DefaultRealm.executeTransaction {
                        purchased.deleteFromRealm()
                    }
//                    RealmSingleton.getInstance(context).DefaultRealm.beginTransaction()
//                    RealmSingleton.getInstance(context).DefaultRealm.commitTransaction()
                    return true
                } catch (exc: Exception) {
                    RealmSingleton.getInstance(context).DefaultRealm.cancelTransaction()
                }
            }

            return false
        }

        @JvmStatic
        fun setIsDownloadedTrue(context: Context,  username: String, productId: String, productType: String) : Boolean {

            val purchased = this.getByProductId(context, username, productType, productId)
            if (purchased != null) {
                try {
                    RealmSingleton.getInstance(context).DefaultRealm.executeTransaction {
                        purchased.isDownloaded = true
                        purchased.isImageDownloaded = true
                        RealmSingleton.getInstance(context).DefaultRealm.copyToRealmOrUpdate(purchased)
                    }
//                    RealmSingleton.getInstance(context).DefaultRealm.beginTransaction()
//                    RealmSingleton.getInstance(context).DefaultRealm.commitTransaction()
                    return true
                } catch (exc: Exception) {
                    RealmSingleton.getInstance(context).DefaultRealm.cancelTransaction()
                }
            }

            return false
        }

        @JvmStatic
        fun setIsLocalDBCreatedTrue(context: Context,  username: String, productId: String, productType: String) : Boolean {

            val purchased = this.getByProductId(context, username, productType, productId)
            if (purchased != null) {
                try {
                    RealmSingleton.getInstance(context).DefaultRealm.executeTransaction {
                        purchased.isLocalDBCreated = true
                        RealmSingleton.getInstance(context).DefaultRealm.copyToRealmOrUpdate(purchased)
                    }
//                    RealmSingleton.getInstance(context).DefaultRealm.beginTransaction()
//                    RealmSingleton.getInstance(context).DefaultRealm.commitTransaction()
                    return true
                } catch (exc: Exception) {
                    RealmSingleton.getInstance(context).DefaultRealm.cancelTransaction()
                }
            }

            return false
        }

        @JvmStatic
        fun isInitialDataDownloaded(context: Context,  username: String, productId: String, productType: String) : Boolean {

            val purchased = this.getByProductId(context, username, productType, productId)
            if (purchased != null)
                if (purchased.isLocalDBCreated) return true

            return false
        }

        @JvmStatic
        fun updateDownloadTimes(context: Context,  username: String, id: Int, newDownloadTimes: Int) : Boolean? {

            val purchased = this.getByUsernameAndId(context, username, id)
            if (purchased != null) {
                try {
                    RealmSingleton.getInstance(context).DefaultRealm.executeTransaction {
                        purchased.downloadTimes = newDownloadTimes
                        RealmSingleton.getInstance(context).DefaultRealm.copyToRealmOrUpdate(purchased)
                    }
//                    RealmSingleton.getInstance(context).DefaultRealm.beginTransaction()
//                    RealmSingleton.getInstance(context).DefaultRealm.commitTransaction()
                    return true
                } catch (exc: Exception) {
                    RealmSingleton.getInstance(context).DefaultRealm.cancelTransaction()
                }
            }

            return false
        }

        @JvmStatic
        fun resetDownloadFlags(context: Context,  username: String, id: Int) : Boolean {

            val purchased = this.getByUsernameAndId(context, username, id)
            if (purchased != null) {
                try {
                    RealmSingleton.getInstance(context).DefaultRealm.executeTransaction {
                        purchased.isDownloaded = false
                        purchased.isImageDownloaded = false
                        purchased.isLocalDBCreated = false
                        RealmSingleton.getInstance(context).DefaultRealm.copyToRealmOrUpdate(purchased)
                    }
//                    RealmSingleton.getInstance(context).DefaultRealm.beginTransaction()
//                    RealmSingleton.getInstance(context).DefaultRealm.commitTransaction()
                    return true
                } catch (exc: Exception) {
//                    RealmSingleton.getInstance(context).DefaultRealm.cancelTransaction()
                }
            }

            return false
        }

        @JvmStatic
        fun getAllPurchased(context: Context, username: String): RealmResults<PurchasedModel>? {
            return RealmSingleton.getInstance(context).DefaultRealm.where(PurchasedModel::class.java)
                    .equalTo("username", username).findAllSorted("created", Sort.DESCENDING)
        }

        @JvmStatic
        fun getAllPurchasedNotIn(context: Context, username: String, ids: Array<Int>): RealmResults<PurchasedModel>? {
            return RealmSingleton.getInstance(context).DefaultRealm.where(PurchasedModel::class.java)
                    .equalTo("username", username).beginGroup().not().`in`("id", ids).endGroup().findAllSorted("created", Sort.DESCENDING)
        }

        @JvmStatic
        fun getByProductId(context: Context, username: String, productType: String, productId: String): PurchasedModel? {
            return RealmSingleton.getInstance(context).DefaultRealm.where(PurchasedModel::class.java)
                    .equalTo("username", username).equalTo("productType", productType).equalTo("productUniqueId", productId).findFirst()
        }

        @JvmStatic
        fun getByUsernameAndId(context: Context, username: String, id: Int): PurchasedModel? {
            return RealmSingleton.getInstance(context).DefaultRealm.where(PurchasedModel::class.java)
                    .equalTo("username", username).equalTo("id", id).findFirst()
        }
    }
}