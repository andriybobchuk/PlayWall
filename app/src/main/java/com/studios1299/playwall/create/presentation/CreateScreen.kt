package com.studios1299.playwall.create.presentation

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.studios1299.playwall.R
import com.studios1299.playwall.core.data.ChangeWallpaperWorker
import com.studios1299.playwall.core.data.s3.S3Handler
import com.studios1299.playwall.core.data.s3.uriToFile
import com.studios1299.playwall.core.presentation.ObserveAsEvents
import com.studios1299.playwall.core.presentation.components.Images
import com.studios1299.playwall.feature.play.presentation.chat.util.rememberRequestPermissionAndPickImage
import com.studios1299.playwall.feature.play.presentation.play.Friend
import ja.burhanrashid52.photoeditor.PhotoEditor
import ja.burhanrashid52.photoeditor.PhotoEditorView
import ja.burhanrashid52.photoeditor.TextStyleBuilder
import ja.burhanrashid52.photoeditor.shape.ShapeBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun CreateScreenRoot(
    viewModel: CreateViewModel,
    onNavigateToDiamonds: () -> Unit,
    bottomNavbar: @Composable () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is CreateScreenEvent.ImageSaved -> {
                Toast.makeText(context, "Image saved successfully", Toast.LENGTH_LONG).show()
            }
            is CreateScreenEvent.ShowError -> {
                Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    CreateScreen(
        state = state,
        onNavigateToDiamonds = onNavigateToDiamonds,
        onAction = { action -> viewModel.onAction(action) },
        bottomNavbar = bottomNavbar
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateScreen(
    state: CreateScreenState,
    onNavigateToDiamonds: () -> Unit,
    onAction: (CreateScreenAction) -> Unit,
    bottomNavbar: @Composable () -> Unit
) {
    val context = LocalContext.current
    var selectedImageUri by remember { mutableStateOf<Uri>(Uri.EMPTY) }
    var pendingImageUri by remember { mutableStateOf<Uri?>(null) }
    var photoEditorView: PhotoEditorView? by remember { mutableStateOf(null) }
    var photoEditor: PhotoEditor? by remember { mutableStateOf(null) }
    var showAddTextSheet by remember { mutableStateOf(false) }
    var previousTextColor: Color? = null
    var showStickerSheet by remember { mutableStateOf(false) }
    var showDrawModeSheet by remember { mutableStateOf(false) }
    var selectedColor by remember { mutableStateOf<Color?>(null) }
    var brushSize by remember { mutableStateOf<Float?>(null) }
    val isImageSelected = selectedImageUri != Uri.EMPTY
    var showReplacePhotoDialog by remember { mutableStateOf(false) }

    val requestImagePicker = rememberRequestPermissionAndPickImage { uri ->
        if (isGif(uri, context)) {
            Toast.makeText(context, "GIFs are not supported.", Toast.LENGTH_SHORT).show()
        } else {
            if (isImageSelected) {
                pendingImageUri = uri
                showReplacePhotoDialog = true
            } else {
                resetPhotoEditor(photoEditor)
                selectedImageUri = uri
            }
        }
    }

    val requestSave = {
        CoroutineScope(Dispatchers.Main).launch {
            if (photoEditor != null && photoEditorView != null) {
                saveImageToGallery(
                    context = context,
                    photoEditor = photoEditor,
                    onSuccess = { savedUri ->
                        selectedImageUri = savedUri
                    }
                )
            } else {
                Toast.makeText(context, "Photo editor is not initialized", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val coroutineScope = rememberCoroutineScope()
    val isFriendsSheetOpen = remember { mutableStateOf(false) }
    val friendsSheetState = rememberModalBottomSheetState()
    FriendsSelectionBottomSheet(
        isSheetOpen = isFriendsSheetOpen,
        sheetState = friendsSheetState,
        friends = state.friends,
        onFriendsSelected = { selectedFriends ->

            CoroutineScope(Dispatchers.Main).launch {
                if (photoEditor != null && photoEditorView != null) {
                    saveImageToGallery(
                        context = context,
                        photoEditor = photoEditor,
                        onSuccess = { savedUri ->
                            CoroutineScope(Dispatchers.IO).launch {
                                setAsWallpaper(S3Handler.uploadToS3(uriToFile(context, savedUri)!!, S3Handler.Folder.WALLPAPERS)?:"", context)
                                selectedImageUri = savedUri
                                onAction(CreateScreenAction.SendToFriends(selectedFriends, selectedImageUri, context))
                            }
                        }
                    )
                } else {
                    Toast.makeText(context, "Photo editor is not initialized", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )

    LaunchedEffect(selectedImageUri) {
        if (selectedImageUri != Uri.EMPTY) {
            try {
                photoEditorView?.source?.setImageURI(selectedImageUri)
            } catch (e: Exception) {
                Toast.makeText(context, "Error loading image: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("CreateScreen", "Error loading image", e)
            }
        }
    }

    Scaffold(
        topBar = {
            Topbar(
                download = { requestSave() },
                send = {
                    coroutineScope.launch { isFriendsSheetOpen.value = true }
                },
                sendEnabled = state.isOnline,
                goToDiamonds = onNavigateToDiamonds,
                setAsMyWallpaper = {
                    CoroutineScope(Dispatchers.Main).launch {
                        if (photoEditor != null && photoEditorView != null) {
                            saveImageToGallery(
                                context = context,
                                photoEditor = photoEditor,
                                onSuccess = { savedUri ->
                                    CoroutineScope(Dispatchers.IO).launch {
                                        setAsWallpaper(S3Handler.uploadToS3(uriToFile(context, savedUri)!!, S3Handler.Folder.WALLPAPERS)?:"", context)
                                        selectedImageUri = savedUri
                                    }
                                }
                            )
                        } else {
                            Toast.makeText(context, "Photo editor is not initialized", Toast.LENGTH_SHORT).show()
                        }

                    } },
                isImageSelected = isImageSelected
            )
        },
        bottomBar = {
            Column {
                DrawingToolbar(
                    onChooseImage = requestImagePicker,
                    onAddText = { showAddTextSheet = true },
                    onAddSticker = { showStickerSheet = true },
                    onDraw = { showDrawModeSheet = true },
                    onUndo = { photoEditor?.undo() },
                    onRedo = { photoEditor?.redo() },
                    enabled = isImageSelected
                )
                bottomNavbar()
            }
        }
    ) { paddingValues ->
        if (!isImageSelected) {
            NoImagePlaceholder(paddingValues, requestImagePicker)
        } else {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                AndroidView(
                    factory = { context ->
                        PhotoEditorView(context).apply {
                            photoEditorView = this
                            selectedImageUri.let { uri ->
                                source.setImageURI(uri)
                            }
                            photoEditor = PhotoEditor.Builder(context, this)
                                .setPinchTextScalable(true)
                                .build()
                            photoEditor!!.setBrushDrawingMode(true)
                            source.scaleType = ImageView.ScaleType.CENTER_CROP
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        val addTextBottomSheetState = rememberModalBottomSheetState()
        if (showAddTextSheet) {
            AddTextBottomSheet(
                sheetState = addTextBottomSheetState,
                onDismiss = { showAddTextSheet = false },
                onTextAdded = { inputText, textColor ->
                    photoEditor?.addText(inputText, TextStyleBuilder().apply {
                        withTextColor(textColor)
                        withTextSize(24f)
                    })
                    previousTextColor = Color(textColor)
                },
                initialColor = previousTextColor
            )
        }

        if (showStickerSheet) {
            StickerBottomSheet(
                onDismiss = { showStickerSheet = false },
                onStickerSelected = { stickerResourceId ->
                    val bitmap = BitmapFactory.decodeResource(context.resources, stickerResourceId)
                    photoEditor?.addImage(bitmap)
                }
            )
        }

        if (showDrawModeSheet) {
            DrawModeBottomSheet(
                initialColor = selectedColor,
                initialBrushSize = brushSize,
                onDismiss = { showDrawModeSheet = false },
                onDrawSettingsSelected = { color, size ->
                    selectedColor = color
                    brushSize = size
                    showDrawModeSheet = false
                    photoEditor?.setShape(ShapeBuilder()
                        .withShapeSize(size)
                        .withShapeColor(android.graphics.Color.parseColor("#" + Integer.toHexString(color.hashCode())))
                    )
                    photoEditor!!.setBrushDrawingMode(true)
                }
            )
        }

        if (showReplacePhotoDialog) {
            AlertDialog(
                onDismissRequest = { showReplacePhotoDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        resetPhotoEditor(photoEditor)
                        selectedImageUri = pendingImageUri ?: Uri.EMPTY
                        pendingImageUri = null
                        showReplacePhotoDialog = false
                    }) {
                        Text("Replace")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        pendingImageUri = null
                        showReplacePhotoDialog = false
                    }) {
                        Text("Cancel")
                    }
                },
                title = { Text("Replace Image?") },
                text = { Text("You will lose your progress if you haven't saved. Do you want to replace the image?") }
            )
        }
    }
}

// TODO: PostDetailViewModel -> boilerplate function to be removed
fun setAsWallpaper(s3Link: String, context: Context) {
    val pathToS3 = S3Handler.downloadableLinkToPath(s3Link)
    val workData = workDataOf(
        "file_name" to pathToS3,
        "from_device" to false
    )
    Log.e("setAsWallpaper", "filename:  " + s3Link)

    val changeWallpaperWork = OneTimeWorkRequestBuilder<ChangeWallpaperWorker>()
        .setInputData(workData)
        .build()

    WorkManager.getInstance(context).enqueue(changeWallpaperWork)
}

// TODO: boilerplate from PostDetailScreen to be removed
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsSelectionBottomSheet(
    isSheetOpen: MutableState<Boolean>,
    sheetState: SheetState,
    friends: List<Friend>,
    onFriendsSelected: (List<Friend>) -> Unit
) {
    // Track the selection state of each friend with a mutable set of friend IDs (Int)
    val selectedFriends = remember { mutableStateListOf<Int>() }
    val context = LocalContext.current

    // Reset selected friends each time the sheet is opened
    if (isSheetOpen.value) {
        selectedFriends.clear()

        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = { isSheetOpen.value = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Send to friends",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)
                )

                if (friends.isEmpty()) {
                    Text(
                        text = "Looks like you have no friends",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                } else {
                    LazyColumn {
                        items(friends) { friend ->
                            val isSelected = selectedFriends.contains(friend.id.toInt())

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isSelected) {
                                            selectedFriends.remove(friend.id.toInt())
                                        } else {
                                            selectedFriends.add(friend.id.toInt())
                                        }
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Images.Circle(
                                    model = friend.avatarId
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = friend.email)
                                Spacer(modifier = Modifier.weight(1f))
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { isChecked ->
                                        if (isChecked) {
                                            selectedFriends.add(friend.id.toInt())
                                        } else {
                                            selectedFriends.remove(friend.id.toInt())
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        // Filter selected friends and pass to the callback
                        val selectedFriendList = friends.filter { friend ->
                            selectedFriends.contains(friend.id.toInt())
                        }
                        if (selectedFriendList.isNotEmpty()) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.sent_to_friend),
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                context,
                                "No friends were selected.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        onFriendsSelected(selectedFriendList)
                        isSheetOpen.value = false
                    },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp)
                ) {
                    Text(text = "Send wallpaper")
                }
            }
        }
    }
}






