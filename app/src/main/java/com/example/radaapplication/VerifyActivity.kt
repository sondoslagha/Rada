package com.example.radaapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class VerifyActivity : RegActivity() {

    private lateinit var auth: FirebaseAuth

    //Creating member variables of FirebaseDatabase and DatabaseReference
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify)
        auth=FirebaseAuth.getInstance()

        //Get instance of FireStore Database
        db = FirebaseFirestore.getInstance()

        val storedVerificationId=intent.getStringExtra("storedVerificationId")
//        intent.putExtra("username", name)
//        intent.putExtra("PhoneNumber", phoneNum)
//        intent.putExtra("Password", passWord)

//        Reference
        val verify=findViewById<Button>(R.id.verifyBtn)
        val otpGiven=findViewById<EditText>(R.id.id_otp)

        verify.setOnClickListener{
            val otp=otpGiven.text.toString().trim()
            if(otp.isNotEmpty()){
                val credential : PhoneAuthCredential = PhoneAuthProvider.getCredential(
                    storedVerificationId.toString(), otp)
                signInWithPhoneAuthCredential(credential)
            }else{
                Toast.makeText(this,"Enter OTP", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    val uid = auth.currentUser?.uid.toString()
                  val  usermane =  nameEdtTxt.text.toString().trim()
                   // newUserAccount(uid , usermane)
                    startActivity(Intent(applicationContext, HomeActivity::class.java))
                    finish()

                } else {

                    // Sign in failed, display a message and update the UI
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {

                        // The verification code entered was invalid
                        Toast.makeText(this,"Invalid OTP",Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

}