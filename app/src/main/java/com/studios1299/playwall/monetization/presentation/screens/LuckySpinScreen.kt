package com.studios1299.playwall.monetization.presentation.screens

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutQuad
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdUnits
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import com.google.firebase.analytics.FirebaseAnalytics
import com.studios1299.playwall.R
import com.studios1299.playwall.core.presentation.components.Toolbars
import com.studios1299.playwall.core.presentation.designsystem.DIAMONDS_SCREEN_PANEL
import com.studios1299.playwall.monetization.data.AdManager
import com.studios1299.playwall.monetization.presentation.AppState
import com.studios1299.playwall.monetization.presentation.LuckySpinViewModel
import com.studios1299.playwall.monetization.presentation.components.NextSpinSheet
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds


@Composable
fun LuckySpinScreen(
    viewModel: LuckySpinViewModel,
    onBackClick: () -> Unit,
    adManager: AdManager
) {
    //val isLoading by viewModel.isNextSpinLoading.collectAsState()
    val isLoading = AppState.isNextSpinLoading.collectAsState().value
    //val isLoading = false
    val context = LocalContext.current
    val analytics = FirebaseAnalytics.getInstance(context)

    LaunchedEffect(Unit) {
        analytics.logEvent("seen_lucky_spin_screen", null)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LuckySpinTopBar(onBackClick = onBackClick, isLoading = isLoading)
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.winbg),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            SpinView(
                viewModel = viewModel,
                adManager = adManager
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LuckySpinTopBar(onBackClick: () -> Unit, isLoading: Boolean) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Toolbars.Primary(
        title = "Lucky Spin!",
        showBackButton = true,
        onBackClick = onBackClick,
        scrollBehavior = scrollBehavior
    )
}

@Composable
fun SpinView(
    viewModel: LuckySpinViewModel,
    adManager: AdManager
) {
    val scope = rememberCoroutineScope()
    val noAdsMessage = stringResource(id = R.string.no_ads_available)
    //val nextSpinSheetShow by viewModel.nextSpinSheetShow.collectAsState()
    //val isLoading by viewModel.isNextSpinLoading.collectAsState()
    //val isLoading = false

    val nextSpinSheetShow = AppState.nextSpinSheetShow.collectAsState().value
    val isLoading = AppState.isNextSpinLoading.collectAsState().value


    var diamonds by remember { mutableStateOf(1) }
    var showButton by remember { mutableStateOf(true) }

    val context = LocalContext.current
    val analytics = FirebaseAnalytics.getInstance(context)

    val colors1 = remember {
        listOf(
            "380048",
            "2B003D",
            "40004A",
            "590058",
            "730067"
        ).map { it.toColor() }
    }
    val colors2 = remember {
        listOf(
            "F9A114",
            "FD7D1B",
            "F9901A",
            "F6A019",
            "EFC017"
        ).map { it.toColor() }
    }
    val listItems = remember { listOf(
        SpinItemData(1),
        SpinItemData(1),
        SpinItemData(1),
        SpinItemData(1),
        SpinItemData(2),
        SpinItemData(2),
        SpinItemData(2),
        SpinItemData(3),
        SpinItemData(3),
        SpinItemData(5),
    ).shuffled()
    }

    BackHandler(enabled = isLoading) {
    }

    val items = remember {
        listItems.mapIndexed { index, spinItemData ->
            val colors = if (index % 2 == 0) colors1 else colors2

            SpinWheelItem(
                colors = colors.toPersistentList()
            ) {
                Text(
                    text = "$EVIL_EMOJI ${spinItemData.diamonds}",
                    style = TextStyle(color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                )
            }
        }.toPersistentList()
    }

    var pickerValue by remember { mutableIntStateOf(0) }

    val spinState = rememberSpinWheelState(
        items = items,
        backgroundImage = R.drawable.spin_wheel_background,
        centerImage = R.drawable.pw,
        indicatorImage = R.drawable.spin_wheel_tick,
        onSpinningFinished = {
            Log.d("SpinView", "Spin finished")
            Log.d("SpinView", "Got ${listItems[pickerValue].diamonds} diamonds")
            diamonds = listItems[pickerValue].diamonds
            viewModel.addDevils(diamonds)
           // viewModel.showNextSpinSheet()
            AppState.updateNextSpinSheetShow(true)
            showButton = true
            //viewModel.setLoadingNextSpinState(false)
            AppState.updateIsNextSpinLoading(false)

            analytics.logEvent("finished_spinning_wheel", null)
        },
        stopNbTurn = 5f,
        stopDuration = 4.seconds
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.size(10.dp))
        Box(modifier = Modifier.size(300.dp)) {
            SpinWheelComponent(spinState)
        }
        Spacer(modifier = Modifier.size(80.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = DIAMONDS_SCREEN_PANEL),
            shape = RoundedCornerShape(8.dp),
            enabled = showButton,
            onClick = {
                //viewModel.setLoadingNextSpinState(true)
                AppState.updateIsNextSpinLoading(true)
                showButton = false
                analytics.logEvent("started_to_spin_wheel", null)
                adManager.loadRewardedAd(
                    onAdLoaded = { adLoaded ->
                        if(adLoaded) {
                            adManager.showRewardedAdIfLoaded(
                                onAdClosed = {
                                    pickerValue = listItems
                                        .mapIndexed { index, item -> index to item.diamonds }
                                        .filter { it.second == 1 || it.second == 2 }
                                        .map { it.first }.random()
                                    spinState.stoppingWheel(pickerValue)
                                }
                            )

                        } else {
                            scope.launch {
//                                SnackbarController.sendEvent(
//                                    SnackbarEvent(
//                                        message = noAdsMessage,
//                                        duration = SnackbarDuration.Long
//                                    )
//                                )
                            }
                           // viewModel.setLoadingNextSpinState(false)
                        }
                    },
                )
            }
        ) {
            if(!showButton) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 3.dp
                )
            } else {
                Image(
                    imageVector = Icons.Default.AdUnits,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Watch ad to spin")
            }

        }

        if(nextSpinSheetShow) {
            NextSpinSheet(
                //viewModel = viewModel,
                isLoading = isLoading,
                diamonds = diamonds,
                onDismiss = {
                    //viewModel.hideNextSpinSheet()
                    AppState.updateNextSpinSheetShow(false)
                    showButton = true
                },
                onNextAdButtonClick = {
                   // viewModel.setLoadingNextSpinState(true)
                    AppState.updateIsNextSpinLoading(true)
                    showButton = false
                    //viewModel.hideNextSpinSheet()
                    AppState.updateNextSpinSheetShow(false)
                    adManager.loadRewardedAd(
                        onAdLoaded = { adLoaded ->

                            if(adLoaded) {
                                adManager.showRewardedAdIfLoaded(
                                    onAdClosed = {
                                        showButton = false
                                        pickerValue = listItems
                                            .mapIndexed { index, item -> index to item.diamonds }
                                            .filter { it.second == 1 || it.second == 2 }
                                            .map { it.first }.random()
                                        spinState.stoppingWheel(pickerValue)
                                    }
                                )

                            } else {
//                                scope.launch {
//                                    SnackbarController.sendEvent(
//                                        SnackbarEvent(
//                                            message = noAdsMessage,
//                                            duration = SnackbarDuration.Long
//                                        )
//                                    )
//                                }
                                //viewModel.setLoadingNextSpinState(false)
                                AppState.updateIsNextSpinLoading(false)
                            }
                        },
                    )
                }
            )
        }
    }
}

