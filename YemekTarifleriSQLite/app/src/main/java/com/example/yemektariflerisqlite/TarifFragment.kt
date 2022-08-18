package com.example.yemektariflerisqlite

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.fragment_tarif.*
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.lang.Exception
import java.time.Year


class TarifFragment : Fragment() {

    var secilenGorsel : Uri? = null
    var secilenBitmap : Bitmap ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tarif, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttonKaydet.setOnClickListener {
            //SQLite'a Kaydetme

            val yemekIsmı = yemekIsmiText.text.toString()
            val yemekMalzeme = yemekMalzemeText.text.toString()

            if (secilenBitmap != null)
            {
                val kucukBitmap = kucukBitmapOlustur(secilenBitmap!!,300)

                val outputStream = ByteArrayOutputStream()
                kucukBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
                val byteDizisi = outputStream.toByteArray()

                //VERİTABANI İŞLEMLERİ
                try {

                    context.let {
                        val database = it!!.openOrCreateDatabase("Yemekler", Context.MODE_PRIVATE,null)
                        database.execSQL("CREATE TABLE IF NOT EXISTS yemekler(id INTEGER PRIMARY KEY,yemekismi VARCHAR,yemekmalzemesi VARCHAR,gorsel BLOB)")

                        val sqlString = "INSERT INTO yemekler (yemekismi,yemekmalzemesi,gorsel) VALUES (?,?,?)"
                        val statement = database.compileStatement(sqlString)
                        statement.bindString(1,yemekIsmı)
                        statement.bindString(2,yemekMalzeme)
                        statement.bindBlob(3,byteDizisi)
                        statement.execute()
                    }

                }catch (e :Exception){
                    e.printStackTrace()
                }

                val action = TarifFragmentDirections.actionTarifFragmentToListeFragment()
                Navigation.findNavController(view).navigate(action)
            }

        }



        imageViewGorsel.setOnClickListener{ //resim seçe basıldığında kullanıcıdan izin istedik
            activity.let {
                if (ContextCompat.checkSelfPermission(it!!.applicationContext,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                {//izin verilmedi , izin iste
                    requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),1)
                }
                else
                {//izin zaten verilmiş , tekrar istemeden galeriye git
                    val galeriIntent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(galeriIntent,2)

                }
            }
        }



        arguments?.let {
            var gelenBilgi = TarifFragmentArgs.fromBundle(it).bilgi

            if (gelenBilgi == "menudengeldim")
            {// Yeni yemek eklemeye gidecek
                yemekIsmiText.setText("")
                yemekMalzemeText.setText("")
                buttonKaydet.visibility =View.VISIBLE

                val gorselSecmeArkaPlani = BitmapFactory.decodeResource(context?.resources,R.drawable.secilenresim)
                imageViewGorsel.setImageBitmap(gorselSecmeArkaPlani)

            }
            else
            {//Var olan yemeğe tıklanmış onun bilgileri gösterilecek
                buttonKaydet.visibility = View.INVISIBLE

                val secilenId = TarifFragmentArgs.fromBundle(it).id

                context?.let {
                   try {
                       val db = it.openOrCreateDatabase("Yemekler",Context.MODE_PRIVATE,null)
                       val cursor = db.rawQuery("SELECT * FROM yemekler WHERE id = ?", arrayOf(secilenId.toString()))

                       val yemekIsmiIndex = cursor.getColumnIndex("yemekismi")
                       val yemekMalzemeIndex = cursor.getColumnIndex("yemekmalzemesi")
                       val yemekGorsel = cursor.getColumnIndex("gorsel")


                       while (cursor.moveToNext()){
                           yemekIsmiText.setText(cursor.getString(yemekIsmiIndex))
                           yemekMalzemeText.setText(cursor.getString(yemekMalzemeIndex))

                           val byteDizisi = cursor.getBlob(yemekGorsel)
                           val bitmap = BitmapFactory.decodeByteArray(byteDizisi,0,byteDizisi.size)
                           imageViewGorsel.setImageBitmap(bitmap)
                       }
                       cursor.close()

                   } catch (e :Exception){
                       e.printStackTrace()
                   }
                }

            }
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)
    {
        if (requestCode == 1)
        {
            if(grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {//izini aldık
                val galeriIntent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galeriIntent,2)
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if(requestCode == 2 && resultCode == Activity.RESULT_OK && data != null) {
            secilenGorsel = data.data //Uri aldık yani kayıtlı olan yeri

            try {

                context?.let {
                    if (secilenGorsel != null)
                    {
                        if (Build.VERSION.SDK_INT >= 28) {
                           val source =  ImageDecoder.createSource(it.contentResolver,secilenGorsel!!)
                            secilenBitmap = ImageDecoder.decodeBitmap(source)
                            imageViewGorsel.setImageBitmap(secilenBitmap)
                        }
                        else{
                            secilenBitmap = MediaStore.Images.Media.getBitmap(it.contentResolver,secilenGorsel)
                            imageViewGorsel.setImageBitmap(secilenBitmap)
                        }
                    }
                }
            }
            catch (e : Exception){
                e.printStackTrace()
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }




    //Bitmapi küçültüğümüz fonkisyon Fotoğraflar için
    fun kucukBitmapOlustur(kullanicininSectigiBimap : Bitmap, maximumBoyut : Int) : Bitmap{

        var width = kullanicininSectigiBimap.width
        var height = kullanicininSectigiBimap.height

        val bitmapOrani : Double = width.toDouble() / height.toDouble()

        if (bitmapOrani > 1 )
        {//Görselimiz Yatay
            width = maximumBoyut
            val kisaltilmisHeight = width / bitmapOrani
            height = kisaltilmisHeight.toInt()
        }
        else
        {//Görselimiz dikey
            height = maximumBoyut
            val kisaltilmisWidth = height * bitmapOrani
            width = kisaltilmisWidth.toInt()
        }
        return Bitmap.createScaledBitmap(kullanicininSectigiBimap,width,height,true)

    }

}
