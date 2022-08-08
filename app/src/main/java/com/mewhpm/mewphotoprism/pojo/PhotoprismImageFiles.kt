package com.mewhpm.mewphotoprism.pojo

import com.google.gson.annotations.SerializedName

data class PhotoprismImageFiles(
    @SerializedName("UID"          ) var UID          : String?           = null,
    @SerializedName("PhotoUID"     ) var PhotoUID     : String?           = null,
    @SerializedName("Name"         ) var Name         : String?           = null,
    @SerializedName("Root"         ) var Root         : String?           = null,
    @SerializedName("OriginalName" ) var OriginalName : String?           = null,
    @SerializedName("Hash"         ) var Hash         : String?           = null,
    @SerializedName("Size"         ) var Size         : Int?              = null,
    @SerializedName("Codec"        ) var Codec        : String?           = null,
    @SerializedName("Type"         ) var Type         : String?           = null,
    @SerializedName("Mime"         ) var Mime         : String?           = null,
    @SerializedName("Primary"      ) var Primary      : Boolean?          = null,
    @SerializedName("Width"        ) var Width        : Int?              = null,
    @SerializedName("Height"       ) var Height       : Int?              = null,
    @SerializedName("Orientation"  ) var Orientation  : Int?              = null,
    @SerializedName("AspectRatio"  ) var AspectRatio  : Double?           = null,
    @SerializedName("Colors"       ) var Colors       : String?           = null,
    @SerializedName("Luminance"    ) var Luminance    : String?           = null,
    @SerializedName("Diff"         ) var Diff         : Int?              = null,
    @SerializedName("Chroma"       ) var Chroma       : Int?              = null,
    @SerializedName("CreatedAt"    ) var CreatedAt    : String?           = null,
    @SerializedName("UpdatedAt"    ) var UpdatedAt    : String?           = null,
    //@SerializedName("Markers"      ) var Markers      : Map<Any, Any> = HashMap<Any, Any>()
)
