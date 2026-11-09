package com.juandiego.tactica.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.juandiego.tactica.ui.theme.*
import java.time.Instant
import java.time.ZoneOffset

fun Modifier.dashedBorder(color: Color, radius: Dp): Modifier = this.drawBehind {
    val stroke = Stroke(
        width = 1.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx())),
    )
    drawRoundRect(color = color, style = stroke, cornerRadius = CornerRadius(radius.toPx()))
}

@Composable
private fun FieldLabel(label: String, dark: Boolean) {
    Txt(
        label,
        Modifier.padding(bottom = 6.dp),
        size = 12.sp,
        weight = FontWeight.Bold,
        color = if (dark) Slate400 else Slate600,
    )
}

/** Campo de texto con etiqueta encima, como en el diseño. */
@Composable
fun Field(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    password: Boolean = false,
    dark: Boolean = false,
) {
    Column(modifier.padding(bottom = 16.dp)) {
        FieldLabel(label, dark)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = if (dark) Slate500 else Slate400, fontSize = 14.sp) },
            singleLine = true,
            shape = R12,
            textStyle = TextStyle(fontSize = 14.sp, color = if (dark) White else Slate950),
            visualTransformation = if (password) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = if (password) KeyboardType.Password else keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (dark) White30 else Slate950,
                unfocusedBorderColor = if (dark) White10 else Slate200,
                focusedContainerColor = if (dark) White10 else White,
                unfocusedContainerColor = if (dark) White10 else White,
                cursorColor = if (dark) White else Slate950,
            ),
        )
    }
}

/** Caja clicable con aspecto de campo (para selects, fecha y hora). */
@Composable
fun PickerBox(label: String?, value: String, onClick: () -> Unit, modifier: Modifier = Modifier, placeholder: Boolean = false) {
    Column(modifier.padding(bottom = 16.dp)) {
        if (label != null) FieldLabel(label, false)
        Row(
            Modifier
                .fillMaxWidth()
                .card(R12)
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Txt(value, Modifier.weight(1f), size = 14.sp, color = if (placeholder) Slate400 else Slate950, maxLines = 1)
            Icon(Ic.chevronDown, null, Modifier.size(18.dp), tint = Slate400)
        }
    }
}

@Composable
fun SelectField(
    label: String?,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        PickerBox(label, selected.ifEmpty { "Seleccionar" }, { expanded = true }, placeholder = selected.isEmpty())
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, fontSize = 14.sp) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateField(label: String, value: String, onChange: (String) -> Unit, modifier: Modifier = Modifier) {
    var show by remember { mutableStateOf(false) }
    PickerBox(label, value.ifEmpty { "AAAA-MM-DD" }, { show = true }, modifier, placeholder = value.isEmpty())
    if (show) {
        val state = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { show = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        onChange(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate().toString())
                    }
                    show = false
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { show = false }) { Text("Cancelar") } },
        ) { DatePicker(state = state) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeField(label: String, value: String, onChange: (String) -> Unit, modifier: Modifier = Modifier) {
    var show by remember { mutableStateOf(false) }
    PickerBox(label, value.ifEmpty { "HH:MM" }, { show = true }, modifier, placeholder = value.isEmpty())
    if (show) {
        val state = rememberTimePickerState(is24Hour = true)
        AlertDialog(
            onDismissRequest = { show = false },
            confirmButton = {
                TextButton(onClick = {
                    onChange("%02d:%02d".format(state.hour, state.minute))
                    show = false
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { show = false }) { Text("Cancelar") } },
            text = { TimePicker(state = state) },
        )
    }
}

/** Hoja inferior con título y botón de cerrar (el "Dialog" del diseño). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Sheet(title: String, onClose: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = state,
        containerColor = White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = null,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
                .navigationBarsPadding()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Txt(title, Modifier.weight(1f), size = 20.sp, weight = FontWeight.Black)
                CircleIconButton(Ic.close, onClose, contentDescription = "Cerrar")
            }
            Spacer(Modifier.height(20.dp))
            content()
        }
    }
}

/** Aviso de color (ámbar, rosa, azul...) usado en formularios y alertas. */
@Composable
fun Notice(text: String, bg: Color, fg: Color, modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .clip(R12)
            .background(bg)
            .padding(12.dp)
    ) { Txt(text, size = 12.sp, weight = FontWeight.SemiBold, color = fg, lineHeight = 17.sp) }
}
