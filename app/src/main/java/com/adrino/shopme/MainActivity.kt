package com.adrino.shopme

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class MainActivity : AppCompatActivity(), RecyclerViewAdapter.ItemClickListener{
    private var sqlHandler: SQLHandler? = null
    private var recyclerView: RecyclerView? = null
    private var adapter: RecyclerViewAdapter? = null
    var animalNames = ArrayList<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val searchView = findViewById<SearchView>(R.id.search)
        searchView.setOnQueryTextListener(queryListener)

        sqlHandler = SQLHandler(
            resources.getString(R.string.db_user),
            resources.getString(R.string.db_password),
            resources.getString(R.string.db_database),
            resources.getString(R.string.db_server)
        )

        animalNames.add("Akash")
        animalNames.add("Akash")
        animalNames.add("Akash")
        animalNames.add("Akash")

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView?.setLayoutManager(LinearLayoutManager(this));
        adapter = RecyclerViewAdapter(this, animalNames);
        adapter?.setClickListener(this)
        recyclerView?.setAdapter(adapter);
    }

    /**
     * Search Query Listener
     */
    private val queryListener = object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            // When OK is pressed
            if (query != null) {

                Thread {
                    val mobileList =
                        sqlHandler?.executeQuery("SELECT name FROM characters where name like '%$query%'")
                    updateRecyclerView(mobileList)
                }.start()
                return true
            }
            return false
        }

        override fun onQueryTextChange(query: String?): Boolean {
            // On Each Key-press
            if (query != null) {
                Thread {
                    val mobileList =
                        sqlHandler?.executeQuery("SELECT name FROM characters where name like '%$query%'")
                    updateRecyclerView(mobileList)
                }.start()
                return true
            }
            return false
        }
    }

    fun updateRecyclerView(mobileList: MutableList<Mobile>?){
        animalNames.clear()
        if (mobileList == null)
            return

        for (m in mobileList)
            animalNames.add(m.toString())
        runOnUiThread {adapter?.notifyDataSetChanged()}
    }

    override fun onItemClick(view: View?, position: Int) {
        TODO("Not yet implemented")
    }
}