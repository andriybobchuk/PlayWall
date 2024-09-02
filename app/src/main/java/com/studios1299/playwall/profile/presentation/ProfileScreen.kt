package com.studios1299.playwall.profile.presentation

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text2.input.TextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Facebook
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Support
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.filled.Whatsapp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.karumi.dexter.Dexter
import com.karumi.dexter.listener.single.PermissionListener
import com.studios1299.playwall.core.data.UserProfile
import com.studios1299.playwall.core.presentation.ObserveAsEvents
import com.studios1299.playwall.core.presentation.components.Buttons
import com.studios1299.playwall.core.presentation.components.Images
import com.studios1299.playwall.core.presentation.components.TextFields
import com.studios1299.playwall.core.presentation.components.Toolbars
import com.studios1299.playwall.core.presentation.components.image_grid.ImageGrid
import com.studios1299.playwall.explore.presentation.explore.Photo
import com.studios1299.playwall.feature.play.presentation.chat.util.rememberRequestPermissionAndPickImage


@Composable
fun ProfileScreenRoot(
    viewModel: ProfileViewModel,
    onNavigateTo: (ProfileDestination) -> Unit,
    onNavigateToPhotoDetail: (String) -> Unit,
    bottomNavbar: @Composable () -> Unit
) {
    val context = LocalContext.current
    val state = viewModel.state
    var showHelpLegalSheet by remember { mutableStateOf(false) }
    var showSocialMediaSheet by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }

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
            is ProfileEvent.NavigateToPhotoDetail -> {
                onNavigateToPhotoDetail(event.initialPhotoIndex)
            }
            is ProfileEvent.ProfileUpdated -> {
                Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
                showEditProfileDialog = false
            }
            is ProfileEvent.ProfileEditCancelled -> {
                showEditProfileDialog = false
            }
        }
    }

    ProfileScreen(
        state = state,
        onAction = { action ->
            when (action) {
                ProfileAction.OnHelpClick -> showHelpLegalSheet = true
                ProfileAction.OnSocialClick -> showSocialMediaSheet = true
                ProfileAction.OnEditProfileClick -> showEditProfileDialog = true
                else -> viewModel.onAction(action)
            }
        },
        bottomNavbar = bottomNavbar
    )

    if (showHelpLegalSheet) {
        HelpLegalBottomSheet(onDismiss = { showHelpLegalSheet = false }, onNavigateTo)
    }

    if (showSocialMediaSheet) {
        SocialMediaBottomSheet(onDismiss = { showSocialMediaSheet = false }, onNavigateTo)
    }

    if (showEditProfileDialog) {
        EditProfileDialog(
            state = state,
            onAction = { action ->
                when (action) {
                    ProfileAction.OnSaveProfileClick -> {
                        viewModel.onAction(action)
                        showEditProfileDialog = false
                    }
                    ProfileAction.OnCancelEditProfileClick -> {
                        viewModel.onAction(action)
                        showEditProfileDialog = false
                    }
                    else -> viewModel.onAction(action)
                }
            },
            onDismiss = { showEditProfileDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class,
    ExperimentalFoundationApi::class
)
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
                        icon = Icons.Default.Edit,
                        contentDescription = "Edit",
                        onClick = { onAction(ProfileAction.OnEditProfileClick) }
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
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                ProfileHeader(
                    name = state.userName.text.toString(),
                    email = state.userEmail.text.toString(),
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
            item {
                Text(
                    text = "Saved Photos",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else if (state.photos.isEmpty()) {
                        Text(
                            text = "No photos available",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
            items(state.photos.chunked(3)) { photoRow ->
                PhotoGridRow(photoRow, state, onAction)
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class,
    ExperimentalFoundationApi::class
)
@Composable
fun EditProfileDialog(
    state: ProfileState,
    onAction: (ProfileAction) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(state.userName) }
    var email by remember { mutableStateOf(state.userEmail) }

    val requestImagePicker = rememberRequestPermissionAndPickImage { uri ->
        onAction(ProfileAction.OnPhotoSelected(uri))
    }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        confirmButton = {
            TextButton(onClick = { onAction(ProfileAction.OnSaveProfileClick) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Cancel")
            }
        },
        title = {
            Text(text = "Edit Profile")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp),
                ) {
                    Images.Circle(
                        model = state.userAvatar,
                        size = 100.dp
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = requestImagePicker,
                            modifier = Modifier
                                // .align(Alignment.BottomEnd)
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Change Photo",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        IconButton(
                            onClick = { onAction(ProfileAction.OnDeletePhotoClick) },
                            modifier = Modifier
                                //  .align(Alignment.BottomEnd)
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.error)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Photo",
                                tint = MaterialTheme.colorScheme.errorContainer
                            )
                        }
//                        Buttons.Primary(
//                            text = "Change Photo",
//                            isLoading = false,
//                            onClick = requestImagePicker,
//                            modifier = Modifier.fillMaxWidth(),
//                            style = MaterialTheme.typography.bodySmall
//                        )
//                        Buttons.Primary(
//                            text = "Delete Photo",
//                            isLoading = false,
//                            onClick = { onAction(ProfileAction.OnDeletePhotoClick) },
//                            style = MaterialTheme.typography.bodySmall
//                        )

                    }

                }

                TextFields.Primary(
                    state = remember { name },
                    startIcon = Icons.Default.Person,
                    endIcon = null,
                    hint = "Enter your name",
                    title = "Name",
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(16.dp))

                TextFields.Primary(
                    state = remember { email },
                    startIcon = Icons.Default.Email,
                    endIcon = null,
                    hint = "Enter your email",
                    title = "Email",
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(16.dp))


            }
        }
    )
}




@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun PhotoGridRow(
    photos: List<Photo>,
    state: ProfileState,
    onAction: (ProfileAction) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        photos.forEach { photo ->
            GlideImage(
                model = photo.url,
                contentDescription = photo.description,
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .clickable {
                        if (state.photos.isNotEmpty()) {
                            onAction(ProfileAction.OnPhotoClick(photo.id))
                        }
                    }
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface),
                contentScale = ContentScale.Crop
            )
        }

        // Fill any remaining spaces if less than 3 photos are provided
        repeat(3 - photos.size) {
            Spacer(modifier = Modifier.weight(1f))
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
    ) {
        Row(
            modifier = Modifier
                .padding(9.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier
                    .size(24.dp)
                    .padding(start = 4.dp)
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
                    modifier = Modifier
                        .size(24.dp)
                        .padding(start = 4.dp)
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
                    modifier = Modifier
                        .size(24.dp)
                        .padding(start = 4.dp)
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
           // color = MaterialTheme.colorScheme.surface,
        ) {
            Column {
                items.forEachIndexed { index, item ->
                    item()
                    if (index < items.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun ProfileHeader(
    name: String,
    email: String,
    avatar: String,
) {
    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center)
        ) {
            Images.Circle(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                model = avatar,
                size = 70.dp
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
