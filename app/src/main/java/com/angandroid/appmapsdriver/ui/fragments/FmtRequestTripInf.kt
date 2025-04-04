package com.angandroid.appmapsdriver.ui.fragments

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.angandroid.appmapsdriver.R
import com.angandroid.appmapsdriver.databinding.FmtRequestTripInfBinding
import com.angandroid.appmapsdriver.models.Booking
import com.angandroid.appmapsdriver.ui.MapsDriver
import com.angandroid.appmapsdriver.ui.TripAcceptAct
import com.angandroid.appmapsdriver.utils_code.BookingProvider
import com.angandroid.appmapsdriver.utils_code.FrbAuthProviders
import com.angandroid.appmapsdriver.utils_code.GeoProvider
import com.angandroid.appmapsdriver.utils_code.ReutiliceCode
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class FmtRequestTripInf: BottomSheetDialogFragment(), View.OnClickListener {

    // Views
    private var _bindInf: FmtRequestTripInfBinding? = null
    private val bindInf get() = _bindInf!!

    // Objects
    private var geoProvider = GeoProvider()
    private val authProvider = FrbAuthProviders()
    private val bookProvider = BookingProvider()
    private var getData: Booking? = null
    private var checkCancel = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
         initObjects()
        _bindInf = FmtRequestTripInfBinding.inflate(inflater, container, false)
        return _bindInf?.root
    }

    companion object {
        const val TAG = "BSDT"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
    }

    private fun initObjects() {
        getData = arguments?.getParcelable<Booking>("object_booking")
        Log.d("LG_REQTRIP","$getData")
    }

    private fun initViews() {

        bindInf.tvOrigin.text = getData?.origin
        bindInf.tvDestination.text = getData?.destination
        bindInf.tvTimeDistance.text = getData?.time.toString().plus("-").plus(getData?.km).plus("km")
        bindInf.tvPriceTrip.text = getData?.price.toString().plus("MXN")

        bindInf.btnCancel.setOnClickListener(this)
        bindInf.btnAccept.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.btn_cancel -> {
                cancelRequTrip(getData?.idClient.toString())
            }
            R.id.btn_accept -> {
                acceptRequTrip(getData?.idClient.toString())
            }
        }
    }

    // Only cancel or rechaza the trip, flow estando disponible en map
    private fun cancelRequTrip(idClient: String) {

         bookProvider.updateStatus(idClient, "cancel").addOnCompleteListener { result ->
             if (result.isSuccessful) {
                 checkCancel = true
                 dismiss()
                 ReutiliceCode.msgToast(requireContext(),"Viaje cancelado",true)
             }else{
                 ReutiliceCode.msgToast(requireContext(), "No se pudo cancelar el viaje!", true)
             }
         }
    }

    // Ya no esta disponible en map (Driver ocuped), not update location
    private fun  acceptRequTrip(idClient: String) {

        bookProvider.updateStatus(idClient, "accept").addOnCompleteListener { result ->
            if (result.isSuccessful) {
                checkCancel = true
                (activity as? MapsDriver)?.ewlLocation?.endUpdates() // Que ya no actualize la ubicacion
                geoProvider.removeLocationOnly(authProvider.getIdFrb()) // Para que ya no aparesca el driver en el mapa
                goToMapTripAccept(TripAcceptAct::class.java)
                ReutiliceCode.msgToast(requireContext(),"Viaje aceptado",true)
                dismiss()
            }else{
                ReutiliceCode.msgToast(requireContext(), "No se pudo aceptar el viaje!", true)
            }
        }
    }

    private fun goToMapTripAccept(navCls: Class<*>) {
        startActivity(Intent(requireContext(), navCls))
    }

    // If not accept trip it cancel in 30s only
    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (!checkCancel) {
            cancelRequTrip(getData?.idClient.toString())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _bindInf = null  // Prevent memory leaks
    }
}