package com.test.workmanagersample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.work.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    companion object{
        const val KEY_COUNT_VALUE = "key_valuer"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button:Button = findViewById(R.id.btn_click)
        button.setOnClickListener {
//            setOneTimeWorkRequest()
            setPeriodicWorkRequest()
        }

    }

    private fun setPeriodicWorkRequest() {
        val periodicWorkRequest = PeriodicWorkRequest.Builder(DownloadingWorker::class.java, 16, TimeUnit.MINUTES).build()

        WorkManager.getInstance(applicationContext).enqueue(periodicWorkRequest)
    }

    private fun setOneTimeWorkRequest(){

        val tv = findViewById<TextView>(R.id.tv_asa)
        val workManager = WorkManager.getInstance(applicationContext)

        val data: Data = Data.Builder()
            .putInt(KEY_COUNT_VALUE, 125)
            .build()
        val constraints = Constraints.Builder()
            .setRequiresCharging(true)
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val uploadRequest = OneTimeWorkRequest.Builder(UploadWorker::class.java)
            .setConstraints(constraints)
            .setInputData(data)
            .build()

         val filteringRequest = OneTimeWorkRequest.Builder(FilteringWorker::class.java)
             .build()
        val compressingRequest = OneTimeWorkRequest.Builder(CompressingWorker::class.java)
            .build()
        val downloadWorker = OneTimeWorkRequest.Builder(DownloadingWorker::class.java)
            .build()
        val parallelWorks = mutableListOf<OneTimeWorkRequest>()
        parallelWorks.add(downloadWorker)
        parallelWorks.add(filteringRequest)

        workManager
            .beginWith(parallelWorks )
            .then(compressingRequest)
            .then(uploadRequest)
            .enqueue()
        workManager.getWorkInfoByIdLiveData(uploadRequest.id)
            .observe(this, Observer {
                tv.text = it.state.name
                if (it.state.isFinished){
                    val data = it.outputData
                    val message = data.getString(UploadWorker.KEY_WORKER)
                    Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
                }
            })

    }
}