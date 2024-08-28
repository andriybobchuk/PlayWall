package com.studios1299.playwall.profile.presentation

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Facebook
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Support
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.filled.Whatsapp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.studios1299.playwall.core.presentation.ObserveAsEvents
import com.studios1299.playwall.core.presentation.components.Toolbars


@Composable
fun ProfileScreenRoot(
    viewModel: ProfileViewModel,
    onNavigateTo: (ProfileDestination) -> Unit,
    bottomNavbar: @Composable () -> Unit
) {
    val context = LocalContext.current
    val state = viewModel.state
    var showHelpLegalSheet by remember { mutableStateOf(false) }
    var showSocialMediaSheet by remember { mutableStateOf(false) }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is ProfileEvent.ShowError -> {
                Toast.makeText(context, event.error.asString(context), Toast.LENGTH_LONG).show()
            }
            is ProfileEvent.NavigateTo -> {
                //onNavigateTo(event.destination)
                Toast.makeText(context, event.destination.toString(), Toast.LENGTH_LONG).show()
            }
            ProfileEvent.WallpaperRolledBack -> {
                Toast.makeText(context, "Rollback successful", Toast.LENGTH_SHORT).show()
            }
            ProfileEvent.WallpapersSavedSettingToggled -> {
                Toast.makeText(context, "Setting updated", Toast.LENGTH_SHORT).show()
            }
            ProfileEvent.SupportContacted -> {
                Toast.makeText(context, "Support contacted", Toast.LENGTH_SHORT).show()
            }
        }
    }

    ProfileScreen(
        state = state,
        onAction = { action ->
            when (action) {
                ProfileAction.OnHelpClick -> showHelpLegalSheet = true
                ProfileAction.OnSocialClick -> showSocialMediaSheet = true
                else -> viewModel.onAction(action)
            }
        },
        bottomNavbar = bottomNavbar
    )

    // Help & Legal Bottom Sheet
    if (showHelpLegalSheet) {
        HelpLegalBottomSheet(onDismiss = { showHelpLegalSheet = false }, onNavigateTo)
    }

    // Social Media Bottom Sheet
    if (showSocialMediaSheet) {
        SocialMediaBottomSheet(onDismiss = { showSocialMediaSheet = false }, onNavigateTo)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    state: ProfileState,
    onAction: (ProfileAction) -> Unit,
    bottomNavbar: @Composable () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            Toolbars.Primary(
                title = "Profile",
                actions = listOf(
                    Toolbars.ToolBarAction(
                        icon = Icons.Default.EditNote,
                        contentDescription = "Edit",
                        onClick = {  }
                    )
                ),
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = { bottomNavbar() }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
        ) {
            item {
                ProfileHeader(
                    name = state.userName,
                    email = state.userEmail,
                    avatar = state.userAvatar
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            item {
                Group(
                    items = listOf(
                        {
                            SettingMenuItem(
                                icon = Icons.Default.Support,
                                label = "Help & Legal info",
                                onClick = { onAction(ProfileAction.OnHelpClick) }
                            )
                        },
                        {
                            SettingMenuItem(
                                icon = Icons.Default.Whatsapp,
                                label = "Our social media",
                                onClick = { onAction(ProfileAction.OnSocialClick) }
                            )
                        }
                    )
                )
            }
            item {
                Group(
                    items = listOf(
                        {
                            SelectorMenuItem(
                                icon = Icons.Default.Wallpaper,
                                label = "Change Wallpaper on",
                                selectedOption = state.selectedWallpaperOption,
                                options = WallpaperOption.entries, // Pass the enum options directly
                                onOptionSelected = { option ->
                                   // onAction(ProfileAction.ChangeWallpaperScreen() // Use the enum directly
                                }
                            )
                        },
                        {
                            SwitchMenuItem(
                                icon = Icons.Default.Save,
                                label = "Save wallpapers set by friends",
                                isChecked = state.isSaveWallpapersEnabled,
                                onCheckedChange = { onAction(ProfileAction.ToggleSaveWallpapers) }
                            )
                        },
                        {
                            SettingMenuItem(
                                icon = Icons.Default.Restore,
                                label = "Rollback to previous wallpaper",
                                onClick = { onAction(ProfileAction.RollbackPreviousWallpaper) }
                            )
                        },
                        {
                            SettingMenuItem(
                                icon = Icons.Default.Restore,
                                label = "Rollback to default wallpaper",
                                onClick = { onAction(ProfileAction.RollbackDefaultWallpaper) }
                            )
                        }
                    )
                )
            }
        }
    }
}

@Composable
fun SettingMenuItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(24.dp).padding(start = 4.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun SelectorMenuItem(
    icon: ImageVector,
    label: String,
    selectedOption: String,
    options: List<WallpaperOption>,
    onOptionSelected: (String) -> Unit
) {
    var isDialogOpen by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isDialogOpen = true }
            .background(MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.size(24.dp).padding(start = 4.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = selectedOption,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }

    if (isDialogOpen) {
        AlertDialog(
            onDismissRequest = { isDialogOpen = false },
            title = { Text(text = "Select an option") },
            text = {
                Column {
                    options.forEach { option ->
                        TextButton(onClick = {
                            onOptionSelected(option.toString())
                            isDialogOpen = false
                        }) {
                            Text(text = option.toString())
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { isDialogOpen = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun SwitchMenuItem(
    icon: ImageVector,
    label: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { onCheckedChange(!isChecked) })
            .background(MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.size(24.dp).padding(start = 4.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
            )
        }
    }
}





@Composable
fun Group(
    label: String = "",
    items: List<@Composable () -> Unit>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        if(label.isNotEmpty()) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 4.dp, start = 16.dp)
            )
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column {
                items.forEachIndexed { index, item ->
                    item()
                    if (index < items.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                        )
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ProfileHeader(
    name: String,
    email: String,
    avatar: String,
) {
    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().align(Alignment.Center)
        ) {
            GlideImage(
                model = avatar,
                contentDescription = name,
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.outline)
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = name,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = email,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpLegalBottomSheet(onDismiss: () -> Unit, onNavigateTo: (ProfileDestination) -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column {
            Group(
                label = "Help & Legal",
                items = listOf(
                    {
                        SettingMenuItem(
                            icon = Icons.Default.Support,
                            label = "Contact Support",
                            onClick = {
                                //onNavigateTo(ProfileDestination.ContactSupport)
                                onDismiss()
                            }
                        )
                    },
                    {
                        SettingMenuItem(
                            icon = Icons.Default.QuestionAnswer,
                            label = "FAQ",
                            onClick = {
                                onNavigateTo(ProfileDestination.Faq)
                                onDismiss()
                            }
                        )
                    },
                    {
                        SettingMenuItem(
                            icon = Icons.Default.Info,
                            label = "Terms of Service",
                            onClick = {
                                onNavigateTo(ProfileDestination.TermsOfService)
                                onDismiss()
                            }
                        )
                    },
                    {
                        SettingMenuItem(
                            icon = Icons.Default.PrivacyTip,
                            label = "Privacy Policy",
                            onClick = {
                                onNavigateTo(ProfileDestination.PrivacyPolicy)
                                onDismiss()
                            }
                        )
                    },
                    {
                        SettingMenuItem(
                            icon = Icons.Default.Policy,
                            label = "Content Policy",
                            onClick = {
                                onNavigateTo(ProfileDestination.ContentPolicy)
                                onDismiss()
                            }
                        )
                    }
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialMediaBottomSheet(onDismiss: () -> Unit, onNavigateTo: (ProfileDestination) -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column {
            Group(
                label = "Our Social Media",
                items = listOf(
                    {
                        SettingMenuItem(
                            icon = Icons.Default.Facebook,
                            label = "Instagram",
                            onClick = {
                                onNavigateTo(ProfileDestination.Instagram)
                                onDismiss()
                            }
                        )
                    },
                    {
                        SettingMenuItem(
                            icon = Icons.Default.Facebook,
                            label = "TikTok",
                            onClick = {
                                onNavigateTo(ProfileDestination.TikTok)
                                onDismiss()
                            }
                        )
                    }
                )
            )
        }
    }
}
