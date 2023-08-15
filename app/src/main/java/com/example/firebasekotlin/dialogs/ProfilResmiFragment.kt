package com.example.firebasekotlin.dialogs

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.firebasekotlin.R

class ProfilResmiFragment : DialogFragment() {

    lateinit var tvKameradanFoto:TextView
    lateinit var tvGaleridenFoto: TextView

    interface onProfilResimListener{
        fun getResimYolu(resimPath: Uri?)
        fun getResimBitmap(bitmap: Bitmap)
    }
    lateinit var mProfilResimListener: onProfilResimListener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        var v = inflater!!.inflate(R.layout.fragment_profil_resmi, container, false)
        tvKameradanFoto = v.findViewById(R.id.tvKameradanFoto)
        tvGaleridenFoto = v.findViewById(R.id.tvGaleridenFoto)

        tvGaleridenFoto.setOnClickListener{
            var intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, 100)
        }
        tvKameradanFoto.setOnClickListener{
            var intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, 200)
        }
        return v
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //GALERİDEN
        if (requestCode == 100 && resultCode == Activity.RESULT_OK && data != null){
            var galeridenSecilenResimYolu = data.data
            Log.e("Ali","galeridenSecilenResimYolu")
            mProfilResimListener.getResimYolu(galeridenSecilenResimYolu)
        }
        //KAMERADAN
        else if(requestCode == 100 && resultCode == Activity.RESULT_OK && data != null){
            var kameradanCekilenResim:Bitmap
            kameradanCekilenResim = data.extras!!.get("data") as Bitmap
            mProfilResimListener.getResimBitmap(kameradanCekilenResim)
            dialog?.dismiss()
        }
    }
    override fun onAttach(context: Context) {
        mProfilResimListener = activity as onProfilResimListener
        super.onAttach(context)
    }



}