@Composable
internal fun SpinWheel(
    modifier: Modifier = Modifier,
    items: List<SpinWheelItem>,
) {
    BoxWithConstraints(modifier = modifier) {
        val degreesPerItems = items.getDegreesPerItem()
        val size = min(this.maxHeight, this.maxWidth)
        val brushEnd = with(LocalDensity.current) { size.toPx() / 2f }

        items.forEachIndexed { index, item ->
            SpinWheelSlice(
                modifier = Modifier.rotate(degrees = degreesPerItems * index),
                size = size,
                brush = item.colors.toBrush(brushEnd),
                degree = degreesPerItems,
                content = item.content
            )
        }
    }
}

@Composable
internal fun SpinWheelComponent(spinWheelState: SpinWheelState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.769f)
    ) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(id = spinWheelState.backgroundImage),
            contentScale = ContentScale.FillBounds,
            contentDescription = null,
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(0.769f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(27f))
            Image(
                modifier = Modifier
                    .weight(12f)
                    .aspectRatio(1f),
                painter = painterResource(id = spinWheelState.indicatorImage),
                contentDescription = null
            )
            BoxWithConstraints(
                modifier = Modifier
                    .weight(82f)
                    .aspectRatio(1f)
            ) {
                val imageSize = this.maxHeight.times(0.14f)
                SpinWheel(modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationZ = spinWheelState.rotation.value
                    }, items = spinWheelState.items)
                Image(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(imageSize),
                    painter = painterResource(id = spinWheelState.centerImage),
                    contentDescription = null
                )
            }
            Spacer(modifier = Modifier.weight(9f))
        }
    }
}

@Stable
data class SpinWheelItem(
    val colors: PersistentList<Color>,
    val content: @Composable () -> Unit,
)


internal fun List<SpinWheelItem>.getDegreesPerItem(): Float =  360f / this.size.toFloat()

