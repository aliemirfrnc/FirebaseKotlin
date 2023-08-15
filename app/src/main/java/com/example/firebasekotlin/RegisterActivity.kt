package com.example.firebasekotlin

import android.content.Intent

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.example.firebasekotlin.model.Kullanici
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class RegisterActivity : AppCompatActivity() {

    lateinit var etMail:EditText
    lateinit var etSifre:EditText
    lateinit var etSifreTekrar:EditText
    lateinit var btnKayitOl:Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        etMail = findViewById(R.id.etMail)
        etSifre = findViewById(R.id.etSifre)
        etSifreTekrar = findViewById(R.id.etSifreTekrar)
        btnKayitOl = findViewById(R.id.btnKayitOl)

        btnKayitOl.setOnClickListener {
            if (etMail.text.isNotEmpty() && etSifre.text.isNotEmpty() && etSifreTekrar.text.isNotEmpty()) {
                if (etSifre.text.toString().equals(etSifreTekrar.text.toString())) {
                    progressBarGoster()
                    yeniUyeKayit(etMail.text.toString(), etSifre.text.toString())
                } else {
                    Toast.makeText(this, "Şifreler aynı değil", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Boş alanları doldurunuz", Toast.LENGTH_SHORT).show()
            }
        }

    }
    private fun yeniUyeKayit(mail: String,sifre: String){
        etMail = findViewById(R.id.etMail)
        etSifre = findViewById(R.id.etSifre)
        etSifreTekrar = findViewById(R.id.etSifreTekrar)
        btnKayitOl = findViewById(R.id.btnKayitOl)
        progressBarGoster()

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(mail,sifre).addOnCompleteListener(object :
            OnCompleteListener<AuthResult> {
            override fun onComplete(p0: Task<AuthResult>) {
                if (p0.isSuccessful()){
                    Toast.makeText(this@RegisterActivity, "Kayıt Başarılı!", Toast.LENGTH_SHORT).show()
                    onayMailiGonder()

                    val veriTabaninaEklenecekKullanici = Kullanici(
                        isim = etMail.text.toString().substring(0, etMail.text.toString().indexOf("@")),
                        kullanici_id = FirebaseAuth.getInstance().currentUser?.uid!!,
                        profil_resmi = "",
                        telefon = "123",
                        seviye = "1",
                        mesaj_token = "1")

                    FirebaseDatabase.getInstance().reference
                        .child("kullanici")
                        .child(FirebaseAuth.getInstance().currentUser?.uid!!)
                        .setValue(veriTabaninaEklenecekKullanici).addOnCompleteListener {task ->
                            if (task.isSuccessful){
                                Toast.makeText(this@RegisterActivity, "Kayıt Başarılı!", Toast.LENGTH_SHORT).show()
                                FirebaseAuth.getInstance().signOut()
                                loginSayfasinaYonlendir()
                            } else {
                                Toast.makeText(this@RegisterActivity, "Kayıt Başarısız!", Toast.LENGTH_SHORT).show()
                            }
                        }
                }else{

                    Toast.makeText(this@RegisterActivity, "Kayıt Başarısız!"+p0.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
        })
        progressBarGizle()
    }

    private fun onayMailiGonder(){
        var kullanici = FirebaseAuth.getInstance().currentUser

        if (kullanici != null){
            kullanici.sendEmailVerification()
                .addOnCompleteListener(object:OnCompleteListener<Void>{
                     override fun onComplete(p0: Task<Void>) {
                         if (p0.isSuccessful()){
                             Toast.makeText(this@RegisterActivity, "E-Mail Onayı Gönderildi!", Toast.LENGTH_SHORT).show()
                         }else{
                             Toast.makeText(this@RegisterActivity, "E-Mail Onayı Gönderilemedi!"+p0.exception?.message, Toast.LENGTH_SHORT).show()
                         }
                     }
                })
        }
    }
    private fun progressBarGoster(){
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        progressBar.visibility = View.VISIBLE
    }
    private fun progressBarGizle(){
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        progressBar.visibility = View.INVISIBLE
    }
    private fun loginSayfasinaYonlendir(){
        var intent = Intent(this@RegisterActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}