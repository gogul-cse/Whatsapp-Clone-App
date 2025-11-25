package com.application.messagechat.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.application.messagechat.R
import com.application.messagechat.adapter.SearchCallAdapter
import com.application.messagechat.databinding.ActivitySearchForCallBinding
import com.application.messagechat.model.CallLog
import com.application.messagechat.model.UserContacts
import com.application.messagechat.util.CallPermissionHandler
import com.application.messagechat.util.MakeAction
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

class SearchForCallActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchForCallBinding
    private lateinit var adapter: SearchCallAdapter
    val callList = mutableListOf<UserContacts>()
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var callLauncher: CallPermissionHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        callLauncher = CallPermissionHandler(this)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search_for_call)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        adapter = SearchCallAdapter(callList){contactName,phoneNumber,contactId->
            callLauncher.setphoneNumber(phoneNumber)
            callLauncher.requestCallPermission()
            addCallLog(contactName,phoneNumber,contactId)
        }

        binding.searchCallRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.searchCallRecyclerView.adapter = adapter

        binding.searchCallToolbar.setNavigationOnClickListener { finish() }
        binding.searchCall.setOnClickListener {
            binding.searchCall.isIconified = false
        }

        binding.searchCall.setOnQueryTextListener(object :android.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
               adapter.filter.filter(query)
                return true
            }
        })

        val sharedPreferences = getSharedPreferences("MyLoginInfo", MODE_PRIVATE)
        val loggedId = sharedPreferences.getString("userId",null)
        if (loggedId != null){
            firestore.collection("users")
                .document(loggedId)
                .collection("contacts")
                .addSnapshotListener {snapshots,error ->
                    if (error != null){
                        return@addSnapshotListener
                    }
                    callList.clear()
                    if (snapshots != null){
                        for (documents in snapshots){
                            val userContat = documents.toObject(UserContacts::class.java)
                            callList.add(userContat)
                        }
                        adapter.updateCallList(callList)
                    }else{
                        adapter.updateCallList(emptyList())
                    }
                }
        }

    }
    fun addCallLog(contactName: String,phoneNumber:String,contactId: String){
        val sharedPreferences = getSharedPreferences("MyLoginInfo", MODE_PRIVATE)
        val loggedId = sharedPreferences.getString("userId",null)

        val callId = firestore.collection("callLog").document().id
        val callLog = CallLog(
            id = callId,
            contactId = contactId,
            contactName = contactName,
            contactPhoneNumber = phoneNumber,
            callType = "Outgoing",
            currentTime = Timestamp.now()
        )
        firestore.collection("users")
            .document(loggedId!!)
            .collection("callLog")
            .document(callId)
            .set(callLog)
            .addOnSuccessListener {
                MakeAction.makeCall(this,phoneNumber)
                setResult(RESULT_OK)
            }
    }


}