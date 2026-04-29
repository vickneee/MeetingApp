package com.meetup.meetingapp.network

import com.google.gson.annotations.SerializedName

/**
 * Represents a photo object returned by the Google Places API.
 *
 * A Place Photo does not contain the actual image data. Instead, it provides
 * a `photo_reference` token that can be used to request the image through the
 * Places Photo API endpoint.
 *
 * Example JSON:
 * {
 *   "photo_reference": "Aap_uEC...",
 *   "height": 3024,
 *   "width": 4032
 * }
 *
 * Notes:
 *  - Only the `photo_reference` is required to fetch the actual image.
 *  - Height and width describe the original image dimensions and may be used
 *    to request scaled versions.
 *
 * @property photoReference Token used to retrieve the photo via the Places Photo API.
 * @property height Original height of the photo in pixels.
 * @property width Original width of the photo in pixels.
 */
data class PlacePhoto(
    @SerializedName("photo_reference")
    val photoReference: String?,
    val height: Int?,
    val width: Int?,
)
