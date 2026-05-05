package com.renos.moreno240040017ba244

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "AbsensiDB", null, 1) {

    override fun onCreate(db: SQLiteDatabase?) {
        // P6: Membuat tabel riwayat[cite: 3]
        val query = "CREATE TABLE tb_riwayat (id INTEGER PRIMARY KEY AUTOINCREMENT, nama TEXT, lat TEXT, lon TEXT)"
        db?.execSQL(query)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS tb_riwayat")
        onCreate(db)
    }

    // P6: Fungsi Simpan Data[cite: 3]
    fun simpanRiwayat(nama: String, lat: String, lon: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("nama", nama)
            put("lat", lat)
            put("lon", lon)
        }
        return db.insert("tb_riwayat", null, values)
    }

    // P7: Fungsi Read Data
    fun bacaSemuaData(): Cursor {
        return readableDatabase.rawQuery("SELECT * FROM tb_riwayat", null)
    }

    // P7: Fungsi Delete Data
    fun hapusSemuaData() {
        writableDatabase.execSQL("DELETE FROM tb_riwayat")
    }
}