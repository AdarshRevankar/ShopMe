package com.adrino.shopme

import android.os.AsyncTask
import android.os.StrictMode
import android.util.Log
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException


private val mobiles = mutableListOf<Mobile>()

fun getMobilesList() {
    mobiles.add(Mobile(1, "Redmi K20", 21000))
    mobiles.add(Mobile(1, "Redmi Note 9 Pro", 15000))
    mobiles.add(Mobile(1, "OnePlus 7", 30000))
    mobiles.add(Mobile(1, "Samsung S20", 79000))
}

fun searchPhone(keyword: String): MutableList<Mobile> {
    if (mobiles.size == 0)
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

/**
 * Manages the SQL Queries
 */
class SQLHandler(
    private val user: String, private val password: String, private val database: String,
    private val server: String
) :
    AsyncTask<String, String, String>() {
    private val connection = getConnectionObject(user, password, database, server)
    public val mobilesList = mutableListOf<Mobile>()

    // Background Process
    override fun doInBackground(vararg query: String?): String? {
        val statement = connection?.createStatement()
        val res = statement?.executeQuery(query[0].toString())
        while (res != null && res.next())
            mobilesList.add(Mobile(1, res.getString("name"), 2000))
        return null
    }


    // Getting connection object
    private fun getConnectionObject(
        user: String,
        password: String,
        database: String,
        server: String
    ): Connection? {
        val policy =
            StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        var connection: Connection? = null

        try {
            Class.forName("com.mysql.jdbc.Driver")
            connection =
                DriverManager.getConnection("jdbc:mysql://$server:3306/$database", user, password)
        } catch (se: SQLException) {
            Log.e("SQL Execution Error : ", se.message)
        } catch (e: ClassNotFoundException) {
            Log.e("Class Not found Error: ", e.message)
        } catch (e: Exception) {
            Log.e("Unknown Error: ", e.message)
        }
        return connection
    }
}
