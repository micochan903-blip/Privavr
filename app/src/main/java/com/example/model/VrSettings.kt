package com.example.model

enum class VrEnvironment(val displayName: String, val description: String) {
    DEEP_SPACE("Deep Space Nebula", "Vast cosmic vista with distant stars & nebula glow"),
    PURE_VOID("OLED Pure Void", "Pitch black infinity for zero distraction & max OLED battery life"),
    CYBER_GRID("Cyber Horizon", "Minimalist holographic matrix grid with neon accents"),
    WARM_CINEMA("Ambient Theater", "Subtle acoustic theater hall with soft floor lights")
}

enum class VirtualAppType(val label: String, val iconName: String) {
    SYSTEM_HOME("Phone Launcher", "Home"),
    BROWSER("Secure Browser", "Language"),
    GALLERY("Private Gallery", "Photo"),
    NOTES("Encrypted Notes", "Description"),
    MEDIA_PLAYER("Cinema Player", "PlayCircle"),
    SYSTEM_STATS("Privacy Monitor", "Shield")
}

data class VrSettings(
    val isFullscreenVr: Boolean = false,
    val isStereoCardboardMode: Boolean = false,
    val screenCurvature: Float = 0.35f,
    val screenScale: Float = 1.0f,
    val screenDistance: Float = 1.0f,
    val ipdOffset: Float = 0f,
    val headTrackingEnabled: Boolean = true,
    val ambientGlowEnabled: Boolean = true,
    val privacyCurtainActive: Boolean = false,
    val environment: VrEnvironment = VrEnvironment.DEEP_SPACE,
    val screenBrightness: Float = 1.0f,
    val hudVisible: Boolean = true,
    val selectedApp: VirtualAppType = VirtualAppType.SYSTEM_HOME
)
