package com.locatetag

import android.Manifest
import android.bluetooth.*
import android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.util.*


class WorkerFindTag(context: Context, params: WorkerParameters): Worker(context,params) {

    var contextWorker: Context = context;
    lateinit var bluetoothAdapter: BluetoothAdapter;
    var bluetoothGatt: BluetoothGatt? = null
    var scannedDevices: MutableSet<BluetoothDevice> = mutableSetOf()
    override fun doWork(): Result {
        Log.e("TACTICULL","DO WORK")
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        //CHECK PERMISSIONS BEFORE STARTING SCAN
        var fineGranted = ContextCompat.checkSelfPermission(contextWorker, Manifest.permission.ACCESS_FINE_LOCATION)
        var courseGranted = ContextCompat.checkSelfPermission(contextWorker, Manifest.permission.ACCESS_COARSE_LOCATION)
        var scanGranted = ContextCompat.checkSelfPermission(contextWorker, Manifest.permission.BLUETOOTH_SCAN)
        var enabled = BluetoothAdapter.getDefaultAdapter().isEnabled

        Log.e("TACTICULL","WORKER STARTED fine: "+ fineGranted + " course: " + courseGranted + " scan: " + scanGranted + " enabled: "+ enabled);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (fineGranted == 0 && courseGranted == 0 && scanGranted == 0 && enabled) { scanLeDevice(); }
        }
        else {
            if (fineGranted == 0 && courseGranted == 0 && enabled) {  scanLeDevice(); }
        }
        return Result.success()
    }

    //FOR LOW ENERGY DEVICES
    private fun scanLeDevice() {
        try {
            val bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
            val parcelUuid = ParcelUuid(UUID.fromString("0000fd5a-0000-1000-8000-00805f9b34fb"))
            val uuid = ScanFilter.Builder().setServiceUuid(parcelUuid).build();
            var filter = listOf<ScanFilter>(uuid)
            val settings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()

            when {
                ContextCompat.checkSelfPermission(
                    contextWorker,
                    Manifest.permission.BLUETOOTH_SCAN
                ) == PackageManager.PERMISSION_GRANTED -> { } else -> { }
            }
            Log.e("TACTICULL START", "START");
            bluetoothLeScanner.startScan(filter,settings,leScanCallback)

            Handler(Looper.getMainLooper()).postDelayed({
                Log.e("TACTICULL STOP", "STOP");
                bluetoothLeScanner.stopScan(leScanCallback)
                scannedDevices.forEach{ it.connectGatt(contextWorker, false, bluetoothGattCallback)}
                Log.e("TACTICULL","GATT CONNECTION ATTEMPTED")
            }, 5000)
        }
        catch(e: Exception) { }
    }

    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            Log.e("TACTICULL","IN SCAN");
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                //return
            }

            if (result.device.name != null) {
                val name = result.device.name
                val device = result.device
                if (name.contains("Smart Tag")) {
                        try {
                            scannedDevices.add(device)
                            //bluetoothGatt = device.connectGatt(contextWorker, false, bluetoothGattCallback)
                            //Log.e("TACTICULL","GATT CONNECTION ATTEMPTED " + contextWorker)
                        }
                        catch(e: Exception) { Log.e("TACTICULL","ERROR " + e); }
                    Log.e("TACTICULL", "EQUIPMENT LIST")
                }
            }

            Log.e("TACTICULL","TESTING LOW DEVICE: " + result.device.name + " " + result.device.address + " Class " + result.device.bluetoothClass + " " + result.device.bondState + " UUID " + result.scanRecord?.serviceUuids);
            //GET BLE DEVICES WITH NAME THAT CONTAINS TACTICULL
        }
    }

    private val bluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.e("TACTICULL","CONNECTED")
                if (ActivityCompat.checkSelfPermission(
                        contextWorker,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    //return
                }
                gatt?.discoverServices()
            }
            else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.e("TACTICULL","DISCONNECTED")
            }
            else {
                Log.e("TACTICULL","ELSE")
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            Log.e("TACTICULL","SERVICES DISCOVERED")
            val x = gatt?.services
            //val c = x?.get(0)?.characteristics?.get(2)
            Log.e("TACTICULL","SERVICES DISCOVERED " + x?.size)
            Log.e("TACTICULL","SERVICES DISCOVERED #1 " + x?.get(0)?.characteristics?.size)
            Log.e("TACTICULL","SERVICES DISCOVERED #2 " + x?.get(1)?.characteristics?.size)
            Log.e("TACTICULL","SERVICES DISCOVERED #3 " + x?.get(2)?.characteristics?.size)
            Log.e("TACTICULL","SERVICES DISCOVERED #4 " + x?.get(3)?.characteristics?.size)
            Log.e("TACTICULL","SERVICES DISCOVERED #5 " + x?.get(4)?.characteristics?.size)
            Log.e("TACTICULL","SERVICES DISCOVERED #6 " + x?.get(5)?.characteristics?.size)
            Log.e("TACTICULL","SERVICES DISCOVERED #7 " + x?.get(6)?.characteristics?.size)
            Log.e("TACTICULL","SERVICES DISCOVERED #7 " + (x?.get(6)?.characteristics?.get(0)))

            if (ActivityCompat.checkSelfPermission(
                    contextWorker,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                //return
            }

            var a = 5; var b = 2;
            Log.e("TACTICULL","GATT CHARACTERISTIC READ" + gatt?.readCharacteristic(x?.get(a)?.characteristics?.get(b)))
            Log.e("TACTICULL","GATT CHARACTERISTIC UUID " + x?.get(a)?.characteristics?.get(b)?.uuid)
            Log.e("TACTICULL","GATT CHARACTERISTIC WRITE TYPE " + x?.get(a)?.characteristics?.get(b)?.writeType)
            Log.e("TACTICULL","GATT CHARACTERISTIC VALUE " + x?.get(a)?.characteristics?.get(b)?.value)
            Log.e("TACTICULL","GATT CHARACTERISTIC DESCRIPTOR " + x?.get(a)?.characteristics?.get(b)?.getDescriptor(x?.get(a)?.characteristics?.get(b)?.uuid))
            Log.e("TACTICULL","GATT CHARACTERISTIC VALUE " + x?.get(a)?.characteristics?.get(b)?.permissions)
            //var bg = x?.get(0)?.characteristics?.get(0) as BluetoothGattCharacteristic
            //bg.value = "TESTING896745368".toByteArray()
            //bg.writeType = WRITE_TYPE_DEFAULT
            //gatt?.writeCharacteristic(bg)



        }


        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)

            Log.e("TACTICULL", "READ CHARACTERISTIC " + characteristic?.value)
            Log.e("TACTICULL", "READ CHARACTERISTIC " + characteristic?.toString())
            Log.e("TACTICULL", "READ CHARACTERISTIC " + characteristic?.descriptors)
            Log.e("TACTICULL", "READ CHAR BYTE ARRAY " + String(characteristic?.value as ByteArray))

        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            Log.e("TACTICULL","WRITTEN TO " + status)
        }



    }





}