package com.beratolmez.permissionsandroomdemo.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ColorSpace.Model
import android.graphics.ImageDecoder
import android.icu.text.ListFormatter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.beratolmez.permissionsandroomdemo.databinding.FragmentModelBinding
import com.google.android.material.snackbar.Snackbar
import android.provider.MediaStore
import androidx.navigation.Navigation
import androidx.room.Room
import com.beratolmez.permissionsandroomdemo.model.Brand
import com.beratolmez.permissionsandroomdemo.roomdb.ModelDAO
import com.beratolmez.permissionsandroomdemo.roomdb.modelDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.ByteArrayOutputStream
import java.io.IOException


class ModelFragment : Fragment() {
    private var _binding: FragmentModelBinding? = null
    private val binding get() = _binding!!
    private lateinit var permissionLauncher : ActivityResultLauncher<String> // request permission
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent> //access to gallery
    private var selectedImage : Uri? = null// example-> data/data/media/dowloands/image.jpeg
    private var selectedBitmap : Bitmap? = null // to convert the uri to bitmap(image)
    private var selectedBrand : Brand? = null
    private val mDisoposable = CompositeDisposable()

    private lateinit var db: modelDatabase
    private lateinit var modelDao: ModelDAO


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerLauncher()

        db = Room.databaseBuilder(requireContext(),modelDatabase::class.java,"Brands").build()
        modelDao = db.modelDao()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentModelBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.saveButton.setOnClickListener{ save(it) }
        binding.deleteButton.setOnClickListener{ delete(it) }
        binding.imageView.setOnClickListener{ pickImage(it) }
        arguments?.let {
            val info = ModelFragmentArgs.fromBundle(it).info
            if (info =="new"){
                //new model will being added
                selectedBrand = null
                binding.modelEditText.setText("")
                binding.nameEditText.setText("")
                binding.saveButton.isEnabled=true
                binding.deleteButton.isEnabled=false

            } else {
                // will showed old model from room database

                binding.saveButton.isEnabled=false
                binding.deleteButton.isEnabled=true
                val id = ModelFragmentArgs.fromBundle(it).id

                mDisoposable.add(
                    modelDao.findById(id)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::handleRespond)

                )




            }
        }

    }

    private fun handleRespond(brand : Brand){
        binding.modelEditText.setText(brand.modelName)
        binding.nameEditText.setText(brand.brandName)
        val bitmap = BitmapFactory.decodeByteArray(brand.image,0,brand.image.size)
        binding.imageView.setImageBitmap(bitmap)
        selectedBrand = brand
    }
    fun pickImage(view: View) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){// for new version
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){
                    //no permission  needed to require permissions
                    if(ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),Manifest.permission.READ_MEDIA_IMAGES)){
                        //snackbar will be showed to explain why we need permission
                        Snackbar.make(view,"We need to access your images to enhance your experience.",Snackbar.LENGTH_INDEFINITE).setAction(
                            "Give Permission",View.OnClickListener {
                                //request permission
                                permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                            }
                        ).show()
                    }else{
                        // request permisson
                        permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)

                    }
                }else{
                    // permission was already granted
                    val intenToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    activityResultLauncher.launch(intenToGallery)

                }

            }else{// for old version
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    //no permission  needed to require permissions
                    if(ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),Manifest.permission.READ_EXTERNAL_STORAGE)){
                        //snackbar will be showed to explain why we need permission
                        Snackbar.make(view,"We need to access your images to enhance your experience.",Snackbar.LENGTH_INDEFINITE).setAction(
                            "Give Permission",View.OnClickListener {
                                //request permission
                                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                            }
                        ).show()
                    }else{
                        // request permisson
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)

                    }
                }else{
                    // permission was already granted
                    val intenToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    activityResultLauncher.launch(intenToGallery)

                }

            }
        }




    fun save(view: View){
        val brandName = binding.nameEditText.text.toString()
        val modelName = binding.modelEditText.text.toString()

        if (selectedBitmap!= null){
            val smallBitmap = createSmallBitmap(selectedBitmap!!,300)
            val outputStream = ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            val byteArray = outputStream.toByteArray()
            val brand = Brand(brandName,modelName,byteArray)

            //threading RxJava
            mDisoposable.add(modelDao.insert(brand)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()) // which thread will be used
                .subscribe(this::handleResponseForInsert) )// what will happen as a result)

        }


    }
    private fun handleResponseForInsert(){ // what will happen in the result of insert (may turned back previous page)

        val action = ModelFragmentDirections.actionModelFragmentToListFragment()
        Navigation.findNavController(requireView()).navigate(action)
    }
    fun delete(view: View){

        if (selectedBrand!= null){
            mDisoposable.add(
                modelDao.delete(brand = selectedBrand!!)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this:: handleResponseForInsert)
            )

        }


    }

     private fun registerLauncher(){

        activityResultLauncher =registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result->
            if (result.resultCode == AppCompatActivity.RESULT_OK){
                val intentFromResult = result.data
                if(intentFromResult != null){
                   val selectedImage = intentFromResult.data

                    try {// if there is any problem try these else catch will be executed and printed the error message
                        if (Build.VERSION.SDK_INT >=28){
                            val source = ImageDecoder.createSource(requireActivity().contentResolver,selectedImage!!)
                            selectedBitmap = ImageDecoder.decodeBitmap(source)
                            binding.imageView.setImageBitmap(selectedBitmap)

                        }else{
                            selectedBitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver,selectedImage)
                            binding.imageView.setImageBitmap(selectedBitmap)


                        }
                    }catch (e: IOException){
                        println(e.localizedMessage)
                    }



                }
            }

        }
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){result ->
            if (result){
                // permission was given
                // be able to access galery
                val intenToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intenToGallery)

            }else{
                // permisson was not given
                Toast.makeText(requireContext(),"Permission needed!",Toast.LENGTH_LONG).show()

            }

        }
    }

    private fun createSmallBitmap(userBitmap : Bitmap,maxSize : Int) : Bitmap{

        var width = userBitmap.width
        var hight = userBitmap.height
        val bitmapRatio : Double = width.toDouble() / hight.toDouble()

        if(bitmapRatio > 1){//horizontal
            width = maxSize
            val scaledHight = width / bitmapRatio
            hight = scaledHight.toInt()

        }else{//vertical
            hight = maxSize
            val scaledWidth = hight * bitmapRatio
            width = scaledWidth.toInt()

        }
        return Bitmap.createScaledBitmap(userBitmap,width,hight,true)

    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mDisoposable.clear()
    }

}