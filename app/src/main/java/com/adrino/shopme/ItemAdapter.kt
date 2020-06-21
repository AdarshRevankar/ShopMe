package com.adrino.shopme

import android.os.StrictMode
import android.util.Log
import com.mysql.jdbc.CommunicationsException
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

/**
 * Manages the SQL Queries
 */
class SQLHandler(
    private val user: String,
    private val password: String,
    private val database: String,
    private val server: String
) {

    private val connection = getConnectionObject(user, password, database, server)

    // Background Process
    fun executeQuery(query: String): MutableList<Mobile>? {
        val mobilesList = mutableListOf<Mobile>()
        val statement = connection?.createStatement()
        try {
            val res = statement?.executeQuery(query)
            while (res != null && res.next())
                mobilesList.add(Mobile(1, res.getString("name"), 2000, res.getString("img_url")))
        } catch (e: CommunicationsException) {
            Log.e("Execution Query Error", "executeQuery: " + e.message)
        }
        return mobilesList
    }

    // Getting connection object
    private fun getConnectionObject(
        user: String,
        password: String,
        database: String,
        server: String
    ): Connection? {
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        var connection: Connection? = null
        val url = "jdbc:mysql://$server:3306/$database"

        try {
            Class.forName("com.mysql.jdbc.Driver")
            connection = DriverManager.getConnection(url, user, password)
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
