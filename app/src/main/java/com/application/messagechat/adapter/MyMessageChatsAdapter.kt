package com.application.messagechat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.application.messagechat.R
import com.application.messagechat.databinding.ItemMessageDateBinding
import com.application.messagechat.databinding.ItemMessagesReceiveBinding
import com.application.messagechat.databinding.ItemMessagesSentBinding
import com.application.messagechat.model.Messages
import com.application.messagechat.util.ConvertTime
import com.google.android.material.appbar.MaterialToolbar

class MyMessageChatsAdapter(private var toolbar: MaterialToolbar,
                            private val activity: AppCompatActivity,
                            private val onEdit: (Messages,String)-> Unit,
                            private val onDelete: (Messages,String)-> Unit,
                            private val userId: String): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var selectedMessageId: String? = null
    var messageList: List<Messages> = emptyList()
    companion object{
        private const val TYPE_DATE = 0
        private const val TYPE_SENT = 1
        private const val TYPE_RECIVE = 2
    }

    fun setMessages(message: List<Messages>){
        this.messageList = message
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder{

        return if (viewType==TYPE_SENT){
            val view = ItemMessagesSentBinding.inflate(LayoutInflater.from(parent.context),parent,false)
            MyMessageSentViewHolder(view)
        }else if (viewType == TYPE_RECIVE){
            val view = ItemMessagesReceiveBinding.inflate(LayoutInflater.from(parent.context),parent,false)
            MyMessageReciveViewHolder(view)
        }else{
            val view = ItemMessageDateBinding.inflate(LayoutInflater.from(parent.context),parent,false)
            MyMessageDateViewHolder(view)
        }
    }


    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        val message = messageList[position]
        when(holder){
           is MyMessageSentViewHolder ->{
               holder.itemSentBinding.textViewMessage.text = message.message
               holder.itemSentBinding.textViewTime.text = ConvertTime.convertLongToTime(message.messageTime!!.toDate().time)
               if (message.id == selectedMessageId) {
                   holder.itemSentBinding.messageSent.setBackgroundResource(R.color.textbackground)
               } else {
                   holder.itemSentBinding.messageSent.setBackgroundResource(android.R.color.transparent)
               }
               holder.itemSentBinding.messageSent.setOnLongClickListener {
                   selectedMessageId = message.id
                   notifyDataSetChanged()

                   toolbar.menu.clear()
                   activity.menuInflater.inflate(R.menu.message_edit,toolbar.menu)

                   toolbar.setOnMenuItemClickListener { item ->
                       when(item.itemId){

                           R.id.deleteMessage ->{
                               onDelete(message,selectedMessageId!!)
                               clearSelection()
                               true
                           }
                           R.id.editMessage ->{
                               onEdit(message,selectedMessageId!!)
                               true
                           }
                           else ->  false
                       }
                   }

                   true
               }
               holder.itemSentBinding.messageSent.setOnClickListener {
                   clearSelection()
               }
           }is MyMessageReciveViewHolder ->{
               holder.itemReciveBinding.textViewMessageRecive.text = message.message
            holder.itemReciveBinding.textViewTimeRecive.text = ConvertTime.convertLongToTime(message.messageTime!!.toDate().time)
           }
            is MyMessageDateViewHolder -> {
                holder.itemMessageDateBinding.textViewTimeFormat.text = ConvertTime.convertLongToTime(message.messageTime!!.toDate().time)
            }


        }

    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun getItemViewType(position: Int): Int {
        val message = messageList[position]
        return when{
            message.senderId == userId -> TYPE_SENT
            else -> TYPE_RECIVE
        }
    }

    class MyMessageSentViewHolder(val itemSentBinding: ItemMessagesSentBinding): RecyclerView.ViewHolder(itemSentBinding.root)
    class MyMessageReciveViewHolder(val itemReciveBinding: ItemMessagesReceiveBinding): RecyclerView.ViewHolder(itemReciveBinding.root)
    class MyMessageDateViewHolder(val itemMessageDateBinding: ItemMessageDateBinding): RecyclerView.ViewHolder(itemMessageDateBinding.root)

    fun clearSelection() {
        selectedMessageId = null
        notifyDataSetChanged()
        toolbar.menu.clear()
        activity.menuInflater.inflate(R.menu.menu_item, toolbar.menu)
    }


}