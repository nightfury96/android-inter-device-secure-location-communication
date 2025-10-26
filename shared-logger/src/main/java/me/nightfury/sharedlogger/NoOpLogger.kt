package me.nightfury.sharedlogger

/***
 * Use for inject in test classes
 */
class NoOpLogger : Logger {
    override fun d(tag: String, message: String) = Unit

    override fun i(tag: String, message: String) = Unit

    override fun w(tag: String, message: String) = Unit

    override fun e(tag: String, message: String, throwable: Throwable?) = Unit
}