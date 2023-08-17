package com.example.firebasekotlin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    lateinit var mAuthStateListener: FirebaseAuth.AuthStateListener


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        initMyAuthStateListener()


        val tvKayitOl = findViewById<TextView>(R.id.tvKayitOl)
        val etMail = findViewById<EditText>(R.id.etMail)
        val etSifre = findViewById<EditText>(R.id.etSifre)
        val btnGirisYap = findViewById<Button>(R.id.btnGirisYap)

        tvKayitOl.setOnClickListener {
            var intent = Intent(this,RegisterActivity::class.java)
            startActivity(intent)
        }

        btnGirisYap.setOnClickListener {
            if (etMail.text.isNotEmpty() && etSifre.text.isNotEmpty()){
                progressBarGoster()


                FirebaseAuth.getInstance().signInWithEmailAndPassword(etMail.text.toString(), etSifre.text.toString())
                    .addOnCompleteListener(object : OnCompleteListener<AuthResult>{
                        override fun onComplete(p0: Task<AuthResult>) {
                            if (p0.isSuccessful){
                                progressBarGizle()
                            }else{
                                progressBarGizle()

                                Toast.makeText(this@LoginActivity, "Hatalı Giriş:"+p0.exception?.message, Toast.LENGTH_SHORT).show()

                            }
                        }
                    })
                }
            }
    }
    private fun progressBarGoster(){
        val progressBarLogin = findViewById<ProgressBar>(R.id.progressBarLogin)
        progressBarLogin.visibility = View.VISIBLE
    }
    private fun progressBarGizle(){
        val progressBarLogin = findViewById<ProgressBar>(R.id.progressBarLogin)
        progressBarLogin.visibility = View.INVISIBLE
    }

    private fun initMyAuthStateListener(){
        mAuthStateListener = object : FirebaseAuth.AuthStateListener{
            override fun onAuthStateChanged(p0: FirebaseAuth) {
                var kullanici = p0.currentUser
                if (kullanici!= null){
                    if (kullanici.isEmailVerified){
                        Toast.makeText(this@LoginActivity,"Mail Onaylanmış Giriş Yapılabilir", Toast.LENGTH_SHORT).show()
                        var intent=Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }else{
                        Toast.makeText(this@LoginActivity,"Mail Adresiniz Onaylanmamış!", Toast.LENGTH_SHORT).show()
                        FirebaseAuth.getInstance().signOut()
                    }
                }
            }
        }
    }
    override fun onStart() {
        super.onStart()
        FirebaseAuth.getInstance().addAuthStateListener(mAuthStateListener)
    }

    override fun onStop() {
        super.onStop()
        FirebaseAuth.getInstance().removeAuthStateListener(mAuthStateListener)
    }
}