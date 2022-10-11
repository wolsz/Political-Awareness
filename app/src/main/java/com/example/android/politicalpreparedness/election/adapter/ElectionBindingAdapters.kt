package com.example.android.politicalpreparedness.election.adapter

import android.util.Log
import android.view.View
import android.widget.Button
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.network.models.Election

    @BindingAdapter("electionsListData")
    fun electionsRecyclerView(recyclerView: RecyclerView, data: List<Election>?) {
        val adapter = recyclerView.adapter as ElectionListAdapter
        adapter.submitList(data)
    }

@BindingAdapter("setFollowedState")
 fun Button.setFollowedState(isFollowed: Boolean) {
     if (isFollowed) {
         Log.v("BindingAdapter -- Followed", "$isFollowed" )
         this.text = context.getString(R.string.unfollow_label)
     } else {
         Log.v("BindingAdapter -- not Followed", "$isFollowed" )
         this.text = context.getString(R.string.follow_label)
     }
 }