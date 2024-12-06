package com.studios1299.playwall.create.presentation

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.res.stringResource
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
import com.studios1299.playwall.explore.presentation.detail.PostDetailViewModel
import com.studios1299.playwall.explore.presentation.explore.ExploreState
import com.studios1299.playwall.play.presentation.chat.util.rememberRequestPermissionAndPickImage
import com.studios1299.playwall.play.presentation.play.Friend
import com.yalantis.ucrop.UCrop
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

    LaunchedEffect(Unit) {
        viewModel.errorMessages.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is CreateScreenEvent.ImageSaved -> {
                Toast.makeText(context, context.getString(R.string.image_saved_successfully), Toast.LENGTH_LONG).show()
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
        bottomNavbar = bottomNavbar,
        viewmodel = viewModel
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateScreen(
    state: CreateScreenState,
    onNavigateToDiamonds: () -> Unit,
    onAction: (CreateScreenAction) -> Unit,
    viewmodel: CreateViewModel,
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
            Toast.makeText(context,
                context.getString(R.string.gifs_are_not_supported), Toast.LENGTH_SHORT).show()
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
                Toast.makeText(context,
                    context.getString(R.string.photo_editor_is_not_initialized), Toast.LENGTH_SHORT).show()
            }
        }
    }

    val coroutineScope = rememberCoroutineScope()
    val isFriendsSheetOpen = remember { mutableStateOf(false) }
    val friendsSheetState = rememberModalBottomSheetState()

    val cropOpen = remember { mutableStateOf(false) }
    val cropState = rememberModalBottomSheetState()

    LaunchedEffect(state.imageString) {
        Log.e("CreateScreen", "LaunchedEffect imageString0: = ${state.imageString}")
    }

    FriendsSelectionBottomSheet(
        isSheetOpen = isFriendsSheetOpen,
        sheetState = friendsSheetState,
        friends = state.friends,
        cropOpen = cropOpen,
        state = state,
        viewModel = viewmodel,
        onFriendsSelected = { selectedFriends ->
            Log.e("CreateScreen", "onFriendsSelected: = ${selectedFriends.size}")
            CoroutineScope(Dispatchers.Main).launch {
                if (photoEditor != null && photoEditorView != null) {
                    saveImageToGallery(
                        context = context,
                        photoEditor = photoEditor,
                        onSuccess = { savedUri ->
                            Log.e("CreateScreen", "saveImageToGallery.onSuccess: savedUri = ${savedUri}")
                            CoroutineScope(Dispatchers.IO).launch {
                                //setAsWallpaper(S3Handler.uploadToS3(uriToFile(context, savedUri)!!, S3Handler.Folder.WALLPAPERS)?:"", context)
                                //selectedImageUri =
                                  //  savedUri

                                val string = S3Handler.uploadToS3(uriToFile(context, savedUri)!!, S3Handler.Folder.WALLPAPERS)?:""
                                Log.e("CreateScreen", "saveImageToGallery.imageString11: = ${string}")
                                Log.e("CreateScreen", "uriToFile(context, savedUri)!!12: = ${uriToFile(context, savedUri)!!}")
                                Log.e("CreateScreen", "S3Handler.uploadToS3(uriToFile(context, savedUri)!!, S3Handler.Folder.WALLPAPERS)13: = ${S3Handler.uploadToS3(uriToFile(context, savedUri)!!, S3Handler.Folder.WALLPAPERS)}")


                                viewmodel.updateState(state.copy(selectedFriend = selectedFriends.get(0), imageString = string))
                                Log.e("CreateScreen", "saveImageToGallery.imageString0: = ${state.imageString}")

                                cropOpen.value = true
                                //onAction(CreateScreenAction.SendToFriends(selectedFriends, selectedImageUri, context))
                            }
                        }
                    )
                } else {
                    Toast.makeText(context, "Photo editor is not initialized", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )

    if (cropOpen.value) {
        CropScreen(
            cropOpen = cropOpen,
            state = state,
            onWallpaperSent = { croppedImagePath ->
                // Toast.makeText(context, "Send image to friend", Toast.LENGTH_LONG).show()
            },
            viewModel = viewmodel
        )
    }

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
                                        viewmodel.updateState(state.copy(selectedImageUri = savedUri))
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
                        Text(stringResource(R.string.replace))
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
                title = { Text(stringResource(R.string.replace_image)) },
                text = { Text(stringResource(R.string.you_will_lose_your_progress_if_you_haven_t_saved_do_you_want_to_replace_the_image)) }
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


@Composable
private fun CropScreen(
    cropOpen: MutableState<Boolean>,
    state: CreateScreenState,
    onWallpaperSent: (String) -> Unit, // Callback after sending the wallpaper
    viewModel: CreateViewModel
) {
    val context = LocalContext.current
    var hasLaunched by remember { mutableStateOf(false) } // Added flag to ensure single execution

    val cropLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val resultUri = UCrop.getOutput(result.data ?: return@rememberLauncherForActivityResult)
        if (resultUri != null) {
            viewModel.sendWallpaperToFriends(listOf(state.selectedFriend), resultUri)

            // Notify parent to close the CropScreen or reset the state
            onWallpaperSent(resultUri.toString())
            cropOpen.value = false
        }
        hasLaunched = false // Reset for future executions if necessary
    }

    fun launchUCrop(sourceUri: Uri, screenRatio: Float?) {
        val destinationUri =
            Uri.fromFile(File(context.cacheDir, "cropped_image_${System.currentTimeMillis()}.jpg"))

        val uCrop = UCrop.of(sourceUri, destinationUri)
            .withAspectRatio(screenRatio ?: 1f, 1f)
            .withMaxResultSize(4096, 4096)
            .withOptions(UCrop.Options().apply {
                setCompressionQuality(100)
                setFreeStyleCropEnabled(false)
                setHideBottomControls(true)
                setToolbarTitle("Adjust wallpaper")
            })

        cropLauncher.launch(uCrop.getIntent(context))
    }

    LaunchedEffect(state.imageString) {
        Log.e("ANDRII", "LaunchEffect triggered, imageString: '${state.imageString}'")

        if (!hasLaunched && state.imageString.isNotBlank()) {
            Log.e("ANDRII", "Going inside, imageString: '${state.imageString}'")
            val sourceUri = Uri.parse(S3Handler.pathToDownloadableLink(state.imageString))
            val screenRatio = 1 / (state.selectedFriend.screenRatio ?: 2f)
            launchUCrop(sourceUri, screenRatio)
            hasLaunched = true // Mark as launched
        } else {
            Log.e("ANDRII", "Skipped processing: hasLaunched=$hasLaunched, imageString='${state.imageString}'")
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsSelectionBottomSheet(
    state: CreateScreenState,
    viewModel: CreateViewModel,
    isSheetOpen: MutableState<Boolean>,
    sheetState: SheetState,
    friends: List<Friend>,
    onFriendsSelected: (List<Friend>) -> Unit,
    cropOpen: MutableState<Boolean>,
) {
   // val context = LocalContext.current

    if (isSheetOpen.value) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = { isSheetOpen.value = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.send_to_friends),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)
                )

                if (friends.isEmpty()) {
                    Text(
                        text = stringResource(R.string.looks_like_you_have_no_friends),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                } else {
                    LazyColumn {
                        items(friends) { friend ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        // Send wallpaper to the clicked friend
                                        onFriendsSelected(listOf(friend))
                                        cropOpen.value = true
                                        isSheetOpen.value = false
                                        viewModel.updateState(state.copy(selectedFriend = friend))


//                                        onFriendsSelected(listOf(friend))
//                                        isSheetOpen.value = false
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Images.Circle(
                                    model = friend.avatarId
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = friend.nick ?: friend.email)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(36.dp))
            }
        }
    }
}


// TODO: boilerplate from PostDetailScreen to be removed
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun FriendsSelectionBottomSheet(
//    isSheetOpen: MutableState<Boolean>,
//    sheetState: SheetState,
//    friends: List<Friend>,
//    onFriendsSelected: (List<Friend>) -> Unit
//) {
//    // Track the selection state of each friend with a mutable set of friend IDs (Int)
//    val selectedFriends = remember { mutableStateListOf<Int>() }
//    val context = LocalContext.current
//
//    // Reset selected friends each time the sheet is opened
//    if (isSheetOpen.value) {
//        selectedFriends.clear()
//
//        ModalBottomSheet(
//            sheetState = sheetState,
//            onDismissRequest = { isSheetOpen.value = false }
//        ) {
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//            ) {
//                Text(
//                    text = stringResource(R.string.send_to_friends),
//                    style = MaterialTheme.typography.titleLarge,
//                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)
//                )
//
//                if (friends.isEmpty()) {
//                    Text(
//                        text = stringResource(R.string.looks_like_you_have_no_friends),
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(16.dp),
//                        textAlign = TextAlign.Center
//                    )
//                } else {
//                    LazyColumn {
//                        items(friends) { friend ->
//                            val isSelected = selectedFriends.contains(friend.id.toInt())
//
//                            Row(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .clickable {
//                                        if (isSelected) {
//                                            selectedFriends.remove(friend.id.toInt())
//                                        } else {
//                                            selectedFriends.add(friend.id.toInt())
//                                        }
//                                    }
//                                    .padding(8.dp),
//                                verticalAlignment = Alignment.CenterVertically
//                            ) {
//                                Images.Circle(
//                                    model = friend.avatarId
//                                )
//                                Spacer(modifier = Modifier.width(8.dp))
//                                Text(text = friend.nick?:friend.email)
//                                Spacer(modifier = Modifier.weight(1f))
//                                Checkbox(
//                                    checked = isSelected,
//                                    onCheckedChange = { isChecked ->
//                                        if (isChecked) {
//                                            selectedFriends.add(friend.id.toInt())
//                                        } else {
//                                            selectedFriends.remove(friend.id.toInt())
//                                        }
//                                    }
//                                )
//                            }
//                        }
//                    }
//                }
//
//                Button(
//                    onClick = {
//                        // Filter selected friends and pass to the callback
//                        val selectedFriendList = friends.filter { friend ->
//                            selectedFriends.contains(friend.id.toInt())
//                        }
//                        if (selectedFriendList.isNotEmpty()) {
//
//                        } else {
//                            Toast.makeText(
//                                context,
//                                context.getString(R.string.no_friends_were_selected),
//                                Toast.LENGTH_SHORT
//                            ).show()
//                        }
//                        onFriendsSelected(selectedFriendList)
//                        isSheetOpen.value = false
//                    },
//                    modifier = Modifier
//                        .align(Alignment.CenterHorizontally)
//                        .padding(16.dp)
//                ) {
//                    Text(text = stringResource(R.string.send_wallpaper))
//                }
//            }
//        }
//    }
//}