@Composable
internal fun SpinWheelSlice(
    modifier: Modifier = Modifier,
    size: Dp,
    brush: Brush,
    degree: Float,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .size(size)
    ) {
        Canvas(
            modifier = Modifier
                .size(size)
        ) {

            drawArc(
                brush = brush,
                startAngle = -90f - (degree / 2),
                sweepAngle = degree,
                useCenter = true,
            )
        }
        Box(modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(top = 20.dp)) {
            content()
        }
    }
}

@Stable
data class SpinWheelState(
    internal val items: List<SpinWheelItem>,
    @DrawableRes internal val backgroundImage: Int,
    @DrawableRes internal val centerImage: Int,
    @DrawableRes internal val indicatorImage: Int,
    private val initSpinWheelSection: Int?,
    private val onSpinningFinished: (() -> Unit)?,
    private val stopDuration: Duration,
    private val stopNbTurn: Float,
    private val rotationPerSecond: Float,
    private val scope: CoroutineScope,
) {
    internal val rotation = Animatable(0f)

    init {
        initSpinWheelSection?.let {
            goto(it)
        } ?: launchInfinite()
    }

    fun stoppingWheel(sectionToStop: Int) {
        if (sectionToStop !in items.indices) {
            Log.e("spin-wheel", "cannot stop wheel, section $sectionToStop not exists in items")
            return
        }
        scope.launch {
            val destinationDegree = getDegreeFromSectionWithRandom(items, sectionToStop)
            rotation.animateTo(
                targetValue = rotation.value + (stopNbTurn * 360f) + destinationDegree + (360f - (rotation.value % 360f)),
                animationSpec = tween(
                    durationMillis = stopDuration.inWholeMilliseconds.toInt(),
                    easing = EaseOutQuad
                )
            )
            onSpinningFinished?.invoke()
        }
    }

    fun goto(section: Int) {
        scope.launch {
            if (section !in items.indices) {
                Log.e(
                    "spin-wheel",
                    "cannot goto specific section of wheel, section $section not exists in items"
                )
                return@launch
            }
            val positionDegree = getDegreeFromSection(items, section)
            rotation.snapTo(positionDegree)
        }
    }

    fun launchInfinite() {
        scope.launch {
            // Infinite repeatable rotation when is playing
            rotation.animateTo(
                targetValue = rotation.value + 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = (rotationPerSecond * 1000f).toInt(),
                        easing = LinearEasing
                    ),
                    repeatMode = RepeatMode.Restart
                )
            )
        }
    }
}

@Composable
fun rememberSpinWheelState(
    items: PersistentList<SpinWheelItem>,
    @DrawableRes backgroundImage: Int,
    @DrawableRes centerImage: Int,
    @DrawableRes indicatorImage: Int,
    onSpinningFinished: (() -> Unit)?,
    initSpinWheelSection: Int? = 0, //if null then infinite
    stopDuration: Duration = 8.seconds,
    stopNbTurn: Float = 3f,
    rotationPerSecond: Float = 0.8f,
    scope: CoroutineScope = rememberCoroutineScope(),
): SpinWheelState {
    return remember {
        SpinWheelState(
            items = items,
            backgroundImage = backgroundImage,
            centerImage = centerImage,
            indicatorImage = indicatorImage,
            initSpinWheelSection = initSpinWheelSection,
            stopDuration = stopDuration,
            stopNbTurn = stopNbTurn,
            rotationPerSecond = rotationPerSecond,
            scope = scope,
            onSpinningFinished = onSpinningFinished,
        )
    }
}

fun String.toColor(): Color {
    return if (this.length == 6) {
        val r = this.substring(0, 2).toInt(16)
        val g = this.substring(2, 4).toInt(16)
        val b = this.substring(4, 6).toInt(16)
        Color(r, g, b)
    } else {
        Color.White
    }
}

fun List<Color>.toBrush(endY: Float) =
    if (this.size == 1) {
        Brush.verticalGradient(colors = this)
    } else {
        val colorStops = this.mapIndexed { index, color ->
            val stop = if (index == 0) 0f else (index.toFloat() + 1f) / this.size.toFloat()
            Pair(stop, color)
        }.toTypedArray()
        Brush.verticalGradient(
            colorStops = colorStops,
            endY = endY,
        )
    }

fun getDegreeFromSection(items: List<SpinWheelItem>, section: Int): Float {
    val pieDegree = 360f / items.size
    return pieDegree * section.times(-1)
}

fun getDegreeFromSectionWithRandom(items: List<SpinWheelItem>, section: Int): Float {
    val pieDegree = 360f / items.size
    val exactDegree = pieDegree * section.times(-1)

    val pieReduced = pieDegree * 0.9f //to avoid stop near border
    val multiplier = if (Random.nextBoolean()) 1f else -1f //before or after exact degree
    val randomDegrees = Random.nextDouble(0.0, pieReduced / 2.0)
    return exactDegree + (randomDegrees.toFloat() * multiplier)
}

data class SpinItemData(
    val diamonds: Int
)

