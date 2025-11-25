package com.application.messagechat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.application.messagechat.databinding.ItemCallBinding
import com.application.messagechat.model.CallLog
import com.application.messagechat.util.ConvertImage
import com.application.messagechat.util.ConvertTime
import com.google.firebase.firestore.FirebaseFirestore

class MyCallAdapter( private val onCallClick:(String, String, String)->Unit): RecyclerView.Adapter<MyCallAdapter.MyCallViewHolder>() {

    var callList: List<CallLog> = emptyList()
    val firestore = FirebaseFirestore.getInstance()
    fun setCalls(call: List<CallLog>){
        this.callList = call
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyCallViewHolder {
        val view = ItemCallBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MyCallViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: MyCallViewHolder,
        position: Int
    ) {
        val call = callList[position]

        firestore.collection("users")
            .whereEqualTo("id",call.contactPhoneNumber)
            .get()
            .addOnSuccessListener {documentSnapshots ->
                if (!documentSnapshots.isEmpty || documentSnapshots.documents.isNotEmpty()){
                    val document = documentSnapshots.documents.first()
                    val image = document.getString("profileImage")
                    holder.itemCallBinding.callProfile.setImageBitmap(ConvertImage.convertToBitmap(image))
                }

            }

        holder.itemCallBinding.callPersonName.text = call.contactName
        holder.itemCallBinding.callTime.text = ConvertTime.convertLongToTime(call.currentTime!!.toDate().time)
        holder.itemCallBinding.makeCall.setOnClickListener {
                onCallClick(call.contactPhoneNumber,call.contactName,call.contactId)
            }


    }

    override fun getItemCount(): Int {
        return callList.size
    }

    class MyCallViewHolder(val itemCallBinding: ItemCallBinding): RecyclerView.ViewHolder(itemCallBinding.root){
    }

}