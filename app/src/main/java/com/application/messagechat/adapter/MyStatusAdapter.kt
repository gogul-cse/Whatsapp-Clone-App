package com.application.messagechat.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.application.messagechat.activity.ViewStatusActivity
import com.application.messagechat.databinding.ItemStatusBinding
import com.application.messagechat.model.CallLog
import com.application.messagechat.model.Stauts

class MyStatusAdapter(private val longClick: (Int)-> Unit): RecyclerView.Adapter<MyStatusAdapter.MyStatusViewHolder>() {


    var statusList: List<Stauts> = emptyList()

    fun setStatus(status: List<Stauts>){
        this.statusList = status
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyStatusViewHolder {
        val view = ItemStatusBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MyStatusViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: MyStatusViewHolder,
        position: Int
    ) {
        val statusId = statusList[position]
        holder.itemStatusBinding.addStatusImage.setImageResource(com.application.messagechat.R.drawable.person_profile)
        holder.itemStatusBinding.textViewStatusPersonName.text = "Einfo Chips"
        holder.itemStatusBinding.linearLayoutItemStatus.setOnClickListener {
            val intent = Intent(holder.itemView.context, ViewStatusActivity::class.java)

            holder.itemView.context.startActivity(intent)
        }
        holder.itemStatusBinding.linearLayoutItemStatus.setOnLongClickListener {

            true
        }
    }

    override fun getItemCount(): Int {
        return statusList.size
    }

    class MyStatusViewHolder(val itemStatusBinding: ItemStatusBinding): RecyclerView.ViewHolder(itemStatusBinding.root)
}