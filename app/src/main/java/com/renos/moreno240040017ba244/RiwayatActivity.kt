package com.renos.moreno240040017ba244

import android.content.Intent
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

        dbHelper = DatabaseHelper(this)

        val lvRiwayat = findViewById<ListView>(R.id.lvRiwayat)
        val btnKosong = findViewById<Button>(R.id.btnKosong)

        muatData()

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listData)
        lvRiwayat.adapter = adapter

        // P7: Klik item untuk lihat detail (Navigasi P3)
        lvRiwayat.setOnItemClickListener { _, _, position, _ ->
            val dataTerpilih = listData[position]
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("DATA_DETAIL", dataTerpilih)
            startActivity(intent)
        }

        // P7: Hapus Semua Data
        btnKosong.setOnClickListener {
            dbHelper.hapusSemuaData()
            muatData()
            adapter.notifyDataSetChanged()
            Toast.makeText(this, "Semua riwayat dihapus!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun muatData() {
        listData.clear()
        val cursor = dbHelper.bacaSemuaData()

        if (cursor.moveToFirst()) {
            do {
                val nama = cursor.getString(cursor.getColumnIndexOrThrow("nama"))
                val lat = cursor.getString(cursor.getColumnIndexOrThrow("lat"))
                val lon = cursor.getString(cursor.getColumnIndexOrThrow("lon"))
                listData.add("Nama: $nama\nLokasi: $lat, $lon")
            } while (cursor.moveToNext())
        }
        cursor.close()
    }
}