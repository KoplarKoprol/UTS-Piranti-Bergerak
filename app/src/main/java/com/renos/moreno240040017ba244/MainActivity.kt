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
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.core.os.BundleCompat
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var lm: LocationManager

    // P4: Register Kamera
    private val ambilFoto = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val imageBitmap = result.data?.extras?.let {
                BundleCompat.getParcelable(it, "data", Bitmap::class.java)
            }
            imageBitmap?.let {
                findViewById<ImageView>(R.id.ivFoto).setImageBitmap(it)
            }

            // P6: Setelah foto, ambil lokasi dan simpan
            ambilLokasiGps()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // P5: Inisialisasi OSM
        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", MODE_PRIVATE))
        setContentView(R.layout.activity_main)

        dbHelper = DatabaseHelper(this)
        lm = getSystemService(LOCATION_SERVICE) as LocationManager
        
        val sharedPref = getSharedPreferences("DataUser", MODE_PRIVATE)
        val inputNama = findViewById<EditText>(R.id.inputNama)
        val btnKamera = findViewById<Button>(R.id.btnKamera)
        val btnRiwayat = findViewById<Button>(R.id.btnLihatData)

        val namaLama = sharedPref.getString("KEY_NAMA", "")
        if (!namaLama.isNullOrEmpty()) inputNama.setText(namaLama)

        btnKamera.setOnClickListener {
            val nama = inputNama.text.toString()
            if (nama.isEmpty()) {
                Toast.makeText(this, "Masukkan nama dulu!", Toast.LENGTH_SHORT).show()
            } else {
                sharedPref.edit { putString("KEY_NAMA", nama) }
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                ambilFoto.launch(intent)
            }
        }

        btnRiwayat.setOnClickListener {
            startActivity(Intent(this, RiwayatActivity::class.java))
        }
    }

    private fun ambilLokasiGps() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
            return
        }

        // Coba ambil lokasi terakhir
        val location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

        if (location != null) {
            prosesDanSimpan(location)
        } else {
            // Jika lokasi null, minta update lokasi baru
            Toast.makeText(this, "Mencari lokasi GPS...", Toast.LENGTH_SHORT).show()
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, object : LocationListener {
                override fun onLocationChanged(loc: Location) {
                    prosesDanSimpan(loc)
                    lm.removeUpdates(this) // Stop update setelah dapat satu
                }
                override fun onStatusChanged(p: String?, s: Int, e: Bundle?) {}
                override fun onProviderEnabled(p: String) {}
                override fun onProviderDisabled(p: String) {}
            })
        }
    }

    private fun prosesDanSimpan(loc: Location) {
        val mapView = findViewById<MapView>(R.id.mapView)
        val userPoint = GeoPoint(loc.latitude, loc.longitude)

        // Tampilkan di Peta
        mapView.controller.setZoom(18.0)
        mapView.controller.setCenter(userPoint)
        val marker = Marker(mapView)
        marker.position = userPoint
        marker.title = "Lokasi Saya"
        mapView.overlays.clear()
        mapView.overlays.add(marker)
        mapView.invalidate()

        // Simpan ke SQLite
        val nama = getSharedPreferences("DataUser", MODE_PRIVATE).getString("KEY_NAMA", "Anonim") ?: "Anonim"
        val hasil = dbHelper.simpanRiwayat(nama, loc.latitude.toString(), loc.longitude.toString())
        
        if (hasil != -1L) {
            Toast.makeText(this, "Data Berhasil Disimpan!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        findViewById<MapView>(R.id.mapView).onResume()
    }

    override fun onPause() {
        super.onPause()
        findViewById<MapView>(R.id.mapView).onPause()
    }
}