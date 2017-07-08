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
    UnKnown(0),
    Refresh(1000);

    companion object Factory {
        fun toType(item: Int): HTTPErrorType {
            when (item) {
                in 200..209 -> return Success
                400 -> return BadRequest
                401 -> return UnAuthorized
                403 -> return ForbiddenAccess
                404 -> return NotFound
                500 -> return ServerInternalError
                1000 -> return Refresh
                else -> return UnKnown
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
            HTTPErrorType.Refresh -> "Refresh"
        }

}