package com.angandroid.appmapsdriver.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.angandroid.appmapsdriver.R
import com.angandroid.appmapsdriver.databinding.ShowDetailHistoryFmtBinding
import com.angandroid.appmapsdriver.models.ClientModel
import com.angandroid.appmapsdriver.models.HistoryTripModel
import com.angandroid.appmapsdriver.utils_codes.RelativeTime
import com.angandroid.appmapsdriver.utils_codes.ReuseCode
import com.angandroid.appmapsdriver.utils_provider.ClientProvider
import com.angandroid.appmapsdriver.utils_provider.DriverProvider
import com.angandroid.appmapsdriver.utils_provider.FrbAuthProviders
import com.angandroid.appmapsdriver.utils_provider.HistoryProvider
import com.bumptech.glide.Glide

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ShowDetailHistoryFmt : Fragment() {

    // Vars
    private var param1: String? = null
    private var param2: String? = null
    private var getArgId: String? = null

    // Objects
    private val authProvider = FrbAuthProviders()
    private val driverProvider = DriverProvider()
    private val clientProvider = ClientProvider()

    private val histProvider = HistoryProvider()

    var client: ClientModel? = null
    var history: HistoryTripModel? = null

    // View
    private var _bindDetHist: ShowDetailHistoryFmtBinding? = null
    private val bindDetHist get() = _bindDetHist!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getArgId = arguments?.getString("idHistDoc")
        Log.d("GET_DATA->", getArgId?: "")
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
        _bindDetHist = ShowDetailHistoryFmtBinding.inflate(inflater, container, false)
        initViews()
        return _bindDetHist?.root
    }

    private fun initObjects() {

    }

    private fun initViews() {
        setToolBar()
        getHistoryById(getArgId?: "")
    }

    private fun setToolBar() {
        (activity as AppCompatActivity).setSupportActionBar(bindDetHist.tbProf)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        bindDetHist.tbProf.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun getDataClient(idClient: String) {
        clientProvider.getDataClient(idClient).addOnSuccessListener { document ->
            if (document.exists()) {

                client = document.toObject(ClientModel::class.java)

                Glide.with(this)
                    .load(client?.imgUser)
                    .placeholder(R.drawable.ic_login)
                    .error(R.drawable.ic_login)
                    .into(bindDetHist.ivDetHist)

                (activity as AppCompatActivity).title = StringBuilder()
                                             .append("Cliente: ").append(client?.nameUser)
                bindDetHist.tvEmail.text = client?.emailUser
            }else{
                ReuseCode.msgToast(requireContext(), "No se encontro el cliente!", true)
            }
        }
    }

    // Get detail history trip
    private fun getHistoryById(idHistDoc: String) {
        histProvider.getHistoryDriverById(idHistDoc).addOnSuccessListener { document ->
            if (document.exists()) {
                history = document.toObject(HistoryTripModel::class.java)
                bindDetHist.tvOrigin.text = history?.origin
                bindDetHist.tvDestination.text = history?.destination
                bindDetHist.tvTimeDistance.text = StringBuilder()
                    .append(history?.time)
                    .append(" Min")
                    .append(" - ")
                    .append(history?.km)
                    .append(" Km")
                bindDetHist.tvPriceTrip.text =    history?.price.toString()
                bindDetHist.tvDateTrip.text =     RelativeTime.getTimeAgo(history?.timeStamp?: 0, context)
                bindDetHist.tvRatingDriv.text =   history?.ratingToDriver.toString()
                bindDetHist.tvRatingClient.text = history?.ratingToClient.toString()

                Log.d("LG_HIST", history?.destination.toString())

                getDataClient(history?.idClient?: "")
            }
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _bindDetHist = null
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ShowDetailHistoryFmt().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}