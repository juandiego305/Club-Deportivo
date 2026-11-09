package com.juandiego.tactica.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.net.Uri
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.SportsVolleyball
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.juandiego.tactica.data.EventType
import com.juandiego.tactica.data.PaymentStatus
import com.juandiego.tactica.data.SPANISH
import com.juandiego.tactica.data.Sport
import com.juandiego.tactica.data.TeamEvent
import com.juandiego.tactica.ui.theme.*
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// ─── Formas y modificadores ──────────────────────────────────────────────────

val R8 = RoundedCornerShape(8.dp)
val R12 = RoundedCornerShape(12.dp)
val R16 = RoundedCornerShape(16.dp)
val R24 = RoundedCornerShape(24.dp)

/** Tarjeta blanca con borde gris (equivale a "rounded-2xl border border-slate-200 bg-white"). */
fun Modifier.card(shape: Shape = R16, bg: Color = White, border: Color = Slate200): Modifier =
    this.clip(shape).background(bg, shape).border(1.dp, border, shape)

// ─── Iconos ──────────────────────────────────────────────────────────────────

object Ic {
    val home = Icons.Outlined.Home
    val ball = Icons.Outlined.SportsSoccer
    val volley = Icons.Outlined.SportsVolleyball
    val calendar = Icons.Outlined.CalendarMonth
    val team = Icons.Outlined.Groups
    val chart = Icons.Outlined.BarChart
    val plus = Icons.Outlined.Add
    val shield = Icons.Outlined.Shield
    val close = Icons.Outlined.Close
    val logout = Icons.AutoMirrored.Outlined.Logout
    val trash = Icons.Outlined.Delete
    val check = Icons.Outlined.Check
    val chevron = Icons.AutoMirrored.Outlined.KeyboardArrowRight
    val chevronDown = Icons.Outlined.KeyboardArrowDown
    val edit = Icons.Outlined.Edit
    val image = Icons.Outlined.Image
    val card = Icons.Outlined.CreditCard
    val bell = Icons.Outlined.Notifications
}

fun sportIcon(sport: Sport): ImageVector = if (sport == Sport.FUTBOL) Ic.ball else Ic.volley

// ─── Texto y botones ─────────────────────────────────────────────────────────

@Composable
fun Txt(
    text: String,
    modifier: Modifier = Modifier,
    size: TextUnit = 14.sp,
    weight: FontWeight = FontWeight.Normal,
    color: Color = Slate950,
    align: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    lineHeight: TextUnit = TextUnit.Unspecified,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    family: FontFamily? = null,
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = size,
        fontWeight = weight,
        textAlign = align,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        lineHeight = lineHeight,
        letterSpacing = letterSpacing,
        fontFamily = family,
    )
}

@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    bg: Color = Slate950,
    fg: Color = White,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    border: Color? = null,
    vertical: Dp = 14.dp,
    size: TextUnit = 14.sp,
    shape: Shape = R12,
) {
    val background = if (enabled) bg else Slate200
    val content = if (enabled) fg else Slate400
    Row(
        modifier
            .clip(shape)
            .background(background, shape)
            .then(if (border != null) Modifier.border(1.dp, border, shape) else Modifier)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = vertical),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, null, Modifier.size(18.dp), tint = content)
            Spacer(Modifier.width(8.dp))
        }
        Txt(text, size = size, weight = FontWeight.Black, color = content, align = TextAlign.Center)
        if (trailingIcon != null) {
            Spacer(Modifier.width(6.dp))
            Icon(trailingIcon, null, Modifier.size(16.dp), tint = content)
        }
    }
}

@Composable
fun CircleIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    bg: Color = Slate100,
    tint: Color = Slate950,
    size: Dp = 36.dp,
    iconSize: Dp = 20.dp,
    contentDescription: String? = null,
) {
    Box(
        modifier.size(size).clip(CircleShape).background(bg).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Icon(icon, contentDescription, Modifier.size(iconSize), tint = tint) }
}

@Composable
fun Chip(text: String, bg: Color, fg: Color, modifier: Modifier = Modifier, icon: ImageVector? = null, onClick: (() -> Unit)? = null) {
    Row(
        modifier
            .clip(CircleShape)
            .background(bg)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, null, Modifier.size(16.dp), tint = fg)
            Spacer(Modifier.width(6.dp))
        }
        Txt(text, size = 12.sp, weight = FontWeight.Black, color = fg, maxLines = 1)
    }
}

