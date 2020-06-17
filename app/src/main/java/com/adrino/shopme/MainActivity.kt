package com.adrino.shopme

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.widget.SearchView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val searchView = findViewById<SearchView>(R.id.search)
        searchView.setOnQueryTextListener(queryListener)

        val sqlHandler = SQLHandler(
            resources.getString(R.string.db_user),
            resources.getString(R.string.db_password),
            resources.getString(R.string.db_database),
            resources.getString(R.string.db_server)
        )

        sqlHandler.execute("SELECT name FROM characters")
    }

    /**
     * Search Query Listener
     */
    private val queryListener = object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            // When OK is pressed
            return false
        }

        override fun onQueryTextChange(query: String?): Boolean {
            // On Each Key-press
            if (query != null) {
                val resultList = searchPhone(query)
                var resultString = ""
                for (mobile in resultList)
                    resultString += mobile.getName() + " "
                Log.e("String", "onQueryTextChange: "+resultString )
                runOnUiThread(
                    Runnable { findViewById<TextView>(R.id.mobileTextView).setText(resultString) }
                )
                return true
            }
            return false
        }
    }
}