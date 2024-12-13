package com.beratolmez.permissionsandroomdemo.roomdb

import androidx.room.Database
import androidx.room.RoomDatabase
import com.beratolmez.permissionsandroomdemo.model.Brand

@Database(entities = [Brand::class], version = 1)
abstract class modelDatabase : RoomDatabase() {
    abstract fun modelDao() : ModelDAO
}