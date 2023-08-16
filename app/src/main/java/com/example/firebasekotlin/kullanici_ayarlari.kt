package com.example.firebasekotlin

import com.squareup.picasso.Picasso
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.firebasekotlin.dialogs.ProfilResmiFragment
import com.example.firebasekotlin.model.Kullanici
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import java.io.ByteArrayOutputStream
import android.net.Uri as Uri

class kullanici_ayarlari : AppCompatActivity(), ProfilResmiFragment.onProfilResimListener {

    lateinit var imgBackKullanici:ImageView
    lateinit var etKullaniciAdi:EditText
    lateinit var etKullaniciTelefon:EditText
    lateinit var etKullaniciSuankiSifre:EditText
    lateinit var tvMailAdresi:TextView
    lateinit var btnDegisiklikleriKaydet:Button
    lateinit var btnSifreGuncelle:Button
    lateinit var tvMailSifreGuncelle:TextView
    lateinit var tvSifremiUnuttum:TextView
    lateinit var guncelleLayout:ConstraintLayout
    lateinit var etYeniMail:EditText
    lateinit var btnMailGuncelle:Button
    lateinit var etYeniSifre:EditText
    lateinit var progressPicture:ProgressBar
    lateinit var progressGenel:ProgressBar
    lateinit var myTextView:TextView
    lateinit var imgCircleProfil:CircleImageView



    var izinlerVerildi = false
    var galeridenGelenURI: Uri? = null
    var kameradanGelenBitmap: Bitmap? = null
    val MEGABYTE = 1000000.toDouble()

    override fun getResimYolu(resimPath: Uri?) {
        galeridenGelenURI = resimPath
        Picasso.get().load(galeridenGelenURI).resize(100, 100).into(imgCircleProfil)
    }

    override fun getResimBitmap(bitmap: Bitmap) {
        kameradanGelenBitmap = bitmap
        imgCircleProfil.setImageBitmap(bitmap)
    }

    inner class BackgroundResimCompress : AsyncTask<Uri, Double, ByteArray?> {

        var myBitmap: Bitmap? = null

        constructor() {}

        constructor(bm: Bitmap) {

            if (bm != null) {
                myBitmap = bm
            }


        }

        override fun onPreExecute() {
            super.onPreExecute()
        }


        override fun doInBackground(vararg params: Uri?): ByteArray? {

            //galeriden resim seçilmiş
            if (myBitmap == null) {
                myBitmap = MediaStore.Images.Media.getBitmap(
                    this@kullanici_ayarlari.contentResolver,
                    params[0]
                )

            }

            var resimBytes: ByteArray? = null

            for (i in 1..10) {
                resimBytes = convertBitmaptoByte(myBitmap, 100 / i)
                publishProgress(resimBytes!!.size.toDouble())
            }

            return resimBytes

        }


        override fun onProgressUpdate(vararg values: Double?) {
            super.onProgressUpdate(*values)
        }

        private fun convertBitmaptoByte(myBitmap: Bitmap?, i: Int): ByteArray? {

            var stream = ByteArrayOutputStream()
            myBitmap?.compress(Bitmap.CompressFormat.JPEG, i, stream)
            return stream.toByteArray()

        }

        override fun onPostExecute(result: ByteArray?) {
            super.onPostExecute(result)
            uploadResimtoFirebase(result)
        }

    }

