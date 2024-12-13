package com.beratolmez.permissionsandroomdemo.roomdb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.beratolmez.permissionsandroomdemo.model.Brand
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
@Dao
interface ModelDAO {

  @Query("SELECT * FROM Brand")
  fun getAll() : Flowable<List<Brand>>

  @Query("SELECT * FROM Brand WHERE id = :id")
  fun findById(id : Int) : Flowable<Brand>

  @Insert
  fun insert(brand: Brand) :Completable

  @Delete
  fun delete(brand: Brand): Completable
}