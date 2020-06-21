package com.adrino.shopme

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mancj.materialsearchbar.MaterialSearchBar
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(), RecyclerViewAdapter.ItemClickListener {
    private var sqlHandler: SQLHandler? = null
    private var recyclerView: RecyclerView? = null
    private var adapter: RecyclerViewAdapter? = null
    private var viewItemsList = mutableListOf<Mobile>()
    private var speechRecognizer: SpeechRecognizer? = null
    private var mSpeechRecognizerIntent: Intent? = null
    private var searchBar: MaterialSearchBar? = null
    private var handler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        handler = Handler()
        // View - View Items
        searchBar = findViewById(R.id.searchBar)
        recyclerView = findViewById(R.id.recyclerView)

        // Init - Search Bar
        searchBar?.setOnSearchActionListener(queryListener)

        // Init - Speech Recognizer
        initSpeechRecognizer()

        // Init - RecyclerView
        initRecyclerView()

        startRepeatingTask()

        // SQL Handler
        Thread {
            sqlHandler = SQLHandler(
                resources.getString(R.string.db_user),
                resources.getString(R.string.db_password),
                resources.getString(R.string.db_database),
                resources.getString(R.string.db_server)
            )
        }.start()
    }

    /**
     * ===================================================================
     * Init - Methods
     * ===================================================================
     */
    var mInternetStatusCheck: Runnable? = object : Runnable {
        @RequiresApi(Build.VERSION_CODES.M)
        override fun run() {
            if (!hasActiveInternetConnection()) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "No Internet", Toast.LENGTH_LONG).show()
                }
            }
            handler?.postDelayed(this, 5000)
        }
    }

    fun startRepeatingTask() {
        mInternetStatusCheck!!.run()
    }

    fun stopRepeatingTask() {
        handler?.removeCallbacks(mInternetStatusCheck)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRepeatingTask()
    }

    private fun initRecyclerView() {
        recyclerView?.layoutManager =
            GridLayoutManager(this, resources.getInteger(R.integer.ui_rv_col_count))
        adapter = RecyclerViewAdapter(this, viewItemsList)
        adapter?.setClickListener(this)
        recyclerView?.adapter = adapter
    }

    private fun initSpeechRecognizer() {
        speechRecognizer?.setRecognitionListener(recognitionListener)
        checkPermission()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this@MainActivity)
        mSpeechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        mSpeechRecognizerIntent?.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        mSpeechRecognizerIntent?.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            Locale.getDefault()
        )
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun hasActiveInternetConnection(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                return true
            }
        }
        return false
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
            // Mic - Button is Clicked
            when (buttonCode) {
                MaterialSearchBar.BUTTON_SPEECH -> speechRecognizer?.startListening(
                    mSpeechRecognizerIntent
                )
                MaterialSearchBar.BUTTON_BACK -> {
                    speechRecognizer?.stopListening()
                    searchKeyAndUpdateView("")
                }
            }
        }

        override fun onSearchStateChanged(enabled: Boolean) {
            speechRecognizer?.stopListening()
            searchKeyAndUpdateView("")
            Log.e("TAG", "search state: $enabled")
        }

        override fun onSearchConfirmed(text: CharSequence?) {
            searchKeyAndUpdateView(text.toString())
        }
    }

    private fun searchKeyAndUpdateView(searchKey: String): Boolean {
        Thread {
            val mobileList =
                sqlHandler?.executeQuery("SELECT name, img_url FROM characters where name like '%$searchKey%'")
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
        viewItemsList.addAll(mobileList)

        // Update the Recycler View
        runOnUiThread { adapter?.notifyDataSetChanged() }
    }

    private val recognitionListener = object : RecognitionListener {
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
            //displaying the first match
            if (matches != null) searchBar?.text = matches[0]
        }

        override fun onPartialResults(bundle: Bundle) {}
        override fun onEvent(i: Int, bundle: Bundle) {}
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