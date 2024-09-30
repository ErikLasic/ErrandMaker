package com.example.lib

import io.github.serpro69.kfaker.Faker
import java.util.*

fun generate(iterator: Int): ErrandList {


    val cal: Calendar = Calendar.getInstance()
    cal.set(Calendar.YEAR, 2001)
    cal.set(Calendar.MONTH, Calendar.FEBRUARY)
    cal.set(Calendar.DAY_OF_MONTH, 1)

    Date(2001,2,23)

    val faker = Faker()
    val opravki = ErrandList("Opravki")
    for (i in 1..iterator) {
        opravki.add(Errand(faker.game.title(), faker.cat.name(), cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH)+1, cal.get(Calendar.YEAR), 46.55145455, 15.651183907952792))
    }
    return opravki
}


fun main() {
    val opravki = generate(10)
    opravki.sort()
    println(opravki.toString())
}