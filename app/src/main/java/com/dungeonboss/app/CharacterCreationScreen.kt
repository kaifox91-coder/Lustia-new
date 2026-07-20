package com.dungeonboss.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CharacterCreationScreen(
    onCreationComplete: (Boss) -> Unit,
    gameState: GameState
) {
    val cs = MaterialTheme.colorScheme
    var currentStep by remember { mutableStateOf(gameState.getCreationStep()) }

    var name by remember { mutableStateOf("") }
    var race by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var appearance by remember { mutableStateOf("") }

    var setting by remember { mutableStateOf("") }
    var floorTheme by remember { mutableStateOf("") }
    var bosspower by remember { mutableStateOf("") }

    var skill1 by remember { mutableStateOf("") }
    var skill2 by remember { mutableStateOf("") }
    var skill3 by remember { mutableStateOf("") }

    var technique1 by remember { mutableStateOf("") }
    var technique2 by remember { mutableStateOf("") }
    var technique3 by remember { mutableStateOf("") }

    var spell1 by remember { mutableStateOf("") }
    var spell2 by remember { mutableStateOf("") }
    var spell3 by remember { mutableStateOf("") }

    var size by remember { mutableStateOf("Average") }
    var physique by remember { mutableStateOf("Fit") }
    var resilience by remember { mutableStateOf("Tough") }
    var willpower by remember { mutableStateOf("Steady") }
    var speed by remember { mutableStateOf("Average") }
    var agility by remember { mutableStateOf("Agile") }
    var reflexes by remember { mutableStateOf("Sharp") }
    var weaponHandling by remember { mutableStateOf("Skilled") }
    var tactics by remember { mutableStateOf("Calculated") }
    var aim by remember { mutableStateOf("Precise") }
    var charisma by remember { mutableStateOf("Charming") }
    var deception by remember { mutableStateOf("Slippery") }
    var seduction by remember { mutableStateOf("Enticing") }
    var manipulation by remember { mutableStateOf("Cunning") }
    var trapCraft by remember { mutableStateOf("Expert") }
    var floorKnowledge by remember { mutableStateOf("Intimate") }
    var minionCommand by remember { mutableStateOf("Feared") }
    var arcana by remember { mutableStateOf("Versed") }
    var manaSurge by remember { mutableStateOf("Lake") }

    var dungeonVoice by remember { mutableStateOf("Chronicle") }
    var customDungeonVoice by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(cs.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        LinearProgressIndicator(
            progress = currentStep / 7f,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = cs.primary,
            trackColor = cs.surfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "STEP $currentStep — ${getStepTitle(currentStep)}",
            color = cs.primary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        when (currentStep) {
            1 -> Step1Screen(
                name = name, onNameChange = { name = it },
                race = race, onRaceChange = { race = it },
                age = age, onAgeChange = { age = it },
                height = height, onHeightChange = { height = it },
                gender = gender, onGenderChange = { gender = it },
                appearance = appearance, onAppearanceChange = { appearance = it }
            )
            2 -> Step2Screen(
                setting = setting, onSettingChange = { setting = it },
                floorTheme = floorTheme, onFloorThemeChange = { floorTheme = it }
            )
            3 -> Step3Screen(
                bosspower = bosspower, onBossPowerChange = { bosspower = it },
                skill1 = skill1, onSkill1Change = { skill1 = it },
                skill2 = skill2, onSkill2Change = { skill2 = it },
                skill3 = skill3, onSkill3Change = { skill3 = it },
                technique1 = technique1, onTechnique1Change = { technique1 = it },
                technique2 = technique2, onTechnique2Change = { technique2 = it },
                technique3 = technique3, onTechnique3Change = { technique3 = it },
                spell1 = spell1, onSpell1Change = { spell1 = it },
                spell2 = spell2, onSpell2Change = { spell2 = it },
                spell3 = spell3, onSpell3Change = { spell3 = it }
            )
            4 -> Step4Screen(
                size = size, onSizeChange = { size = it },
                physique = physique, onPhysiqueChange = { physique = it },
                resilience = resilience, onResilienceChange = { resilience = it },
                willpower = willpower, onWillpowerChange = { willpower = it },
                speed = speed, onSpeedChange = { speed = it },
                agility = agility, onAgilityChange = { agility = it },
                reflexes = reflexes, onReflexesChange = { reflexes = it },
                weaponHandling = weaponHandling, onWeaponHandlingChange = { weaponHandling = it },
                tactics = tactics, onTacticsChange = { tactics = it },
                aim = aim, onAimChange = { aim = it },
                charisma = charisma, onCharismaChange = { charisma = it },
                deception = deception, onDeceptionChange = { deception = it },
                seduction = seduction, onSeductionChange = { seduction = it },
                manipulation = manipulation, onManipulationChange = { manipulation = it },
                trapCraft = trapCraft, onTrapCraftChange = { trapCraft = it },
                floorKnowledge = floorKnowledge, onFloorKnowledgeChange = { floorKnowledge = it },
                minionCommand = minionCommand, onMinionCommandChange = { minionCommand = it },
                arcana = arcana, onArcanaChange = { arcana = it },
                manaSurge = manaSurge, onManaSurgeChange = { manaSurge = it }
            )
            5 -> Step5Screen(
                dungeonVoice = dungeonVoice,
                onDungeonVoiceChange = { dungeonVoice = it },
                customDungeonVoice = customDungeonVoice,
                onCustomDungeonVoiceChange = { customDungeonVoice = it }
            )
            6 -> Step6Screen()
            7 -> Step7Screen()
        }

        if (validationError.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = validationError,
                color = cs.error,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {
                    validationError = ""
                    if (currentStep > 1) currentStep--
                },
                enabled = currentStep > 1,
                colors = ButtonDefaults.buttonColors(
                    containerColor = cs.surfaceVariant,
                    contentColor = cs.onSurfaceVariant,
                    disabledContainerColor = cs.surfaceVariant.copy(alpha = 0.5f),
                    disabledContentColor = cs.onSurfaceVariant.copy(alpha = 0.6f)
                )
            ) {
                Text("BACK")
            }

            if (currentStep < 7) {
                Button(
                    onClick = {
                        validationError = ""
                        gameState.setCreationStep(currentStep + 1)
                        currentStep++
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = cs.primary,
                        contentColor = cs.onPrimary
                    )
                ) {
                    Text("NEXT", fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = {
                        val parsedAge = age.toIntOrNull()
                        if (parsedAge == null || parsedAge <= 0) {
                            validationError = "Please enter a valid age greater than 0 before completing creation."
                            return@Button
                        }

                        val finalDungeonVoice = if (dungeonVoice == "Custom") {
                            customDungeonVoice.ifBlank { "Custom" }
                        } else {
                            dungeonVoice
                        }

                        val boss = Boss(
                            name = name,
                            race = race,
                            age = parsedAge,
                            height = height,
                            gender = gender,
                            appearance = appearance,
                            setting = setting,
                            floorTheme = floorTheme,
                            bosspower = bosspower,
                            skills = listOf(skill1, skill2, skill3).filter { it.isNotEmpty() },
                            techniques = listOf(technique1, technique2, technique3).filter { it.isNotEmpty() },
                            spells = listOf(spell1, spell2, spell3).filter { it.isNotEmpty() },
                            stats = BossStats(
                                size = size,
                                physique = physique,
                                resilience = resilience,
                                willpower = willpower,
                                speed = speed,
                                agility = agility,
                                reflexes = reflexes,
                                weaponHandling = weaponHandling,
                                tactics = tactics,
                                aim = aim,
                                charisma = charisma,
                                deception = deception,
                                seduction = seduction,
                                manipulation = manipulation,
                                trapCraft = trapCraft,
                                floorKnowledge = floorKnowledge,
                                minionCommand = minionCommand,
                                arcana = arcana,
                                manaSurge = manaSurge
                            ),
                            dungeonVoice = finalDungeonVoice
                        )
                        gameState.updateBoss(boss)
                        gameState.setCreationStep(0)
                        onCreationComplete(boss)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = cs.tertiary,
                        contentColor = cs.onTertiary
                    )
                ) {
                    Text("COMPLETE", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun getStepTitle(step: Int): String = when (step) {
    1 -> "WHO ARE YOU"
    2 -> "SETTING AND FLOOR"
    3 -> "YOUR POWER"
    4 -> "STATS"
    5 -> "THE DUNGEON'S VOICE"
    6 -> "NSFW (OPTIONAL)"
    7 -> "FLOOR CONFIGURATION"
    else -> "CREATION"
}

@Composable
fun CreationInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    Column(modifier = modifier) {
        Text(label, color = cs.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = cs.surface,
                unfocusedContainerColor = cs.surface,
                focusedTextColor = cs.onSurface,
                unfocusedTextColor = cs.onSurface,
                focusedIndicatorColor = cs.primary,
                unfocusedIndicatorColor = cs.outline,
                focusedLabelColor = cs.primary,
                unfocusedLabelColor = cs.onSurfaceVariant,
                cursorColor = cs.primary
            )
        )
    }
}

@Composable
fun Step1Screen(
    name: String, onNameChange: (String) -> Unit,
    race: String, onRaceChange: (String) -> Unit,
    age: String, onAgeChange: (String) -> Unit,
    height: String, onHeightChange: (String) -> Unit,
    gender: String, onGenderChange: (String) -> Unit,
    appearance: String, onAppearanceChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CreationInputField("Name", name, onNameChange)
        CreationInputField("Race", race, onRaceChange)
        CreationInputField("Age", age, onAgeChange)
        CreationInputField("Height", height, onHeightChange)
        CreationInputField("Gender", gender, onGenderChange)
        CreationInputField("Appearance", appearance, onAppearanceChange)
    }
}

@Composable
fun Step2Screen(
    setting: String, onSettingChange: (String) -> Unit,
    floorTheme: String, onFloorThemeChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CreationInputField("Setting (where your dungeon exists)", setting, onSettingChange)
        CreationInputField("Floor Theme (aesthetic)", floorTheme, onFloorThemeChange)
    }
}

@Composable
fun Step3Screen(
    bosspower: String, onBossPowerChange: (String) -> Unit,
    skill1: String, onSkill1Change: (String) -> Unit,
    skill2: String, onSkill2Change: (String) -> Unit,
    skill3: String, onSkill3Change: (String) -> Unit,
    technique1: String, onTechnique1Change: (String) -> Unit,
    technique2: String, onTechnique2Change: (String) -> Unit,
    technique3: String, onTechnique3Change: (String) -> Unit,
    spell1: String, onSpell1Change: (String) -> Unit,
    spell2: String, onSpell2Change: (String) -> Unit,
    spell3: String, onSpell3Change: (String) -> Unit
) {
    val cs = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CreationInputField("Boss Power (your defining ability)", bosspower, onBossPowerChange)
        Text("Skills", color = cs.primary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        CreationInputField("Skill 1", skill1, onSkill1Change)
        CreationInputField("Skill 2", skill2, onSkill2Change)
        CreationInputField("Skill 3", skill3, onSkill3Change)
        Text("Techniques", color = cs.primary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        CreationInputField("Technique 1", technique1, onTechnique1Change)
        CreationInputField("Technique 2", technique2, onTechnique2Change)
        CreationInputField("Technique 3", technique3, onTechnique3Change)
        Text("Spells (Optional)", color = cs.primary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        CreationInputField("Spell 1", spell1, onSpell1Change)
        CreationInputField("Spell 2", spell2, onSpell2Change)
        CreationInputField("Spell 3", spell3, onSpell3Change)
    }
}

@Composable
fun Step4Screen(
    size: String, onSizeChange: (String) -> Unit,
    physique: String, onPhysiqueChange: (String) -> Unit,
    resilience: String, onResilienceChange: (String) -> Unit,
    willpower: String, onWillpowerChange: (String) -> Unit,
    speed: String, onSpeedChange: (String) -> Unit,
    agility: String, onAgilityChange: (String) -> Unit,
    reflexes: String, onReflexesChange: (String) -> Unit,
    weaponHandling: String, onWeaponHandlingChange: (String) -> Unit,
    tactics: String, onTacticsChange: (String) -> Unit,
    aim: String, onAimChange: (String) -> Unit,
    charisma: String, onCharismaChange: (String) -> Unit,
    deception: String, onDeceptionChange: (String) -> Unit,
    seduction: String, onSeductionChange: (String) -> Unit,
    manipulation: String, onManipulationChange: (String) -> Unit,
    trapCraft: String, onTrapCraftChange: (String) -> Unit,
    floorKnowledge: String, onFloorKnowledgeChange: (String) -> Unit,
    minionCommand: String, onMinionCommandChange: (String) -> Unit,
    arcana: String, onArcanaChange: (String) -> Unit,
    manaSurge: String, onManaSurgeChange: (String) -> Unit
) {
    val sizeOptions = listOf("Petite", "Small", "Average", "Lithe", "Heavyset", "Tall", "Imposing", "Massive")
    val physiqueOptions = listOf("Slender", "Lean", "Fit", "Curvy", "Athletic", "Toned", "Muscular", "Hulking")
    val resilienceOptions = listOf("Fragile", "Delicate", "Average", "Sturdy", "Tough", "Hardened", "Ironclad", "Unbreakable")
    val willpowerOptions = listOf("Timid", "Wavering", "Steady", "Resolute", "Driven", "Unyielding", "Fanatical")
    val speedOptions = listOf("Sluggish", "Measured", "Average", "Quick", "Swift", "Lightning")
    val agilityOptions = listOf("Stiff", "Balanced", "Nimble", "Agile", "Acrobatic", "Serpentine", "Ghostlike")
    val reflexOptions = listOf("Dulled", "Slow", "Aware", "Sharp", "Honed", "Instinctive", "Preternatural")
    val weaponHandlingOptions = listOf("Untrained", "Novice", "Competent", "Skilled", "Expert", "Masterful")
    val tacticsOptions = listOf("Reckless", "Opportunistic", "Calculated", "Adaptive", "Strategic", "Tactician")
    val aimOptions = listOf("Erratic", "Shaky", "Steady", "Precise", "Surgical", "Unerring")
    val charismaOptions = listOf("Abrasive", "Plain", "Likeable", "Charming", "Magnetic", "Regal")
    val deceptionOptions = listOf("Transparent", "Awkward", "Convincing", "Slippery", "Masterful", "Mythic")
    val seductionOptions = listOf("Awkward", "Coy", "Flirtatious", "Enticing", "Hypnotic", "Irresistible")
    val manipulationOptions = listOf("Naive", "Persuasive", "Cunning", "Calculating", "Dominant", "Puppetmaster")
    val trapCraftOptions = listOf("Clumsy", "Novice", "Competent", "Expert", "Masterful", "Legendary")
    val floorKnowledgeOptions = listOf("Newcomer", "Familiar", "Intimate", "Master Cartographer", "One With the Stone")
    val minionCommandOptions = listOf("Ignored", "Tolerated", "Obeyed", "Respected", "Feared", "Worshipped")
    val arcanaOptions = listOf("Untrained", "Initiate", "Studied", "Versed", "Arcanist", "Sage", "Archmage")
    val manaSurgeOptions = listOf("Faint", "Trickle", "Pool", "Reservoir", "Lake", "Ocean", "Wellspring", "Cataclysmic")

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        StatDropdown("Size", size, sizeOptions, onSizeChange)
        StatDropdown("Physique", physique, physiqueOptions, onPhysiqueChange)
        StatDropdown("Resilience", resilience, resilienceOptions, onResilienceChange)
        StatDropdown("Willpower", willpower, willpowerOptions, onWillpowerChange)
        StatDropdown("Speed", speed, speedOptions, onSpeedChange)
        StatDropdown("Agility", agility, agilityOptions, onAgilityChange)
        StatDropdown("Reflexes", reflexes, reflexOptions, onReflexesChange)
        StatDropdown("Weapon Handling", weaponHandling, weaponHandlingOptions, onWeaponHandlingChange)
        StatDropdown("Tactics", tactics, tacticsOptions, onTacticsChange)
        StatDropdown("Aim", aim, aimOptions, onAimChange)
        StatDropdown("Charisma", charisma, charismaOptions, onCharismaChange)
        StatDropdown("Deception", deception, deceptionOptions, onDeceptionChange)
        StatDropdown("Seduction", seduction, seductionOptions, onSeductionChange)
        StatDropdown("Manipulation", manipulation, manipulationOptions, onManipulationChange)
        StatDropdown("Trap Craft", trapCraft, trapCraftOptions, onTrapCraftChange)
        StatDropdown("Floor Knowledge", floorKnowledge, floorKnowledgeOptions, onFloorKnowledgeChange)
        StatDropdown("Minion Command", minionCommand, minionCommandOptions, onMinionCommandChange)
        StatDropdown("Arcana", arcana, arcanaOptions, onArcanaChange)
        StatDropdown("Mana Surge", manaSurge, manaSurgeOptions, onManaSurgeChange)
    }
}

@Composable
fun Step5Screen(
    dungeonVoice: String,
    onDungeonVoiceChange: (String) -> Unit,
    customDungeonVoice: String,
    onCustomDungeonVoiceChange: (String) -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val voiceOptions = listOf("Chronicle", "Advisor", "Witness", "Fondly Tired", "Custom")

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "How should the Dungeon speak to you?",
            color = cs.primary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        voiceOptions.forEach { voice ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = dungeonVoice == voice,
                    onClick = { onDungeonVoiceChange(voice) },
                    colors = RadioButtonDefaults.colors(selectedColor = cs.primary)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(voice, color = cs.onSurface)
            }
        }

        if (dungeonVoice == "Custom") {
            CreationInputField(
                label = "Custom Dungeon Voice",
                value = customDungeonVoice,
                onValueChange = onCustomDungeonVoiceChange
            )
        }
    }
}

@Composable
fun Step6Screen() {
    Text(
        "NSFW options are optional — skip to proceed to floor configuration.",
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
fun Step7Screen() {
    Text(
        "You can customize your floor traps and minions after creation using #traps and #minions commands.",
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatDropdown(
    label: String,
    selected: String,
    options: List<String>,
    onSelectionChange: (String) -> Unit
) {
    val cs = MaterialTheme.colorScheme
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected.ifEmpty { "Select..." },
            onValueChange = {},
            readOnly = true,
            label = { Text(label, color = cs.primary) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = cs.primary,
                unfocusedBorderColor = cs.outline,
                focusedContainerColor = cs.surface,
                unfocusedContainerColor = cs.surface,
                focusedTextColor = cs.onSurface,
                unfocusedTextColor = cs.onSurface,
                focusedLabelColor = cs.primary,
                unfocusedLabelColor = cs.onSurfaceVariant,
                cursorColor = cs.primary
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, color = cs.onSurface) },
                    onClick = {
                        onSelectionChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
