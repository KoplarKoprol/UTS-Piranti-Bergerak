package com.renos.moreno240040017ba244

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RiwayatActivity : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapter: ArrayAdapter<String>
    private val listData = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_riwayat)

        // Inisialisasi DatabaseHelper[cite: 3, 4]
        dbHelper = DatabaseHelper(this)

        // Hubungkan variabel dengan komponen di layout XML
        val lvRiwayat = findViewById<ListView>(R.id.lvRiwayat)
        val btnKosong = findViewById<Button>(R.id.btnKosong)

        // Memuat data dari SQLite saat activity pertama kali dibuka
        muatData()

        // Mengatur adapter untuk menampilkan listData ke ListView
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listData)
        lvRiwayat.adapter = adapter

        // P7: Logika Tombol Hapus Semua Data[cite: 4]
        btnKosong.setOnClickListener {
            dbHelper.hapusSemuaData() // Menjalankan perintah DELETE di DatabaseHelper[cite: 4]
            muatData() // Mengosongkan listData dan mengambil data terbaru (yang sudah kosong)[cite: 4]
            adapter.notifyDataSetChanged() // Menyegarkan tampilan daftar di layar[cite: 4]

            Toast.makeText(this, "Semua riwayat dihapus!", Toast.LENGTH_SHORT).show()[cite: 4]
        }
    }

    // Fungsi untuk mengambil data dari database dan memasukkannya ke listData[cite: 4]
    private fun muatData() {
        listData.clear() // Bersihkan list agar tidak duplikat saat di-refresh[cite: 4]
        val cursor = dbHelper.bacaSemuaData() // Mengambil data via Cursor[cite: 4]

        if (cursor.moveToFirst()) {
            do {
                // Mengambil nilai berdasarkan nama kolom di database[cite: 3, 4]
                val nama = cursor.getString(cursor.getColumnIndexOrThrow("nama"))
                val lat = cursor.getString(cursor.getColumnIndexOrThrow("lat"))
                val lon = cursor.getString(cursor.getColumnIndexOrThrow("lon"))

                // Menambahkan teks ke list untuk ditampilkan[cite: 4]
                listData.add("Nama: $nama\nLokasi: $lat, $lon")
            } while (cursor.moveToNext())
        }
        cursor.close() // Selalu tutup cursor setelah selesai digunakan[cite: 4]
    }
}