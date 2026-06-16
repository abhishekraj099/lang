# LinguaPath Color Palette - Quick Reference

## Hex Color Codes (Copy-Paste Ready)

```
JAPANESE INDIGO (藍色)
├── #0F2942  - Deep Indigo (backgrounds)
├── #1B3A52  - Medium Indigo (surfaces)
├── #264653  - Rich Indigo (containers)
└── #3A5A7C  - Light Indigo (borders)

TEMPLE GOLD (金色)
├── #FFD700  - Bright Gold (primary buttons, highlights)
├── #FFC107  - Temple Gold (secondary actions)
├── #CC9500  - Dark Gold (hover states)
└── #FFE082  - Light Gold (tertiary accents)

SUPPORTING
├── #FFFFFF  - White (text on dark)
├── #B0C4D4  - Muted Text (secondary)
├── #FF6B6B  - Error Red
└── #51CF66  - Success Green
```

## Jetpack Compose Color Objects

```kotlin
// Import in your Compose files
import androidx.compose.ui.graphics.Color

// Main Colors
val JapaneseIndigoDark = Color(0xFF0F2942)
val JapaneseIndigo = Color(0xFF1B3A52)
val JapaneseIndigoMedium = Color(0xFF264653)
val JapaneseIndigoLight = Color(0xFF3A5A7C)

val TempleGoldBright = Color(0xFFFFD700)
val TempleGold = Color(0xFFFFC107)
val TempleGoldDark = Color(0xFFCC9500)
val TempleGoldAccent = Color(0xFFFFE082)

// Usage Example
Button(
    onClick = { },
    colors = ButtonDefaults.buttonColors(
        containerColor = TempleGoldBright,
        contentColor = JapaneseIndigoDark
    )
) {
    Text("Action")
}
```

## Material Design 3 Mapping

```kotlin
DarkColorScheme(
    primary = TempleGoldBright,           // #FFD700 - Main accent
    onPrimary = JapaneseIndigoDark,       // #0F2942 - Text on gold
    secondary = TempleGold,                // #FFC107 - Secondary accent
    onSecondary = JapaneseIndigoDark,     // #0F2942 - Text on secondary
    tertiary = TempleGoldAccent,           // #FFE082 - Tertiary accent
    onTertiary = JapaneseIndigoDark,      // #0F2942 - Text on tertiary
    
    background = JapaneseIndigoDark,      // #0F2942 - App background
    onBackground = Color.White,            // #FFFFFF - Text on background
    surface = JapaneseIndigo,              // #1B3A52 - Card backgrounds
    onSurface = Color.White,               // #FFFFFF - Text on surface
    surfaceContainer = JapaneseIndigoMedium, // #264653 - Surface container
    onSurfaceVariant = Color(0xFFB0C4D4), // Muted text
    
    error = Color(0xFFFF6B6B),            // Error state
    onError = Color.White,                // Text on error
    outline = JapaneseIndigoLight,        // #3A5A7C - Borders
    outlineVariant = Color(0xFF5A7A8D)    // Muted borders
)
```

## CSS/Web Colors

```css
--indigo-dark: #0F2942;
--indigo-medium: #1B3A52;
--indigo-rich: #264653;
--indigo-light: #3A5A7C;

--gold-bright: #FFD700;
--gold-main: #FFC107;
--gold-dark: #CC9500;
--gold-light: #FFE082;

--text-muted: #B0C4D4;
--error: #FF6B6B;
--success: #51CF66;
```

## Color Psychology & Usage

### Japanese Indigo - Where to Use
- ✓ App backgrounds (creates calm, professional atmosphere)
- ✓ Card/container backgrounds (hierarchy and separation)
- ✓ Text (white on dark for high contrast)
- ✓ Borders and outlines (subtle visual structure)
- ✓ Navigation elements (top/bottom bars)

### Temple Gold - Where to Use
- ✓ Primary action buttons (draws attention)
- ✓ Active/selected states (visual feedback)
- ✓ Progress indicators (shows completion)
- ✓ Important highlights (warnings, achievements)
- ✓ Links and interactive elements (clickable affordance)

### Contrast Examples
- White text on Indigo: AAA Accessibility Rating ✓
- Indigo text on Gold: AAA Accessibility Rating ✓
- Gold on Indigo: High contrast, visually striking ✓

## Files Using These Colors

All these files have been updated to use this palette:

1. `app/src/main/res/values/colors.xml` - XML color resources
2. `app/src/main/java/com/example/lang/ui/theme/Color.kt` - Compose color definitions
3. `app/src/main/java/com/example/lang/ui/theme/Theme.kt` - Material 3 theme scheme

## Integration Notes

- ✓ No additional dependencies needed
- ✓ Jetpack Compose automatic color application
- ✓ Accessible WCAG AAA contrast ratios
- ✓ Supports light/dark theme switching
- ✓ Dynamic colors disabled to preserve brand palette
- ✓ All Material 3 components work with this palette

## Testing the Colors

```kotlin
// In Compose Preview or your main activity, you'll see:
// - Deep indigo backgrounds
// - Gold buttons that stand out
// - White text with excellent readability
// - Muted secondary text for hierarchy
// - Gold progress bars and highlights
```

---

**Remember**: The power of this palette is in its cultural meaning. 
Users see indigo + gold = Japanese learning, instantly and intuitively.

