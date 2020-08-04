package com.dan.school.models

import android.os.Parcel
import android.os.Parcelable

data class Subtask(var title: String, var done: Boolean) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readByte() != 0.toByte()
    )

    constructor()
        : this("", false)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeByte(if (done) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Subtask> {
        override fun createFromParcel(parcel: Parcel): Subtask {
            return Subtask(parcel)
        }

        override fun newArray(size: Int): Array<Subtask?> {
            return arrayOfNulls(size)
        }
    }
}