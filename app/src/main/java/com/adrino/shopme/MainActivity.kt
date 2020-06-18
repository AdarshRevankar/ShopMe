package com.adrino.shopme

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mancj.materialsearchbar.MaterialSearchBar
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(), RecyclerViewAdapter.ItemClickListener {
    private var sqlHandler: SQLHandler? = null
    private var recyclerView: RecyclerView? = null
    private var adapter: RecyclerViewAdapter? = null
    var viewItemsList = ArrayList<String>()
    var speechRecognizer: SpeechRecognizer? = null
    var mSpeechRecognizerIntent: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // View Items
        val searchBar = findViewById<MaterialSearchBar>(R.id.searchBar)
        recyclerView = findViewById(R.id.recyclerView);

        // SQL Handler
        sqlHandler = SQLHandler(
            resources.getString(R.string.db_user),
            resources.getString(R.string.db_password),
            resources.getString(R.string.db_database),
            resources.getString(R.string.db_server)
        )

        // Add Search-bar Listener
        searchBar.setOnSearchActionListener(queryListener)
//        searchBar.inflateMenu(R.menu.main);
//        searchBar.getMenu().setOnMenuItemClickListener(this);

        // Recycler View Initialize
        recyclerView?.layoutManager =
            GridLayoutManager(this, resources.getInteger(R.integer.ui_rv_col_count))
        adapter = RecyclerViewAdapter(this, viewItemsList)
        adapter?.setClickListener(this)
        recyclerView?.adapter = adapter


        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this@MainActivity)
        checkPermission()
        mSpeechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        mSpeechRecognizerIntent?.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mSpeechRecognizerIntent?.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
            Locale.getDefault());
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(bundle: Bundle) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(v: Float) {}
            override fun onBufferReceived(bytes: ByteArray) {}
            override fun onEndOfSpeech() {}
            override fun onError(i: Int) {}
            override fun onResults(bundle: Bundle) {
                //getting all the matches
                val matches = bundle
                    .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                Log.e("TAG", "onResults: " )
                //displaying the first match
                if (matches != null) searchBar.setText(matches[0])
            }

            override fun onPartialResults(bundle: Bundle) {}
            override fun onEvent(i: Int, bundle: Bundle) {}
        })
    }

    /**
     * ===================================================================
     * OnClick - RecyclerView Item Click Listener
     * ===================================================================
     */
    override fun onItemClick(view: View?, position: Int) {

    }

    /**
     * ===================================================================
     * Search Listener - Search Query Listener
     * ===================================================================
     */
    private val queryListener = object : MaterialSearchBar.OnSearchActionListener {
        override fun onButtonClicked(buttonCode: Int) {
            speechRecognizer?.startListening(mSpeechRecognizerIntent);
            Log.e("TAG", "button clicked: $buttonCode" )
        }

        override fun onSearchStateChanged(enabled: Boolean) {
//            speechRecognizer?.stopListening();
            searchKeyAndUpdateView("")
            Log.e("TAG", "search state: $enabled" )
        }

        override fun onSearchConfirmed(text: CharSequence?) {
            searchKeyAndUpdateView(text.toString())
        }
    }

//    override fun onQueryTextSubmit(key: String?): Boolean {
//        // When OK is pressed
//        if (key != null) {
//            return searchKeyAndUpdateView(key)
//        }
//        return false
//    }
//
//    override fun onQueryTextChange(key: String?): Boolean {
//        // On Each Key-press
//        if (key != null) {
//            return searchKeyAndUpdateView(key)
//        }
//        return false
//    }

    private fun searchKeyAndUpdateView(searchKey: String): Boolean {
        Thread {
            val mobileList =
                sqlHandler?.executeQuery("SELECT name FROM characters where name like '%$searchKey%'")
            updateRecyclerView(mobileList)
        }.start()
        return true
    }

    private fun updateRecyclerView(mobileList: MutableList<Mobile>?) {
        // if list is empty return
        if (mobileList == null)
            return

        // Clear the Items
        viewItemsList.clear()
        for (m in mobileList)
            viewItemsList.add(m.toString())

        // Update the Recycler View
        runOnUiThread { adapter?.notifyDataSetChanged() }
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
                finish()
            }
        }
    }
}