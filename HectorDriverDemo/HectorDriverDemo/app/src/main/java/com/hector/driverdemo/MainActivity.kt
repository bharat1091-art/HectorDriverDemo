
package com.hector.driverdemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { Surface(Modifier.fillMaxSize().background(Color.Black)) {
            DashboardDemo()
        } } }
    }
}

data class LiveCan(
    val speedKmh: Int,
    val rpm: Int,
    val throttle: Int,
)

enum class Advice { UP, DOWN, NONE }

@Composable
fun DashboardDemo() {
    // Mock stream
    var can by remember { mutableStateOf(LiveCan(speedKmh = 58, rpm = 2200, throttle = 34)) }
    var gear by remember { mutableStateOf(4) }
    var idealGear by remember { mutableStateOf(5) }
    var advice by remember { mutableStateOf(Advice.UP) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(120)
            val spd = (can.speedKmh + Random.nextInt(-1, 2)).coerceIn(0, 140)
            val thr = (can.throttle + Random.nextInt(-3, 4)).coerceIn(0, 100)
            val rpmBase = (spd * 40) / (gear + 1)
            val rpmVar = Random.nextInt(-120, 160)
            val rpm = (rpmBase + rpmVar).coerceIn(700, 5500)
            can = LiveCan(spd, rpm, thr)

            // simple mock gear logic
            val rObs = if (spd > 1) rpm.toFloat() / (spd.toFloat() * 20f) else 3f
            val newGear = when {
                rObs > 5 -> 2
                rObs > 4 -> 3
                rObs > 3 -> 4
                rObs > 2 -> 5
                else -> 6
            }
            if (abs(newGear - gear) >= 1 && Random.nextFloat() > 0.4f) {
                gear = newGear
            }
            idealGear = (gear + if (rpm > 2200 && thr < 55) 1 else 0).coerceAtMost(6)
            advice = when {
                rpm > 2300 && thr < 55 && gear < 6 -> Advice.UP
                rpm < 1200 && thr > 45 -> Advice.DOWN
                else -> Advice.NONE
            }
        }
    }

    DashboardScreen(
        time = "12:45",
        dayDate = "Tue, 12 Mar",
        tripName = "Trip 1",
        tripDistance = "14.7 km",
        gear = gear,
        idealGear = if (idealGear != gear) idealGear else null,
        advice = advice,
        speed = can.speedKmh,
        rpm = can.rpm,
        throttle = can.throttle,
        inTemp = 24.5,
        outTemp = 30.2,
        reason = when (advice) {
            Advice.UP -> "Better torque"
            Advice.DOWN -> "Avoid lugging"
            Advice.NONE -> null
        }
    )
}

@Composable
fun DashboardScreen(
    time: String,
    dayDate: String,
    tripName: String,
    tripDistance: String,
    gear: Int,
    idealGear: Int?,
    advice: Advice,
    speed: Int,
    rpm: Int,
    throttle: Int,
    inTemp: Double?,
    outTemp: Double?,
    reason: String?,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Top time & date row
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(time, color = Color.White, fontSize = 44.sp, fontWeight = FontWeight.SemiBold)
            Text(dayDate, color = Color(0xFFDDDDDD), fontSize = 22.sp)
        }

        // Trip row
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.DirectionsCar, contentDescription = null, tint = Color(0xFFE7E2CC))
                Spacer(Modifier.width(10.dp))
                Text(tripName, color = Color(0xFFE7E2CC), fontSize = 24.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Flag, contentDescription = null, tint = Color(0xFFE7E2CC))
                Spacer(Modifier.width(10.dp))
                Text(tripDistance, color = Color(0xFFE7E2CC), fontSize = 24.sp)
            }
        }

        // Gear + ghost gear and advice
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            GearAnimated(gear)
            if (idealGear != null) {
                Text(" "+idealGear.toString(), color = Color(0x55FFFFFF), fontSize = 56.sp, modifier = Modifier.align(Alignment.CenterEnd))
            }
        }

        ShiftBanner(advice, reason)

        // Performance row
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            SpeedAnimated(speed)
            RpmBar(rpm, redline = 6000)
            Text("Throttle ${throttle}%", color = Color.White, fontSize = 22.sp)
        }

        Spacer(Modifier.height(6.dp))
        // Temperatures
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            inTemp?.let { Text("In: ${String.format("%.1f", it)}°C", color = Color(0xFF32D74B), fontSize = 28.sp) }
            outTemp?.let { Text("Out: ${String.format("%.1f", it)}°C", color = Color(0xFF32D74B), fontSize = 28.sp, textAlign = TextAlign.Right) }
        }
    }
}

@Composable
fun GearAnimated(currentGear: Int) {
    var shown by remember { mutableStateOf(currentGear) }
    val scale = remember { Animatable(1f) }
    val alpha = remember { Animatable(1f) }

    LaunchedEffect(currentGear) {
        if (currentGear != shown) {
            // pop-out old
            scale.animateTo(1.15f, tween(80, easing = FastOutSlowInEasing))
            alpha.animateTo(0f, tween(80))
            shown = currentGear
            scale.snapTo(0.8f)
            alpha.snapTo(0f)
            scale.animateTo(1f, tween(150, easing = FastOutSlowInEasing))
            alpha.animateTo(1f, tween(150))
        }
    }

    Text(
        text = shown.toString(),
        color = Color.White.copy(alpha = alpha.value),
        fontSize = 120.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.graphicsLayer(scaleX = scale.value, scaleY = scale.value)
    )
}

@Composable
fun ShiftBanner(advice: Advice, reason: String?) {
    if (advice == Advice.NONE) return
    val color = if (advice == Advice.UP) Color(0xFFFF9F0A) else Color(0xFFFF453A)
    val pulse = rememberInfiniteTransition().animateFloat(
        initialValue = 0.35f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(250, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = if (advice == Advice.UP) "▲ SHIFT UP" else "▼ SHIFT DOWN",
            color = color.copy(alpha = pulse.value),
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold
        )
        reason?.let { Text("($it)", color = color.copy(alpha = pulse.value * 0.95f), fontSize = 20.sp) }
    }
}

@Composable
fun SpeedAnimated(speed: Int) {
    AnimatedContent(targetState = speed, transitionSpec = {
        (slideInVertically { it/3 } + fadeIn(tween(120))) togetherWith
        (slideOutVertically { -it/3 } + fadeOut(tween(120)))
    }, label = "speed") {
        Text("${it} km/h", color = Color.White, fontSize = 26.sp)
    }
}

@Composable
fun RpmBar(rpm: Int, redline: Int) {
    val animRpm by animateIntAsState(rpm, tween(120), label = "rpm")
    val pct = animRpm.toFloat() / redline
    val color = when {
        pct > 0.90f -> Color(0xFFFF5A52)
        pct > 0.70f -> Color(0xFFFFD369)
        else -> Color.White
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("RPM", color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp)
        Box(Modifier.width(150.dp).height(6.dp).background(Color.White.copy(alpha = 0.12f))) {
            Box(Modifier.fillMaxHeight().width((150 * pct.coerceIn(0f,1f)).dp).background(color.copy(alpha = 0.9f)))
        }
        Text("$animRpm", color = color, fontSize = 22.sp)
    }
}
