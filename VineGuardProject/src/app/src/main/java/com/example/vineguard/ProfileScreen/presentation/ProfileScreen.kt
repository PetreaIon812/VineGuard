package com.example.vineguard.ProfileScreen.presentation

import com.example.vineguard.R

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit = {},
    onMenuItemClick: (String) -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profile",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .padding(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00C853))
                            .size(40.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.back),
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(25.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        ProfileContent(
            modifier = Modifier.padding(paddingValues),
            onMenuItemClick = onMenuItemClick
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTopBar(
    onNavigateBack: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = "Profile",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    painter = painterResource(R.drawable.right_arrow),
                    contentDescription = "Back",
                    modifier = Modifier.scale(-1f)
                )
            }
        },
        actions = {
            // Three dots menu
            IconButton(onClick = { /* Show menu */ }) {
                Icon(
                    painter =  painterResource(R.drawable.dots),
                    contentDescription = "More options"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White
        )
    )
}

@Composable
fun ProfileContent(
    modifier: Modifier = Modifier,
    onMenuItemClick: (String) -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // Profile Header Card
        item {
            ProfileHeaderCard()
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Account Section
        item {
            SectionHeader(title = "ACCOUNT")
        }
        item {
            MenuItem(
                icon = R.drawable.user,
                title = "Personal Information",
                iconColor = Color(0xFF4CAF50),
                backgroundColor = Color(0xFFE8F5E9),
                onClick = { onMenuItemClick("Personal Information") }
            )
        }
        item {
            MenuItem(
                icon = R.drawable.bell,
                title = "Notifications",
                iconColor = Color(0xFF2196F3),
                backgroundColor = Color(0xFFE3F2FD),
                onClick = { onMenuItemClick("Notifications") }
            )
        }
        item {
            MenuItem(
                icon = R.drawable.lock,
                title = "Privacy & Security",
                iconColor = Color(0xFF9C27B0),
                backgroundColor = Color(0xFFF3E5F5),
                onClick = { onMenuItemClick("Privacy & Security") }
            )
        }

        // Preferences Section
        item {
            SectionHeader(title = "PREFERENCES")
        }
        item {
            MenuItem(
                icon = R.drawable.plant,
                title = "My Vineyards",
                iconColor = Color(0xFFFF9800),
                backgroundColor = Color(0xFFFFF3E0),
                onClick = { onMenuItemClick("My Vineyards") }
            )
        }
        item {
            MenuItem(
                icon = R.drawable.warning,
                title = "Disease Alerts",
                iconColor = Color(0xFFF44336),
                backgroundColor = Color(0xFFFFEBEE),
                onClick = { onMenuItemClick("Disease Alerts") }
            )
        }
        item {
            MenuItem(
                icon = R.drawable.language,
                title = "Language",
                subtitle = "English",
                iconColor = Color(0xFF00BCD4),
                backgroundColor = Color(0xFFE0F7FA),
                onClick = { onMenuItemClick("Language") }
            )
        }

        // Support Section
        item {
            SectionHeader(title = "SUPPORT")
        }
        item {
            MenuItem(
                icon = R.drawable.help,
                title = "Help Center",
                iconColor = Color(0xFF2196F3),
                backgroundColor = Color(0xFFE3F2FD),
                onClick = { onMenuItemClick("Help Center") }
            )
        }
        item {
            MenuItem(
                icon = R.drawable.info,
                title = "About",
                iconColor = Color(0xFFE91E63),
                backgroundColor = Color(0xFFFCE4EC),
                onClick = { onMenuItemClick("About") }
            )
        }

        // Logout Button
        item {
            Spacer(modifier = Modifier.height(16.dp))
            LogoutButton(onClick = { onMenuItemClick("Logout") })
            Spacer(modifier = Modifier.height(70.dp))
        }
    }
}

@Composable
fun ProfileHeaderCard(
    name: String = "John Vineyard",
    role: String = "Vineyard Manager",
    location: String = "Napa Valley, CA",
    vineyardsCount: String = "12",
    alertsCount: String = "48",
    healthPercentage: String = "95%",
    isOnline: Boolean = true
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFE8F5F1),
                            Color(0xFFD4EDE4)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Profile Image with Online Indicator
                ProfileImage(isOnline = isOnline)

                Spacer(modifier = Modifier.height(12.dp))

                // Name
                Text(
                    text = name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Role
                Text(
                    text = role,
                    fontSize = 14.sp,
                    color = Color(0xFF666666)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Location
                LocationInfo(location = location)

                Spacer(modifier = Modifier.height(24.dp))

                // Stats
                StatsRow(
                    vineyardsCount = vineyardsCount,
                    alertsCount = alertsCount,
                    healthPercentage = healthPercentage
                )
            }
        }
    }
}

