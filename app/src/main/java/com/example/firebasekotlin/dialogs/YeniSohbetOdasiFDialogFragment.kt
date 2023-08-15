package com.example.firebasekotlin.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.firebasekotlin.R
import com.example.firebasekotlin.SohbetActivity
import com.example.firebasekotlin.model.Kullanici
import com.example.firebasekotlin.model.SohbetMesaj
import com.example.firebasekotlin.model.SohbetOdasi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class YeniSohbetOdasiFDialogFragment : DialogFragment() {

    private lateinit var etSohbetOdasiAdi: EditText
    private lateinit var btnSohbetOdasiOlustur: Button
    private lateinit var seekBarSeviye: SeekBar
    private lateinit var tvKullaniciSeviye: TextView
    private var mSeekProgress = 0
    private var kullaniciSeviye = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_yeni_sohbet_odasi_dialog, container, false)

        btnSohbetOdasiOlustur = view.findViewById(R.id.btnYeniSohbetodasiOlustur)
        etSohbetOdasiAdi = view.findViewById(R.id.etYeniSohbetOdasiAdi)
        seekBarSeviye = view.findViewById(R.id.seekBarSeviye)
        tvKullaniciSeviye = view.findViewById(R.id.tvYeniSohbetSeviye)
        tvKullaniciSeviye.text = mSeekProgress.toString()

        seekBarSeviye.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                mSeekProgress = progress
                tvKullaniciSeviye.text = mSeekProgress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        kullaniciSeviyeBilgisiniGetir()

        btnSohbetOdasiOlustur.setOnClickListener {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

            if (!etSohbetOdasiAdi.text.isNullOrEmpty()) {
                if (kullaniciSeviye >= mSeekProgress) {
                    val ref = FirebaseDatabase.getInstance().reference
                    val sohbetOdasiID = ref.child("sohbet_odasi").push().key

                    val yeniSohbetOdasi = SohbetOdasi().apply {
                        olusturan_id = currentUserId
                        seviye = mSeekProgress.toString()
                        sohbetodasi_adi = etSohbetOdasiAdi.text.toString()
                        sohbetodasi_id = sohbetOdasiID
                    }

                    ref.child("sohbet_odasi").child(sohbetOdasiID!!).setValue(yeniSohbetOdasi)

                    val mesajID = ref.child("sohbet_odasi").push().key
                    val karsilamaMesaji = SohbetMesaj().apply {
                        mesaj = "Sohbet odasına hoşgeldiniz"
                        timestamp = getMesajTarihi()
                    }

                    ref.child("sohbet_odasi")
                        .child(sohbetOdasiID)
                        .child("sohbet_odasi_mesajlari")
                        .child(mesajID!!)
                        .setValue(karsilamaMesaji)

                    Toast.makeText(activity, "Sohbet Odası Oluşturuldu", Toast.LENGTH_SHORT).show()
                    (activity as? SohbetActivity)?.init()
                    dialog?.dismiss()
                } else {
                    Toast.makeText(
                        activity,
                        "Seviyeniz: $kullaniciSeviye ve bu seviyeden yukarı sohbet odası oluşturamazsınız",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(activity, "Sohbet odası adını yazınız", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun kullaniciSeviyeBilgisiniGetir() {
        val ref = FirebaseDatabase.getInstance().reference
        val sorgu = ref.child("kullanici").orderByKey().equalTo(FirebaseAuth.getInstance().currentUser?.uid)
        sorgu.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                for (tekKayit in snapshot.children) {
                    kullaniciSeviye = tekKayit.getValue(Kullanici::class.java)?.seviye?.toIntOrNull() ?: 0
                }
            }
        })
    }

    private fun getMesajTarihi(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }
}
