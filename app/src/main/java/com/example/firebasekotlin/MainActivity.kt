package com.example.firebasekotlin

import android.content.ClipData.Item
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.Menu
import android.view.MenuItem
import android.view.Window
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    lateinit var myAuthStateListener:FirebaseAuth.AuthStateListener
    lateinit var tvKullaniciAdi:TextView
    lateinit var tvKullaniciEmail:TextView
    lateinit var tvKullaniciUid:TextView
    lateinit var imgMenu: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initAuthStateListener()
        setKullaniciBilgileri()

        menuyuayarla()


    }

    private fun setKullaniciBilgileri() {
        tvKullaniciEmail = findViewById(R.id.tvKullaniciEmail)
        tvKullaniciAdi = findViewById(R.id.tvKullaniciAdi)
        tvKullaniciUid = findViewById(R.id.tvKullaniciUid)
        var kullanici = FirebaseAuth.getInstance().currentUser!!
        if (kullanici != null) {
            tvKullaniciAdi.text = if (kullanici.displayName.isNullOrEmpty()) "Tanımlanmadı" else kullanici.displayName
            tvKullaniciEmail.text = kullanici.email
            tvKullaniciUid.text = kullanici.uid
        }
    }



    private fun initAuthStateListener() {
        myAuthStateListener=object : FirebaseAuth.AuthStateListener{
            override fun onAuthStateChanged(p0: FirebaseAuth) {

                var kullanici= p0.currentUser

                if(kullanici != null){

                }else {

                    var intent=Intent(this@MainActivity, LoginActivity::class.java)
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                    finish()


                }

            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.anamenu, menu)
        return true
    }

    private fun menuyuayarla() {
        imgMenu = findViewById(R.id.imgMenu)
        imgMenu.setOnClickListener {

            val wrapper = ContextThemeWrapper(this@MainActivity, R.style.popupMenuStyle)
            val popupMenu = PopupMenu(wrapper, imgMenu)

            popupMenu.menuInflater.inflate(R.menu.anamenu, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener{
                override fun onMenuItemClick(item: MenuItem?): Boolean {
                    when (item?.itemId) {
                        R.id.menuCikisyap -> {
                            cikisyap()
                            return true
                        }

                        R.id.menuHesapAyalari -> {

                            var intent = Intent(this@MainActivity, kullanici_ayarlari::class.java)
                            startActivity(intent)
                            return true
                        }

                        R.id.menuSohbet -> {

                            var intent = Intent(this@MainActivity, SohbetActivity::class.java)
                            startActivity(intent)
                            return true
                        }
                        else -> return false
                    }
                }
            })
            popupMenu.show()
        }
    }

    private fun cikisyap() {
        FirebaseAuth.getInstance().signOut()
    }

    override fun onResume() {
        super.onResume()
        kullaniciyiKontrolEt()
    }

    private fun kullaniciyiKontrolEt() {
        var kullanici=FirebaseAuth.getInstance().currentUser

        if(kullanici == null){
            var intent=Intent(this@MainActivity, LoginActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        FirebaseAuth.getInstance().addAuthStateListener(myAuthStateListener)
    }

    override fun onStop() {
        super.onStop()
        if(myAuthStateListener != null){
            FirebaseAuth.getInstance().removeAuthStateListener(myAuthStateListener)
        }
    }
}