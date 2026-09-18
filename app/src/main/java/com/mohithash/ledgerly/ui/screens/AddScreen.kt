@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class)

package com.mohithash.ledgerly.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mohithash.ledgerly.domain.CATEGORIES
import com.mohithash.ledgerly.domain.LineItem
import com.mohithash.ledgerly.ui.AppViewModel
import com.mohithash.ledgerly.ui.Job
import com.mohithash.ledgerly.ui.Label
import com.mohithash.ledgerly.ui.MealPhoto
import com.mohithash.ledgerly.ui.Photo
import com.mohithash.ledgerly.ui.StatCard
import com.mohithash.ledgerly.ui.money
import java.time.LocalDate

@Composable
fun AddScreen(vm: AppViewModel, onBack: () -> Unit) {
    val ai by vm.ai.collectAsState()
    val s by vm.settings.collectAsState()
    val parse by vm.parse.collectAsState()
    val cs = MaterialTheme.colorScheme
    var quick by remember { mutableStateOf("") }
    var merchant by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(LocalDate.now().toString()) }
    var category by remember { mutableStateOf("Other") }
    var note by remember { mutableStateOf("") }
    var items by remember { mutableStateOf(listOf<LineItem>()) }
    var photo by remember { mutableStateOf<MealPhoto?>(null) }
    val ctx = LocalContext.current
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { b -> b?.let { photo = Photo.fromBitmap(it) } }
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { u -> u?.let { photo = Photo.fromUri(ctx, it) } }

    // When the AI finishes, prefill the form so the user can correct before saving.
    LaunchedEffect(parse) {
        (parse as? Job.Done)?.value?.let { p ->
            merchant = p.merchant; amount = if (p.total > 0) p.total.toString() else amount; date = p.date.ifBlank { date }
            category = if (p.category in CATEGORIES) p.category else "Other"; note = p.note; items = p.items
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Add expense") }, colors = TopAppBarDefaults.topAppBarColors(containerColor = cs.surface), navigationIcon = { IconButton({ vm.clearParse(); onBack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }) }) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(container = cs.secondaryContainer) {
                Label("Snap a receipt or type it", cs.onSecondaryContainer)
                photo?.let { p ->
                    Box(Modifier.fillMaxWidth()) {
                        Image(p.bitmap.asImageBitmap(), null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().height(220.dp).clip(MaterialTheme.shapes.large))
                        FilledTonalIconButton({ photo = null }, Modifier.align(Alignment.TopEnd).padding(8.dp)) { Icon(Icons.Default.Close, null) }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton({ camera.launch(null) }, shapes = ButtonDefaults.shapes(), modifier = Modifier.weight(1f)) { Icon(Icons.Default.PhotoCamera, null, Modifier.size(18.dp)); Spacer(Modifier.size(6.dp)); Text("Camera") }
                    OutlinedButton({ gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, shapes = ButtonDefaults.shapes(), modifier = Modifier.weight(1f)) { Icon(Icons.Default.PhotoLibrary, null, Modifier.size(18.dp)); Spacer(Modifier.size(6.dp)); Text("Gallery") }
                }
                OutlinedTextField(quick, { quick = it }, placeholder = { Text("e.g. “uber to airport 32” or leave blank with a photo") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large)
                when (parse) {
                    Job.Loading -> Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) { LoadingIndicator(); Spacer(Modifier.size(10.dp)); Text("Reading…") }
                    is Job.Failed -> Text((parse as Job.Failed).message, color = cs.error)
                    else -> {}
                }
                Button({ vm.parseExpense(photo?.base64, quick) }, enabled = ai.configured && (photo != null || quick.isNotBlank()) && parse != Job.Loading, shapes = ButtonDefaults.shapes(), modifier = Modifier.fillMaxWidth().height(50.dp)) {
                    Icon(Icons.Default.AutoAwesome, null); Spacer(Modifier.size(8.dp)); Text("Extract with AI")
                }
                if (!ai.configured) Text("Add an API key in Settings to use extraction. Manual entry works without it.", style = MaterialTheme.typography.bodySmall, color = cs.onSecondaryContainer)
            }
            StatCard {
                Label("Details")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(merchant, { merchant = it }, label = { Text("Merchant") }, singleLine = true, modifier = Modifier.weight(2f), shape = MaterialTheme.shapes.large)
                    OutlinedTextField(amount, { amount = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Amount") }, prefix = { Text(s.currency) }, singleLine = true, modifier = Modifier.weight(1f), shape = MaterialTheme.shapes.large, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                }
                OutlinedTextField(date, { date = it }, label = { Text("Date (yyyy-mm-dd)") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) { CATEGORIES.forEach { c -> FilterChip(selected = category == c, onClick = { category = c }, label = { Text(c) }) } }
                OutlinedTextField(note, { note = it }, label = { Text("Note") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large)
                if (items.isNotEmpty()) {
                    Label("Line items")
                    items.forEach { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(it.name, Modifier.weight(1f)); Text(it.price.money(s.currency)) } }
                }
            }
            Button(
                onClick = { vm.add(date, merchant, amount.toDoubleOrNull() ?: 0.0, category, note, items); onBack() },
                enabled = (amount.toDoubleOrNull() ?: 0.0) > 0, shapes = ButtonDefaults.shapes(), modifier = Modifier.fillMaxWidth().height(56.dp),
            ) { Text("Save expense", style = MaterialTheme.typography.titleMedium) }
            Spacer(Modifier.height(24.dp))
        }
    }
}
