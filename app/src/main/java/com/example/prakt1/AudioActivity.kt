package com.example.prakt1

//import android.support.v4.app.ActivityCompat
//import android.support.v7.app.AppCompatActivity
//import android.widget.Toolbar

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.prakt1.MainActivity
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


//import kotlin.coroutines.jvm.internal.CompletedContinuation.context

private const val LOG_TAG = "AudioRecordTest"
private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
var file: File? = null

class AudioActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private var fileName: String = ""

    private var recordButton: RecordButton? = null
    private var recorder: MediaRecorder? = null

    private var playButton: PlayButton? = null
    private var player: MediaPlayer? = null

    //private var toolbar: Toolbar0? = null


    // Requesting permission to RECORD_AUDIO
    private var permissionToRecordAccepted = false
    private var permissions: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO)

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionToRecordAccepted = if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        } else {
            false
        }
        if (!permissionToRecordAccepted) finish()
    }

    private fun onRecord(start: Boolean) = if (start) {
        startRecording()
    } else {
        stopRecording()
    }

    private fun onPlay(start: Boolean) = if (start) {
        startPlaying()
    } else {
        stopPlaying()
    }

    private fun startPlaying() {
        player = MediaPlayer().apply {
            try {
                setDataSource(fileName)
                prepare()
                start()
            } catch (e: IOException) {
                Log.e(LOG_TAG, "prepare() failed")
            }
        }
    }

    private fun stopPlaying() {
        player?.release()
        player = null
    }

    private fun startRecording() {
        val timeWhenRecorded = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val audioFileName = "audio_$timeWhenRecorded.3gp"



        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(Environment.getExternalStorageDirectory().absolutePath + "/" + audioFileName)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()
            } catch (e: IOException) {
                Log.e(LOG_TAG, "prepare() failed")
            }

            start()
        }
    }

    private fun stopRecording() {
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
    }

    internal inner class RecordButton(ctx: Context) : androidx.appcompat.widget.AppCompatButton(ctx) {

        var mStartRecording = true

        var clicker: OnClickListener = OnClickListener {
            onRecord(mStartRecording)
            text = when (mStartRecording) {
                true -> "Stop recording"
                false -> "Start recording"
            }
            mStartRecording = !mStartRecording
        }

        init {
            text = "Start recording"
            setOnClickListener(clicker)
        }
    }

    internal inner class PlayButton(ctx: Context) : androidx.appcompat.widget.AppCompatButton(ctx) {
        var mStartPlaying = true
        var clicker: OnClickListener = OnClickListener {
            onPlay(mStartPlaying)
            text = when (mStartPlaying) {
                true -> "Stop playing"
                false -> "Start playing"
            }
            mStartPlaying = !mStartPlaying
        }

        init {
            text = "Start playing"
            setOnClickListener(clicker)
        }
    }




    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        setContentView(R.layout.activity_audio)

        //change menu title
        this.title = "Audio"
        //val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        //recyclerView.layoutManager = LinearLayoutManager(this)


        // Record to the external cache directory for visibility
        fileName = "${externalCacheDir?.absolutePath}/audiorecordtest.3gp"

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)

        recordButton = RecordButton(this)
        playButton = PlayButton(this)


        val ll = LinearLayout(this).apply {
            addView(
                recordButton,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    0f
                )
            )
            addView(
                playButton,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    0f
                )
            )
        }

        setContentView(ll)
        setupToolbar(ll)


        //215 rinda KRAŠO. Itkā būtu devis nepareizo ID priekš recyclerView un tāpēc tur null. nevaru atrisināt problēmu
        //recyclerView = findViewById(R.id.recyclerView)
        //recyclerView.layoutManager = LinearLayoutManager(this)
        val files = getListOfFiles()
        Log.d("hi", files.toString())

        val fileItems = files.map { FileItem(it.name) }
        val adapter = FileAdapter(fileItems)
        recyclerView.adapter = adapter




    }
    private fun getListOfFiles(): List<File> {
        val directory = File(Environment.getExternalStorageDirectory().absolutePath)
        return directory.listFiles().toList()
    }


    private fun setupToolbar(linearLayout: LinearLayout) {
        val toolbar = Toolbar(this)

        val constraintLayoutParams = ConstraintLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        toolbar.layoutParams = constraintLayoutParams


        toolbar.setNavigationOnClickListener {
            Toast.makeText(
                applicationContext,
                "Navigation icon was clicked",
                Toast.LENGTH_SHORT
            ).show()
        }
        //toolbar.setBackgroundResource(R.color.purple_500)
        linearLayout.addView(toolbar)

        //setSupportActionBar(toolbar)
    }



    override fun onStop() {
        super.onStop()
        recorder?.release()
        recorder = null
        player?.release()
        player = null
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(0, 0, 0, "Camera")
        menu.add(0, 1, 0, "Delete records")
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == 0) {
            val intentAudio = Intent(this@AudioActivity, MainActivity::class.java)
            startActivity(intentAudio)
        }
        return super.onOptionsItemSelected(item)
    }
}