package com.angandroid.appmapsdriver.models

// Model
data class HistoryTripModel(
    var id: String? = null,
    var idClient: String? = null,
    var idDriver: String? = null,
    var origin: String? = null,
    var destination: String? = null,
    var originLat: Double? = null,
    var originLng: Double? = null,
    var destinationLat: Double? = null,
    var destinationLng: Double? = null,
    var time: Double? = null,
    var km: Double? = null,
    var price: Double? = null,
    var ratingToClient: Double? = null,
    var ratingToDriver: Double? = null,
    var timeStamp: Long? = null
)
