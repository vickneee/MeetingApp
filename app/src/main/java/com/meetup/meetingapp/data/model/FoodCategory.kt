package com.meetup.meetingapp.data.model

/**
 * Represents food categories used as keyword candidates when querying
 * the Google Places API for restaurant searches.
 *
 * Each enum entry contains:
 * - A display label for the category
 * - A `queryName` string used directly as a keyword in Places API requests
 *
 * These categories help refine restaurant search results based on cuisine type.
 */
enum class FoodCategory(
    val queryName: String,
) {
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
    INDIAN("Indian"),
}