// ─── Identidad visual ────────────────────────────────────────────────────────

@Composable
fun AppLogo(onDark: Boolean, modifier: Modifier = Modifier, size: Dp = 40.dp, subtitle: String? = null) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(size).clip(R12).background(if (onDark) Lime300 else Slate950),
            contentAlignment = Alignment.Center,
        ) { Icon(Ic.shield, null, Modifier.size(20.dp), tint = if (onDark) Slate950 else Lime300) }
        Spacer(Modifier.width(8.dp))
        Column {
            Txt(
                "Janua",
                size = if (subtitle != null) 18.sp else 20.sp,
                weight = FontWeight.ExtraBold,
                color = if (onDark) White else Slate950,
            )
            if (subtitle != null) Txt(subtitle, size = 12.sp, weight = FontWeight.Bold, color = Slate400)
        }
    }
}

@Composable
fun TeamBadge(sport: Sport, logo: String?, dark: Boolean = false, size: Dp = 44.dp) {
    val bitmap = rememberLogoBitmap(logo)
    Box(
        Modifier.size(size).clip(CircleShape).background(if (dark) White10 else sport.badgeBg),
        contentAlignment = Alignment.Center,
    ) {
        if (bitmap != null) {
            Image(bitmap, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        } else {
            Icon(sportIcon(sport), null, Modifier.size(size * 0.55f), tint = if (dark) White else Slate950)
        }
    }
}

@Composable
fun NumberCircle(number: Int, bg: Color, fg: Color = Slate950, size: Dp = 44.dp) {
    Box(Modifier.size(size).clip(CircleShape).background(bg), contentAlignment = Alignment.Center) {
        Txt("$number", size = 14.sp, weight = FontWeight.Black, color = fg)
    }
}

@Composable
fun PulsingDot(color: Color, size: Dp = 12.dp) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "pulseAlpha",
    )
    Box(Modifier.size(size).alpha(alpha).clip(CircleShape).background(color))
}

// ─── Pagos ───────────────────────────────────────────────────────────────────

data class StatusStyle(val label: String, val short: String, val bg: Color, val text: Color, val dot: Color, val border: Color)

fun PaymentStatus.style(): StatusStyle = when (this) {
    PaymentStatus.AL_DIA -> StatusStyle("Al día", "Al día", Emerald50, Emerald700, Emerald500, Emerald200)
    PaymentStatus.PROXIMO -> StatusStyle("Próximo a pagar", "Próximo", Amber50, Amber700, Amber500, Amber200)
    PaymentStatus.VENCIDO -> StatusStyle("Vencido", "Vencido", Rose50, Rose700, Rose500, Rose200)
    PaymentStatus.PENDIENTE -> StatusStyle("Pendiente", "Pendiente", Slate100, Slate600, Slate400, Slate200)
}

@Composable
fun PaymentBadge(status: PaymentStatus, large: Boolean = false) {
    val s = status.style()
    Row(
        Modifier
            .clip(CircleShape)
            .background(s.bg)
            .padding(horizontal = if (large) 12.dp else 8.dp, vertical = if (large) 6.dp else 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(6.dp).clip(CircleShape).background(s.dot))
        Spacer(Modifier.width(5.dp))
        Txt(s.label, size = 12.sp, weight = FontWeight.Black, color = s.text, maxLines = 1)
    }
}

// ─── Bloques reutilizables ───────────────────────────────────────────────────

@Composable
fun Empty(text: String) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(R16)
            .background(White)
            .dashedBorder(Slate300, 16.dp)
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) { Txt(text, size = 14.sp, color = Slate500, align = TextAlign.Center) }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier, trailing: (@Composable () -> Unit)? = null) {
    Row(modifier.fillMaxWidth().padding(bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Txt(text, Modifier.weight(1f), size = 18.sp, weight = FontWeight.Black)
        trailing?.invoke()
    }
}

@Composable
fun StatCell(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier.padding(horizontal = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Txt(value, size = 24.sp, weight = FontWeight.Black)
        Txt(label, size = 12.sp, color = Slate500, maxLines = 1)
    }
}

@Composable
fun StatTile(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier.card().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Txt(value, size = 24.sp, weight = FontWeight.Black)
        Spacer(Modifier.height(4.dp))
        Txt(label, size = 12.sp, weight = FontWeight.Bold, color = Slate500, maxLines = 1)
    }
}

