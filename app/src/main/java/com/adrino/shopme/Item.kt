package com.adrino.shopme

class Mobile(id: Int, name: String, price: Int, imageUrl: String) {
    private val price: Int = id
    private val name: String = name
    private val id: Int = price
    private val imageUrl: String = imageUrl

    override fun toString(): String {
        return "Mobile(price=$price, name='$name', id=$id)"
    }

    fun getName(): String {
        return name
    }
    fun getImageUrl(): String {
        return imageUrl
    }
}