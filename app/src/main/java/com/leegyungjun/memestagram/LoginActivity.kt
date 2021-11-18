package com.leegyungjun.memestagram

import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*


class LoginActivity : AppCompatActivity() {

    var auth : FirebaseAuth? = null
    var googleSignInClient : GoogleSignInClient? = null
    var GOOGLE_LOGIN_CODE = 9001
    var callbackManager : CallbackManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()
        email_login_button.setOnClickListener {
            signinAndSignup()
        }
        facebook_login_button.setOnClickListener {
            //First step
            facebookLogin()
        }
        google_sign_in_button.setOnClickListener {
            //First step
            googleLogin()
        }
        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("827636127296-crhii03c732noa0mehpdl5loe48u549c.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this,gso)
        //printHashKey() // QYi/ZXomofJjDORGCQL0mJEDMGQ=
        callbackManager = CallbackManager.Factory.create()

    }

    override fun onStart() {
        super.onStart()
        moveMainPage(auth?.currentUser)
    }

    fun printHashKey() {
        try {
            val info: PackageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md: MessageDigest = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val hashKey = String(Base64.encode(md.digest(), 0))
                Log.i("TAG", "printHashKey() Hash Key: $hashKey")
            }
        } catch (e: NoSuchAlgorithmException) {
            Log.e("TAG", "printHashKey()", e)
        } catch (e: Exception) {
            Log.e("TAG", "printHashKey()", e)
        }
    }

    fun facebookLogin() {
        LoginManager.getInstance().logInWithReadPermissions(this, listOf("public_profile", "email"))
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult>{
            override fun onSuccess(result: LoginResult?) {
                //Second step
                handlFacebookAccessToken(result?.accessToken)
            }

            override fun onCancel() {
            }

            override fun onError(error: FacebookException?) {
            }
        })
    }

    fun handlFacebookAccessToken(token : AccessToken?) {
        var credential = FacebookAuthProvider.getCredential(token?.token!!)
        auth?.signInWithCredential(credential)?.addOnCompleteListener {
                task ->
            if(task.isSuccessful){
                //Third step
                //Login
                moveMainPage(task.result?.user)
            }else{
                //Show error message
                Toast.makeText(this,task.exception?.message,Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager?.onActivityResult(requestCode, resultCode, data)
        if(requestCode==GOOGLE_LOGIN_CODE){
            var result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if(result.isSuccess){
                var account = result.signInAccount
                //Second step
                firebaseAuthWithGoogle(account)
            }
        }
    }
    fun firebaseAuthWithGoogle(account:GoogleSignInAccount?){
        var credential = GoogleAuthProvider.getCredential(account?.idToken,null)
        auth?.signInWithCredential(credential)?.addOnCompleteListener {
                task ->
            if(task.isSuccessful){
                //Login
                moveMainPage(task.result?.user)
            }else{
                //Show error message
                Toast.makeText(this,task.exception?.message,Toast.LENGTH_SHORT).show()
            }
        }
    }
    fun googleLogin() {
        var signInIntent = googleSignInClient?.signInIntent
        startActivityForResult(signInIntent,GOOGLE_LOGIN_CODE)
    }

    fun signinAndSignup() {
        auth?.createUserWithEmailAndPassword(email_edittext.text.toString(), password_edittext.text.toString())
            ?.addOnCompleteListener {
                task ->
                    if(task.isSuccessful){
                        //Creating a user account
                        moveMainPage(task.result?.user)
                    }else if(!task.exception?.message.isNullOrEmpty()){
                        //Show error message
                        Toast.makeText(this,task.exception?.message,Toast.LENGTH_SHORT).show()
                    }else{
                        //Login if you have account
                        signinEmail()
                    }
        }
    }

    fun signinEmail(){
        auth?.createUserWithEmailAndPassword(email_edittext.text.toString(), password_edittext.text.toString())
            ?.addOnCompleteListener {
                    task ->
                if(task.isSuccessful){
                    //Login
                    moveMainPage(task.result?.user)
                }else{
                    //Show error message
                    Toast.makeText(this,task.exception?.message,Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun moveMainPage(user:FirebaseUser?){
        if(user!=null){
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }
    }
}