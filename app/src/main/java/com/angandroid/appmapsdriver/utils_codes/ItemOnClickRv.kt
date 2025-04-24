package com.angandroid.appmapsdriver.utils_codes

import com.angandroid.appmapsdriver.models.HistoryTripModel

// Onclick
interface ItemOnClickRv {
    fun onClick(position: Int, model: HistoryTripModel)
}