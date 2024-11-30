package com.studios1299.playwall.core.presentation.components

import android.annotation.SuppressLint
import android.icu.text.ListFormatter.Width
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


/**This data class holds 2 variables icon and text which is required by [CustomExpandableFAB] to show the item in expanded form or for the button*/
data class ExpendableFabItem(
    val icon: ImageVector,
    val text: String,
    val onClick: () -> Unit
)

/**This [CustomExpandableFAB] creates the UI for the FAB, a Material-3 looking Floating Action Button in Jetpack Compose that expands into a Sheet with multiple options when clicked.
 * - This composable function should be called in an floatingActionButton(value-parameter) inside of a Scaffold */
@SuppressLint("UnrememberedMutableInteractionSource")
@Composable
fun ExpandableFab(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconExpanded: ImageVector = icon,
    text: String,
    items: List<ExpendableFabItem>
) {

    var buttonClicked by remember {
        mutableStateOf(false)
    }

    Row {
        Spacer(modifier = Modifier.weight(1f))
        Card(
            modifier = modifier,
            elevation = CardDefaults.elevatedCardElevation(4.dp)
        ) {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer)) {
                // The Expandable Sheet layout
                AnimatedVisibility(
                    visible = buttonClicked,
                    enter = expandVertically(tween(1200)) + fadeIn(),
                    exit = shrinkVertically(tween(700)) + fadeOut(
                        animationSpec = tween(400)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(vertical = 15.dp)
                            //.width(if (buttonClicked) width else 0.dp)
                    ) {
                        items.forEach { item ->
                            Row(modifier = Modifier
                                .clickable(
                                    onClick = {
                                        item.onClick()
                                        buttonClicked = false
                                    }
                                )
                                //.fillMaxWidth()
                                .padding(vertical = 10.dp, horizontal = 20.dp)
                            ) {
                                Icon(
                                    imageVector = item.icon, contentDescription = "refresh"
                                )

                                Spacer(modifier = Modifier.width(15.dp))

                                Text(text = item.text, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }

                // The FAB main button
                Card(
                    modifier = Modifier
                        .clickable(
                            onClick = {
                                buttonClicked = !buttonClicked
                            }
                        )
                        .align(Alignment.End),
                    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.primary).also {
                    }) {
                    Row(
                        modifier = Modifier.padding(vertical = 15.dp, horizontal = 20.dp)
                    ) {
                        Icon(
                            imageVector = if (buttonClicked) iconExpanded else icon, contentDescription = "refresh"
                        )
                        AnimatedVisibility(
                            visible = buttonClicked,
                            enter = expandVertically(animationSpec = tween(700)) + fadeIn(),
                            exit = shrinkVertically(tween(700)) + fadeOut(tween(700))
                        ) {
                            Row {
                                Spacer(modifier = Modifier.width(20.dp))
                                Text(text = text)
                            }
                        }
                    }
                }

            }

        }
    }
    
    

}