package com.example.radaapplication

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.utils.widget.MotionButton
import com.example.radaapplication.model.User
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

private const val TAG = "FIRESTORE_LogIn"
class MainActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var phoneText: EditText
   // private lateinit var passText: EditText
    private lateinit var progress: ProgressBar
    private lateinit var regTxt : TextView
    private lateinit var loginBtn : MotionButton
    //Creating member variables of FirebaseDatabase and DatabaseReference
    private lateinit var db: FirebaseFirestore


    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    lateinit var verificationID: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mAuth = FirebaseAuth.getInstance()
        phoneText = findViewById(R.id.editTextMobileLog)

       // passText = findViewById(R.id.editTextPasswordLog)

        progress = findViewById(R.id.progress)
        regTxt = findViewById(R.id.regTxt)
        loginBtn = findViewById(R.id.cirLoginButton)
        //Get instance of FireStore Database
        db = FirebaseFirestore.getInstance()

        val currentUser = mAuth.currentUser
        if (currentUser != null) {
            startActivity(Intent(applicationContext, HomeActivity::class.java))
            finish()
        }

        loginBtn.setOnClickListener{
           // signIn()
            var number = phoneText.text.toString().trim()
            if (number.length != 10 && number.isEmpty()) {
                phoneText.error = " unValid Phone Number "
                Toast.makeText(this, "Enter Phone Number ", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
           } else {
                val userRef = db.collection("User")
                val query = userRef.whereEqualTo("phone", number)

                query.get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        if (!task.result.isEmpty) {
                            Log.d(TAG, "$number already exists.")
                            Toast.makeText(this, "Welcome Back", Toast.LENGTH_SHORT).show()
                            number = "+218$number"
                            sendVersioncode(number)
                            showAlertDialog()
                        } else {
                            Log.d(TAG, "$number doesn't exist.")
                            Toast.makeText(this, "Your account doesn't exist", Toast.LENGTH_SHORT).show()

                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.exception)
                    }
                }
           }

        }

    }
    private fun sendVersioncode(number: String) {
        val options = PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber(number) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this) // Activity (for callback binding)
            .setCallbacks(mCallbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)

    }

    private val mCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks =
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                startActivity(Intent(applicationContext, HomeActivity::class.java))
                finish()
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Toast.makeText(this@MainActivity, "Verification Failed", Toast.LENGTH_SHORT).show()
            }

            override fun onCodeSent(
                s: String,
                token: PhoneAuthProvider.ForceResendingToken,
            ) {
                super.onCodeSent(s, token)
                verificationID = s
                resendToken = token
                Toast.makeText(this@MainActivity, "Code sent", Toast.LENGTH_SHORT).show()

            }
        }
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    startActivity(Intent(applicationContext, HomeActivity::class.java))
                    overridePendingTransition(R.anim.slide_in_right, R.anim.stay)
                    progress.visibility = View.VISIBLE
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

    private fun showAlertDialog() {
        val placeFormView = LayoutInflater.from(this).inflate(R.layout.dialog_code_verify,null)
        val dialog =
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.enterCode_dialog))
                .setView(placeFormView)
                .setPositiveButton(getString(R.string.done_dialog), null)
                .show()

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener{
            val otp= placeFormView.findViewById<EditText>(R.id.id_otp).text.toString()

            if(otp.isNotEmpty()){
                val credential : PhoneAuthCredential = PhoneAuthProvider.getCredential(
                    verificationID, otp)
                signInWithPhoneAuthCredential(credential)
            }else{
                Toast.makeText(this,"Enter OTP",Toast.LENGTH_SHORT).show()
                return@setOnClickListener }


        }}




//    private fun signIn() {
//        val phoneNum = phoneText.text.toString().trim()
//        //val passWord = passText.text.toString().trim()
//
//        val userRef = db.collection("User")
//        val query = userRef.whereEqualTo("phone" , phoneNum)
//
//        query.get().addOnSuccessListener { snapshot ->
//            snapshot?.forEach {
//
//                val user = it.toObject(User::class.java)
//
//                startActivity(Intent(this,HomeActivity::class.java))
//                overridePendingTransition(R.anim.slide_in_right, R.anim.stay)
//                progress.visibility = View.VISIBLE
//
//            }
//        }
//            .addOnFailureListener { exception->
//                Log.w(TAG ,"Error getting current user name $exception")
//            }
//    }


    fun onRegClick(view: View) {
        startActivity(Intent(this, RegActivity::class.java))
        overridePendingTransition(R.anim.slide_in_right, R.anim.stay)
        progress.visibility = View.VISIBLE
    }
}