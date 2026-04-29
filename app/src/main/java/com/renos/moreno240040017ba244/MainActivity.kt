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
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EdgeToEdge.enable(this)
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inisialisasi View
        val btnNext = findViewById<Button>(R.id.btnNext)
        val inputNama = findViewById<EditText>(R.id.inputNama)

        btnNext.setOnClickListener {
            val nama = inputNama.text.toString()

            // Pindah ke DetailActivity dengan membawa data
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("EXTRA_NAMA", nama)
            startActivity(intent)
        }
    }
}