package com.renos.moreno240040017ba244

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.EdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
//Pert6 materi ada bahas gabung button kirim nama ma button foto(ketika ngambil foto nama dan lokasi langsung kekirim juga, makanya digabung aja)
//12 mei deadline projeknya karena rabu wisuda, bikin videonya screen recording atau tampilan visual android studio boleh juga, dikasi penjelasan
//ngumpulin link video aja di txt
// Tambahkan di dalam class MainActivity
private lateinit var dbHelper: DatabaseHelper

// P4: Register Activity untuk Kamera[cite: 1]
private val ambilFoto = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
    if (result.resultCode == RESULT_OK) {
        val imageBitmap = result.data?.extras?.get("data") as Bitmap
        findViewById<ImageView>(R.id.ivFoto).setImageBitmap(imageBitmap)
        // Setelah foto diambil, langsung picu ambil lokasi & simpan[cite: 3]
        ambilLokasiGps(findViewById(R.id.mapView))
    }
}

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // P5: Inisialisasi OSM sebelum setContentView[cite: 2]
    Configuration.getInstance().load(this, getSharedPreferences("osmdroid", MODE_PRIVATE))
    setContentView(R.layout.activity_main)

    dbHelper = DatabaseHelper(this)
    val sharedPref = getSharedPreferences("DataUser", MODE_PRIVATE)
    val inputNama = findViewById<EditText>(R.id.inputNama)

    // P6: SharedPreferences - Load nama otomatis[cite: 3]
    val namaLama = sharedPref.getString("KEY_NAMA", "")
    if (!namaLama.isNullOrEmpty()) inputNama.setText(namaLama)

    // P4: Klik Tombol Kamera[cite: 1]
    findViewById<Button>(R.id.btnKamera).setOnClickListener {
        // Simpan nama ke SharedPreferences dulu[cite: 3]
        sharedPref.edit().putString("KEY_NAMA", inputNama.text.toString()).apply()

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        ambilFoto.launch(intent)
    }
}

// P5 & P6: Ambil Lokasi dan Simpan ke SQLite[cite: 2, 3]
private fun ambilLokasiGps(mapView: MapView) {
    val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    // Cek Permission (Pastikan sudah minta izin runtime)[cite: 2]
    val location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

    location?.let {
        val userPoint = GeoPoint(it.latitude, it.longitude)

        // Update Peta[cite: 2]
        val marker = Marker(mapView)
        marker.position = userPoint
        marker.title = "Lokasi Absen"
        mapView.overlays.add(marker)
        mapView.controller.animateTo(userPoint)

        // P6: Simpan ke Database[cite: 3]
        val nama = getSharedPreferences("DataUser", MODE_PRIVATE).getString("KEY_NAMA", "Anonim")!!
        dbHelper.simpanRiwayat(nama, it.latitude.toString(), it.longitude.toString())
        Toast.makeText(this, "Data & Lokasi Tersimpan!", Toast.LENGTH_SHORT).show()
    }
}