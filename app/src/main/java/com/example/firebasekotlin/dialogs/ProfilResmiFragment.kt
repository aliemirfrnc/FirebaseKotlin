package com.example.firebasekotlin.dialogs

import android.app.Activity
import android.util.Log
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.firebasekotlin.R

class ProfilResmiFragment : DialogFragment() {

    lateinit var tvKameradanFoto: TextView
    lateinit var tvGaleridenFoto: TextView

    interface onProfilResimListener {
        fun getResimYolu(resimPath: Uri?)
        fun getResimBitmap(bitmap: Bitmap)
    }
    lateinit var mProfilResimListener: onProfilResimListener

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val v = inflater.inflate(R.layout.fragment_profil_resmi, container, false)
        tvKameradanFoto = v.findViewById(R.id.tvKameradanFoto)
        tvGaleridenFoto = v.findViewById(R.id.tvGaleridenFoto)

        tvGaleridenFoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, 100)
        }
        tvKameradanFoto.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, 200)
        }
        return v
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        if (requestCode == 100 && resultCode == Activity.RESULT_OK && data != null) {
            val galeridenSecilenResimYolu: Uri? = data.data
            Log.e("Ali", "galeridenSecilenResimYolu: $galeridenSecilenResimYolu")
            mProfilResimListener.getResimYolu(galeridenSecilenResimYolu)
        }
        // KAMERADAN
        else if (requestCode == 200 && resultCode == Activity.RESULT_OK && data != null) {
            val kameradanCekilenResim: Bitmap? =
                data.extras?.get("data") as Bitmap?
            if (kameradanCekilenResim != null) {
                mProfilResimListener.getResimBitmap(kameradanCekilenResim)
            }
            dialog?.dismiss()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            mProfilResimListener = context as onProfilResimListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement onProfilResimListener")
        }
    }

}
