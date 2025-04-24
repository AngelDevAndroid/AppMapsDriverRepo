package com.angandroid.appmapsdriver.ui.fragments

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.angandroid.appmapsdriver.R
import com.angandroid.appmapsdriver.databinding.MenuMapMainFmtBinding
import com.angandroid.appmapsdriver.models.Booking
import com.angandroid.appmapsdriver.models.DriverModel
import com.angandroid.appmapsdriver.ui.activities.ContinerMenuFmts
import com.angandroid.appmapsdriver.ui.activities.MainActivity
import com.angandroid.appmapsdriver.utils_provider.BookingProvider
import com.angandroid.appmapsdriver.utils_provider.DriverProvider
import com.angandroid.appmapsdriver.utils_provider.FrbAuthProviders
import com.angandroid.appmapsdriver.utils_provider.GeoProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MenuMapMainFmt: BottomSheetDialogFragment(), View.OnClickListener {

    // Views
    private var _bindInf: MenuMapMainFmtBinding? = null
    private val bindInf get() = _bindInf!!

    // Objects
    private var geoProvider = GeoProvider()
    private val authProvider = FrbAuthProviders()
    private val bookProvider = BookingProvider()
    private val driverProvider = DriverProvider()

    private var getData: Booking? = null
    private var checkCancel = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
         initObjects()
        _bindInf = MenuMapMainFmtBinding.inflate(inflater, container, false)
        return _bindInf?.root
    }

    companion object {
        const val TAG = "MENU_BSD"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        getDataDriver()
    }

    private fun initObjects() {
        getData = arguments?.getParcelable<Booking>("object_booking")
        Log.d("LG_REQTRIP","$getData")
    }

    private fun initViews() {
        bindInf.ibProfile.setOnClickListener(this)
        bindInf.ibHistory.setOnClickListener(this)
        bindInf.ibCloseSesion.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.ib_profile -> {
                goToFragmentsOption(ContinerMenuFmts::class.java, 0)
            }
            R.id.ib_history -> {
                goToFragmentsOption(ContinerMenuFmts::class.java, 1)
            }
            R.id.ib_close_sesion -> {
                goToMainLogin(MainActivity::class.java)
            }
        }
    }

    private fun getDataDriver() {
        driverProvider.getDataDriver(authProvider.getIdFrb()).addOnSuccessListener { document ->
             if (document.exists()) {
                 val driver = document.toObject(DriverModel::class.java)
                 bindInf.tvNameDriver.text = StringBuilder()
                     .append("Conductor: ")
                     .append("${driver?.nameUser}")
             }
        }
    }

    private fun goToFragmentsOption(navCls: Class<*>, typeFmt: Int) {
        val intent = Intent(requireContext(), navCls)
        intent.putExtra("kTypeFmt", typeFmt)
        startActivity(intent)
    }

    private fun goToMainLogin(navCls: Class<*>) {
        authProvider.logOut()
        val intent = Intent(requireContext(), navCls)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    // If not accept trip it cancel in 30s only
    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (!checkCancel) {

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _bindInf = null  // Prevent memory leaks
    }
}