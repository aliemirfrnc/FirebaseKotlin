package com.example.firebasekotlin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasekotlin.adapters.SohbetMesajRecyclerviewAdapter
import com.example.firebasekotlin.model.FCMModel
import com.example.firebasekotlin.model.Kullanici
import com.example.firebasekotlin.model.SohbetMesaj
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

lateinit var rvMesajlar : RecyclerView
lateinit var etYeniMesaj:EditText
lateinit var imgMesajGonder:ImageView
lateinit var imgBackSohbetOdasiActivity:ImageView
lateinit var tvSohbetOdasiAdi: TextView

class SohbetOdasiActivity : AppCompatActivity() {

    companion object {
        var activityAcikMi:Boolean = false
    }
    var mAuthListener: FirebaseAuth.AuthStateListener? = null
    var mMesajReferans: DatabaseReference? = null
    var SERVER_KEY: String? = null
    var BASE_URL = "https://fcm.googleapis.com/fcm/"
    var secilenSohbetOdasiId: String = ""
    var tumMesajlar: ArrayList<SohbetMesaj>? = null
    var mesajIDSet: HashSet<String>? = null
    var myAdapter: SohbetMesajRecyclerviewAdapter? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_sohbet_odasi)

        rvMesajlar = findViewById(R.id.rvMesajlar)
        etYeniMesaj = findViewById(R.id.etYeniMesaj)
        imgMesajGonder = findViewById(R.id.imgMesajGonder)
        imgBackSohbetOdasiActivity = findViewById(R.id.imgBackSohbetOdasiActivity)
        tvSohbetOdasiAdi = findViewById(R.id.tvSohbetOdasiAdi)

        baslatFirebaseAuthListener()
        sohbetOdasiniOgren()
        serverkeyOku()
        init()


    }

    private fun serverkeyOku() {
        var ref = FirebaseDatabase.getInstance().reference
            .child("server")
            .orderByValue()
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }
            override fun onDataChange(snapshot: DataSnapshot) {
                var singleSnapShot = snapshot?.children?.iterator()?.next()
                SERVER_KEY = singleSnapShot?.getValue().toString()

            }


        })
    }

    private fun init() {

        etYeniMesaj.setOnClickListener {

            rvMesajlar.smoothScrollToPosition(myAdapter!!.itemCount - 1)

        }

        imgBackSohbetOdasiActivity.setOnClickListener {

            super.onBackPressed()
        }



        imgMesajGonder.setOnClickListener {

            if (!etYeniMesaj.text.toString().equals("")) {

                var yazilanMesaj = etYeniMesaj.text.toString()

                var kaydedilecekMesaj = SohbetMesaj()
                kaydedilecekMesaj.mesaj = yazilanMesaj
                kaydedilecekMesaj.kullanici_id = FirebaseAuth.getInstance().currentUser?.uid
                kaydedilecekMesaj.timestamp = getMesajTarihi()

                var referans = FirebaseDatabase.getInstance().reference
                    .child("sohbet_odasi")
                    .child(secilenSohbetOdasiId)
                    .child("sohbet_odasi_mesajlari")

                var yeniMesajID = referans.push().key
                referans.child(yeniMesajID!!)
                    .setValue(kaydedilecekMesaj)


                var retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                var myInterface = retrofit.create(FCMInterface::class.java)

                var headers = HashMap<String, String>()
                headers.put("Content-Type", "application/json")
                headers.put("Authorization", "key=" + SERVER_KEY)

                var ref = FirebaseDatabase.getInstance().reference
                    .child("sohbet_odasi")
                    .child(secilenSohbetOdasiId)
                    .child("odadaki_kullanicilar")
                    .orderByKey()
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(error: DatabaseError) {

                        }

                        override fun onDataChange(snapshot: DataSnapshot) {

                            for (kullaniciID in snapshot?.children!!) {

                                var id = kullaniciID?.key

                                if (!id.equals(FirebaseAuth.getInstance().currentUser?.uid)) {


                                    var ref = FirebaseDatabase.getInstance().reference
                                        .child("kullanici")
                                        .orderByKey()
                                        .equalTo(id)
                                        .addListenerForSingleValueEvent(object : ValueEventListener {
                                            override fun onCancelled(error: DatabaseError) {

                                            }

                                            override fun onDataChange(snapshot: DataSnapshot) {

                                                var tekKullanici=snapshot?.children?.iterator()?.next()
                                                var kullaniciMesajToken=tekKullanici?.getValue(Kullanici::class.java)?.mesaj_token

                                                var data = FCMModel.Data("Yeni Mesaj", etYeniMesaj.text.toString(),"sohbet", secilenSohbetOdasiId)
                                                var to = kullaniciMesajToken

                                                var bildirim: FCMModel = FCMModel(to!!, data)


                                                var istek = myInterface.bildirimGonder(headers, bildirim)
                                                istek.enqueue(object :
                                                    Callback<Response<FCMModel>> {
                                                    override fun onResponse(call: Call<Response<FCMModel>>?, response: Response<Response<FCMModel>>?) {
                                                    }
                                                    override fun onFailure(call: Call<Response<FCMModel>>?, t: Throwable?) {
                                                    }
                                                })
                                            }
                                        })
                                }
                            }
                        }
                    })
            }
            etYeniMesaj.setText("")

        }
    }

    private fun getMesajTarihi(): String {

        var sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale("tr"))
        return sdf.format(Date())


    }

    private fun bildirimeGoreListele() {

        var gelenintent=intent

        if(intent.hasExtra("sohbet_odasi_id")){
            secilenSohbetOdasiId = intent.getStringExtra("sohbet_odasi_id").toString()
            baslatMesajListener()
        }


    }

    private fun sohbetOdasiniOgren() {
        secilenSohbetOdasiId = intent.getStringExtra("sohbet_odasi_id").toString()
        var secilenSohbetOdasiAdi:String?=null
        var ref=FirebaseDatabase.getInstance().reference
            .child("sohbet_odasi")
            .child(secilenSohbetOdasiId)
            .child("sohbetodasi_adi")
            .addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onCancelled(error: DatabaseError) {

                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    secilenSohbetOdasiAdi=snapshot?.value.toString()
                    tvSohbetOdasiAdi.text=secilenSohbetOdasiAdi
                }

            })

        baslatMesajListener()
    }

    var mValueEventListener: ValueEventListener = object : ValueEventListener {
        override fun onCancelled(error: DatabaseError) {
        }

        override fun onDataChange(snapshot: DataSnapshot) {
            sohbetOdasindakiMesajlariGetir()
            if(activityAcikMi)
                gorunenMesajSayisiniGuncelle(snapshot?.childrenCount?.toInt())
        }


    }

    private fun sohbetOdasindakiMesajlariGetir() {

        if (tumMesajlar == null) {
            tumMesajlar = ArrayList<SohbetMesaj>()
            mesajIDSet = HashSet<String>()
        }


        mMesajReferans = FirebaseDatabase.getInstance().getReference()

        var sorgu = mMesajReferans?.child("sohbet_odasi")?.child(secilenSohbetOdasiId)?.child("sohbet_odasi_mesajlari")!!
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {

                }

                override fun onDataChange(snapshot: DataSnapshot) {

                    for (tekMesaj in snapshot!!.children) {

                        var geciciMesaj = SohbetMesaj()
                        var kullaniciID = tekMesaj.getValue(SohbetMesaj::class.java)!!.kullanici_id


                        if (!mesajIDSet!!.contains(tekMesaj.key)) {

                            mesajIDSet!!.add(tekMesaj.key!!)

                            if (kullaniciID != null) {
                                geciciMesaj.mesaj = tekMesaj.getValue(SohbetMesaj::class.java)!!.mesaj
                                geciciMesaj.kullanici_id = tekMesaj.getValue(SohbetMesaj::class.java)!!.kullanici_id
                                geciciMesaj.timestamp = tekMesaj.getValue(SohbetMesaj::class.java)!!.timestamp

                                var kullaniciDetaylari = mMesajReferans?.child("kullanici")?.orderByKey()?.equalTo(kullaniciID)
                                kullaniciDetaylari?.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onCancelled(error: DatabaseError) {

                                    }

                                    override fun onDataChange(snapshot: DataSnapshot) {

                                        if(snapshot?.exists()!!){
                                            var bulunanKullanici = snapshot.children?.iterator()?.next()
                                            geciciMesaj.profil_resmi = bulunanKullanici?.getValue(Kullanici::class.java)?.profil_resmi
                                            geciciMesaj.adi = bulunanKullanici?.getValue(Kullanici::class.java)?.isim

                                            myAdapter?.notifyDataSetChanged()
                                        }
                                    }


                                })

                                tumMesajlar?.add(geciciMesaj)
                                myAdapter?.notifyDataSetChanged()
                                rvMesajlar.scrollToPosition(myAdapter!!.itemCount - 1)

                            } else {

                                geciciMesaj.mesaj = tekMesaj.getValue(SohbetMesaj::class.java)!!.mesaj
                                geciciMesaj.timestamp = tekMesaj.getValue(SohbetMesaj::class.java)!!.timestamp
                                geciciMesaj.profil_resmi = ""
                                geciciMesaj.adi = ""
                                tumMesajlar?.add(geciciMesaj)
                                myAdapter?.notifyDataSetChanged()
                            }
                        }
                    }
                }
            })


        if (myAdapter == null) {
            initMesajlarListesi()
        }


    }

    private fun initMesajlarListesi() {
        myAdapter = SohbetMesajRecyclerviewAdapter(this, tumMesajlar!!)
        rvMesajlar.adapter = myAdapter
        rvMesajlar.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rvMesajlar.scrollToPosition(myAdapter?.itemCount!! - 1)
    }


    private fun baslatMesajListener() {
        mMesajReferans = FirebaseDatabase.getInstance().getReference().child("sohbet_odasi")
            .child(secilenSohbetOdasiId)
            .child("sohbet_odasi_mesajlari")

        mMesajReferans?.addValueEventListener(mValueEventListener)
    }

    private fun baslatFirebaseAuthListener() {
        mAuthListener = object : FirebaseAuth.AuthStateListener {
            override fun onAuthStateChanged(p0: FirebaseAuth) {

                var kullanici = p0.currentUser

                if (kullanici == null) {
                    var intent = Intent(this@SohbetOdasiActivity, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }


            }


        }
    }

    private fun gorunenMesajSayisiniGuncelle(toplamMesaj: Int?) {

        var ref=FirebaseDatabase.getInstance().reference
            .child("sohbet_odasi")
            .child(secilenSohbetOdasiId)
            .child("odadaki_kullanicilar")
            .child(FirebaseAuth.getInstance().currentUser?.uid!!)
            .child("okunan_mesaj_sayisi")
            .setValue(toplamMesaj)

    }


    override fun onStart() {
        super.onStart()
        activityAcikMi=true
        FirebaseAuth.getInstance().addAuthStateListener(mAuthListener!!)
    }

    override fun onPause() {
        super.onPause()
        activityAcikMi=false

    }

    override fun onStop() {
        super.onStop()
        activityAcikMi=false
        if (mAuthListener != null) {
            FirebaseAuth.getInstance().removeAuthStateListener(mAuthListener!!)
        }
    }

    override fun onResume() {
        super.onResume()
        kullaniciKontrolEt()
    }

    private fun kullaniciKontrolEt() {
        var kullanici = FirebaseAuth.getInstance().currentUser

        if (kullanici == null) {
            var intent = Intent(this@SohbetOdasiActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }


}