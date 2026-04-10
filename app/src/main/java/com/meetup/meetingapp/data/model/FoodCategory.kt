package com.meetup.meetingapp.data.model

// Food categories used as keyword candidates when querying the Google Places API.
enum class FoodCategory(val queryName: String) {
    ITALIAN("Italian"),
    PIZZA("Pizza"),
    BURGER("Burger"),
    STEAK("Steak"),
    BRUNCH("Brunch"),
    BBQ("BBQ"),
    MEXICAN("Mexican"),
    VEGAN("Vegan"),
    VEGETARIAN("Vegetarian"),
    ASIAN("Asian"),
    SUSHI("Sushi"),
    THAI("Thai"),
    INDIAN("Indian")
}
