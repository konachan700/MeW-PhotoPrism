package com.mewhpm.mewphotoprism.dto


import com.google.gson.annotations.SerializedName

data class PhotoprismConfigDTO(
    @SerializedName("mode")
    val mode: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("edition")
    val edition: String,
    @SerializedName("version")
    val version: String,
    @SerializedName("copyright")
    val copyright: String,
    @SerializedName("flags")
    val flags: String,
    @SerializedName("baseUri")
    val baseUri: String,
    @SerializedName("staticUri")
    val staticUri: String,
    @SerializedName("cssUri")
    val cssUri: String,
    @SerializedName("jsUri")
    val jsUri: String,
    @SerializedName("manifestUri")
    val manifestUri: String,
    @SerializedName("apiUri")
    val apiUri: String,
    @SerializedName("contentUri")
    val contentUri: String,
    @SerializedName("wallpaperUri")
    val wallpaperUri: String,
    @SerializedName("siteUrl")
    val siteUrl: String,
    @SerializedName("siteDomain")
    val siteDomain: String,
    @SerializedName("siteAuthor")
    val siteAuthor: String,
    @SerializedName("siteTitle")
    val siteTitle: String,
    @SerializedName("siteCaption")
    val siteCaption: String,
    @SerializedName("siteDescription")
    val siteDescription: String,
    @SerializedName("sitePreview")
    val sitePreview: String,
    @SerializedName("imprint")
    val imprint: String,
    @SerializedName("imprintUrl")
    val imprintUrl: String,
    @SerializedName("appName")
    val appName: String,
    @SerializedName("appMode")
    val appMode: String,
    @SerializedName("appIcon")
    val appIcon: String,
    @SerializedName("debug")
    val debug: Boolean,
    @SerializedName("trace")
    val trace: Boolean,
    @SerializedName("test")
    val test: Boolean,
    @SerializedName("demo")
    val demo: Boolean,
    @SerializedName("sponsor")
    val sponsor: Boolean,
    @SerializedName("readonly")
    val readonly: Boolean,
    @SerializedName("uploadNSFW")
    val uploadNSFW: Boolean,
    @SerializedName("public")
    val `public`: Boolean,
    @SerializedName("experimental")
    val experimental: Boolean,
    @SerializedName("albumCategories")
    val albumCategories: Any,
    @SerializedName("albums")
    val albums: List<Any>,
    @SerializedName("cameras")
    val cameras: List<Camera>,
    @SerializedName("lenses")
    val lenses: List<Lense>,
    @SerializedName("countries")
    val countries: List<Country>,
    @SerializedName("people")
    val people: List<Any>,
    @SerializedName("thumbs")
    val thumbs: List<Thumb>,
    @SerializedName("status")
    val status: String,
    @SerializedName("mapKey")
    val mapKey: String,
    @SerializedName("downloadToken")
    val downloadToken: String,
    @SerializedName("previewToken")
    val previewToken: String,
    @SerializedName("settings")
    val settings: Settings,
    @SerializedName("disable")
    val disable: Disable,
    @SerializedName("count")
    val count: Count,
    @SerializedName("pos")
    val pos: Pos,
    @SerializedName("years")
    val years: List<Int>,
    @SerializedName("colors")
    val colors: List<Color>,
    @SerializedName("categories")
    val categories: List<Category>,
    @SerializedName("clip")
    val clip: Int,
    @SerializedName("server")
    val server: Server,
    @SerializedName("ext")
    val ext: Ext
) {
    data class Camera(
        @SerializedName("ID")
        val iD: Int,
        @SerializedName("Slug")
        val slug: String,
        @SerializedName("Name")
        val name: String,
        @SerializedName("Make")
        val make: String,
        @SerializedName("Model")
        val model: String
    )

    data class Lense(
        @SerializedName("ID")
        val iD: Int,
        @SerializedName("Slug")
        val slug: String,
        @SerializedName("Name")
        val name: String,
        @SerializedName("Make")
        val make: String,
        @SerializedName("Model")
        val model: String,
        @SerializedName("Type")
        val type: String
    )

    data class Country(
        @SerializedName("ID")
        val iD: String,
        @SerializedName("Slug")
        val slug: String,
        @SerializedName("Name")
        val name: String
    )

    data class Thumb(
        @SerializedName("size")
        val size: String,
        @SerializedName("use")
        val use: String,
        @SerializedName("w")
        val w: Int,
        @SerializedName("h")
        val h: Int
    )

    data class Settings(
        @SerializedName("ui")
        val ui: Ui,
        @SerializedName("search")
        val search: Search,
        @SerializedName("maps")
        val maps: Maps,
        @SerializedName("features")
        val features: Features,
        @SerializedName("import")
        val `import`: Import,
        @SerializedName("index")
        val index: Index,
        @SerializedName("stack")
        val stack: Stack,
        @SerializedName("share")
        val share: Share,
        @SerializedName("download")
        val download: Download,
        @SerializedName("templates")
        val templates: Templates
    ) {
        data class Ui(
            @SerializedName("scrollbar")
            val scrollbar: Boolean,
            @SerializedName("zoom")
            val zoom: Boolean,
            @SerializedName("theme")
            val theme: String,
            @SerializedName("language")
            val language: String
        )

        data class Search(
            @SerializedName("batchSize")
            val batchSize: Int
        )

        data class Maps(
            @SerializedName("animate")
            val animate: Int,
            @SerializedName("style")
            val style: String
        )

        data class Features(
            @SerializedName("upload")
            val upload: Boolean,
            @SerializedName("download")
            val download: Boolean,
            @SerializedName("private")
            val `private`: Boolean,
            @SerializedName("review")
            val review: Boolean,
            @SerializedName("files")
            val files: Boolean,
            @SerializedName("videos")
            val videos: Boolean,
            @SerializedName("folders")
            val folders: Boolean,
            @SerializedName("albums")
            val albums: Boolean,
            @SerializedName("moments")
            val moments: Boolean,
            @SerializedName("estimates")
            val estimates: Boolean,
            @SerializedName("people")
            val people: Boolean,
            @SerializedName("labels")
            val labels: Boolean,
            @SerializedName("places")
            val places: Boolean,
            @SerializedName("edit")
            val edit: Boolean,
            @SerializedName("archive")
            val archive: Boolean,
            @SerializedName("delete")
            val delete: Boolean,
            @SerializedName("share")
            val share: Boolean,
            @SerializedName("library")
            val library: Boolean,
            @SerializedName("import")
            val `import`: Boolean,
            @SerializedName("logs")
            val logs: Boolean
        )

        data class Import(
            @SerializedName("path")
            val path: String,
            @SerializedName("move")
            val move: Boolean
        )

        data class Index(
            @SerializedName("path")
            val path: String,
            @SerializedName("convert")
            val convert: Boolean,
            @SerializedName("rescan")
            val rescan: Boolean,
            @SerializedName("skipArchived")
            val skipArchived: Boolean
        )

        data class Stack(
            @SerializedName("uuid")
            val uuid: Boolean,
            @SerializedName("meta")
            val meta: Boolean,
            @SerializedName("name")
            val name: Boolean
        )

        data class Share(
            @SerializedName("title")
            val title: String
        )

        data class Download(
            @SerializedName("name")
            val name: String,
            @SerializedName("disabled")
            val disabled: Boolean,
            @SerializedName("originals")
            val originals: Boolean,
            @SerializedName("mediaRaw")
            val mediaRaw: Boolean,
            @SerializedName("mediaSidecar")
            val mediaSidecar: Boolean
        )

        data class Templates(
            @SerializedName("default")
            val default: String
        )
    }

    data class Disable(
        @SerializedName("backups")
        val backups: Boolean,
        @SerializedName("webdav")
        val webdav: Boolean,
        @SerializedName("settings")
        val settings: Boolean,
        @SerializedName("places")
        val places: Boolean,
        @SerializedName("exiftool")
        val exiftool: Boolean,
        @SerializedName("ffmpeg")
        val ffmpeg: Boolean,
        @SerializedName("raw")
        val raw: Boolean,
        @SerializedName("darktable")
        val darktable: Boolean,
        @SerializedName("rawtherapee")
        val rawtherapee: Boolean,
        @SerializedName("sips")
        val sips: Boolean,
        @SerializedName("heifconvert")
        val heifconvert: Boolean,
        @SerializedName("tensorflow")
        val tensorflow: Boolean,
        @SerializedName("faces")
        val faces: Boolean,
        @SerializedName("classification")
        val classification: Boolean
    )

    data class Count(
        @SerializedName("all")
        val all: Int,
        @SerializedName("photos")
        val photos: Int,
        @SerializedName("live")
        val live: Int,
        @SerializedName("videos")
        val videos: Int,
        @SerializedName("cameras")
        val cameras: Int,
        @SerializedName("lenses")
        val lenses: Int,
        @SerializedName("countries")
        val countries: Int,
        @SerializedName("hidden")
        val hidden: Int,
        @SerializedName("favorites")
        val favorites: Int,
        @SerializedName("private")
        val `private`: Int,
        @SerializedName("review")
        val review: Int,
        @SerializedName("stories")
        val stories: Int,
        @SerializedName("albums")
        val albums: Int,
        @SerializedName("moments")
        val moments: Int,
        @SerializedName("months")
        val months: Int,
        @SerializedName("folders")
        val folders: Int,
        @SerializedName("files")
        val files: Int,
        @SerializedName("people")
        val people: Int,
        @SerializedName("places")
        val places: Int,
        @SerializedName("states")
        val states: Int,
        @SerializedName("labels")
        val labels: Int,
        @SerializedName("labelMaxPhotos")
        val labelMaxPhotos: Int
    )

    data class Pos(
        @SerializedName("uid")
        val uid: String,
        @SerializedName("cid")
        val cid: String,
        @SerializedName("utc")
        val utc: String,
        @SerializedName("lat")
        val lat: Int,
        @SerializedName("lng")
        val lng: Int
    )

    data class Color(
        @SerializedName("Example")
        val example: String,
        @SerializedName("Name")
        val name: String,
        @SerializedName("Slug")
        val slug: String
    )

    data class Category(
        @SerializedName("UID")
        val uID: String,
        @SerializedName("Slug")
        val slug: String,
        @SerializedName("Name")
        val name: String
    )

    data class Server(
        @SerializedName("cores")
        val cores: Int,
        @SerializedName("routines")
        val routines: Int,
        @SerializedName("memory")
        val memory: Memory
    ) {
        data class Memory(
            @SerializedName("total")
            val total: Long,
            @SerializedName("free")
            val free: Long,
            @SerializedName("used")
            val used: Int,
            @SerializedName("reserved")
            val reserved: Int,
            @SerializedName("info")
            val info: String
        )
    }

    class Ext
}