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
import com.juandiego.tactica.data.DEFAULT_ENROLLMENT
import com.juandiego.tactica.data.DEFAULT_MONTHLY
import com.juandiego.tactica.data.EventType
import com.juandiego.tactica.data.Player
import com.juandiego.tactica.data.TeamEvent
import com.juandiego.tactica.data.TeamInput
import com.juandiego.tactica.data.money
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
    onSubmit: (TeamInput) -> Unit,
    onClose: () -> Unit,
    onDelete: (() -> Unit)? = null,
) {
    var enrollment by remember { mutableStateOf((initial?.enrollmentFee ?: DEFAULT_ENROLLMENT).toString()) }
    var monthly by remember { mutableStateOf((initial?.monthlyFee ?: DEFAULT_MONTHLY).toString()) }
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
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MoneyField("Matrícula", enrollment, { enrollment = it }, Modifier.weight(1f))
            MoneyField("Mensualidad", monthly, { monthly = it }, Modifier.weight(1f))
        }
        if (initial != null) {
            Notice(
                "Si cambias la mensualidad, aplica a toda la plantilla. Una nueva matrícula solo aplica a quienes aún no la han pagado.",
                Slate100,
                Slate600,
            )
        }
        val enrollmentValue = enrollment.toIntOrNull() ?: 0
        val monthlyValue = monthly.toIntOrNull() ?: 0
        AppButton(
            submitLabel,
            onClick = {
                onSubmit(TeamInput(name.trim(), sport, category.trim(), logo, enrollmentValue, monthlyValue))
            },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            bg = Lime300,
            fg = Slate950,
            enabled = name.isNotBlank() && category.isNotBlank() && enrollmentValue > 0 && monthlyValue > 0,
        )
        if (onDelete != null) DeleteButton("Eliminar equipo", onDelete)
    }
}

@Composable
fun EventFormSheet(
    accent: Color,
    onSubmit: (EventType, String, String, String, String) -> Unit,
    onClose: () -> Unit,
    initial: TeamEvent? = null,
    onDelete: (() -> Unit)? = null,
) {
    var type by remember { mutableStateOf(initial?.type ?: EventType.ENTRENAMIENTO) }
    var title by remember { mutableStateOf(initial?.title.orEmpty()) }
    var date by remember { mutableStateOf(initial?.date.orEmpty()) }
    var time by remember { mutableStateOf(initial?.time.orEmpty()) }
    var location by remember { mutableStateOf(initial?.location.orEmpty()) }
    val valid = title.isNotBlank() && date.isNotBlank() && time.isNotBlank() && location.isNotBlank()

    Sheet(if (initial == null) "Nueva actividad" else "Editar actividad", onClose) {
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
            if (initial == null) "Guardar actividad" else "Guardar cambios",
            onClick = { onSubmit(type, title.trim(), date, time, location.trim()) },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            bg = accent,
            fg = Slate950,
            enabled = valid,
        )
        if (onDelete != null) DeleteButton("Eliminar actividad", onDelete)
    }
}

@Composable
fun PlayerFormSheet(
    sport: Sport,
    accent: Color,
    onSubmit: (String, Int, String) -> Unit,
    onClose: () -> Unit,
    takenNumbers: Set<Int> = emptySet(),
    initial: Player? = null,
    onDelete: (() -> Unit)? = null,
) {
    var name by remember { mutableStateOf(initial?.name.orEmpty()) }
    var number by remember { mutableStateOf(initial?.number?.toString().orEmpty()) }
    var position by remember { mutableStateOf(initial?.position.orEmpty()) }
    val numberValue = number.toIntOrNull()
    val duplicated = numberValue != null && numberValue in takenNumbers
    val valid = name.isNotBlank() && numberValue != null && !duplicated && position.isNotBlank()

    Sheet(if (initial == null) "Añadir jugador" else "Editar jugador", onClose) {
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
        if (duplicated) Notice("El dorsal $number ya lo tiene otro jugador del equipo.", Rose50, Rose700)
        AppButton(
            if (initial == null) "Añadir a la plantilla" else "Guardar cambios",
            onClick = { if (numberValue != null) onSubmit(name.trim(), numberValue, position.trim()) },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            bg = accent,
            fg = Slate950,
            enabled = valid,
        )
        if (onDelete != null) DeleteButton("Eliminar jugador", onDelete)
    }
}

/** Campo para valores en pesos: solo dígitos y muestra el valor formateado. */
@Composable
private fun MoneyField(label: String, value: String, onChange: (String) -> Unit, modifier: Modifier) {
    val formatted = value.toIntOrNull()?.let { money(it) } ?: "\$0"
    Field(
        "$label ($formatted)",
        value,
        { onChange(it.filter(Char::isDigit).take(9)) },
        modifier,
        placeholder = "60000",
        keyboardType = KeyboardType.Number,
    )
}

/** Botón rojo que pide un segundo toque para confirmar. */
@Composable
private fun DeleteButton(label: String, onDelete: () -> Unit) {
    var confirm by remember { mutableStateOf(false) }
    AppButton(
        if (confirm) "Toca otra vez para confirmar" else label,
        onClick = { if (confirm) onDelete() else confirm = true },
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        bg = Rose50,
        fg = Rose700,
        size = 13.sp,
    )
}
