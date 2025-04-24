package com.angandroid.appmapsdriver.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.angandroid.appmapsdriver.databinding.ItemHistoryRvBinding
import com.angandroid.appmapsdriver.models.HistoryTripModel
import com.angandroid.appmapsdriver.utils_codes.ItemOnClickRv
import com.angandroid.appmapsdriver.utils_codes.RelativeTime

open class HistoryTripAdapter (context: Context, private val histList: List<HistoryTripModel>) :
    RecyclerView.Adapter<HistoryTripAdapter.UserViewHolder>() {

    private var onClickListener: ItemOnClickRv? = null
    val context: Context = context

    inner class UserViewHolder(val binding: ItemHistoryRvBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemHistoryRvBinding.inflate(LayoutInflater.from(parent.context), parent,false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val history = histList[position]

        holder.binding.tvOrigin.text =      createString("Origen:", history.origin.toString())
        holder.binding.tvDestination.text = createString("Destino:", history.destination.toString())
        holder.binding.tvDate.text = createString("Fecha:", RelativeTime.getTimeAgo(history.timeStamp?: 0, context))

        holder.itemView.setOnClickListener {
            onClickListener?.onClick(position, history)
        }
    }

    private fun createString(txtHead: String, txt: String): String {
        return StringBuilder().append(txtHead).append("\n").append(txt).toString()
    }

    // Set onclick in rv
    fun setOnClickListener(listener: ItemOnClickRv) {
        this.onClickListener = listener
    }

    // Interface onclick
    interface ItemOnClickRv {
        fun onClick(position: Int, model: HistoryTripModel)
    }

    override fun getItemCount() = histList.size
}