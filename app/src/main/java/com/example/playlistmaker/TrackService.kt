import com.google.gson.annotations.SerializedName

class TracksResponse(
    @SerializedName("results") val tracks: ArrayList<Track>
)

data class Track(
    val trackName: String,
    val artistName: String,
    val trackTimeMillis: Long,
    val artworkUrl100: String,
)