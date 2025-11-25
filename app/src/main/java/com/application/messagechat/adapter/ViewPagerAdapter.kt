package com.application.messagechat.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.application.messagechat.fragmentpage.CallsFragment
import com.application.messagechat.fragmentpage.ChatsFragment
import com.application.messagechat.fragmentpage.StatusFragment

class ViewPagerAdapter(fragmentActivity: FragmentActivity): FragmentStateAdapter(fragmentActivity) {
    override fun createFragment(position: Int): Fragment {
       return when(position){
           0 -> {
               ChatsFragment()
           }
           1 -> StatusFragment()
           else -> CallsFragment()
       }
    }

    override fun getItemCount(): Int {
        return 3
    }
}