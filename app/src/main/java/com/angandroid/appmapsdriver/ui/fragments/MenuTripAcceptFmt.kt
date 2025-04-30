package com.angandroid.appmapsdriver.ui.fragments

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.angandroid.appmapsdriver.R
import com.angandroid.appmapsdriver.databinding.MenuTripAcceptFmtBinding
import com.angandroid.appmapsdriver.models.Booking
import com.angandroid.appmapsdriver.models.ClientModel
import com.angandroid.appmapsdriver.ui.activities.ContinerMenuFmts
import com.angandroid.appmapsdriver.ui.activities.MainActivity
import com.angandroid.appmapsdriver.utils_provider.BookingProvider
import com.angandroid.appmapsdriver.utils_provider.ClientProvider
import com.angandroid.appmapsdriver.utils_provider.DriverProvider
import com.angandroid.appmapsdriver.utils_provider.FrbAuthProviders
import com.angandroid.appmapsdriver.utils_provider.GeoProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.core.net.toUri
import com.angandroid.appmapsdriver.utils_codes.ReuseCode

class MenuTripAcceptFmt: BottomSheetDialogFragment(), View.OnClickListener {

    // Views
    private var _bindInf: MenuTripAcceptFmtBinding? = null
    private val bindInf get() = _bindInf!!

    // Objects
    private var geoProvider = GeoProvider()
    private val authProvider = FrbAuthProviders()
    private val bookProvider = BookingProvider()
    private val driverProvider = DriverProvider()
    private val clientProvider = ClientProvider()

    private var booking: Booking? = null

    private var getData: Booking? = null
    private var checkCancel = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
         initObjects()
        _bindInf = MenuTripAcceptFmtBinding.inflate(inflater, container, false)
        return _bindInf?.root
    }

    companion object {
        const val TAG = "MENU_BSD"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
    }

    private fun initObjects() {
        getData = arguments?.getParcelable<Booking>("object_booking")
        Log.d("LG_REQTRIP","$getData")
        getBookingTrip()
    }

    private fun initViews() {
        bindInf.ivPhoneClient.setOnClickListener(this)
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

    // Draw route and add marker client
    private fun getBookingTrip() {
        bookProvider.getBooking().get().addOnSuccessListener { query ->
            if (query != null) {
                if (query.size() > 0) {
                    booking = query.documents[0].toObject(Booking::class.java)
                    getDataClientAndBook(
                        booking?.idClient?: "",
                        booking?.origin?: "",
                        booking?.destination?: "")
                }
            }
        }
    }

    private fun getDataClientAndBook(idClient: String, originTrip: String, destination: String) {
        clientProvider.getDataClient(idClient).addOnSuccessListener { document ->
             if (document.exists()) {
                 val client = document.toObject(ClientModel::class.java)
                 bindInf.tvNameClient.text = client?.nameUser
                 bindInf.tvOrigin.text = originTrip
                 bindInf.tvDestination.text = destination
                 bindInf.ivPhoneClient.setOnClickListener {
                     openCallDial(client?.numUser?: "")
                 }
             }
        }
    }

    // To call client
    private fun openCallDial(numClient: String) {
        if (numClient.isNotEmpty()) {
            if (ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {

                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = "tel:$numClient".toUri()
                startActivity(intent)
            } else {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CALL_PHONE), 1)
            }
        }else{
            ReuseCode.msgToast(requireContext(), "Telèfono no disponible!", true)
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