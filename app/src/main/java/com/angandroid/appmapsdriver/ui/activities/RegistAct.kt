package com.angandroid.appmapsdriver.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.angandroid.appmapsdriver.R
import com.angandroid.appmapsdriver.databinding.ActRegistBinding
import com.angandroid.appmapsdriver.models.DriverModel
import com.angandroid.appmapsdriver.utils_provider.DriverProvider
import com.angandroid.appmapsdriver.utils_provider.FrbAuthProviders
import com.angandroid.appmapsdriver.utils_codes.ReutiliceCode

class RegistAct() : AppCompatActivity(), View.OnClickListener {

    private lateinit var bindRegister: ActRegistBinding
    private val authProvider = FrbAuthProviders()
    private val driverProvider = DriverProvider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bindRegister = ActRegistBinding.inflate(layoutInflater)

        enableEdgeToEdge()
        setContentView(bindRegister.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initViewsEvents()
    }

    private fun initViewsEvents() {
        bindRegister.etNameReg.setOnClickListener(this)
        bindRegister.etNumReg.setOnClickListener(this)
        bindRegister.etEmailReg.setOnClickListener(this)
        bindRegister.etPasswReg.setOnClickListener(this)

        bindRegister.btnSaveReg.setOnClickListener(this)
        bindRegister.btnLoginReg.setOnClickListener(this)
    }

    override fun onClick(v: View?) {

        when(v?.id) {
            R.id.btn_save_reg -> {
                checkCredentialsLogin()
            }
            R.id.btn_login_reg -> {
                navIntent(RegistAct::class.java)
            }
        }
    }

    private fun checkCredentialsLogin() {

        val etNameReg = bindRegister.etNameReg.text.toString()
        val etNumberReg = bindRegister.etNumReg.text.toString()
        val etEmailReg = bindRegister.etEmailReg.text.toString()
        val etPasswReg = bindRegister.etPasswReg.text.toString()

        Log.d("LG_REG", "$etNameReg, $etNumberReg, $etEmailReg, $etPasswReg")

        if (checkEditEmpty(etNameReg, etNumberReg, etEmailReg, etPasswReg)) {
            authProvider.registerUser(etEmailReg, etPasswReg).addOnCompleteListener { it ->
                if (it.isSuccessful) {
                    ReutiliceCode.msgToast(this, "Credenciales guardadas.", true)
                    val nDriver = DriverModel(
                        authProvider.getIdFrb(),
                        etNameReg, etNumberReg, etEmailReg, etPasswReg
                    )

                    driverProvider.createUser(nDriver).addOnCompleteListener { result ->
                        if (result.isSuccessful) {
                            ReutiliceCode.msgToast(this, "Usuario registrado!", true)
                        }else{
                            ReutiliceCode.msgToast(this, "Usuario no completado... ${result.result.toString()}", true)
                        }
                    }
                    //ReutiliceCode.msgToast(this, "Usuario registrado", true)
                }else{
                    ReutiliceCode.msgToast(this, "Credenciales no guardadas.", true)
                    Log.d("LG_REG", "${it.result}")
                }
            }
            //navIntent(RegistAct::class.java)
        }
    }

    private fun navIntent(navCls: Class<*>) {
        startActivity(Intent(this, navCls))
    }

    // Check edit empty
    private fun checkEditEmpty(name: String, num: String, email: String, passw: String): Boolean {
        if (name.isEmpty()){
            ReutiliceCode.msgToast(this, "Ingrese usuario", true)
            return false
        }
        if (num.isEmpty()){
            ReutiliceCode.msgToast(this, "Ingrese nùmero", true)
            return false
        }
        if (email.isEmpty()){
            ReutiliceCode.msgToast(this, "Ingrese email", true)
            return false
        }
        if (passw.isEmpty()){
            ReutiliceCode.msgToast(this, "Ingrese Contraseña", true)
            return false
        }
        return true
    }
}