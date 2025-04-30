package com.angandroid.appmapsdriver.ui.fragments

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.angandroid.appmapsdriver.R
import com.angandroid.appmapsdriver.databinding.ShowProfileDriverFmtBinding
import com.angandroid.appmapsdriver.models.DriverModel
import com.angandroid.appmapsdriver.utils_provider.DriverProvider
import com.angandroid.appmapsdriver.utils_provider.FrbAuthProviders
import com.angandroid.appmapsdriver.utils_codes.ReuseCode
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import java.io.File


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ShowProfileDriverFmt : Fragment(), View.OnClickListener {

    private var param1: String? = null
    private var param2: String? = null

    // Objects
    private val authProvider = FrbAuthProviders()
    private val driverProvider = DriverProvider()

    private var _bindProfile: ShowProfileDriverFmtBinding? = null
    private val bindProfile get() = _bindProfile!!

    private var imageFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        initObjects()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _bindProfile = ShowProfileDriverFmtBinding.inflate(inflater, container, false)
        initViews()
        return _bindProfile?.root
    }

    private fun initObjects() {
        getDataDriver()
    }

    private fun initViews() {
        setToolBar()
        bindProfile.btnUpdateInfo.setOnClickListener(this)
        bindProfile.ivProfile.setOnClickListener(this)
    }

    private fun setToolBar() {
        (activity as AppCompatActivity).setSupportActionBar(bindProfile.tbProf)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).title = "Datos del conductor"

        bindProfile.tbProf.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun getDataDriver() {
        driverProvider.getDataDriver(authProvider.getIdFrb()).addOnSuccessListener { document ->
            if (document.exists()) {

                val driver = document.toObject(DriverModel::class.java)

                Glide.with(this)
                    .load(driver?.imgUser)
                    .placeholder(R.drawable.ic_login)
                    .error(R.drawable.ic_login)
                    .into(bindProfile.ivProfile)

                bindProfile.tvEmail.text = driver?.emailUser
                bindProfile.etNameReg.setText(driver?.nameUser)
                bindProfile.etBrandReg.setText(driver?.brandCar)
                bindProfile.etColorReg.setText(driver?.colorCar)
                bindProfile.etPlatesReg.setText(driver?.plateNumber)
            }
        }
    }

    // Update info driver
    private fun getDataUpdate() {

        val nameDriver = bindProfile.etNameReg.text.toString()
        val brandCar= bindProfile.etBrandReg.text.toString()
        val colorCar = bindProfile.etColorReg.text.toString()
        val platesCar = bindProfile.etPlatesReg.text.toString()

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

                            //if (getUrl.path.isNullOrEmpty()) {
                                val urlImage = getUrl.toString()
                                urlImageFile = urlImage

                            obDriver =  setObjDriver(
                                nameDriver,
                                urlImageFile?: "",
                                platesCar,
                                colorCar,
                                brandCar
                            )
                            updateDataDriver(obDriver?: DriverModel())
                                Log.d("LG_STORAGE", urlImageFile!!)
                           // }else{
                                Log.d("LG_STORAGE", "No se pudo obtener la uri")
                            //}
                        }

                    }else{
                        Log.d("LG_STORAGE", "img not uploaded")
                    }
                }
        }else{

            obDriver =  setObjDriver(
                nameDriver,
                null,
                platesCar,
                colorCar,
                brandCar
            )
            updateDataDriver(obDriver?: DriverModel())
        }
    }

    private fun setObjDriver(nameDriverParam: String,
                             urlImageFileParam: String?,
                             platesCarParam: String,
                             colorCarParam: String,
                             brandCarParam: String): DriverModel {
        val objDriver = DriverModel(
            authProvider.getIdFrb(),
            nameDriverParam,
            urlImageFileParam,
            null,
            null,
            null,
            platesCarParam,
            colorCarParam,
            brandCarParam
        )
        return objDriver
    }

    private fun updateDataDriver(obDriver: DriverModel) {
        driverProvider.updateInfoDriver(obDriver?: DriverModel()).addOnCompleteListener { update ->
            if (update.isSuccessful) {
                ReuseCode.msgToast(requireActivity(), "Datos actualizados.", true)
            }else{
                ReuseCode.msgToast(requireActivity(), "Error al actualizar.", true)
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
        registerForActivityResult(ActivityResultContracts
            .StartActivityForResult()) { result: ActivityResult ->

            val resultCode = result.resultCode
            val data = result.data

            if (resultCode == Activity.RESULT_OK) {
                val fileUri = data?.data!!

                imageFile = File(fileUri.path?: "")
                bindProfile.ivProfile.setImageURI(fileUri)

            } else if (resultCode == ImagePicker.RESULT_ERROR) {

                ReuseCode.msgToast(requireContext(), ImagePicker.getError(data), true)

            } else {
                ReuseCode.msgToast(requireContext(), "Tarea cancelada!", true)

            }
        }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ShowProfileDriverFmt().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onClick(p0: View?) {
        when(p0?.id) {
            R.id.btn_update_info -> {
                getDataUpdate()
            }
            R.id.iv_profile -> {
                selectImage()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _bindProfile = null
    }
}

/* bajar peso y tamaño de imagen, para poderla subir ----->
imageView.isDrawingCacheEnabled = true
imageView.buildDrawingCache()
val bitmap = (imageView.drawable as BitmapDrawable).bitmap
val baos = ByteArrayOutputStream()
bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
val data = baos.toByteArray()

var uploadTask = mountainsRef.putBytes(data)
uploadTask.addOnFailureListener {
    // Handle unsuccessful uploads
}.addOnSuccessListener { taskSnapshot ->
    // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
    // ...
}*/
