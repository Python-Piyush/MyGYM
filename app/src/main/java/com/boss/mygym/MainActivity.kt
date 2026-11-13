
package com.boss.mygym

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.boss.mygym.ui.theme.MyGYMTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MyGYMTheme {
                MyGYMApp()
            }
        }
    }
}

@Composable
fun MyGYMApp() {

    var showSettings by remember {
        mutableStateOf(false)
    }

    if (showSettings) {
        DurationSettingsScreen(
            onBack = { showSettings = false }
        )
    } else {
        GymHomeScreen(
            onOpenSettings = { showSettings = true }
        )
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun GymHomeScreen(onOpenSettings: () -> Unit) {

    val context = LocalContext.current
    val settings = remember { TimerSettings(context) }

    var selectedMuscle by remember {
        mutableStateOf(GymTimerDefaults.muscleGroups[0])
    }

    val savedDuration by remember(selectedMuscle.name) {
        settings.getDuration(selectedMuscle.name)
    }.collectAsState(
        initial = selectedMuscle.restSeconds
    )

    var remainingSeconds by remember {
        mutableIntStateOf(selectedMuscle.restSeconds)
    }

    var isRunning by remember {
        mutableStateOf(false)
    }

    var editingMuscle by remember {
        mutableStateOf<MuscleGroup?>(null)
    }

    LaunchedEffect(selectedMuscle.name, savedDuration) {
        if (!isRunning) {
            remainingSeconds = savedDuration
        }
    }

    LaunchedEffect(isRunning) {
        while (isRunning && remainingSeconds > 0) {
            delay(1000)
            remainingSeconds--
        }

        if (remainingSeconds == 0) {
            isRunning = false
        }
    }

    val time = "%02d:%02d".format(
        remainingSeconds / 60,
        remainingSeconds % 60
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF101114)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MyGYM",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                TextButton(onClick = onOpenSettings) {
                    Text("⚙ SETTINGS", color = Color(0xFFB7FF5C))
                }
            }

            Spacer(modifier = Modifier.height(35.dp))

            Text(
                text = "REST TIMER",
                color = Color.Gray,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .size(230.dp)
                    .background(
                        Color(0xFF202329),
                        CircleShape
                    )
                    .combinedClickable(
                        onClick = {
                            if (remainingSeconds == 0) {
                                remainingSeconds = savedDuration
                            }
                            isRunning = !isRunning
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = time,
                    fontSize = 55.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB7FF5C)
                )
            }

            Spacer(modifier = Modifier.height(15.dp))

            Text(
                text = if (isRunning)
                    "TAP TIMER TO PAUSE"
                else
                    "TAP TIMER TO START",
                color = Color.Gray,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedButton(
                onClick = {
                    isRunning = false
                    remainingSeconds = savedDuration
                }
            ) {
                Text("RESET", color = Color.White)
            }

            Spacer(modifier = Modifier.height(35.dp))

            Text(
                text = "SELECT MUSCLE GROUP",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Long press a muscle to edit its rest time",
                color = Color.Gray,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(15.dp))

            GymTimerDefaults.muscleGroups.chunked(2).forEach { rowMuscles ->

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    rowMuscles.forEach { muscle ->

                        val isSelected = selectedMuscle == muscle

                        Surface(
                            modifier = Modifier
                                .combinedClickable(
                                    onClick = {
                                        selectedMuscle = muscle
                                        isRunning = false
                                    },
                                    onLongClick = {
                                        editingMuscle = muscle
                                    }
                                ),
                            shape = RoundedCornerShape(50),
                            color = if (isSelected)
                                Color(0xFFB7FF5C)
                            else
                                Color(0xFF292D34)
                        ) {
                            Text(
                                text = muscle.name,
                                modifier = Modifier.padding(
                                    horizontal = 16.dp,
                                    vertical = 10.dp
                                ),
                                color = if (isSelected)
                                    Color.Black
                                else
                                    Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "DISCIPLINE BUILDS RESULTS.",
                color = Color.Gray,
                fontSize = 11.sp
            )
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
