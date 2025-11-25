package com.application.messagechat.fragmentpage

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.application.messagechat.R
import com.application.messagechat.activity.AddNewContactActivity
import com.application.messagechat.adapter.MyChatAdapter
import com.application.messagechat.databinding.FragmentChatBinding
import com.application.messagechat.model.UserContacts
import com.application.messagechat.activity.MainActivity
import com.application.messagechat.util.ConvertImage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class ChatsFragment : Fragment() {
    private lateinit var chatBinding: FragmentChatBinding

    private  lateinit var myChatAdapter: MyChatAdapter

    private val firestore = FirebaseFirestore.getInstance()
    val contacList = mutableListOf<UserContacts>()
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private var contactListener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layuot for this fragment
        chatBinding = FragmentChatBinding.inflate(inflater, container, false)

        myChatAdapter = MyChatAdapter(contacList,{ selectedItem->
            deleteContact(selectedItem)},{selectedProfile,contactId -> onClickProfile(selectedProfile,contactId)
        })
        chatBinding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        chatBinding.recyclerView.adapter = myChatAdapter

        chatBinding.searchViewChats.setOnClickListener {
            chatBinding.searchViewChats.isIconified = false
        }
        chatBinding.searchViewChats.setOnQueryTextListener(object : android.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                myChatAdapter.filter.filter(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                myChatAdapter.filter.filter(newText)
                return true
            }
        })

        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK){
                refreshChatPage()
            }
        }
        chatBinding.floatingActionButtonAddUser.setOnClickListener {
            val intent = Intent(requireContext(), AddNewContactActivity::class.java)
            activityResultLauncher.launch(intent)
        }

        return chatBinding.root

    }

    fun refreshChatPage(){
        val sharedPreferences = requireContext().getSharedPreferences("MyLoginInfo", MODE_PRIVATE)
        val loggedId = sharedPreferences.getString("userId",null)
        contactListener?.remove()
        if (loggedId != null){
            newContactMessage(loggedId)
            contactListener = firestore.collection("users")
                .document(loggedId)
                .collection("contacts")
                .orderBy("lastMessageTime", Query.Direction.DESCENDING)
                .addSnapshotListener {snapshots,error ->
                    if (error != null){
                        chatBinding.emptyChat.isVisible = true
                        chatBinding.recyclerView.isVisible = false
                        return@addSnapshotListener
                    }
                    contacList.clear()
                    if (snapshots == null || snapshots.isEmpty){
                        myChatAdapter.updateList(emptyList())
                        chatBinding.emptyChat.isVisible = true
                        chatBinding.recyclerView.isVisible = false
                        return@addSnapshotListener
                    }
                    for (documents in snapshots){
                        val userContat = documents.toObject(UserContacts::class.java)
                        contacList.add(userContat)
                    }
                    chatBinding.emptyChat.isVisible = false
                    chatBinding.recyclerView.isVisible = true
                    myChatAdapter.updateList(contacList)
                }
        }

    }

    fun newContactMessage(contactPhonenumber:String){
        firestore.collection("chats")
            .addSnapshotListener { snapshots,error ->
                if (snapshots!=null){
                    for (doc in snapshots){
                        val chatId = doc.id
                        val users = chatId.split("_")
                        if (users.size == 2 && users.contains(contactPhonenumber)){
                            val contactAdd = if (users[0] == contactPhonenumber) users[1] else users[0]
                            val contactExist = firestore.collection("users")
                                .document(contactPhonenumber) .collection("contacts")
                            contactExist.whereEqualTo("phoneNumber",contactAdd)
                                .get()
                                .addOnSuccessListener { addContact->
                                    if (addContact.isEmpty){
                                        addNewContact(contactPhonenumber,contactAdd)
                                    }
                                }

                        }
                    }
                }

            }
    }

    fun addNewContact(contactPhonenumber: String,contactAdd: String){
        val contactExist = firestore.collection("users").document(contactPhonenumber)
            .collection("contacts")
        firestore.collection("users").whereEqualTo("userPhoneNumber",contactAdd)
            .get().addOnSuccessListener { result->
                val chatId = if (contactPhonenumber < contactAdd) "${contactPhonenumber}_${contactAdd}"
                else "${contactAdd}_${contactPhonenumber}"
                firestore.collection("chats").document(chatId).get()
                    .addOnSuccessListener { documentSnapshot ->
                        val lastMsgTime = documentSnapshot.getTimestamp("timeStamp")
                        if (!result.isEmpty){
                            val profile = result.documents[0]
                            val contact = UserContacts(contactAdd,
                                userId = contactPhonenumber, name = contactAdd,
                                contactProfile = profile.getString("profileImage")!!,
                                lastMessageTime = lastMsgTime,
                                phoneNumber = contactAdd, existingUser = contactAdd)
                            contactExist.document(contactAdd).set(contact)
                        }else{
                            val contactProfile = ConvertImage.convertToString(BitmapFactory.decodeResource
                                (resources,R.drawable.person_profile))
                            val contact = UserContacts(contactAdd, userId = contactPhonenumber,
                                name = contactAdd, contactProfile = contactProfile!!,
                                lastMessageTime = lastMsgTime,
                                phoneNumber = contactAdd, existingUser = contactAdd)
                            contactExist.document(contactAdd).set(contact)
                        }
                    }

            }
    }

    fun onClickProfile(image: Bitmap,contactId:String){
        (activity as MainActivity).profileImageView(true,image,contactId)
    }

    fun deleteContact(selectedContact: String){
        val sharedPreferences = requireContext().getSharedPreferences("MyLoginInfo", MODE_PRIVATE)
        val loggedId = sharedPreferences.getString("userId",null)
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Contact")
            .setMessage("Do you want to delete user")
            .setCancelable(false)
            .setPositiveButton("Yes"){_,_->
                val contact = firestore.collection("users")
                    .document(loggedId!!)
                    .collection("contacts")
                    .document(selectedContact)
                contact.delete()
//                firestore.collection("chats")
//                    .document()
            }
            .setNegativeButton("No",null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        refreshChatPage()
        (activity as? MainActivity)?.delechat(true)
    }

    override fun onPause() {
        super.onPause()
        (activity as? MainActivity)?.delechat(false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        contactListener?.remove()
        contactListener = null
    }
}

