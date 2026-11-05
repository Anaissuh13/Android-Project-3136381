package ie.griffith.digitalatlas.data.model

//the bits of a wikipedia article's summary that the facts tab shows
data class WikiSummary(
    val title: String,
    //short one-liner like "City in Connacht, Ireland", not every article has one
    val description: String?,
    //the article's opening paragraph
    val extract: String,
    val thumbnailUrl: String?,
    //link to the full article, used by the "open in wikipedia" button
    val pageUrl: String
)