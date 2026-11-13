
package com.boss.mygym

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun DurationSettingsScreen(onBack: () -> Unit) {

    val context = LocalContext.current
    val settings = remember { TimerSettings(context) }
    var editingMuscle by remember {
        mutableStateOf<MuscleGroup?>(null)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        TextButton(onClick = onBack) {
            Text("← Back")
        }

        Text(
            text = "Rest Duration Settings",
            fontSize = 25.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        GymTimerDefaults.muscleGroups.forEach { muscle ->

            DurationRow(
                muscle = muscle,
                settings = settings,
                onEdit = {
                    editingMuscle = muscle
                }
            )

            HorizontalDivider()
        }
    }

    editingMuscle?.let { muscle ->
        DurationEditorDialog(
            muscle = muscle,
            settings = settings,
            onDismiss = {
                editingMuscle = null
            }
        )
    }
}

@Composable
fun DurationRow(
    muscle: MuscleGroup,
    settings: TimerSettings,
    onEdit: () -> Unit
) {

    val seconds by remember(muscle.name) {
        settings.getDuration(muscle.name)
    }.collectAsState(initial = muscle.restSeconds)

    val time = "%02d:%02d".format(
        seconds / 60,
        seconds % 60
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(modifier = Modifier.weight(1f)) {
            Text(muscle.name)
            Text(
                text = "Rest: $time",
                color = MaterialTheme.colorScheme.secondary
            )
        }

        TextButton(onClick = onEdit) {
            Text("EDIT")
        }
    }
}

@Composable
fun DurationEditorDialog(
    muscle: MuscleGroup,
    settings: TimerSettings,
    onDismiss: () -> Unit
) {

    val scope = rememberCoroutineScope()

    val savedSeconds by remember(muscle.name) {
        settings.getDuration(muscle.name)
    }.collectAsState(initial = muscle.restSeconds)

    var minutes by remember(muscle.name) {
        mutableStateOf((muscle.restSeconds / 60).toString())
    }

    var seconds by remember(muscle.name) {
        mutableStateOf((muscle.restSeconds % 60).toString())
    }

    LaunchedEffect(savedSeconds) {
        minutes = (savedSeconds / 60).toString()
        seconds = (savedSeconds % 60).toString()
    }

    val minValue = minutes.toIntOrNull()
    val secValue = seconds.toIntOrNull()

    val valid = minValue != null &&
            secValue != null &&
            minValue >= 0 &&
            secValue in 0..59 &&
            (minValue * 60 + secValue) > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Edit ${muscle.name} Rest")
        },
        text = {
            Column {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = minutes,
                        onValueChange = {
                            minutes = it.filter { c -> c.isDigit() }
                        },
                        label = { Text("Minutes") },
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = seconds,
                        onValueChange = {
                            seconds = it.filter { c -> c.isDigit() }
                        },
                        label = { Text("Seconds") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = valid,
                onClick = {
                    val total = minValue!! * 60 + secValue!!

                    scope.launch {
                        settings.saveDuration(
                            muscle.name,
                            total
                        )
                        onDismiss()
                    }
                }
            ) {
                Text("SAVE")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        }
    )
}
