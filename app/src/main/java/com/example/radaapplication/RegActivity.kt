package com.example.radaapplication

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit


private const val TAG = "FIRESTORE"
open class RegActivity : AppCompatActivity() {

//    lateinit var storedVerificationId:String
//    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    private lateinit var auth: FirebaseAuth

    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    lateinit var verificationID: String

    //Creating member variables of FirebaseDatabase and DatabaseReference
    private lateinit var db: FirebaseFirestore

    //Creating member variable for user info
    private lateinit var progress : ProgressBar
    lateinit var nameEdtTxt: EditText
     lateinit var phoneEdtTxt: EditText
     lateinit var passEdtTxt: EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reg)


        auth = FirebaseAuth.getInstance()

        //Get instance of FireStore Database
        db = FirebaseFirestore.getInstance()

//        Reference
        val regIn = findViewById<Button>(R.id.regButton)
        progress = findViewById(R.id.progress)
        nameEdtTxt = findViewById(R.id.editTextName)
        phoneEdtTxt = findViewById(R.id.editTextMobile)
        passEdtTxt = findViewById(R.id.editTextPassword)


        val currentUser = auth.currentUser
        if (currentUser != null) {
            startActivity(Intent(applicationContext, HomeActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.stay)
            progress.visibility = View.VISIBLE
            finish()
        }

        regIn.setOnClickListener {

            val number = phoneEdtTxt.text.toString().trim()
            val name = nameEdtTxt.text.toString().trim()
            val password = passEdtTxt.text.toString().trim()

            val userRef = db.collection("User")
            val query = userRef.whereEqualTo("phone", number)

            query.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (!task.result.isEmpty) {
                        Log.d(TAG, "$number already exists.")
                        Toast.makeText(this, "User already exist...!", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.d(TAG, "$number doesn't exist.")
                        Toast.makeText(this, "New User In", Toast.LENGTH_SHORT).show()
                        checkValidation(number, name, password)
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.exception)
                }
            }
        }
        }

    private fun checkValidation(number: String, name: String, password: String) {

        if (name.isEmpty()) {
            nameEdtTxt.error = "Name Is Empty!"
            return

        }
        if (password.length <= 6) {
            passEdtTxt.error = "Password must be at least 6 !"
            return
        }
        if (number.length != 10 && number.isEmpty()) {
            phoneEdtTxt.error = " unValid Phone Number "
            Toast.makeText(this, "Enter Phone Number ", Toast.LENGTH_SHORT).show()
        } else {
            val numberCode = "+218$number"
            sendVersioncode(numberCode)
            showAlertDialog()
        }

    }
    private fun sendVersioncode(number: String) {
            val options = PhoneAuthOptions.newBuilder(auth)
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
                Toast.makeText(this@RegActivity, "Verification Failed", Toast.LENGTH_SHORT).show()
            }

            override fun onCodeSent(
                s: String,
                token: PhoneAuthProvider.ForceResendingToken,
            ) {
                super.onCodeSent(s, token)
                verificationID = s
                resendToken = token
                Toast.makeText(this@RegActivity, "Code sent", Toast.LENGTH_SHORT).show()

            }
        }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userID = auth.currentUser?.uid.toString()

                    val username : String = nameEdtTxt.text.toString().trim()
                    val mobilePhone : String = phoneEdtTxt.text.toString().trim()
                    val password : String = passEdtTxt.text.toString().trim()
                    createNewUser(userID , username , mobilePhone , password)
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
    private fun createNewUser(
        userID: String, username: String,
        mobilePhone: String, password: String,
    ) {

        //Create a new user
        val User = HashMap<String, Any>()

        User["name"] = username
        User["phone"]  = mobilePhone
        User["password"] = password
        User["userID"] = userID

        // Add a new document with a generated ID
        val newUser = db.collection("User")//.document(userId)

        /* add(...) and .doc().set(...) are completely equivalent,
so you can use whichever is more convenient */
        newUser.add(User)
            .addOnSuccessListener { documentReference ->
                Log.d("Success", "DocumentSnapshot added with ID: $documentReference")
                Toast.makeText(this, "Success add new user", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.w("Failure", "Error adding document", e)
                Toast.makeText(this, "Failed to add new user", Toast.LENGTH_SHORT).show()
            }
    }

        fun onLoginClickBack(view: View) {
            startActivity(Intent(this, MainActivity::class.java))
            overridePendingTransition(R.anim.slide_in_left, R.anim.stay)
            progress.visibility = View.VISIBLE
        }
    }