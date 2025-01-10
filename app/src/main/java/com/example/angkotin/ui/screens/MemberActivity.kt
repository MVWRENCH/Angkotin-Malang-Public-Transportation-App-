package com.example.angkotin

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MemberActivity : AppCompatActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContent {
            TeamScreen()
        }
    }
}

data class TeamMember(
    val name: String,
    val role: String,
    val imageRes: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamScreen() {
    val context = LocalContext.current

    val teamMembers = listOf(
        TeamMember("BAYU ANUGRAH RAMADAN", "Android Studio Developer", R.drawable.bayu),
        TeamMember("DIMAS SIRAJUDDIN ARAFA", "Artificial Intelligence Specialist", R.drawable.dimas),
        TeamMember("GEA DIAN LESTARI", "Artificial Intelligence Specialist", R.drawable.gea),
        TeamMember("MARVEL STEFANO", "Android Studio Developer", R.drawable.marvel),
        TeamMember("STEFANUS ADAM", "Database Specialist", R.drawable.adam)
    )

    // Menggunakan warna biru yang sama seperti di AboutActivity
    val gradientBackground = Brush.linearGradient(
        colors = listOf(
            Color(0xFF1E88E5).copy(alpha = 0.9f),
            Color(0xFF6AB7FF).copy(alpha = 0.7f)
        )
    )

    // Menggunakan warna biru yang sama untuk aksen
    val angkotinBlue = Color(0xFF1E88E5)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meet the Team", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = {
                        (context as? MemberActivity)?.onBackPressed()
                    }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color(0xFF1E88E5)
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBackground)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(teamMembers) { member ->
                        TeamMemberCard(member, angkotinBlue)
                    }
                }
            }
        }
    }
}

@Composable
fun TeamMemberCard(member: TeamMember, accentColor: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .padding(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))  // Menggunakan background semi-transparan seperti AboutActivity
            )

            Image(
                painter = painterResource(id = member.imageRes),
                contentDescription = "Profile picture of ${member.name}",
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(accentColor)
                    .align(Alignment.BottomEnd)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = member.name,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Text(
            text = member.role,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}