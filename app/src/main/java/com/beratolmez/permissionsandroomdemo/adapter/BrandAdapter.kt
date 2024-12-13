package com.beratolmez.permissionsandroomdemo.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.beratolmez.permissionsandroomdemo.databinding.RecyleRowBinding
import com.beratolmez.permissionsandroomdemo.model.Brand
import com.beratolmez.permissionsandroomdemo.view.ListFragmentDirections
import com.beratolmez.permissionsandroomdemo.view.ModelFragmentDirections

class BrandAdapter(val brandList : List<Brand>) : RecyclerView.Adapter<BrandAdapter.BrandHolder>(){
    class BrandHolder(val binding : RecyleRowBinding) : RecyclerView.ViewHolder(binding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrandHolder {
        val recyleRowBinding = RecyleRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return BrandHolder(recyleRowBinding)
    }

    override fun getItemCount(): Int {
        return brandList.size

    }

    override fun onBindViewHolder(holder: BrandHolder, position: Int) {
        holder.binding.textView.text = brandList[position].brandName
        holder.itemView.setOnClickListener(){
            val action = ListFragmentDirections.actionListFragmentToModelFragment(id=brandList[position].id,info="old")
            Navigation.findNavController(it).navigate(action)
        }
    }
}