@Composable
fun ProfileImage(
    isOnline: Boolean = true,
    imageRes: Int = R.drawable.user
) {
    Box(
        modifier = Modifier.size(80.dp)
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = "Profile Picture",
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        // Online Indicator
        if (isOnline) {
            OnlineIndicator(
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        }
    }
}

@Composable
fun OnlineIndicator(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(20.dp)
            .background(Color.White, CircleShape)
            .padding(3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF00C853), CircleShape)
        )
    }
}

@Composable
fun LocationInfo(
    location: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.location),
            contentDescription = "Location",
            tint = Color(0xFF666666),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = location,
            fontSize = 14.sp,
            color = Color(0xFF666666)
        )
    }
}

@Composable
fun StatsRow(
    vineyardsCount: String,
    alertsCount: String,
    healthPercentage: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItem(count = vineyardsCount, label = "Vineyards")
        StatItem(count = alertsCount, label = "Alerts")
        StatItem(count = healthPercentage, label = "Health")
    }
}

@Composable
fun StatItem(
    count: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00A86B)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF666666)
        )
    }
}

@Composable
fun SectionHeader(
    title: String
) {
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF999999),
        letterSpacing = 0.8.sp,
        modifier = Modifier.padding(top = 20.dp, bottom = 12.dp, start = 4.dp)
    )
}

@Composable
fun MenuItem(
    icon: Int,
    title: String,
    subtitle: String? = null,
    iconColor: Color,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp)),
        color = Color.White,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon with circular background
            MenuIconBackground(
                icon = icon,
                iconColor = iconColor,
                backgroundColor = backgroundColor,
                contentDescription = title
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Text Content
            MenuTextContent(
                title = title,
                subtitle = subtitle,
                modifier = Modifier.weight(1f)
            )

            // Arrow
            MenuArrow()
        }
    }
}

@Composable
fun MenuIconBackground(
    icon: Int,
    iconColor: Color,
    backgroundColor: Color,
    contentDescription: String
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(backgroundColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = contentDescription,
            tint = iconColor,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
fun MenuTextContent(
    title: String,
    subtitle: String?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            fontSize = 15.sp,
            color = Color(0xFF1A1A1A),
            fontWeight = FontWeight.Normal
        )
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = Color(0xFF999999)
            )
        }
    }
}

@Composable
fun MenuArrow() {
    Icon(
        painter = painterResource(R.drawable.right_arrow),
        contentDescription = "Navigate",
        tint = Color(0xFFCCCCCC),
        modifier = Modifier.size(24.dp)
    )
}

@Composable
fun LogoutButton(
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFFFCDD2),
                    Color(0xFFFFCDD2)
                )
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.exit),
                contentDescription = "Logout",
                tint = Color(0xFFFF5252),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Log Out",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFFF5252)
            )
        }
    }
}

// Alternative Logout Button Styles
@Composable
fun LogoutButtonFilled(
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFFFEBEE),
            contentColor = Color(0xFFFF5252)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 2.dp
        )
    ) {
        Icon(
            painter = painterResource(R.drawable.exit),
            contentDescription = "Logout",
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Log Out",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun LogoutButtonMinimal(
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            painter = painterResource(R.drawable.user),
            contentDescription = "Logout",
            tint = Color(0xFFFF5252),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "Log Out",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFFFF5252)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    MaterialTheme {
        ProfileScreen()
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun MenuItemPreview() {
    MaterialTheme {
        Column(modifier = Modifier.background(Color(0xFFF5F5F5))) {
            MenuItem(
                icon = R.drawable.user,
                title = "Personal Information",
                iconColor = Color(0xFF4CAF50),
                backgroundColor = Color(0xFFE8F5E9),
                onClick = {}
            )
            MenuItem(
                icon = R.drawable.language,
                title = "Language",
                subtitle = "English",
                iconColor = Color(0xFF00BCD4),
                backgroundColor = Color(0xFFE0F7FA),
                onClick = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LogoutButtonsPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Outlined Style:", fontWeight = FontWeight.Bold)
            LogoutButton(onClick = {})

            Text("Filled Style:", fontWeight = FontWeight.Bold)
            LogoutButtonFilled(onClick = {})

            Text("Minimal Style:", fontWeight = FontWeight.Bold)
            LogoutButtonMinimal(onClick = {})
        }
    }
}

