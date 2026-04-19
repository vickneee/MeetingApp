package com.meetup.meetingapp.ui.theme
import androidx.compose.ui.unit.dp

/*
* 4dp → tiny internal space
* 8dp → small gap
* 16dp → normal default
* 24dp → section break
* 32dp → big section break
*/

object AppSpacing {

    // very small space (inside components)
    val xs = 4.dp

    // small spacing (between related items)
    val sm = 8.dp

    // default spacing (MOST USED)
    // use: between fields, inputs, texts
    val md = 16.dp

    // section spacing
    // use: between groups (form sections)
    val lg = 24.dp

    // big section gap
    // use: screen sections separation
    val xl = 32.dp
}