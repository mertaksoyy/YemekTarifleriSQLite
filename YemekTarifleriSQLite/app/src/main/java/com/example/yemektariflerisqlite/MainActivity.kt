package com.example.yemektariflerisqlite

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean { //Bu fonkisyonun altında menüyü bağlıyoruz
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.yemek_ekle,menu)
        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean { //Optios Menu den bir şey seçildiğinde ne yapılacağını yazıyoruz

        if (item.itemId == R.id.yemek_ekleme_item)
        {
            val action = ListeFragmentDirections.actionListeFragmentToTarifFragment("menudengeldim",0)
            Navigation.findNavController(this,R.id.fragment).navigate(action)
        }


        return super.onOptionsItemSelected(item)
    }






}