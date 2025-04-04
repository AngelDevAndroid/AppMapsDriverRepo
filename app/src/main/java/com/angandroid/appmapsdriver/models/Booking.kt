package com.angandroid.appmapsdriver.models

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

// Model
data class Booking(
    var id: String? = null,
    var idClient: String? = null,
    var idDriver: String? = null,
    var origin: String? = null,
    var destination: String? = null,
    var status: String? = null,
    var time: Double? = null,
    var km: Double? = null,
    var originLat: Double? = null,
    var originLng: Double? = null,
    var destinationLat: Double? = null,
    var destinationLng: Double? = null,
    var price: Double? = null

): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readValue(Double::class.java.classLoader) as? Double
    )

    override fun describeContents(): Int {
        TODO("Not yet implemented")
    }

    override fun writeToParcel(p0: Parcel, p1: Int) {
        TODO("Not yet implemented")
    }

    companion object CREATOR : Parcelable.Creator<Booking> {
        override fun createFromParcel(parcel: Parcel): Booking {
            return Booking(parcel)
        }

        override fun newArray(size: Int): Array<Booking?> {
            return arrayOfNulls(size)
        }
    }
}
