package com.application.messagechat.fragmentpage

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.application.messagechat.activity.AddStatusActivity
import com.application.messagechat.activity.ViewStatusActivity
import com.application.messagechat.adapter.MyStatusAdapter
import com.application.messagechat.databinding.FragmentStatusBinding


class StatusFragment : Fragment() {

    private lateinit var myStatusBinding: FragmentStatusBinding
    private lateinit var myStatusAdapter: MyStatusAdapter

    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        myStatusBinding = FragmentStatusBinding.inflate(LayoutInflater.from(requireContext()),container,false)

        myStatusBinding.statusRecyclerView.layoutManager = LinearLayoutManager(requireContext(),
            LinearLayoutManager.HORIZONTAL,false)
        myStatusAdapter = MyStatusAdapter() { id ->
            AlertDialog.Builder(requireContext())
                .setTitle("Delete")
                .setMessage("Do you want to delete Status")
                .setCancelable(false)
                .setPositiveButton("Yes") { _, _ ->

                }
                .setNegativeButton("No",null)
                .show()
        }
        myStatusBinding.statusRecyclerView.adapter = myStatusAdapter


        registerActivityForSelectedImage()
        myStatusBinding.addStatus.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            activityResultLauncher.launch(intent)

        }
        myStatusBinding.addMyStatus.setOnClickListener {
            val activity = Intent(requireContext(), ViewStatusActivity::class.java)
            startActivity(activity)
        }

        return myStatusBinding.root
    }

    fun registerActivityForSelectedImage(){
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            val resultCode = result.resultCode
            val imageData = result.data

            if (resultCode == RESULT_OK && imageData != null){
                val imageUri = imageData.data

                imageUri?.let {
                    val intentActivity = Intent(requireContext(), AddStatusActivity::class.java)
                    intentActivity.putExtra("imageAsUri",it.toString())
                    startActivity(intentActivity)
                }

            }

        }
    }

}