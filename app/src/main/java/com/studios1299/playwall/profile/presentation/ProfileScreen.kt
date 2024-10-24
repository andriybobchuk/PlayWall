package com.studios1299.playwall.profile.presentation

import android.app.WallpaperManager
import android.net.Uri
import android.os.Build
import android.util.Log
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Facebook
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Password
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.studios1299.playwall.R
import com.studios1299.playwall.auth.data.EmailPatternValidator
import com.studios1299.playwall.auth.data.UserDataValidator
import com.studios1299.playwall.auth.domain.PatternValidator
import com.studios1299.playwall.core.data.ChangeWallpaperWorker
import com.studios1299.playwall.core.data.local.Preferences
import com.studios1299.playwall.core.domain.model.WallpaperOption
import com.studios1299.playwall.core.presentation.ObserveAsEvents
import com.studios1299.playwall.core.presentation.components.Banners
import com.studios1299.playwall.core.presentation.components.Buttons
import com.studios1299.playwall.core.presentation.components.Images
import com.studios1299.playwall.core.presentation.components.ShimmerLoadingForUserProfile
import com.studios1299.playwall.core.presentation.components.ShimmerLoadingForWallpaperGrid
import com.studios1299.playwall.core.presentation.components.ShimmerWallpaperGridItem
import com.studios1299.playwall.core.presentation.components.TextFields
import com.studios1299.playwall.core.presentation.components.Toolbars
import com.studios1299.playwall.explore.presentation.explore.ExploreWallpaper
import com.studios1299.playwall.feature.play.presentation.chat.util.rememberRequestPermissionAndPickImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@Composable
fun ProfileScreenRoot(
    viewModel: ProfileViewModel,
    onNavigateTo: (ProfileDestination) -> Unit,
    onNavigateToPhotoDetail: (Int) -> Unit,
    onLogOut: () -> Unit,
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
                //Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
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
                ProfileAction.OnLogOut -> {
                    onLogOut()
                    viewModel.onAction(action)
                }
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
                    is ProfileAction.OnSaveProfileClick -> {
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
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            Toolbars.Primary(
                title = stringResource(R.string.profile),
                actions = listOf(
                    Toolbars.ToolBarAction(
                        icon = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.edit),
                        enabled = state.isOnline,
                        onClick = { onAction(ProfileAction.OnEditProfileClick) }
                    ),
                    Toolbars.ToolBarAction(
                        icon = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = stringResource(R.string.log_out),
                        onClick = { onAction(ProfileAction.OnLogOut) }
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
                if (!state.isOnline) {
                    Banners.OfflineStatus()
                }
                Spacer(modifier = Modifier.height(10.dp))
                Box(Modifier.fillMaxSize()) {
                    if (state.isLoading) {
                        ShimmerLoadingForUserProfile()
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .align(Alignment.Center)
                        ) {
                            Images.Circle(
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                model = state.userAvatar,
                                size = 70.dp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            if (state.userName.text.isNotEmpty()) {
                                Text(
                                    modifier = Modifier.align(Alignment.CenterHorizontally),
                                    text = state.userName.text.toString(),
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                            Text(
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                text = state.email.text.toString(),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
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
                                enabled = state.isOnline,
                                selectedOption = state.selectedWallpaperOption,
                                options = WallpaperOption.getDisplayNames(), // Pass the enum options directly
                                onOptionSelected = { option ->
                                     onAction(ProfileAction.ChangeWallpaperScreen(WallpaperOption.getEnumByDisplayName(option) ?: WallpaperOption.HomeScreen))
                                }
                            )
                        },
                        {
                            SwitchMenuItem(
                                icon = Icons.Default.Save,
                                enabled = state.isOnline,
                                label = "Save wallpapers set by friends",
                                isChecked = state.isSaveWallpapersEnabled,
                                onCheckedChange = { onAction(ProfileAction.ToggleSaveWallpapers) }
                            )
                        },
                        {
                            SettingMenuItem(
                                icon = Icons.Default.Restore,
                                enabled = state.isOnline,
                                label = "Rollback to previous wallpaper",
                                onClick = {
                                    val workData = workDataOf("file_name" to Preferences.getPreviousWallpaperId())
                                    val changeWallpaperWork = OneTimeWorkRequestBuilder<ChangeWallpaperWorker>()
                                        .setInputData(workData)
                                        .build()
                                    WorkManager.getInstance(context).enqueue(changeWallpaperWork)
                                    onAction(ProfileAction.RollbackPreviousWallpaper)
                                }
                            )
                        },
                        {
                            SettingMenuItem(
                                icon = Icons.Default.Restore,
                                enabled = state.isOnline,
                                label = "Rollback to default wallpaper",
                                onClick = {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                            WallpaperManager.getInstance(context).clearWallpaper()
                                        }
                                    }
                                    onAction(ProfileAction.RollbackDefaultWallpaper)
                                }
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
            items(state.wallpapers.chunked(3)) { photoRow ->
                PhotoGridRow(photoRow, state, onAction)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EditProfileDialog(
    state: ProfileState,
    onAction: (ProfileAction) -> Unit,
    onDismiss: () -> Unit
) {
    val name by remember { mutableStateOf(state.userName) }
    val oldPassword by remember { mutableStateOf(state.oldPassword) }
    val newPassword by remember { mutableStateOf(state.newPassword) }
    val context = LocalContext.current

    val requestImagePicker = rememberRequestPermissionAndPickImage { uri ->
        onAction(ProfileAction.OnSaveProfileClick(context, null, uri))
    }
    AlertDialog(
        onDismissRequest = { onDismiss() },
        confirmButton = {
            TextButton(onClick = {
                if (oldPassword.text.toString().isNotEmpty() && newPassword.text.toString().isNotEmpty()) {
                    if (!UserDataValidator(EmailPatternValidator).validatePassword(newPassword.text.toString()).isValidPassword) {
                        Toast.makeText(context, "New password is too weak!", Toast.LENGTH_SHORT).show()
                        return@TextButton
                    }
                }
                onAction(ProfileAction.OnSaveProfileClick(
                    context,
                    name.text.toString(),
                    null,
                    oldPassword.text.toString(),
                    newPassword.text.toString(),
                ))
                 }) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Cancel")
            }
        },
        title = {
            Text(text = stringResource(R.string.edit_profile))
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 18.dp),
                ) {
                    Images.Circle(
                        model = state.userAvatar,
                        size = 100.dp
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Buttons.Outlined(
                            text = stringResource(R.string.change_photo),
                            isLoading = false,
                            onClick = requestImagePicker,
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Buttons.Primary(
                            text = stringResource(R.string.delete_photo),
                            isLoading = false,
                            onClick = { onAction(ProfileAction.OnSaveProfileClick(context, null, Uri.EMPTY)) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = Color.Gray,
                        disabledContentColor = Color.Black
                        ),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                TextFields.Primary(
                    state = remember { name },
                    startIcon = Icons.Default.Person,
                    endIcon = null,
                    hint = stringResource(R.string.enter_your_name),
                    title = stringResource(R.string.name),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(16.dp))
                TextFields.Password(
                    state = remember { oldPassword },
                    hint = "Enter current password",
                    title = "Change password",
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(16.dp))
                TextFields.Primary(
                    state = remember { newPassword },
                    startIcon = Icons.Default.Password,
                    endIcon = null,
                    hint = "Enter new password",
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    )
}


@Composable
fun PhotoGridRow(
    exploreWallpapers: List<ExploreWallpaper>,
    state: ProfileState,
    onAction: (ProfileAction) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        if (state.isLoading) {
            //ShimmerLoadingForWallpaperGrid()
Log.e("PPPP", "PPP")
            repeat(3) {
                ShimmerWallpaperGridItem()
            }
//            repeat(3 - 18) {
//                Spacer(modifier = Modifier.weight(1f))
//            }
        } else if (state.wallpapers.isEmpty()) {
        Text(
            text = "No photos available",
        )
        } else {
            exploreWallpapers.forEach { photo ->
                Images.Square(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f),
                    model = photo.fileName,
                    onClick = {
                        if (state.wallpapers.isNotEmpty()) {
                            onAction(ProfileAction.OnPhotoClick(photo.id))
                        }
                    }
                )
            }
            repeat(3 - exploreWallpapers.size) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }


    }
}

@Composable
fun SettingMenuItem(
    icon: ImageVector,
    label: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = {
                if (enabled) {
                    onClick()
                }
            })
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
                color = if (enabled) { MaterialTheme.colorScheme.onSurface} else {MaterialTheme.colorScheme.onSurface.copy(0.5f)}
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorMenuItem(
    icon: ImageVector,
    label: String,
    enabled: Boolean = true,
    selectedOption: WallpaperOption,
    options: List<String>,
    onOptionSelected: (String) -> Unit
) {
    var isSheetOpen by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (enabled) {
                    isSheetOpen = true
                }
            }
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
                    color = if (enabled) { MaterialTheme.colorScheme.onSurface} else {MaterialTheme.colorScheme.onSurface.copy(0.5f)}
                )
            }
            Text(
                text = selectedOption.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }

    if (isSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { isSheetOpen = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Select an option",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))

                options.forEach { option ->
                    Button(
                        onClick = {
                            onOptionSelected(option)
                            isSheetOpen = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { isSheetOpen = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
fun SwitchMenuItem(
    icon: ImageVector,
    label: String,
    enabled: Boolean = true,
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
                enabled = enabled,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpLegalBottomSheet(onDismiss: () -> Unit, onNavigateTo: (ProfileDestination) -> Unit) {
    ModalBottomSheet(
        containerColor = MaterialTheme.colorScheme.surface,
        onDismissRequest = onDismiss
    ) {
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
                label = stringResource(R.string.our_social_media),
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
