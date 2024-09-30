package com.example.lib

import java.util.*

class Errand(var title: String, var description: String, var day: Int, var month: Int, var year: Int, var latitude: Double, var longitude: Double): Comparable<Errand> {
    var id:String = UUID.randomUUID().toString().replace("-", "")

    override fun compareTo(other: Errand): Int {
        return id.compareTo(other.id)
    }

    override fun toString(): String {
        return "$id $title $description $day.$month.$year $latitude $longitude"
    }
}