    private fun uploadResimtoFirebase(result: ByteArray?) {

        progressGoster()
        val storageReferans = FirebaseStorage.getInstance().getReference()
        val resimEklenecekYer = storageReferans.child("images/users" + FirebaseAuth.getInstance().currentUser?.uid + "/profile_resim")

        val uploadGorevi = resimEklenecekYer.putBytes(result!!)

        uploadGorevi.addOnSuccessListener { taskSnapshot ->
            // Profil resmi başarıyla yüklendi, indirme URL'sini alalım
            resimEklenecekYer.downloadUrl.addOnSuccessListener { uri ->
                val firebaseURL = uri.toString()

                FirebaseDatabase.getInstance().reference
                    .child("kullanici")
                    .child(FirebaseAuth.getInstance().currentUser?.uid!!)
                    .child("profil_resmi")
                    .setValue(firebaseURL)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this@kullanici_ayarlari, "Profil resmi başarıyla güncellendi", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@kullanici_ayarlari, "Profil resmi güncellenirken hata oluştu", Toast.LENGTH_SHORT).show()
                        }
                        progressGizle()
                    }
            }.addOnFailureListener { exception ->
                // URL alınamadı, hata mesajını logla
                Log.e("Firebase Storage", "Resim URL'si alınamadı: ${exception.message}")
                Toast.makeText(this@kullanici_ayarlari, "Resim yüklenirken hata oluştu", Toast.LENGTH_SHORT).show()
                progressGizle()
            }
        }.addOnFailureListener { exception ->
            // Resim yükleme başarısız oldu, hata mesajını logla
            Log.e("Firebase Storage", "Resim yüklenirken hata: ${exception.message}")
            Toast.makeText(this@kullanici_ayarlari, "Resim yüklenirken hata oluştu", Toast.LENGTH_SHORT).show()
            progressGizle()
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContentView(R.layout.activity_kullanici_ayarlari)
        etKullaniciAdi = findViewById(R.id.etKullaniciAdi)
        etKullaniciTelefon = findViewById(R.id.etKullaniciTelefon)
        etKullaniciSuankiSifre = findViewById(R.id.etKullaniciSuankiSifre)
        tvMailAdresi = findViewById(R.id.tvMailAdresi)
        btnDegisiklikleriKaydet = findViewById(R.id.btnDegisiklikleriKaydet)
        btnSifreGuncelle = findViewById(R.id.btnSifreGuncelle)
        tvMailSifreGuncelle = findViewById(R.id.tvMailSifreGuncelle)
        tvSifremiUnuttum = findViewById(R.id.tvSifremiUnuttum)
        guncelleLayout = findViewById(R.id.guncelleLayout)
        etYeniMail = findViewById(R.id.etYeniMail)
        btnMailGuncelle = findViewById(R.id.btnMailGuncelle)
        etYeniSifre = findViewById(R.id.etYeniSifre)
        progressPicture = findViewById(R.id.progressPicture)
        progressGenel = findViewById(R.id.progressGenel)
        myTextView = findViewById(R.id.myTextView)
        imgCircleProfil = findViewById(R.id.imgCircleProfil)
        imgBackKullanici = findViewById(R.id.imgBackKullanici)


        imgBackKullanici.setOnClickListener {

            super.onBackPressed()
        }

        var kullanici = FirebaseAuth.getInstance().currentUser!!


        kullaniciBilgileriniOku()

        tvSifremiUnuttum.setOnClickListener {

            FirebaseAuth.getInstance()
                .sendPasswordResetEmail(FirebaseAuth.getInstance().currentUser?.email.toString())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        Toast.makeText(
                            this@kullanici_ayarlari,
                            "Şifre sıfırlama maili gönderildi",
                            Toast.LENGTH_SHORT
                        ).show()

                    } else {

                        Toast.makeText(
                            this@kullanici_ayarlari,
                            "Hata oluştu :" + task.exception?.message,
                            Toast.LENGTH_SHORT
                        ).show()

                    }
                }

        }

        btnDegisiklikleriKaydet.setOnClickListener {

            kullanici = FirebaseAuth.getInstance().currentUser!!

            if (etKullaniciAdi.text.toString().isNotEmpty()) {


                var bilgileriGuncelle = UserProfileChangeRequest.Builder()
                    .setDisplayName(etKullaniciAdi.text.toString())
                    .build()
                kullanici.updateProfile(bilgileriGuncelle)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            FirebaseDatabase.getInstance().reference
                                .child("kullanici")
                                .child(FirebaseAuth.getInstance().currentUser?.uid!!)
                                .child("isim")
                                .setValue(etKullaniciAdi.text.toString())

                        }
                    }
            } else {
                Toast.makeText(
                    this@kullanici_ayarlari,
                    "Kullanıcı adını doldurunuz",
                    Toast.LENGTH_SHORT
                ).show()
            }

            if (etKullaniciTelefon.text.toString().isNotEmpty()) run {

                FirebaseDatabase.getInstance().reference
                    .child("kullanici")
                    .child(FirebaseAuth.getInstance().currentUser?.uid!!)
                    .child("telefon")
                    .setValue(etKullaniciTelefon.text.toString())


            }

            if (galeridenGelenURI != null) {

                fotografCompressed(galeridenGelenURI!!)

            } else if (kameradanGelenBitmap != null) {
                fotografCompressed(kameradanGelenBitmap!!)
            }


        }

        tvMailSifreGuncelle.setOnClickListener {
            progressGenelGoster()
            if (etKullaniciSuankiSifre.text.toString().isNotEmpty()) {


                var credential = EmailAuthProvider.getCredential(
                    kullanici.email.toString(),
                    etKullaniciSuankiSifre.text.toString()
                )
                kullanici.reauthenticate(credential)
                    .addOnCompleteListener { task ->

                        if (task.isSuccessful) {

                            progressGenelGizle()
                            guncelleLayout.visibility = View.VISIBLE


                            btnMailGuncelle.setOnClickListener {

                                mailAdresiniGuncelle()


                            }

                            btnSifreGuncelle.setOnClickListener {

                                sifreBilgisiniGuncelle()

                            }

                        } else {

                            Toast.makeText(
                                this@kullanici_ayarlari,
                                "Şuanki şifrenizi yanlış girdiniz",
                                Toast.LENGTH_SHORT
                            ).show()
                            progressGenelGizle()
                            guncelleLayout.visibility = View.INVISIBLE
                        }


                    }


            } else {
                Toast.makeText(
                    this@kullanici_ayarlari,
                    "Güncellemeler için geçerli şifrenizi yazmalısınız",
                    Toast.LENGTH_SHORT
                ).show()
                progressGenelGizle()
            }


        }

        imgCircleProfil.setOnClickListener {

            if (izinlerVerildi) {
                var dialog = ProfilResmiFragment()
                dialog.show(supportFragmentManager, "fotosec")
            } else {
                izinleriIste()
            }

        }


    }

    private fun fotografCompressed(galeridenGelenURI: Uri) {
        val compressed = BackgroundResimCompress()
        compressed.execute(galeridenGelenURI)
    }

    private fun fotografCompressed(kameradanGelenBitmap: Bitmap) {
        val compressed = BackgroundResimCompress(kameradanGelenBitmap)
        val uri: Uri? = null
        compressed.execute(uri)
    }

    private fun izinleriIste() {

        var izinler = arrayOf(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA
        )
        if (ContextCompat.checkSelfPermission(
                this,
                izinler[0]
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this,
                izinler[1]
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, izinler[2]) == PackageManager.PERMISSION_GRANTED
        ) {

            izinlerVerildi = true
        } else {
            ActivityCompat.requestPermissions(this, izinler, 150)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 150) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                var dialog = ProfilResmiFragment()
                dialog.show(supportFragmentManager, "fotosec")
            } else {
                Toast.makeText(
                    this@kullanici_ayarlari,
                    "Tüm izinleri vermelisiniz",
                    Toast.LENGTH_SHORT
                ).show()
            }


        }
    }


    private fun kullaniciBilgileriniOku() {

        var referans = FirebaseDatabase.getInstance().reference

        var kullanici = FirebaseAuth.getInstance().currentUser
        tvMailAdresi.text = kullanici?.email


        //query 1
        var sorgu = referans.child("kullanici")
            .orderByKey()
            .equalTo(kullanici?.uid)

        sorgu.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                for (singleSnapshot in snapshot!!.children) {
                    var okunanKullanici = singleSnapshot.getValue(Kullanici::class.java)
                    etKullaniciAdi.setText(okunanKullanici?.isim)
                    etKullaniciTelefon.setText(okunanKullanici?.telefon)
                    if (okunanKullanici?.profil_resmi != null) {
                        Picasso.get().load(galeridenGelenURI).resize(100, 100).into(imgCircleProfil)
                    } else {
                        Picasso.get().load(okunanKullanici?.profil_resmi).resize(100, 100)
                            .into(imgCircleProfil)
                    }


                }
            }

        })

    }


    private fun sifreBilgisiniGuncelle() {
        progressGenelGoster()
        var kullanici = FirebaseAuth.getInstance().currentUser!!

        if (kullanici != null) {
            kullanici.updatePassword(etYeniSifre.text.toString())
                .addOnCompleteListener { task ->
                    Toast.makeText(
                        this@kullanici_ayarlari,
                        "Şifreniz değiştirildi tekrar giriş yapın",
                        Toast.LENGTH_SHORT
                    ).show()
                    FirebaseAuth.getInstance().signOut()
                    loginSayfasinaYonlendir()
                }
        }
        progressGenelGizle()

    }

    private fun mailAdresiniGuncelle() {
        var kullanici = FirebaseAuth.getInstance().currentUser!!

        if (kullanici != null) {

            FirebaseAuth.getInstance().fetchSignInMethodsForEmail(etYeniMail.text.toString())
                .addOnCompleteListener { task ->

                    if (task.isSuccessful) {

                        val signInMethods = task.result?.signInMethods

                        if (signInMethods?.isNotEmpty() == true) {
                            Toast.makeText(
                                this@kullanici_ayarlari,
                                "Email Kullanımda",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            kullanici.updateEmail(etYeniMail.text.toString())
                                .addOnCompleteListener { updateEmailTask ->

                                    if (updateEmailTask.isSuccessful) {
                                        Toast.makeText(
                                            this@kullanici_ayarlari,
                                            "Mail adresi değişti! tekrar giriş yapın",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        FirebaseAuth.getInstance().signOut()
                                        loginSayfasinaYonlendir()
                                    } else {
                                        Toast.makeText(
                                            this@kullanici_ayarlari,
                                            "Email Güncellenemedi",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                        }

                    } else {

                        Toast.makeText(
                            this@kullanici_ayarlari,
                            "Email Bilgileri Alınamadı",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }


    }

    fun loginSayfasinaYonlendir() {
        var intent = Intent(this@kullanici_ayarlari, LoginActivity::class.java)
        startActivity(intent)
        finish()

    }

    fun progressGoster() {

        progressPicture.visibility = View.VISIBLE

    }

    fun progressGizle() {

        progressPicture.visibility = View.INVISIBLE

    }

    fun progressGenelGoster() {

        progressGenel.visibility = View.VISIBLE

    }

    fun progressGenelGizle() {

        progressGenel.visibility = View.INVISIBLE

    }
}
