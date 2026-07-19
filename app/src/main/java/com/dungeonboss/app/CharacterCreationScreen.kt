package com.dungeonboss.app

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CharacterCreationScreen(
    onCreationComplete: (Boss) -> Unit,
    gameState: GameState
) {
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A2E))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Progress indicator
        LinearProgressIndicator(
            progress = (currentStep / 7f),
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = Color(0xFFD4AF37),
            trackColor = Color(0xFF333333)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "STEP $currentStep — ${getStepTitle(currentStep)}",
            color = Color(0xFFD4AF37),
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
                dungeonVoice = dungeonVoice, onDungeonVoiceChange = { dungeonVoice = it }
            )
            6 -> Step6Screen()
            7 -> Step7Screen()
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Navigation buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { if (currentStep > 1) currentStep-- },
                enabled = currentStep > 1,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF666666),
                    disabledContainerColor = Color(0xFF333333)
                )
            ) {
                Text("BACK")
            }

            if (currentStep < 7) {
                Button(
                    onClick = { 
                        gameState.setCreationStep(currentStep + 1)
                        currentStep++ 
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD4AF37)
                    )
                ) {
                    Text("NEXT", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = {
                        val boss = Boss(
                            name = name,
                            race = race,
                            age = age.toIntOrNull() ?: 0,
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
                            dungeonVoice = dungeonVoice
                        )
                        gameState.updateBoss(boss)
                        gameState.setCreationStep(0)
                        onCreationComplete(boss)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Text("COMPLETE", color = Color.White, fontWeight = FontWeight.Bold)
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
    Column(modifier = modifier) {
        Text(label, color = Color(0xFFD4AF37), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF0F3460),
                unfocusedContainerColor = Color(0xFF0F3460),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
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
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CreationInputField("Boss Power (your defining ability)", bosspower, onBossPowerChange)
        Text("Skills", color = Color(0xFFD4AF37), fontSize = 14.sp, fontWeight = FontWeight.Bold)
        CreationInputField("Skill 1", skill1, onSkill1Change)
        CreationInputField("Skill 2", skill2, onSkill2Change)
        CreationInputField("Skill 3", skill3, onSkill3Change)
        Text("Techniques", color = Color(0xFFD4AF37), fontSize = 14.sp, fontWeight = FontWeight.Bold)
        CreationInputField("Technique 1", technique1, onTechnique1Change)
        CreationInputField("Technique 2", technique2, onTechnique2Change)
        CreationInputField("Technique 3", technique3, onTechnique3Change)
        Text("Spells (Optional)", color = Color(0xFFD4AF37), fontSize = 14.sp, fontWeight = FontWeight.Bold)
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
    val sizeOptions = listOf("Slight", "Average", "Tall", "Imposing", "Massive", "Towering")
    val physiqueOptions = listOf("Frail", "Lean", "Fit", "Athletic", "Muscular", "Hulking", "Monstrous")
    val resilenceOptions = listOf("Fragile", "Delicate", "Average", "Tough", "Hardened", "Ironclad", "Unbreakable")
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        StatDropdown("Size", size, sizeOptions, onSizeChange)
        StatDropdown("Physique", physique, physiqueOptions, onPhysiqueChange)
        StatDropdown("Resilience", resilience, resilenceOptions, onResilienceChange)
        StatDropdown("Willpower", willpower, listOf("Broken", "Wavering", "Uncertain", "Steady", "Resolute", "Driven", "Fanatical"), onWillpowerChange)
        StatDropdown("Speed", speed, listOf("Sluggish", "Slow", "Average", "Quick", "Fast", "Rushing", "Blinding"), onSpeedChange)
        StatDropdown("Agility", agility, listOf("Clumsy", "Stiff", "Average", "Nimble", "Agile", "Acrobatic", "Ghostlike"), onAgilityChange)
        StatDropdown("Reflexes", reflexes, listOf("Oblivious", "Slow", "Average", "Sharp", "Honed", "Wired", "Instinctive"), onReflexesChange)
        StatDropdown("Weapon Handling", weaponHandling, listOf("Untrained", "Novice", "Competent", "Skilled", "Expert", "Masterful"), onWeaponHandlingChange)
        StatDropdown("Tactics", tactics, listOf("Reckless", "Impulsive", "Chaotic", "Average", "Calculated", "Adaptive", "Strategic", "Tactician"), onTacticsChange)
        StatDropdown("Aim", aim, listOf("Wild", "Shaky", "Average", "Steady", "Precise", "Surgical", "Unmatched"), onAimChange)
        StatDropdown("Charisma", charisma, listOf("Repulsive", "Offputting", "Plain", "Likeable", "Charming", "Magnetic"), onCharismaChange)
        StatDropdown("Deception", deception, listOf("Transparent", "Clumsy", "Average", "Slippery", "Convincing", "Masterful"), onDeceptionChange)
        StatDropdown("Seduction", seduction, listOf("Awkward", "Bland", "Average", "Flirtatious", "Enticing", "Irresistible"), onSeductionChange)
        StatDropdown("Manipulation", manipulation, listOf("Naive", "Clumsy", "Average", "Cunning", "Calculating", "Puppetmaster"), onManipulationChange)
        StatDropdown("Trap Craft", trapCraft, listOf("Helpless", "Novice", "Average", "Competent", "Expert", "Masterful", "Legendary"), onTrapCraftChange)
        StatDropdown("Floor Knowledge", floorKnowledge, listOf("Lost", "Basic", "Average", "Familiar", "Intimate", "OneWithTheStone"), onFloorKnowledgeChange)
        StatDropdown("Minion Command", minionCommand, listOf("Ignored", "Tolerated", "Obeyed", "Respected", "Feared", "Worshipped"), onMinionCommandChange)
        StatDropdown("Arcana", arcana, listOf("Mundane", "Initiate", "Studied", "Versed", "Learned", "Arcanist", "Sage"), onArcanaChange)
        StatDropdown("Mana Surge", manaSurge, listOf("Dry", "Trickle", "Pool", "Reservoir", "Lake", "Ocean", "Wellspring"), onManaSurgeChange)
    }
}

@Composable
fun Step5Screen(
    dungeonVoice: String, onDungeonVoiceChange: (String) -> Unit
) {
    val voiceOptions = listOf("Chronicle", "Advisor", "Witness", "Fondly Tired", "Custom")
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("How should the Dungeon speak to you?", color = Color(0xFFD4AF37), fontSize = 14.sp, fontWeight = FontWeight.Bold)
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
                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFD4AF37))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(voice, color = Color.White)
            }
        }
    }
}

@Composable
fun Step6Screen() {
    Text("NSFW options are optional — skip to proceed to floor configuration.", color = Color(0xFFCCCCCC))
}

@Composable
fun Step7Screen() {
    Text("You can customize your floor traps and minions after creation using #traps and #minions commands.", color = Color(0xFFCCCCCC))
}

@Composable
fun StatDropdown(
    label: String,
    selected: String,
    options: List<String>,
    onSelectionChange: (String) -> Unit
) {
    Column {
        Text(label, color = Color(0xFFD4AF37), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        var expanded by remember { mutableStateOf(false) }
        
        Button(
            onClick = { expanded = !expanded },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F3460))
        ) {
            Text(selected.ifEmpty { "Select..." }, color = Color.White)
        }
        
        if (expanded) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                color = Color(0xFF16213E)
            ) {
                LazyColumn {
                    items(options.size) { index ->
                        Text(
                            text = options[index],
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            color = Color.White
                        )
                        Divider(color = Color(0xFF333333))
                    }
                }
            }
        }
    }
}
