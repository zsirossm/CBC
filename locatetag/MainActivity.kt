package com.locatetag

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    var granted = arrayOf<Int>(0, 0, 0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /*
        Intent(this, FindSmartTag::class.java).also { intent ->
            startService(intent)
        }
        */

        //CANCEL ALL PREVIOUS WORK WITH THE TAG
        val tag = "SmartTag"
        WorkManager.getInstance(this).cancelAllWorkByTag(tag)

        //CHECK FOR LOCATION PERMISSION - REQUEST LOCATION PERMISSION WHEN NECESSARY
        var fineGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        var courseGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (fineGranted == 0 || courseGranted == 0) {
            if(isBleSupported(this)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    var scanGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                    if (scanGranted == 0) {
                        if (BluetoothAdapter.getDefaultAdapter().isEnabled) { startPeriodicScan(); }
                        else {
                            //START REQUEST TO ENABLE BLUETOOTH
                            show("Tacticull Requirement", "Tacticull requires bluetooth enabled for locating company equipment. Select allow when prompted.",2);
                        }
                    }
                    else {
                        //START REQUEST TO ENABLE BLUETOOTH SCAN
                        show("Tacticull Requirement", "Tacticull requires the ability to scan for nearby BLE devices. Select allow when prompted.",1);
                    }
                }
                else {
                    if (BluetoothAdapter.getDefaultAdapter().isEnabled) { startPeriodicScan(); }
                    else {
                        show("Tacticull Requirement", "Tacticull requires bluetooth enabled for locating company equipment. Select allow when prompted.",2);
                    }
                }
            }
        }
        else {
            show("Tacticull Requirement","Tacticull requires your location to show work location(s) and to find equipment. Select allow when prompted or change location permission in the Tacticull app.",0)
        }


    }

    //CHECK DEVICE SUPPORT FOR BLUETOOTH
    fun isBleSupported(context: Context): Boolean {
        return BluetoothAdapter.getDefaultAdapter() != null && context.packageManager.hasSystemFeature(
            PackageManager.FEATURE_BLUETOOTH_LE
        );
    }

    //START WORK MANAGER
    fun startPeriodicScan() {
        Log.e("TACTICULL","PERIODIC SCAN CALLED")
        val tag = "SmartTag"
        val findWorkRequest: WorkRequest = PeriodicWorkRequestBuilder<WorkerFindTag>(15,TimeUnit.MINUTES).addTag(tag).build()
        WorkManager.getInstance(this).enqueue(findWorkRequest)
    }

    //RESULT OF BLUETOOTH ENABLE
    var activityResultLauncher = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            startPeriodicScan();
        } else { show("Tacticull Requirement", "Tacticull requires bluetooth to be enabled to find company equipment. Enable bluetooth in settings.",3) }
    }


    //RESULT OF LOCATION PERMISSION REQUEST
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
       Log.e("TACTICULL", "PERMISSION " + permissions[0])
       var permission = permissions[0]

        when(permission) {
            "android.permission.ACCESS_FINE_LOCATION" -> {
                Log.e("TACTICULL", "GRANTED " + grantResults[0])
                granted[0] = grantResults[0];
                if (granted[0] == 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if(isBleSupported(this)) {
                            var scanGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                            //CHECK SCAN ENABLED - REQUEST WHEN NEEDED AND SHOW DIALOG FOR REASON
                            if(scanGranted == 0) {
                                if(!BluetoothAdapter.getDefaultAdapter().isEnabled) {
                                    show("Tacticull Requirement", "Tacticull requires bluetooth enabled for locating company equipment. Select allow when prompted.",2);
                                }
                                else {
                                    startPeriodicScan();
                                }
                            }
                            else {
                                show("Tacticull Requirement", "Tacticull requires the ability to scan for nearby BLE devices. Select allow when prompted.",1);
                            }
                        }
                    }
                    else {
                        //CHECK BLUETOOTH ENABLED - REQUEST WHEN NEEDED AND SHOW DIALOG FOR REASON
                        if(!BluetoothAdapter.getDefaultAdapter().isEnabled) {
                            show("Tacticull Requirement", "Tacticull requires bluetooth enabled for locating company equipment. Select allow when prompted.",2);
                        } else { startPeriodicScan(); }
                    }
                } else {

                }



            }
            "android.permission.BLUETOOTH_SCAN" -> {
                Log.e("TACTICULL SCAN RESULTS", grantResults[0].toString() + " " + grantResults[1].toString())
                if (grantResults[0] == 0) {
                    if(!BluetoothAdapter.getDefaultAdapter().isEnabled) {
                        show("Tacticull Requirement", "Tacticull requires bluetooth enabled for locating company equipment. Select allow when prompted.",2);
                    } else { startPeriodicScan(); }
                }
            }
       }
       super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    fun show(title: String, message: String, action: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setIcon(getResources().getDrawable(R.drawable.tacticulli))
        builder.setPositiveButton("Ok") { dialog, which ->

            if (action == 0) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    ActivityCompat.requestPermissions(
                        this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_BACKGROUND_LOCATION), 1
                    )
                }
                else {
                    ActivityCompat.requestPermissions(
                        this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION), 1
                    )
                }
            }
            else if (action == 1) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT), 1
                )
            }
            else if (action == 2) {
                val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                activityResultLauncher.launch(intent)
            } else {

            }

        }
        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false)
        alertDialog.show()
    }




}