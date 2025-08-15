package com.example.hardemusic.data

data class DayEntry(
    val day: Int,
    val songs: List<Song>
)

data class MonthEntry(
    val year: Int,
    val month: Int,
    val days: List<DayEntry>,
)