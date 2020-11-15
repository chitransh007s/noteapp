package com.cs007s.live.notes.splashscreen

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.cs007s.live.notes.R
import com.cs007s.live.notes.mainscreen.view.MainView
import com.cs007s.live.notes.utilities.helpers.ViewUtils
import com.cs007s.live.notes.utilities.sharedpreference.SharedPreferenceHelper
import java.util.concurrent.Executor


/**
 * Created by Chitransh Shrivastav on 10/11/2020 for Notes
 */
class SplashView : AppCompatActivity() {

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        if (SharedPreferenceHelper.getInstance(this)!!.lockStatus) {
            showBiomertricDialog()
        } else {
            Handler().postDelayed({
                startActivity(Intent(this@SplashView, MainView::class.java))
                finish()
            }, 2000)
        }
    }

    private fun showBiomertricDialog(){
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int,
                                                   errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    ViewUtils.showToast(applicationContext, "Authentication error: $errString")
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Handler().postDelayed({
                        startActivity(Intent(this@SplashView, MainView::class.java))
                        finish()
                    }, 2000)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    ViewUtils.showToast(applicationContext, "Authentication failed")
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock " + getString(R.string.app_name))
            .setSubtitle("Confirm your screen lock pattern, PIN or password")
            .setDeviceCredentialAllowed(true)
            .build()
        biometricPrompt.authenticate(promptInfo)

    }

    private fun isBiometricWorking() : Boolean {
        var status = false
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate()) {
            BiometricManager.BIOMETRIC_SUCCESS ->
                status = true
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                status = false
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                status = status
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                status = status
        }
        return status
    }
}