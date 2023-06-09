package com.example.bluetoothtest

import android.Manifest
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bluetoothtest.databinding.ActivityMainBinding
import java.security.Permission
import android.bluetooth.BluetoothGattService





class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding;
    lateinit var button1: Button;
    lateinit var button2: Button;
    lateinit var button3: Button;

    lateinit var recyclerview: RecyclerView;
    lateinit var adapterBT: CustomAdapter;

    var arrayList = ArrayList<BluetoothDevice>()
    lateinit var bluetoothAdapter: BluetoothAdapter;
    val REQUEST_ENABLE_BT: Int = 1;
    val REQUEST_CODE_DISCOVERABLE_BT: Int = 2;

    //FOR BLUETOOTH LOW ENERGY
    var alBLE = ArrayList<BluetoothDevice>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)

        recyclerview = findViewById<RecyclerView>(R.id.recyclerView)
        val layoutManager = LinearLayoutManager(applicationContext)
        recyclerview.layoutManager = layoutManager

        adapterBT = CustomAdapter(arrayList) { item ->
            Toast.makeText(this, "Bluetooth Device " + item.name, Toast.LENGTH_LONG).show()
        };
        recyclerview.adapter = adapterBT;

        // Register for broadcasts when a device is discovered.
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, filter)

        button1 = findViewById(R.id.button1)
        button2 = findViewById(R.id.button2)
        button3 = findViewById(R.id.button3)
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        button1.setOnClickListener {

            if (button1.text == "Bluetooth Enabled") {
                bluetoothAdapter.disable()
                button1.text = "Bluetooth Disabled";

            } else {

                // Do something in response to button click
                Log.e("TAG", "TESTING CLICK")
                if (bluetoothAdapter == null) {
                    // Device doesn't support Bluetooth
                    Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Bluetooth supported", Toast.LENGTH_LONG).show()

                    if (bluetoothAdapter?.isEnabled == false) {
                        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
                    } else {
                        Toast.makeText(
                            this,
                            "Bluetooth Already Enabled " + bluetoothAdapter?.isEnabled,
                            Toast.LENGTH_LONG
                        ).show()
                        button1.text = "Bluetooth Enabled";
                    }

                    val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    //Log.e("TAG","TESTING GRANTED " + granted);

                    if (granted >= 0) {
                        val x = bluetoothAdapter.startDiscovery();
                        //Log.e("TAG","TESTING DISCOVERY " + x);
                    }
                    else {
                        val MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1
                        ActivityCompat.requestPermissions(
                            this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION
                        )
                    }

                    //var intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
                    //startActivityForResult(intent,REQUEST_CODE_DISCOVERABLE_BT)

                }

            }

        }


        button2.setOnClickListener {

            val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)

            val btManager = baseContext.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
            val pairedDevices = btManager.adapter.bondedDevices

            //Log.e("TAG", "TESTING " + pairedDevices.size + " " + granted)

            if (pairedDevices.size > 0) {

                for (device in pairedDevices) {
                    val deviceName = device.name
                    val macAddress = device.address

                    //Log.e("TAG", "TESTING " + deviceName)

                }
            }

        }

        button3.setOnClickListener {
            scanLeDevice()
        }





        //NEED TO REQUEST AT RUNTIME LOCATION, BLUETOOTH.SCAN, BLUETOOTH.CONNECT

        val grantedLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        //Log.e("TAG","TESTING GRANTED " + grantedLocation);
        val grantedScan = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
        //Log.e("TAG","TESTING GRANTED " + grantedScan);
        val grantedConnect = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
        //Log.e("TAG","TESTING GRANTED " + grantedConnect);



        val list = listOf<String>(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestPermissions(list.toTypedArray(),7867865)
        }
        else {

            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),36456)

        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

       //Log.e("TAG", "TESTING PERMISSION " + permissions[0] + " " + " " + grantResults[0])

        if (permissions[0].equals("android.permission.ACCESS_FINE_LOCATION")) {
            if (grantResults[0] >= 0) {
                //val x = bluetoothAdapter.startDiscovery();
                //Log.e("TAG","TESTING DISCOVERY " + x);
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            button1.text = "Bluetooth Enabled";
        }
        else if (requestCode == 2) { }

    }

    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val action: String? = intent.action

            //Log.e("TAG","TESTING BR");

            when(action) {
                BluetoothDevice.ACTION_FOUND -> {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    val deviceName = device?.name
                    val deviceHardwareAddress = device?.address // MAC address

                    if (deviceName != null) { arrayList.add(device); }

                    //arrayList.add(deviceName);
                    //Log.e("TAG", "TESTING " + deviceName)

                    adapterBT = CustomAdapter(arrayList) { item ->
                        Toast.makeText(context, "Bluetooth Device " + item.name, Toast.LENGTH_LONG).show()
                    };
                    recyclerview.adapter = adapterBT;

                    //Toast.makeText(context, "TESTING", Toast.LENGTH_LONG).show();

                    //recyclerview.adapter?.notifyDataSetChanged();
                    //adapterBT.notifyDataSetChanged();

                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()

        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver)
    }

    private fun onListItemClick(position: Int) {
        Toast.makeText(this, "TESTING CLICK", Toast.LENGTH_SHORT).show()
    }

    //FOR LOW ENERGY DEVICES
    private fun scanLeDevice() {
        //FOR LOW ENERGY DEVICES
        val bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner

        //Log.e("TAG", "TESTING BLUE " + bluetoothAdapter.bluetoothLeScanner)

        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED -> {
                // You can use the API that requires the permission.
                //Log.e("TAG", "TESTING yes permission");
            }
            else -> {
                // You can directly ask for the permission.
                //Log.e("TAG", "TESTING no permission");
            }
        }

        var scanning = false
        val handler = Handler()

        // Stops scanning after 10 seconds. 
        val SCAN_PERIOD: Long = 10000

        //private val leDeviceListAdapter = LeDeviceListAdapter()

        if (!scanning) { // Stops scanning after a pre-defined scan period.
            handler.postDelayed({
                scanning = false
                bluetoothLeScanner.stopScan(leScanCallback)
            }, SCAN_PERIOD)
            scanning = true
            bluetoothLeScanner.startScan(leScanCallback)
        } else {
            scanning = false
            bluetoothLeScanner.stopScan(leScanCallback)
        }
    }

    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            //leDeviceListAdapter.addDevice(result.device)
            //leDeviceListAdapter.notifyDataSetChanged()

            Log.e("TAG","TESTING LOW DEVICE: " + result.device.name + " " + result.device.address + " Class " + result.device.bluetoothClass + " " + result.device.bondState);
            //ONLY ADD UNIQUE DEVICES TO LIST

            //DO NOT SHOW BLUETOOTH DEVICES WITH NO NAME AND ONLY UNIQUE MAC-ADDRESS
            //if (result.device.name != null) {
                  if (!alBLE.contains(result.device)) {
                    alBLE.add(result.device);
                  }
            //}

            adapterBT = CustomAdapter(alBLE) { item ->
                Toast.makeText(this@MainActivity, "Bluetooth Device " + item.name, Toast.LENGTH_LONG).show()
                var bluetoothGatt: BluetoothGatt? = null

                try {
                    bluetoothGatt = item.connectGatt(this@MainActivity, false, bluetoothGattCallback)
                } catch(e: Exception) {}


            };
            recyclerview.adapter = adapterBT;

        }
    }

    private val bluetoothGattCallback = object : BluetoothGattCallback() {

        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            Log.e("TAG", "TESTING GATT CALLBACK")
            //Toast.makeText(this@MainActivity, "GATT CALLBACK", Toast.LENGTH_LONG).show()
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // successfully connected to the GATT Server
                Log.e("TAG", "TESTING CONNECTION SUCCESS " + newState)
                gatt?.discoverServices();
                //Toast.makeText(this@MainActivity, "CONNECTED", Toast.LENGTH_LONG).show()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // disconnected from the GATT Server
                Log.e("TAG", "TESTING CONNECTION DISCONNECTED " + newState)
                //Toast.makeText(this@MainActivity, "DISCONNECTED", Toast.LENGTH_LONG).show()
            }
            else {
                Log.e("TAG", "TESTING CONNECTION " + newState)
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED)
                Log.e("TAG","TESTING SERVICES DISCOVERED")
                val x = gatt?.services
                Log.e("TAG","TESTING SIZE " + x?.size)

                x?.forEach {

                    Log.e("TAG", "TESTING UUID " + it.uuid.toString())
                    Log.e("TAG", "TESTING CHARACTERISTIC " + it.characteristics[2].uuid)
                    Log.e("TAG", "TESTING CONTENTS " + it.describeContents())

                    for (u in it.characteristics) {
                        Log.e("TAG", "TESTING LOOP: " + u)
                    }


                }




            } else {
                //Log.w(BluetoothService.TAG, "onServicesDiscovered received: $status")
            }
        }


    }





}