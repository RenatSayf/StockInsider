package com.renatsayf.stockinsider.utils


//TODO Sending events between Activities/Fragments: Step 1 - create this class, next step in com.renatsayf.stockinsider.ui.dialogs.SearchListDialog.kt
@Suppress("RedundantSetter")
open class Event<out T>(private val content: T?) {
    var hasBeenHandled = false
        private set(value) // Allow external read but not write
        {
            field = value
        }

    /**
     * Returns the content and prevents its use again.
     */
    fun getContent(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    /**
     * Returns the content, even if it's already been handled.
     */
    fun peekContent(): T? = content
}