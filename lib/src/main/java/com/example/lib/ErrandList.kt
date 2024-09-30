package com.example.lib

class ErrandList(var name: String) {
    var errands = mutableListOf<Errand>()

    fun add(errand: Errand) {
        errands.add(errand)
    }
    fun sort() {
        errands.sort()
    }
    fun size(): Int {
        return errands.size
    }

    override fun toString(): String {
        var errandInfo: String = "$name:\n"
        for (errand in errands) {
            errandInfo += "$errand\n"
        }
        return errandInfo
    }
}