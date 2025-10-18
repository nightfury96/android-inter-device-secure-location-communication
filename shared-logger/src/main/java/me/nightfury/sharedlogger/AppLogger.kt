package me.nightfury.sharedlogger

object AppLogger {
    fun d(tag: String, message: String) {
        android.util.Log.d(tag, message)
    }

    fun i(tag: String, message: String) {
        android.util.Log.i(tag, message)
    }

    fun w(tag: String, message: String) {
        android.util.Log.w(tag, message)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            android.util.Log.e(tag, message, throwable)
        } else {
            android.util.Log.e(
                tag,
                message
            )
        }
    }
}