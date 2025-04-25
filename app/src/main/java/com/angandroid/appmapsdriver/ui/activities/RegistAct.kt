package com.angandroid.appmapsdriver.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.angandroid.appmapsdriver.R
import com.angandroid.appmapsdriver.databinding.ActRegistBinding
import com.angandroid.appmapsdriver.models.DriverModel
import com.angandroid.appmapsdriver.utils_provider.DriverProvider
import com.angandroid.appmapsdriver.utils_provider.FrbAuthProviders
import com.angandroid.appmapsdriver.utils_codes.ReutiliceCode
import com.github.dhaval2404.imagepicker.ImagePicker
import java.io.File

class RegistAct() : AppCompatActivity(), View.OnClickListener {

    // View
    private lateinit var bindRegister: ActRegistBinding

    // Objects
    private val authProvider = FrbAuthProviders()
    private val driverProvider = DriverProvider()
    private var imageFile: File? = null

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

        bindRegister.ivCamReg.setOnClickListener(this)
        bindRegister.btnSaveReg.setOnClickListener(this)
        bindRegister.btnLoginReg.setOnClickListener(this)
    }

    override fun onClick(v: View?) {

        when(v?.id) {
            R.id.iv_cam_reg -> {
                selectImage()
            }
            R.id.btn_save_reg -> {
                registerNewDriver()
            }
            R.id.btn_login_reg -> {
                navIntent(MainActivity::class.java)
            }
        }
    }

    private fun selectImage() {
        ImagePicker.with(this)
            .crop()	    			//Crop image(Optional), Check Customization for more option
            .compress(1024)			//Final image size will be less than 1 MB(Optional)
            .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
            .createIntent { intent ->
                startImageForResult.launch(intent)
            }
        //.start()
    }

    private val startImageForResult =
        registerForActivityResult(
            ActivityResultContracts
            .StartActivityForResult()) { result: ActivityResult ->

            val resultCode = result.resultCode
            val data = result.data

            if (resultCode == Activity.RESULT_OK) {
                val fileUri = data?.data!!

                imageFile = File(fileUri.path?: "")
                bindRegister.ivCamReg.setImageURI(fileUri)

            } else if (resultCode == ImagePicker.RESULT_ERROR) {

                ReutiliceCode.msgToast(this, ImagePicker.getError(data), true)

            } else {
                ReutiliceCode.msgToast(this, "Tarea cancelada!", true)

            }
        }

    // Update info driver
    private fun registerNewDriver() {

        var urlImageFile: String? = null
        var obDriver: DriverModel? = null

        if (imageFile?.exists() == true) {
            // Upload image
            driverProvider
                .uploadFileStorage(authProvider.getIdFrb(),imageFile?: File(""))
                .addOnSuccessListener { tSnapshot ->
                    if (tSnapshot.task.isSuccessful) {
                        Log.d("LG_STORAGE", "img uploaded")

                        // Get url image
                        driverProvider.getImageUrlStorage().addOnSuccessListener { getUrl ->

                            val urlImage = getUrl.toString()
                            urlImageFile = urlImage
                            checkDataRegister(urlImage)
                            Log.d("LG_STORAGE", "No se pudo obtener la uri")
                        }
                    }else{
                        Log.d("LG_STORAGE", "img not uploaded")
                    }
                }
        }else{
            checkDataRegister("")
            Log.d("LG_STORAGE", "img not uploaded")

        }
    }

    private fun checkDataRegister(imgUrl: String) {

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
                        authProvider.getIdFrb(), etNameReg, imgUrl, etNumberReg, etEmailReg, etPasswReg
                    )

                    driverProvider.createUser(nDriver).addOnCompleteListener { result ->
                        if (result.isSuccessful) {
                            ReutiliceCode.msgToast(this, "Usuario registrado!", true)
                        }else{
                            ReutiliceCode.msgToast(this, "Usuario no completado... ${result.result.toString()}", true)
                        }
                    }
                }else{
                    ReutiliceCode.msgToast(this, "Credenciales no guardadas.", true)
                    Log.d("LG_REG", "${it.result}")
                }
            }
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