package com.crypticsamsara.sabikudi.ui.screens.mgmt

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crypticsamsara.sabikudi.ui.theme.NavyPrimary
import com.crypticsamsara.sabikudi.ui.theme.SabiKudiTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeopleGroupsScreen(
    onBackRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = SabiKudiTheme.extendedColors
    val scrollState = rememberScrollState()

    var activePeople by remember { mutableStateOf(mutableListOf("Ada", "Chidi", "Ngozi", "Tunde")) }
    var archivedPeople by remember { mutableStateOf(mutableListOf("Emeka")) }
    var groups by remember { mutableStateOf(listOf("Flatmates", "Shop staff")) }
    var newPersonName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "People & Groups Management",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackRequested) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Active People",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = extendedColors.muted
            )

            activePeople.forEach { person ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, extendedColors.line, RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(extendedColors.surface2, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = person.take(1), fontWeight = FontWeight.Bold, color = extendedColors.navy)
                            }
                            Text(text = person, fontWeight = FontWeight.Bold, color = extendedColors.navy)
                        }

                        OutlinedButton(
                            onClick = {
                                activePeople.remove(person)
                                archivedPeople.add(person)
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(text = "Archive", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = extendedColors.terracotta)
                        }
                    }
                }
            }

            // Add Person Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newPersonName,
                    onValueChange = { newPersonName = it },
                    placeholder = { Text("Add person") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        if (newPersonName.isNotBlank()) {
                            activePeople.add(newPersonName.trim())
                            newPersonName = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = extendedColors.navy),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(56.dp)
                ) {
                    Text(text = "Add", fontWeight = FontWeight.Bold)
                }
            }

            Text(
                text = "Archived People",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = extendedColors.muted,
                modifier = Modifier.padding(top = 8.dp)
            )

            if (archivedPeople.isEmpty()) {
                Text(text = "None", fontSize = 13.sp, color = extendedColors.muted)
            } else {
                archivedPeople.forEach { person ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().border(1.dp, extendedColors.line, RoundedCornerShape(12.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = person, color = extendedColors.muted)
                            OutlinedButton(
                                onClick = {
                                    archivedPeople.remove(person)
                                    activePeople.add(person)
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(text = "Restore", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = extendedColors.teal)
                            }
                        }
                    }
                }
            }

            Text(
                text = "Groups",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = extendedColors.muted,
                modifier = Modifier.padding(top = 8.dp)
            )

            groups.forEach { group ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, extendedColors.line, RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = group, fontWeight = FontWeight.Bold, color = extendedColors.navy)
                        Text(text = "›", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = extendedColors.muted)
                    }
                }
            }
        }
    }
}
