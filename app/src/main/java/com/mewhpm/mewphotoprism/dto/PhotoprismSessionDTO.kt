package com.mewhpm.mewphotoprism.dto


import com.google.gson.annotations.SerializedName

data class PhotoprismSessionDTO(
    @SerializedName("config")
    val config: PhotoprismConfigDTO,
    @SerializedName("data")
    val `data`: Data,
    @SerializedName("id")
    val id: String,
    @SerializedName("status")
    val status: String
) {
    data class Data(
        @SerializedName("user")
        val user: User,
        @SerializedName("tokens")
        val tokens: Any,
        @SerializedName("shares")
        val shares: Any
    ) {
        data class User(
            @SerializedName("Address")
            val address: Address,
            @SerializedName("UID")
            val uID: String,
            @SerializedName("MotherUID")
            val motherUID: String,
            @SerializedName("FatherUID")
            val fatherUID: String,
            @SerializedName("GlobalUID")
            val globalUID: String,
            @SerializedName("FullName")
            val fullName: String,
            @SerializedName("NickName")
            val nickName: String,
            @SerializedName("MaidenName")
            val maidenName: String,
            @SerializedName("ArtistName")
            val artistName: String,
            @SerializedName("UserName")
            val userName: String,
            @SerializedName("UserStatus")
            val userStatus: String,
            @SerializedName("UserDisabled")
            val userDisabled: Boolean,
            @SerializedName("PrimaryEmail")
            val primaryEmail: String,
            @SerializedName("EmailConfirmed")
            val emailConfirmed: Boolean,
            @SerializedName("BackupEmail")
            val backupEmail: String,
            @SerializedName("PersonURL")
            val personURL: String,
            @SerializedName("PersonPhone")
            val personPhone: String,
            @SerializedName("PersonStatus")
            val personStatus: String,
            @SerializedName("PersonAvatar")
            val personAvatar: String,
            @SerializedName("PersonLocation")
            val personLocation: String,
            @SerializedName("PersonBio")
            val personBio: String,
            @SerializedName("BusinessURL")
            val businessURL: String,
            @SerializedName("BusinessPhone")
            val businessPhone: String,
            @SerializedName("BusinessEmail")
            val businessEmail: String,
            @SerializedName("CompanyName")
            val companyName: String,
            @SerializedName("DepartmentName")
            val departmentName: String,
            @SerializedName("JobTitle")
            val jobTitle: String,
            @SerializedName("BirthYear")
            val birthYear: Int,
            @SerializedName("BirthMonth")
            val birthMonth: Int,
            @SerializedName("BirthDay")
            val birthDay: Int,
            @SerializedName("TermsAccepted")
            val termsAccepted: Boolean,
            @SerializedName("IsArtist")
            val isArtist: Boolean,
            @SerializedName("IsSubject")
            val isSubject: Boolean,
            @SerializedName("RoleAdmin")
            val roleAdmin: Boolean,
            @SerializedName("RoleGuest")
            val roleGuest: Boolean,
            @SerializedName("RoleChild")
            val roleChild: Boolean,
            @SerializedName("RoleFamily")
            val roleFamily: Boolean,
            @SerializedName("RoleFriend")
            val roleFriend: Boolean,
            @SerializedName("WebDAV")
            val webDAV: Boolean,
            @SerializedName("StoragePath")
            val storagePath: String,
            @SerializedName("CanInvite")
            val canInvite: Boolean,
            @SerializedName("CreatedAt")
            val createdAt: String,
            @SerializedName("UpdatedAt")
            val updatedAt: String
        ) {
            data class Address(
                @SerializedName("ID")
                val iD: Int,
                @SerializedName("CellID")
                val cellID: String,
                @SerializedName("Src")
                val src: String,
                @SerializedName("Lat")
                val lat: Int,
                @SerializedName("Lng")
                val lng: Int,
                @SerializedName("Line1")
                val line1: String,
                @SerializedName("Line2")
                val line2: String,
                @SerializedName("Zip")
                val zip: String,
                @SerializedName("City")
                val city: String,
                @SerializedName("State")
                val state: String,
                @SerializedName("Country")
                val country: String,
                @SerializedName("Notes")
                val notes: String,
                @SerializedName("CreatedAt")
                val createdAt: String,
                @SerializedName("UpdatedAt")
                val updatedAt: String
            )
        }
    }
}