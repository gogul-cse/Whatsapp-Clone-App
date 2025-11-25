package com.application.messagechat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.application.messagechat.databinding.ItemChatBinding
import com.application.messagechat.model.UserContacts
import com.application.messagechat.util.ConvertImage

class SearchCallAdapter(var searchCallList: MutableList<UserContacts>, private val callPerson:(String, String, String)-> Unit): RecyclerView.Adapter<SearchCallAdapter.SeachCallViewHolder>(),
    Filterable {

    var callList: List<UserContacts> = searchCallList.toMutableList()
    fun updateCallList(list: List<UserContacts>){
        callList = list.toMutableList()
        searchCallList = list.toMutableList()
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SeachCallViewHolder {
        val view = ItemChatBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return SeachCallViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: SeachCallViewHolder,
        position: Int
    ) {
        val calls = callList[position]
        holder.itembinding.imageViewProfile.setImageBitmap(ConvertImage.convertToBitmap(calls.contactProfile))
        holder.itembinding.textViewSenderName.text = calls.name
        holder.itembinding.linearLayoutItemChat.setOnClickListener {
            callPerson(calls.name,calls.phoneNumber,calls.id)
        }
    }

    override fun getItemCount(): Int {
        return callList.size
    }

    override fun getFilter(): Filter {
        return object : Filter(){
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val search = constraint?.toString()?.lowercase()?.trim() ?: ""
                val find = if (search.isEmpty()){
                    searchCallList
                }else{
                    searchCallList.filter { item->
                        item.name.lowercase().contains(search)|| item.phoneNumber.contains(search)  }
                }
                val searchedName = FilterResults()
                searchedName.values = find
                return searchedName
            }

            override fun publishResults(
                constraint: CharSequence?,
                results: FilterResults?
            ) {
                callList = (results?.values as List<UserContacts>).toMutableList()?: mutableListOf()
                notifyDataSetChanged()
            }
        }
    }

    class SeachCallViewHolder(val itembinding: ItemChatBinding): RecyclerView.ViewHolder(itembinding.root)
}