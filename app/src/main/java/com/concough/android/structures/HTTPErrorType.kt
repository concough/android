package com.concough.android.structures

import retrofit2.HttpException

/**
 * Created by abolfazl on 7/2/17.
 */
enum class HTTPErrorType(val code: Int) {
    Success(200),
    BadRequest(400),
    UnAuthorized(401),
    ForbiddenAccess(403),
    NotFound(404),
    ServerInternalError(500),
    UnKnown(0);

    companion object Factory {
        fun toType(item: Int): HTTPErrorType {
            when (item) {
                in 200..209 -> return HTTPErrorType.Success
                400 -> return HTTPErrorType.BadRequest
                401 -> return HTTPErrorType.UnAuthorized
                403 -> return HTTPErrorType.ForbiddenAccess
                404 -> return HTTPErrorType.NotFound
                500 -> return HTTPErrorType.ServerInternalError
                else -> return HTTPErrorType.UnKnown
            }
        }

        fun toType(error: Throwable): HTTPErrorType {
            if (error is HttpException) {
                return toType(error.code())
            }
            return UnKnown
        }
    }
    override fun toString(): String =
        when(this) {
            HTTPErrorType.Success -> "Success"
            HTTPErrorType.BadRequest -> "BadRequest"
            HTTPErrorType.ForbiddenAccess -> "ForbiddenAccess"
            HTTPErrorType.UnAuthorized -> "UnAuthorized"
            HTTPErrorType.NotFound -> "NotFound"
            HTTPErrorType.ServerInternalError -> "ServerInternalError"
            HTTPErrorType.UnKnown -> "UnKnown"
        }

}