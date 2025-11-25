package com.application.messagechat.fragmentpage

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.application.messagechat.activity.MainActivity
import com.application.messagechat.activity.SearchForCallActivity
import com.application.messagechat.adapter.MyCallAdapter
import com.application.messagechat.databinding.FragmentCallsBinding
import com.application.messagechat.model.CallLog
import com.application.messagechat.util.MakeAction
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class CallsFragment : Fragment() {

    private lateinit var callBinding: FragmentCallsBinding
    private lateinit var myCallAdapter: MyCallAdapter
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var sharedPreferences: SharedPreferences
    private  var loginId: String? = null
    private var callListener: ListenerRegistration? = null
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        callBinding = FragmentCallsBinding.inflate(inflater,container,false)


        myCallAdapter = MyCallAdapter{phoneNumber,contactName,contactId ->

            addCallLog(phoneNumber,contactName,contactId)

        }

        callBinding.callRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        callBinding.callRecyclerview.adapter = myCallAdapter

        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK){
                refreshPage()
            }
        }
        callBinding.floatingActionButton.setOnClickListener {
            val intent = Intent(requireContext(), SearchForCallActivity::class.java)
            activityResultLauncher.launch(intent)
        }
        return callBinding.root
    }

    fun refreshPage(){
        sharedPreferences = requireContext().getSharedPreferences("MyLoginInfo", MODE_PRIVATE)
        loginId = sharedPreferences.getString("userId", null)
        callListener?.remove()
        // update UI
        callListener = firestore.collection("users")
            .document(loginId!!)
            .collection("callLog")
            .orderBy("currentTime", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots,error->
                if (error != null){
                    callBinding.emptyCallHistory.isVisible = true
                    callBinding.callRecyclerview.isVisible = false
                    return@addSnapshotListener
                }
                if (snapshots == null) return@addSnapshotListener
                val callLog = snapshots.toObjects(CallLog::class.java)
                myCallAdapter.setCalls(callLog)
                if (callLog.isNotEmpty()){
                    callBinding.emptyCallHistory.isVisible = false
                    callBinding.callRecyclerview.isVisible = true
                    callBinding.callRecyclerview.scrollToPosition(0)
                }else{
                    callBinding.emptyCallHistory.isVisible = true
                    callBinding.callRecyclerview.isVisible = false
                }
            }
    }

    fun addCallLog(phoneNumber:String,contactName: String,contactId: String){
        MakeAction.makeCall(requireActivity(),phoneNumber)
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
            .document(loginId!!)
            .collection("callLog")
            .document(callId)
            .set(callLog)
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).clearCall(true)
        refreshPage()
    }

    override fun onPause() {
        super.onPause()
        (activity as MainActivity).clearCall(false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        callListener?.remove()
        callListener = null
    }

}