package com.juandiego.tactica.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.juandiego.tactica.data.EventType
import com.juandiego.tactica.data.Sport
import com.juandiego.tactica.data.Team
import com.juandiego.tactica.ui.components.*
import com.juandiego.tactica.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Crear o editar un equipo (con escudo opcional desde la galería). */
@Composable
fun TeamFormSheet(
    title: String,
    initial: Team?,
    submitLabel: String,
    onSubmit: (name: String, sport: Sport, category: String, logo: String?) -> Unit,
    onClose: () -> Unit,
    onDelete: (() -> Unit)? = null,
) {
    var confirmDelete by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var logo by remember { mutableStateOf(initial?.logo) }
    var name by remember { mutableStateOf(initial?.name.orEmpty()) }
    var category by remember { mutableStateOf(initial?.category.orEmpty()) }
    var sport by remember { mutableStateOf(initial?.sport ?: Sport.FUTBOL) }

    val scope = rememberCoroutineScope()
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            scope.launch {
                val encoded = withContext(Dispatchers.Default) { imageToBase64(context, uri) }
                if (encoded != null) logo = encoded
            }
        }
    }

    Sheet(title, onClose) {
        Row(
            Modifier.fillMaxWidth().clip(R16).background(Slate50).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val bitmap = rememberLogoBitmap(logo)
            Box(
                Modifier.size(80.dp).clip(R16).background(White).border(1.dp, Slate200, R16),
                contentAlignment = Alignment.Center,
            ) {
                if (bitmap != null) {
                    Image(bitmap, "Vista previa del escudo", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Icon(Ic.image, null, Modifier.size(28.dp), tint = Slate400)
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Txt("Escudo del equipo", size = 14.sp, weight = FontWeight.Black)
                Txt(
                    "Sube una imagen cuadrada para que se vea bien en el marcador.",
                    Modifier.padding(top = 4.dp),
                    size = 12.sp,
                    color = Slate500,
                    lineHeight = 17.sp,
                )
                Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    AppButton(
                        if (logo != null) "Cambiar escudo" else "Subir escudo",
                        onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        vertical = 8.dp,
                        size = 12.sp,
                        shape = R8,
                    )
                    if (logo != null) {
                        Txt(
                            "Quitar",
                            Modifier.clip(R8).clickable { logo = null }.padding(horizontal = 12.dp, vertical = 8.dp),
                            size = 12.sp,
                            weight = FontWeight.Bold,
                            color = Rose600,
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(20.dp))

        if (initial != null) {
            Txt("Deporte", Modifier.padding(bottom = 6.dp), size = 12.sp, weight = FontWeight.Bold, color = Slate600)
            Txt(
                initial.sport.label,
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .card(R12, bg = Slate100)
                    .padding(14.dp),
                size = 14.sp,
                weight = FontWeight.Bold,
                color = Slate500,
            )
        } else {
            SelectField("Deporte", Sport.entries.map { it.label }, sport.label, { label ->
                sport = Sport.entries.first { it.label == label }
            })
        }
        Field("Nombre del equipo", name, { name = it }, placeholder = "Ej. Atlético Norte")
        Field("Categoría", category, { category = it }, placeholder = "Ej. Juvenil femenino")
        AppButton(
            submitLabel,
            onClick = { onSubmit(name.trim(), sport, category.trim(), logo) },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            bg = Lime300,
            fg = Slate950,
            enabled = name.isNotBlank() && category.isNotBlank(),
        )
        if (onDelete != null) {
            AppButton(
                if (confirmDelete) "Toca otra vez para eliminar definitivamente" else "Eliminar equipo",
                onClick = { if (confirmDelete) onDelete() else confirmDelete = true },
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                bg = Rose50,
                fg = Rose700,
                size = 13.sp,
            )
        }
    }
}

@Composable
fun EventFormSheet(
    accent: Color,
    onSubmit: (EventType, String, String, String, String) -> Unit,
    onClose: () -> Unit,
) {
    var type by remember { mutableStateOf(EventType.ENTRENAMIENTO) }
    var title by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    val valid = title.isNotBlank() && date.isNotBlank() && time.isNotBlank() && location.isNotBlank()

    Sheet("Nueva actividad", onClose) {
        SelectField("Tipo", EventType.entries.map { it.label }, type.label, { label ->
            type = EventType.entries.first { it.label == label }
        })
        Field("Título", title, { title = it }, placeholder = "Ej. Entrenamiento táctico")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DateField("Fecha", date, { date = it }, Modifier.weight(1f))
            TimeField("Hora", time, { time = it }, Modifier.weight(1f))
        }
        Field("Lugar", location, { location = it }, placeholder = "Campo o pabellón")
        AppButton(
            "Guardar actividad",
            onClick = { onSubmit(type, title.trim(), date, time, location.trim()) },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            bg = accent,
            fg = Slate950,
            enabled = valid,
        )
    }
}

@Composable
fun PlayerFormSheet(
    sport: Sport,
    accent: Color,
    onSubmit: (String, Int, String) -> Unit,
    onClose: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }
    var position by remember { mutableStateOf("") }
    val valid = name.isNotBlank() && number.toIntOrNull() != null && position.isNotBlank()

    Sheet("Añadir jugador", onClose) {
        Field("Nombre completo", name, { name = it }, placeholder = "Nombre del jugador")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Field(
                "Dorsal", number, { number = it.filter(Char::isDigit).take(3) },
                Modifier.weight(1f), placeholder = "10", keyboardType = KeyboardType.Number,
            )
            Field(
                "Posición", position, { position = it },
                Modifier.weight(1f), placeholder = if (sport == Sport.FUTBOL) "Delantero" else "Colocadora",
            )
        }
        AppButton(
            "Añadir a la plantilla",
            onClick = { onSubmit(name.trim(), number.toInt(), position.trim()) },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            bg = accent,
            fg = Slate950,
            enabled = valid,
        )
    }
}
