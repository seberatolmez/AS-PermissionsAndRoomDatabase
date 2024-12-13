package com.beratolmez.permissionsandroomdemo.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.beratolmez.permissionsandroomdemo.adapter.BrandAdapter
import com.beratolmez.permissionsandroomdemo.databinding.FragmentListBinding
import com.beratolmez.permissionsandroomdemo.model.Brand
import com.beratolmez.permissionsandroomdemo.roomdb.ModelDAO
import com.beratolmez.permissionsandroomdemo.roomdb.modelDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers


class ListFragment : Fragment() {
    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: modelDatabase
    private lateinit var modelDao: ModelDAO
    private val mDisoposable = CompositeDisposable()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = Room.databaseBuilder(requireContext(),modelDatabase::class.java,"Brands").build()
        modelDao = db.modelDao()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentListBinding.inflate(inflater, container, false)
        val view = binding.root
        return view

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.floatingActionButton.setOnClickListener{ add(it) }
        binding.listRecycleView.layoutManager = LinearLayoutManager(requireContext())
        takeDatas()
    }

    private fun takeDatas(){
        mDisoposable.add(
            modelDao.getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponse)
        )
    }
    private fun handleResponse(brandList : List<Brand>){
//        brandList.forEach{
//            println(it.brandName)
//            println(it.modelName)
//        }
        val adapter = BrandAdapter(brandList)
        binding.listRecycleView.adapter=adapter

    }

    fun add(view: View){
        val action =ListFragmentDirections.actionListFragmentToModelFragment(id=-1,info="new")
        Navigation.findNavController(view).navigate(action)

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}