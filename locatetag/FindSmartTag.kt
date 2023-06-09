package com.locatetag

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import java.util.TimerTask

class FindSmartTag: Service() {

    override fun onBind(p0: Intent?): IBinder? {
        return null;
    }

    override fun onCreate() {
        super.onCreate()
        Log.e("TACTICULL", "SERVICE HAS ENTERED ONCREATE");
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e("TACTICULL", "SERVICE HAS ENTERED ONSTARTCOMMAND")
        //Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show()
        return super.onStartCommand(intent, flags, startId)
    }


}