package com.mercurialminds

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import com.mercurialminds.sdk.otp.OTP
import com.mercurialminds.sdk.otp.OtpErrorCode
import com.mercurialminds.sdk.otp.callbacks.OtpListener
import com.mercurialminds.sdk.otp.common.network.Logger
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(), OtpListener {

    private val TAG = MainActivity::class.java.simpleName
    var otp: OTP? = OTP()
    private val REQUEST_ID_MULTIPLE_PERMISSIONS = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val connectionManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectionManager.activeNetworkInfo

        if (networkInfo != null && networkInfo.isConnected) {
            Logger.info(TAG, "Internet is Connected")
            otp?.initialize("", this, this, lifecycle)
            countryPicker.registerCarrierNumberEditText(editTextPhoneNo)
            fabBtn.setOnClickListener {
                // country code picker library to use dialer code
                val phoneNumber = countryPicker.fullNumberWithPlus
                Logger.info(TAG, "phoneNumber: $phoneNumber")
                if (checkAndRequestPermissions()) {
                    // otp?.initialize("", this, this, lifecycle)
                    otp?.verifyPhoneNumber(phoneNumber)
                }
            }
        } else {
            Snackbar.make(mainActivityID, "Not Connected to internet", Snackbar.LENGTH_LONG).show()
        }
    }
    
    override fun onError(code: OtpErrorCode, message: String) {
        runOnUiThread {
            Snackbar.make(mainActivityID, message, Snackbar.LENGTH_LONG).show()
        }
    }
    // show snackbar to phoneNumber is verified
    override fun onPhoneNumberVerified() {
        runOnUiThread {
            val phoneNumber = countryPicker.fullNumberWithPlus
            Snackbar.make(mainActivityID, "$phoneNumber is Verified", Snackbar.LENGTH_LONG).show()
            Logger.info(TAG, "Number is Verified onPhoneNumberVerified")
        }
    }

    private fun checkAndRequestPermissions(): Boolean {
        val sendSmsPermissions = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.SEND_SMS
        )

        val readPhoneStatePermissions = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_PHONE_STATE
        )
        val processOutgoingPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.PROCESS_OUTGOING_CALLS
        )
        val callLogPermissions = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_CALL_LOG
        )

        val listPermissionsNeeded = ArrayList<String>()
        if (sendSmsPermissions != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.SEND_SMS)
        }
        if (readPhoneStatePermissions != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_PHONE_STATE)
        }
        if (processOutgoingPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.PROCESS_OUTGOING_CALLS)
        }
        if (callLogPermissions != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_CALL_LOG)
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                listPermissionsNeeded.toTypedArray(),
                REQUEST_ID_MULTIPLE_PERMISSIONS
            )
            return false
        }
        return true
    }
}
