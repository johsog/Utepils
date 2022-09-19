package no.uio.utepils.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DarkColorPalette = darkColors(
    primary = Color(0xFFF8B550),
    primaryVariant = Color(0xFFFF9800),
    secondary = Color(0xFF2196F3),
    secondaryVariant = Color(0xFF03A9F4)
)

private val LightColorPalette = lightColors(
    primary = Color(0xFFF8B550),
    primaryVariant = Color(0xFFFF9800),
    secondary = Color(0xFF2196F3),
    secondaryVariant = Color(0xFF03A9F4)
)

val Colors.accentColor: Color
    get() = Color(0xFFF8B550)

val Colors.primaryLabel: Color
    get() = if (isLight) Color.Black else Color.White

val Colors.secondaryLabel: Color
    get() = Color(0xFF969696)

val Colors.tetiaryLabel: Color
    get() = if (isLight)
        Color(0xFFDADADA)
    else Color(0xFF3C3C3C)

val Colors.secondaryBackground: Color
    get() = if (isLight)
        Color(0xFFF4F4F4)
    else Color(0xFF282828)

@Composable
fun UtepilsTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    val type = Typography(
        h1 = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = 34.sp,
            color = colors.primaryLabel
        ),
        h2 = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.SemiBold,
            fontSize = 28.sp,
            color = colors.primaryLabel
        ),
        h3 = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = colors.primaryLabel
        ),
        h4 = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            color = colors.primaryLabel
        ),
        h5 = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = colors.primaryLabel
        ),
        h6 = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            color = colors.primaryLabel
        ),
        subtitle1 = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = colors.secondaryLabel
        ),
        subtitle2 = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            color = colors.secondaryLabel
        ),
        body1 = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = colors.primaryLabel
        ),
        body2 = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            color = colors.primaryLabel
        ),
        caption = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = colors.secondaryLabel,
            fontFeatureSettings = "c2sc, smcp"
        )
    )

    MaterialTheme(
        colors = colors,
        typography = type,
        shapes = Shapes,
        content = content
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun DefaultPreview() {
    UtepilsTheme(darkTheme = true) {
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            Column {
                Text("Headline 1, Accent Color",
                    style = MaterialTheme.typography.h1,
                    modifier = Modifier
                        .background(MaterialTheme.colors.accentColor)
                        .fillMaxWidth()
                        .padding(8.dp)
                )
                Text("Headline 2, Secondary Background",
                    style = MaterialTheme.typography.h2,
                    modifier = Modifier
                        .background(MaterialTheme.colors.secondaryBackground)
                        .fillMaxWidth()
                        .padding(8.dp)
                )
                Text("Headline 3",
                    style = MaterialTheme.typography.h3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
                Text("Headline 4",
                    style = MaterialTheme.typography.h4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
                Text("Headline 5",
                    style = MaterialTheme.typography.h5,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
                Text("Headline 6",
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
                Text("Subtitle 1",
                    style = MaterialTheme.typography.subtitle1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
                Text("Subtitle 2",
                    style = MaterialTheme.typography.subtitle2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
                Text("Body 1: State in an app is any value that can change over time. This is a very broad definition and encompasses everything from a Room database to a variable on a class.",
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
                Text("Body 2: State in an app is any value that can change over time. This is a very broad definition and encompasses everything from a Room database to a variable on a class.",
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
                Text("This is a caption",
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
                Box(modifier = Modifier.padding(horizontal = 8.dp)) {
                    Text("Søk etter sted",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(CircleShape)
                            .background(MaterialTheme.colors.secondaryBackground)
                            .padding(horizontal = 16.dp)
                            .padding(vertical = 12.dp)
                        ,
                        style = MaterialTheme.typography.subtitle1,
                    )
                }
            }
        }
    }
}