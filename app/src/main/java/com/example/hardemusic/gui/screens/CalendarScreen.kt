package com.example.hardemusic.gui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.hardemusic.data.AppText
import com.example.hardemusic.data.DayEntry
import com.example.hardemusic.data.MonthEntry
import com.example.hardemusic.data.Song
import com.example.hardemusic.viewmodel.MainViewModel
import java.time.LocalDate
import java.time.Month
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarScreen(
    viewModel: MainViewModel,
    context: Context,
    navController: NavController
) {
    var expandedMonths by remember { mutableStateOf(setOf<Pair<Int, Int>>()) }
    val months by viewModel.downloadedSongsByMonth.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadSongs()
        viewModel.loadDownloadedSongsGroupedByDate(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Atrás",
                tint = Color.White,
                modifier = Modifier
                    .size(28.dp)
                    .clickable { navController.popBackStack() }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = AppText.calendarTitle,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge
            )
        }

        if (months.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = AppText.loadingPlaceholder,
                    color = Color.White
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                months.forEach { month ->
                    val monthKey = month.year to month.month
                    item {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        expandedMonths = if (expandedMonths.contains(monthKey)) {
                                            expandedMonths - monthKey
                                        } else {
                                            expandedMonths + monthKey
                                        }
                                    }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${
                                        Month.of(month.month)
                                            .getDisplayName(TextStyle.SHORT, Locale(AppText.language.lowercase()))
                                    }. ${month.year}",
                                    color = Color.Cyan,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(Modifier.weight(1f))
                                Icon(
                                    imageVector = if (expandedMonths.contains(monthKey))
                                        Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }

                            if (expandedMonths.contains(monthKey)) {
                                MonthCalendarGrid(month) { songs, date ->
                                    viewModel.setSelectedSongsForDay(songs)
                                    navController.navigate("day_songs/$date")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MonthCalendarGrid(month: MonthEntry, onDayClick: (List<Song>, String) -> Unit) {
    val days = month.days
    val firstDayOfWeek = LocalDate.of(month.year, month.month, 1).dayOfWeek.value % 7

    Column(Modifier.padding(horizontal = 8.dp)) {
        Row(Modifier.fillMaxWidth()) {
            AppText.daysOfWeek.forEach { label ->
                Text(
                    text = label,
                    color = when(label) {
                        "Dom", "Sun" -> Color.Red
                        "Sáb", "Sat" -> Color.Blue
                        else -> Color.White
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        val rows = mutableListOf<List<DayEntry>>()
        var index = 0
        val leadingEmpty = List(firstDayOfWeek) { DayEntry(-1, emptyList()) }
        val paddedDays = leadingEmpty + days
        val trailingEmpty = List((7 - paddedDays.size % 7) % 7) { DayEntry(-1, emptyList()) }
        val fullPaddedDays = paddedDays + trailingEmpty

        while (index < fullPaddedDays.size) {
            val week = fullPaddedDays.subList(index, index + 7)
            rows.add(week)
            index += 7
        }

        rows.forEach { week ->
            Row(Modifier.fillMaxWidth()) {
                week.forEach { day ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(2.dp)
                            .background(Color.DarkGray, RoundedCornerShape(4.dp))
                            .clickable(enabled = day.songs.isNotEmpty()) {
                                val date = LocalDate.of(month.year, month.month, day.day)
                                    .format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                                onDayClick(day.songs, date)
                            }
                            .border(1.dp, Color.Gray, RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (day.day > 0) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (day.songs.isNotEmpty()) {
                                    val cover = day.songs.firstOrNull { it.albumArtUri != null }?.albumArtUri
                                    if (cover != null) {
                                        AsyncImage(
                                            model = cover,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(4.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.MusicNote,
                                        contentDescription = null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Text(
                                text = day.day.toString(),
                                color = Color.Yellow,
                                fontSize = 10.sp,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}



