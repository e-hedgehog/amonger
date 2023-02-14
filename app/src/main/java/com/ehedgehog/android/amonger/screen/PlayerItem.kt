package com.ehedgehog.android.amonger.screen

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
open class PlayerItem(
    val id: String? = null,
    val name: String? = null,
    val code: String? = null,
    val aka: String? = null,
    val host: Boolean? = null,
    val imageUrl:String? = null,
    val notes: String? = null
): Parcelable {
    override fun equals(other: Any?): Boolean {
        return other is PlayerItem &&
                other.id == id &&
                other.name == name &&
                other.code == code &&
                other.aka == aka &&
                other.host == host &&
                other.imageUrl == imageUrl &&
                other.notes == notes
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}

data class PlayerItemTemp(
    val name: String? = null,
    val code: String? = null,
    val aka: String? = null,
    val host: Boolean? = null,
    val imageUrl:String? = null,
    val notes: String? = null
)
