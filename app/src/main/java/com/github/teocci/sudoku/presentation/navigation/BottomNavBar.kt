package com.github.teocci.sudoku.presentation.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.github.teocci.sudoku.ui.theme.SudokuPuzzleMasterTheme
import com.github.teocci.sudoku.ui.theme.SudokuTheme

/**
 * Bottom navigation bar for the app.
 *
 * @param navController The navigation controller
 * @param modifier Modifier for the nav bar
 */
@Composable
fun BottomNavBar(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val colors = SudokuTheme.colors
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        containerColor = colors.navBarBackground,
        contentColor = colors.navBarSelected
    ) {
        BottomNavItem.items().forEach { item ->
            val isSelected = currentRoute == item.screen.route

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (currentRoute != item.screen.route) {
                        navController.navigate(item.screen.route) {
                            // Pop up to the start destination to avoid building up a large stack
                            popUpTo(Screen.Main.route) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colors.navBarSelected,
                    selectedTextColor = colors.navBarSelected,
                    unselectedIconColor = colors.navBarUnselected,
                    unselectedTextColor = colors.navBarUnselected,
                    indicatorColor = colors.navBarSelected.copy(alpha = 0.1f)
                )
            )
        }
    }
}

/**
 * Custom styled bottom navigation bar with animated selection.
 *
 * @param currentRoute The current navigation route
 * @param onItemClick Called when a nav item is clicked
 * @param modifier Modifier for the nav bar
 */
@Composable
fun CustomBottomNavBar(
    currentRoute: String?,
    onItemClick: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SudokuTheme.colors

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.navBarBackground)
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem.items().forEach { item ->
                val isSelected = currentRoute == item.screen.route

                CustomNavItem(
                    item = item,
                    isSelected = isSelected,
                    onClick = { onItemClick(item) }
                )
            }
        }
    }
}

/**
 * Custom navigation item with animation.
 */
@Composable
private fun CustomNavItem(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SudokuTheme.colors
    val interactionSource = remember { MutableInteractionSource() }

    // Animate color
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) colors.navBarSelected else colors.navBarUnselected,
        animationSpec = tween(durationMillis = 200),
        label = "navIconColor"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) colors.navBarSelected else colors.navBarUnselected,
        animationSpec = tween(durationMillis = 200),
        label = "navTextColor"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) colors.navBarSelected.copy(alpha = 0.15f) else Color.Transparent,
        animationSpec = tween(durationMillis = 200),
        label = "navBgColor"
    )

    // Animate scale
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = tween(durationMillis = 200),
        label = "navScale"
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
            contentDescription = item.label,
            tint = iconColor,
            modifier = Modifier
                .size(24.dp)
                .scale(scale)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = item.label,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

/**
 * Minimal bottom navigation bar (icons only).
 *
 * @param currentRoute The current navigation route
 * @param onItemClick Called when a nav item is clicked
 * @param modifier Modifier for the nav bar
 */
@Composable
fun MinimalBottomNavBar(
    currentRoute: String?,
    onItemClick: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SudokuTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.navBarBackground)
            .navigationBarsPadding()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomNavItem.items().forEach { item ->
            val isSelected = currentRoute == item.screen.route
            val interactionSource = remember { MutableInteractionSource() }

            val iconColor by animateColorAsState(
                targetValue = if (isSelected) colors.navBarSelected else colors.navBarUnselected,
                animationSpec = tween(durationMillis = 200),
                label = "minNavIconColor"
            )

            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.2f else 1f,
                animationSpec = tween(durationMillis = 200),
                label = "minNavScale"
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = { onItemClick(item) }
                    )
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                    contentDescription = item.label,
                    tint = iconColor,
                    modifier = Modifier
                        .size(28.dp)
                        .scale(scale)
                )
            }
        }
    }
}

// ==================== PREVIEWS ====================

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
private fun PreviewBottomNavBar() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        val navController = rememberNavController()
        BottomNavBar(navController = navController)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
private fun PreviewCustomBottomNavBar_Home() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        CustomBottomNavBar(
            currentRoute = Screen.Main.route,
            onItemClick = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
private fun PreviewCustomBottomNavBar_Daily() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        CustomBottomNavBar(
            currentRoute = Screen.DailyChallenges.route,
            onItemClick = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
private fun PreviewCustomBottomNavBar_Profile() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        CustomBottomNavBar(
            currentRoute = Screen.Profile.route,
            onItemClick = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
private fun PreviewMinimalBottomNavBar() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        MinimalBottomNavBar(
            currentRoute = Screen.Main.route,
            onItemClick = {}
        )
    }
}
