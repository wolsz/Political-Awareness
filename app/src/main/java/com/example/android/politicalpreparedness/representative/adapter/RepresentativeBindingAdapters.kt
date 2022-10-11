package com.example.android.politicalpreparedness.representative.adapter

import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.representative.model.Representative

@BindingAdapter("profileImage")
fun fetchImage(view: ImageView, src: String?) {
    src?.let {
        val uri = src.toUri().buildUpon().scheme("https").build()
        Glide.with(view.context)
                .load(uri)
                .apply(RequestOptions()
                        .placeholder(R.drawable.ic_profile))
                .circleCrop()
                .into(view)
    }
}

@BindingAdapter(value = ["stateValue", "stateValueAttrChanged"], requireAll = false)
fun Spinner.setNewValue(value: String?, listener: InverseBindingListener) {
    val adapter = toTypedAdapter<String>(this.adapter as ArrayAdapter<*>)
    setSpinnerListener(this, listener)
    val position = when (adapter.getItem(0)) {
        is String -> adapter.getPosition(value)
        else -> this.selectedItemPosition
    }
    if (position >= 0) {
        setSelection(position)

    }
}

@InverseBindingAdapter(attribute = "stateValue")
fun getStateValue(spinner: Spinner): String {
    return spinner.selectedItem as String
}


@BindingAdapter("representativeData")
fun representativesRecyclerView(recyclerView: RecyclerView, data: List<Representative>?) {
    val adapter = recyclerView.adapter as RepresentativeListAdapter
    adapter.submitList(data)
}

inline fun <reified T> toTypedAdapter(adapter: ArrayAdapter<*>): ArrayAdapter<T> {
    @Suppress("UNCHECKED_CAST")
    return adapter as ArrayAdapter<T>
}

private fun setSpinnerListener(spinner: Spinner, listener: InverseBindingListener) {
    spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) = listener.onChange()
        override fun onNothingSelected(adapterView: AdapterView<*>) = listener.onChange()
    }
}

