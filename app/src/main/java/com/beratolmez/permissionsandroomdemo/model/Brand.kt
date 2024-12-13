package com.beratolmez.permissionsandroomdemo.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Brand (
    @ColumnInfo(name = "brandName")
    var brandName: String,

    @ColumnInfo(name = "modelName")
    var modelName : String,

    @ColumnInfo(name = "image")
    var image : ByteArray
)
{
    @PrimaryKey(autoGenerate = true)
    var id = 0


}