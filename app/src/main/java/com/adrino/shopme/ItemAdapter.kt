package com.adrino.shopme

import android.util.Log
import com.adrino.shopme.Mobile

private val mobiles = mutableListOf<Mobile>()

fun getMobilesList() {
    mobiles.add(Mobile(1, "Redmi K20", 21000))
    mobiles.add(Mobile(1, "Redmi Note 9 Pro", 15000))
    mobiles.add(Mobile(1, "OnePlus 7", 30000))
    mobiles.add(Mobile(1, "Samsung S20", 79000))
}

fun searchPhone(keyword: String): MutableList<Mobile> {
    if(mobiles.size == 0)
        getMobilesList()
    val resultList = mutableListOf<Mobile>()

    for (mobile in mobiles) {
        if (mobile.getName().startsWith(keyword)) {
            resultList.add(mobile)
        }
        Log.e("SearchRes", "searchPhone" + mobile.getName())
    }
    Log.e("Search", "searchPhone: " + resultList.size)
    return resultList
}