@Composable
fun EventRow(event: TeamEvent, onClick: (() -> Unit)? = null, onDelete: (() -> Unit)? = null) {
    val date = runCatching { LocalDate.parse(event.date) }.getOrNull()
    val month = date?.format(DateTimeFormatter.ofPattern("MMM", SPANISH))?.replace(".", "")?.uppercase().orEmpty()
    Row(
        Modifier
            .fillMaxWidth()
            .card()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(48.dp).clip(R12).background(if (event.type == EventType.PARTIDO) Lime100 else Slate100),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Txt(date?.dayOfMonth?.toString() ?: "--", size = 16.sp, weight = FontWeight.Black, lineHeight = 17.sp)
                Txt(month, size = 11.sp, weight = FontWeight.ExtraBold, color = Slate500, lineHeight = 12.sp)
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Txt(event.title, size = 14.sp, weight = FontWeight.ExtraBold, maxLines = 1)
            Spacer(Modifier.height(4.dp))
            Txt("${event.time} · ${event.location}", size = 12.sp, color = Slate500, maxLines = 1)
        }
        if (onDelete != null) {
            CircleIconButton(Ic.trash, onDelete, bg = Color.Transparent, tint = Slate400, iconSize = 18.dp, contentDescription = "Eliminar actividad")
        }
    }
}

/** Control segmentado (pestañas tipo "Entrar / Crear cuenta"). */
@Composable
fun <T> Segmented(
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    bg: Color = Slate100,
    size: TextUnit = 14.sp,
) {
    Row(modifier.fillMaxWidth().clip(R12).background(bg).padding(4.dp)) {
        options.forEach { (value, label) ->
            val isSelected = value == selected
            Box(
                Modifier
                    .weight(1f)
                    .shadow(if (isSelected) 1.dp else 0.dp, R8)
                    .clip(R8)
                    .background(if (isSelected) White else Color.Transparent)
                    .clickable { onSelect(value) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Txt(label, size = size, weight = FontWeight.Black, color = if (isSelected) Slate950 else Slate500, maxLines = 1)
            }
        }
    }
}

/** Cuadrícula simple de N columnas que no necesita LazyGrid (útil dentro de un scroll). */
@Composable
fun <T> Grid(
    items: List<T>,
    columns: Int,
    modifier: Modifier = Modifier,
    spacing: Dp = 8.dp,
    cell: @Composable (T) -> Unit,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(spacing)) {
        items.chunked(columns).forEach { row ->
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(spacing)) {
                row.forEach { item -> Box(Modifier.weight(1f).fillMaxHeight()) { cell(item) } }
                repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

// ─── Imágenes (escudo del equipo) ────────────────────────────────────────────

/** Decodifica el escudo guardado en Base64. */
@Composable
fun rememberLogoBitmap(base64: String?): ImageBitmap? = remember(base64) {
    if (base64.isNullOrEmpty()) null
    else runCatching {
        val bytes = Base64.decode(base64, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
    }.getOrNull()
}

/**
 * Convierte una imagen elegida en la galería en un JPEG cuadrado de [size] px
 * codificado en Base64 (unos 10–20 KB), para guardarla directamente en Firestore.
 */
fun imageToBase64(context: Context, uri: Uri, size: Int = 256): String? = runCatching {
    val resolver = context.contentResolver
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
    var sample = 1
    while (bounds.outWidth / (sample * 2) >= size && bounds.outHeight / (sample * 2) >= size) sample *= 2
    val decoded = resolver.openInputStream(uri)?.use {
        BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
    } ?: return@runCatching null
    val side = minOf(decoded.width, decoded.height)
    val square = Bitmap.createBitmap(decoded, (decoded.width - side) / 2, (decoded.height - side) / 2, side, side)
    val scaled = Bitmap.createScaledBitmap(square, size, size, true)
    val out = ByteArrayOutputStream()
    scaled.compress(Bitmap.CompressFormat.JPEG, 80, out)
    Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
}.getOrNull()
