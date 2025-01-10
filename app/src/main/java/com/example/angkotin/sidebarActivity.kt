package com.example.angkotin

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

class SidebarActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SidebarScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SidebarScreen() {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val gradientBackground = Brush.linearGradient(
        colors = listOf(
            Color(0xFF1E88E5).copy(alpha = 0.9f),
            Color(0xFF6AB7FF).copy(alpha = 0.7f)
        )
    )

    LaunchedEffect(Unit) {
        drawerState.open()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.fillMaxWidth(),
                drawerContainerColor = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(brush = gradientBackground)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Header dengan desain modern
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "ANGKOTIN",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Menu Items
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.2f)
                            ),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                SidebarMenuItem(
                                    icon = Icons.Default.Home,
                                    text = "Home",
                                    onClick = {
                                        (context as? SidebarActivity)?.onBackPressed()
                                    }
                                )
                                Divider(color = Color.White.copy(alpha = 0.3f), thickness = 1.dp)
                                SidebarMenuItem(
                                    icon = Icons.Default.Star,
                                    text = "Routes",
                                    onClick = {
                                        context.startActivity(
                                            Intent(context, ListRouteActivity::class.java)
                                        )
                                    }
                                )
                                Divider(color = Color.White.copy(alpha = 0.3f), thickness = 1.dp)
                                SidebarMenuItem(
                                    icon = Icons.Default.Info,
                                    text = "About",
                                    onClick = {
                                        context.startActivity(
                                            Intent(context, AboutActivity::class.java)
                                        )
                                    }
                                )
                                Divider(color = Color.White.copy(alpha = 0.3f), thickness = 1.dp)
                                SidebarMenuItem(
                                    icon = Icons.Default.Settings,
                                    text = "Settings",
                                    onClick = {
                                        context.startActivity(
                                            Intent(context, SettingsActivity::class.java)
                                        )
                                    }
                                )
                                Divider(color = Color.White.copy(alpha = 0.3f), thickness = 1.dp)
                                SidebarMenuItem(
                                    icon = Icons.Default.Person,
                                    text = "Team Members",
                                    onClick = {
                                        context.startActivity(
                                            Intent(context, MemberActivity::class.java)
                                        )
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Footer
                        Text(
                            text = "Angkotin v1.0.0",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        },
        content = {
            // Konten aplikasi utama (bisa ditambahkan jika perlu)
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // Kosongkan bagian ini jika tidak ada konten tambahan
            }
        }
    )
}

@Composable
fun SidebarMenuItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            modifier = Modifier.size(24.dp),
            tint = Color.White
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            fontSize = 18.sp,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}