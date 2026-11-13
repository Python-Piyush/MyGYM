
package com.boss.mygym

data class MuscleGroup(
    val name: String,
    val restSeconds: Int
)

object GymTimerDefaults {

    val muscleGroups = listOf(
        MuscleGroup("Chest", 120),
        MuscleGroup("Back", 90),
        MuscleGroup("Biceps", 180),
        MuscleGroup("Triceps", 120),
        MuscleGroup("Shoulders", 120),
        MuscleGroup("Legs", 180)
    )
}
