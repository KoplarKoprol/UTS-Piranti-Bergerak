package com.renos.moreno240040017ba244

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        val tvDetail = findViewById<TextView>(R.id.tvDetailData)
        val btnBack = findViewById<Button>(R.id.btnBack)

        // P3: Menerima data dari Intent
        val data = intent.getStringExtra("DATA_DETAIL")
        tvDetail.text = data ?: "Data tidak ditemukan"

        // P3: Navigasi kembali
        btnBack.setOnClickListener {
            finish()
        }
    }
}