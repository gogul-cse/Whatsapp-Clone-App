package com.application.messagechat.adapter

import android.content.Intent
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.application.messagechat.activity.MyMessagesChatActivity
import com.application.messagechat.databinding.ItemChatBinding
import com.application.messagechat.model.UserContacts
import com.application.messagechat.util.ConvertImage
import com.application.messagechat.util.ConvertTime
import com.google.firebase.firestore.FirebaseFirestore


class MyChatAdapter(var searchList: MutableList<UserContacts>,
                    private val longClick: (userId: String) -> Unit, val onClickProfile:(image: Bitmap,contactId: String) -> Unit
): RecyclerView.Adapter<MyChatAdapter.MyMessagesViewHolder>(), Filterable {

    var firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    var userList: MutableList<UserContacts> = searchList.toMutableList()

    fun updateList(list: List<UserContacts>){
        searchList = list.toMutableList()
        userList = list.toMutableList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyMessagesViewHolder {

        val view = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent,false)
        return MyMessagesViewHolder(view)

    }

    override fun onBindViewHolder(
        holder: MyMessagesViewHolder,
        position: Int
    ) {
        val contacts = userList[position]
        with(holder){
            holder.itemBinding.imageViewProfile.setImageBitmap(ConvertImage.convertToBitmap(contacts.contactProfile))
            itemBinding.textViewSenderName.text = contacts.name
            itemBinding.linearLayoutItemChat.setOnClickListener {
                val intent = Intent(itemView.context, MyMessagesChatActivity::class.java)
                intent.putExtra("contactId",contacts.id)
                intent.putExtra("existUserId",contacts.existingUser)
                intent.putExtra("contactNumber",contacts.phoneNumber)
                intent.putExtra("image",contacts.contactProfile)
                itemView.context.startActivity(intent)
            }

            itemBinding.linearLayoutItemChat.setOnLongClickListener {
                longClick(contacts.id)
                true
            }
            itemBinding.imageViewProfile.setOnClickListener {
                onClickProfile(ConvertImage.convertToBitmap(contacts.contactProfile)!!,contacts.id)
            }

            val chatId = if (contacts.userId < contacts.phoneNumber) "${contacts.userId}_${contacts.phoneNumber}" else "${contacts.phoneNumber}_${contacts.userId}"
            firestore.collection("chats")
                .whereEqualTo("chatId",chatId)
                .addSnapshotListener {msg,e->
                    if (e!= null || msg==null || msg.isEmpty){
                        itemBinding.textViewLastMessage.text = ""
                        itemBinding.lastMessageTime.text = ""
                        return@addSnapshotListener
                    }
                    itemBinding.textViewLastMessage.text = msg.documents.firstOrNull()?.getString("lastMessage") ?: ""
                    val time = msg.documents.first().getTimestamp("timeStamp")
                    itemBinding.lastMessageTime.text = ConvertTime.convertLongToTime(time!!.toDate().time)
                }
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun getFilter(): Filter {
        return object : Filter(){
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val search = constraint?.toString()?.lowercase()?.trim() ?: ""
                val find = if (search.isEmpty()){
                    searchList
                }else{
                    searchList.filter { item->
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
                userList = (results?.values as List<UserContacts>)?.toMutableList()?: mutableListOf()
                notifyDataSetChanged()
            }
        }
    }

    class MyMessagesViewHolder(val itemBinding: ItemChatBinding) : RecyclerView.ViewHolder(itemBinding.root)

}