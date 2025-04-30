package com.angandroid.appmapsdriver.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.angandroid.appmapsdriver.R
import com.angandroid.appmapsdriver.databinding.ShowHistoryFmtBinding
import com.angandroid.appmapsdriver.models.HistoryTripModel
import com.angandroid.appmapsdriver.ui.adapters.HistoryTripAdapter
import com.angandroid.appmapsdriver.utils_codes.ReuseCode
import com.angandroid.appmapsdriver.utils_provider.HistoryProvider


class ShowHistoryFmt : Fragment() {

    private var param1: String? = null
    private var param2: String? = null

    // View
    private var _bindHistory: ShowHistoryFmtBinding? = null
    private val bindHistory get() = _bindHistory!!

    // Object
    val historyProvider = HistoryProvider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString("")
            param2 = it.getString("")
        }
        initObjects()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _bindHistory = ShowHistoryFmtBinding.inflate(inflater, container, false)
        initViews()
        return _bindHistory?.root
    }

    private fun initObjects() {
        getListHistories()
    }

    private fun initViews() {

    }

    private fun getListHistories() {

        var idDocHist = ""

        historyProvider.getHistoriesDriver().get().addOnSuccessListener { query ->
            if (query != null) {
                if (query.documents.size > 0) {

                    val lstHistories = query.map { list ->
                        idDocHist = list.id
                        list.toObject(HistoryTripModel::class.java)
                    }
                    Log.d("LG_FRHIST", "history -> ${idDocHist}")
                    setHistoryRecycler(lstHistories)
                }else{
                    setHistoryRecycler(emptyList())
                    Log.d("LG_FRHIST", "history -> ${query.documents.size}")
                    ReuseCode.msgToast(requireContext(), "No se encontro el historial!", true)
                }
            }else{
                Log.d("LG_FRHIST", "history -> $query")
            }
        }.addOnFailureListener { error ->
            Log.d("LG_FRHIST", "history -> ${error.message}")
        }
    }

    private fun setHistoryRecycler(lstHistories: List<HistoryTripModel>) {

        val adapter = HistoryTripAdapter(requireContext(), lstHistories)
        bindHistory.historyRv.layoutManager = LinearLayoutManager(context)
        bindHistory.historyRv.adapter = adapter

        adapter.setOnClickListener(object: HistoryTripAdapter.ItemOnClickRv {
            override fun onClick(position: Int, model: HistoryTripModel) {
                ReuseCode.msgToast(requireContext(), "${model.origin}", true)
                sendDataDetFmt(model.id?: "")
            }
        })
    }

    private fun sendDataDetFmt(idHistDoc: String) {

        val bundleX = Bundle()
        bundleX.putString("idHistDoc", idHistDoc)

        val fmtHist = ShowDetailHistoryFmt()
        fmtHist.arguments = bundleX

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fmtHist)
            //.addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _bindHistory = null
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ShowHistoryFmt().apply {
                arguments = Bundle().apply {
                    putString("", param1)
                    putString("", param2)
                }
            }
    }
}

/*
val list = listOf(
    HistoryTripModel("","", "", "angel ", "angel "),
    HistoryTripModel("","", "", "angel ", "angel "))*/
