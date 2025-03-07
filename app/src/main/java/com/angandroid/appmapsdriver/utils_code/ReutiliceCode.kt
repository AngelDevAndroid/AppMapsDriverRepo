package com.angandroid.appmapsdriver.utils_code

import android.content.Context
import android.widget.Toast

object ReutiliceCode {
    // Toast
    fun msgToast(cxt: Context, msg: String, duration: Boolean) {

        var auxDuration = Toast.LENGTH_SHORT
        if (duration) {
            auxDuration = Toast.LENGTH_LONG
        }
        Toast.makeText(cxt, msg, auxDuration).show()
    }
}