package com.renos.moreno240040017ba244

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.core.os.BundleCompat
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var lm: LocationManager
    private lateinit var mqttClient: MqttAsyncClient

    private val ambilFoto = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val imageBitmap = result.data?.extras?.let {
                BundleCompat.getParcelable(it, "data", Bitmap::class.java)
            }
            imageBitmap?.let {
                findViewById<ImageView>(R.id.ivFoto).setImageBitmap(it)
            }
            ambilLokasiGps()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", MODE_PRIVATE))
        setContentView(R.layout.activity_main)

        dbHelper = DatabaseHelper(this)
        lm = getSystemService(LOCATION_SERVICE) as LocationManager
        
        val inputNama = findViewById<EditText>(R.id.inputNama)
        val etPesanKirim = findViewById<EditText>(R.id.etPesanKirim)
        val btnPublish = findViewById<Button>(R.id.btnPublish)
        val btnKamera = findViewById<Button>(R.id.btnKamera)
        val btnRiwayat = findViewById<Button>(R.id.btnLihatData)

        // --- INISIALISASI MQTT (Java Client) ---
        val serverUri = "tcp://broker.hivemq.com:1883"
        val clientId = MqttClient.generateClientId()
        
        try {
            mqttClient = MqttAsyncClient(serverUri, clientId, MemoryPersistence())
            val options = MqttConnectOptions().apply { isCleanSession = true }
            
            mqttClient.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d("MQTT_DEDEK", "KONEKSI BERHASIL")
                }
                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.e("MQTT_DEDEK", "KONEKSI GAGAL: ${exception?.message}")
                }
            })
        } catch (e: Exception) {
            Log.e("MQTT_DEDEK", "Error MQTT Init: ${e.message}")
        }

        btnPublish.setOnClickListener {
            val isiPesan = etPesanKirim.text.toString()
            if (isiPesan.isNotEmpty()) {
                try {
                    val message = MqttMessage(isiPesan.toByteArray())
                    message.qos = 1
                    
                    mqttClient.publish("stikom/bali/dedek", message, null, object : IMqttActionListener {
                        override fun onSuccess(asyncActionToken: IMqttToken?) {
                            Log.d("MQTT_DEDEK", "PUBLISH BERHASIL: $isiPesan")
                            runOnUiThread {
                                Toast.makeText(this@MainActivity, "Pesan Terkirim!", Toast.LENGTH_SHORT).show()
                                etPesanKirim.setText("")
                            }
                        }
                        override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                            Log.e("MQTT_DEDEK", "PUBLISH GAGAL: ${exception?.message}")
                        }
                    })
                } catch (e: Exception) {
                    Log.e("MQTT_DEDEK", "PUBLISH GAGAL: ${e.message}")
                }
            }
        }

        btnKamera.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            ambilFoto.launch(intent)
        }

        btnRiwayat.setOnClickListener {
            startActivity(Intent(this, RiwayatActivity::class.java))
        }

        val sharedPref = getSharedPreferences("DataUser", MODE_PRIVATE)
        val namaLama = sharedPref.getString("KEY_NAMA", "")
        if (!namaLama.isNullOrEmpty()) inputNama.setText(namaLama)
    }

    private fun ambilLokasiGps() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
            return
        }
        val location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        location?.let { prosesDanSimpan(it) }
    }

    private fun prosesDanSimpan(loc: Location) {
        val userPoint = GeoPoint(loc.latitude, loc.longitude)
        val mapView = findViewById<MapView>(R.id.mapView)
        mapView.controller.setZoom(18.0)
        mapView.controller.setCenter(userPoint)
        mapView.overlays.clear()
        val marker = Marker(mapView)
        marker.position = userPoint
        mapView.overlays.add(marker)
        mapView.invalidate()
        
        val nama = getSharedPreferences("DataUser", MODE_PRIVATE).getString("KEY_NAMA", "Anonim") ?: "Anonim"
        dbHelper.simpanRiwayat(nama, loc.latitude.toString(), loc.longitude.toString())
    }

    override fun onResume() { super.onResume(); findViewById<MapView>(R.id.mapView).onResume() }
    override fun onPause() { super.onPause(); findViewById<MapView>(R.id.mapView).onPause() }
}