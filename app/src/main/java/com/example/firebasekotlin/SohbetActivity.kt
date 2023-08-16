package com.example.firebasekotlin

import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasekotlin.adapters.SohbetOdasiRecyclerViewAdapter
import com.example.firebasekotlin.databinding.ActivitySohbetBinding
import com.example.firebasekotlin.dialogs.YeniSohbetOdasiFDialogFragment
import com.example.firebasekotlin.model.SohbetMesaj
import com.example.firebasekotlin.model.SohbetOdasi
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SohbetActivity : AppCompatActivity() {
    private lateinit var fabYeniSohbetOdasi: FloatingActionButton
    private lateinit var rvSohbetOdalari: RecyclerView
    private lateinit var imgBackSohbetActivity: ImageView

    private var tumSohbetOdalari: ArrayList<SohbetOdasi> = ArrayList()
    private var myAdapter: SohbetOdasiRecyclerViewAdapter? = null
    private val sohbetOdasiIDSet: HashSet<String> = HashSet()

    private var mSohbetOdasiReferans: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_sohbet)

        fabYeniSohbetOdasi = findViewById(R.id.fabYeniSohbetOdasi)
        rvSohbetOdalari = findViewById(R.id.rvSohbetOdalari)
        imgBackSohbetActivity = findViewById(R.id.imgBackSohbetActivity)

        baslatOdaListener()
        init()
    }

    fun init() {
        tumSohbetOdalariniGetir()

        fabYeniSohbetOdasi.setOnClickListener {
            val dialog = YeniSohbetOdasiFDialogFragment()
            dialog.show(supportFragmentManager, "gosteryenisohbetodasi")
        }

        imgBackSohbetActivity.setOnClickListener {
            onBackPressed()
        }
    }

    private val mValueEventListener: ValueEventListener = object : ValueEventListener {
        override fun onCancelled(error: DatabaseError) {}

        override fun onDataChange(snapshot: DataSnapshot) {
            myAdapter?.notifyDataSetChanged()
            tumSohbetOdalariniGetir()
        }
    }

    private fun baslatOdaListener() {
        mSohbetOdasiReferans = FirebaseDatabase.getInstance().getReference().child("sohbet_odasi")
        mSohbetOdasiReferans?.addValueEventListener(mValueEventListener)
    }

    private fun tumSohbetOdalariniGetir() {
        val ref = FirebaseDatabase.getInstance().reference
        val sorgu = ref.child("sohbet_odasi")

        sorgu.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(snapshot: DataSnapshot) {
                for (tekSohbetOdasi in snapshot.children) {
                    val sohbetOdasiID = tekSohbetOdasi.key.toString()
                    if (!sohbetOdasiIDSet.contains(sohbetOdasiID)) {
                        sohbetOdasiIDSet.add(sohbetOdasiID)

                        val nesneMap = tekSohbetOdasi.getValue() as HashMap<String, Any>
                        val oAnkiSohbetOdasi = SohbetOdasi(
                            sohbetodasi_id = sohbetOdasiID,
                            sohbetodasi_adi = nesneMap["sohbetodasi_adi"].toString(),
                            olusturan_id = nesneMap["olusturan_id"].toString(),
                            sohbet_odasi_mesajlari = ArrayList()
                        )

                        val tumMesajlar = ArrayList<SohbetMesaj>()
                        for (mesajlar in tekSohbetOdasi.child("sohbet_odasi_mesajlari").children) {
                            val okunanMesaj = mesajlar.getValue(SohbetMesaj::class.java) ?: SohbetMesaj()
                            tumMesajlar.add(okunanMesaj)
                        }
                        oAnkiSohbetOdasi.sohbet_odasi_mesajlari = tumMesajlar
                        tumSohbetOdalari.add(oAnkiSohbetOdasi)
                    }
                }
                if (myAdapter == null) {
                    sohbetOdalariListele()
                }
            }
        })
    }

    private fun sohbetOdalariListele() {
        myAdapter = SohbetOdasiRecyclerViewAdapter(this@SohbetActivity, tumSohbetOdalari)
        rvSohbetOdalari.adapter = myAdapter
        rvSohbetOdalari.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    }
}
