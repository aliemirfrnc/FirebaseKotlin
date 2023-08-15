package com.example.firebasekotlin.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasekotlin.R
import com.example.firebasekotlin.SohbetOdasiActivity
import com.example.firebasekotlin.model.Kullanici
import com.example.firebasekotlin.model.SohbetOdasi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class SohbetOdasiRecyclerViewAdapter(private val mActivity: AppCompatActivity, private val tumSohbetOdalari: ArrayList<SohbetOdasi>) : RecyclerView.Adapter<SohbetOdasiRecyclerViewAdapter.SohbetOdasiHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SohbetOdasiHolder {
        val inflater = LayoutInflater.from(parent.context)
        val tekSatirSohbetOdalari = inflater.inflate(R.layout.tek_satir_sohbet_odasi, parent, false)

        return SohbetOdasiHolder(tekSatirSohbetOdalari)
    }

    override fun getItemCount(): Int {
        return tumSohbetOdalari.size
    }

    override fun onBindViewHolder(holder: SohbetOdasiHolder, position: Int) {
        val oAnOlusturulanSohbetOdasi = tumSohbetOdalari[position]
        holder.setData(oAnOlusturulanSohbetOdasi)
    }

    inner class SohbetOdasiHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tekSatirSohbetOdasi=itemView as ConstraintLayout
        private val sohbetOdasiResim = itemView.findViewById<ImageView>(R.id.imgProfilResmiSohbetOdasi)
        private val sohbetOdasiMesajSayisi = itemView.findViewById<TextView>(R.id.tvMesajSayisi)
        private val sohbetOdasiAdi = itemView.findViewById<TextView>(R.id.tvSohbetOdasiAdi)


        fun setData(oAnOlusturulanSohbetOdasi: SohbetOdasi) {
            sohbetOdasiAdi.text = oAnOlusturulanSohbetOdasi.sohbetodasi_adi
            sohbetOdasiResim.setOnClickListener{
                var intent = Intent(mActivity, SohbetOdasiActivity::class.java)
                intent.putExtra("sohbet_odasi_id", oAnOlusturulanSohbetOdasi.sohbetodasi_id)
                mActivity.startActivity(intent)
            }


            tekSatirSohbetOdasi.setOnClickListener {
                var kullanici = FirebaseAuth.getInstance().currentUser
                var seviye = 0
                var ref = FirebaseDatabase.getInstance().reference
                    .child("kullanici")
                    .orderByKey()
                    .equalTo(kullanici?.uid)
                ref.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (tekKullanici in snapshot.children) {
                            var okunanKullanici = tekKullanici.getValue(Kullanici::class.java)
                            var seviye = okunanKullanici?.seviye?.toInt() ?: 0

                            if (oAnOlusturulanSohbetOdasi.seviye?.toInt() ?: 0 <= seviye) {
                                kullaniciyiSohbetOdasinaKaydet(oAnOlusturulanSohbetOdasi)
                                var intent = Intent(mActivity, SohbetOdasiActivity::class.java)
                                intent.putExtra("sohbet_odasi_id", oAnOlusturulanSohbetOdasi.sohbetodasi_id)
                                mActivity.startActivity(intent)
                            } else {
                                Toast.makeText(mActivity, "Seviyeniz yeterli değil", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                })
            }

            sohbetOdasiMesajSayisi.text = "Toplam Mesaj Sayısı: " + (oAnOlusturulanSohbetOdasi.sohbet_odasi_mesajlari?.size ?: 0).toString()

            var ref = FirebaseDatabase.getInstance().reference
            var sorgu = ref.child("kullanici")
                .orderByKey()
                .equalTo(oAnOlusturulanSohbetOdasi.olusturan_id)
            sorgu.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    for (kullanici in snapshot.children) {
                        val profilResmiPath = kullanici.getValue(Kullanici::class.java)?.profil_resmi.toString()
                        if (profilResmiPath.isNullOrEmpty()) {
                            Picasso.get().load(R.drawable.defaultprofilepic).resize(72, 72).into(sohbetOdasiResim)
                        } else {
                            Picasso.get().load(profilResmiPath).resize(72, 72).into(sohbetOdasiResim)
                        }
                    }
                }
            })
        }

        private fun kullaniciyiSohbetOdasinaKaydet(oAnOlusturulanSohbetOdasi: SohbetOdasi) {
            val ref = FirebaseDatabase.getInstance().reference
                .child("sohbet_odasi")
                .child(oAnOlusturulanSohbetOdasi.sohbetodasi_id!!)
                .child("odadaki_kullanicilar")
                .child(FirebaseAuth.getInstance().currentUser?.uid!!)
                .child("okunan_mesaj_sayisi")
                .setValue((oAnOlusturulanSohbetOdasi.sohbet_odasi_mesajlari?.size ?: 0).toString())
        }
    }
}
