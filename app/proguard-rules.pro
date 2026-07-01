# ── Android Service/Activity loaded by system via reflection ──────────────────
-keep public class * extends android.app.Service
-keep public class * extends android.app.Activity
-keep class * extends androidx.media3.session.MediaSessionService

# ── Room entities/DAOs (reflection) ──────────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }

# ── NewPipe Extractor (uses reflection internally) ──────────────────────────
-keep class org.schabi.newpipe.extractor.** { *; }
-dontwarn org.schabi.newpipe.extractor.**
-dontwarn org.mozilla.javascript.**
-dontwarn java.beans.**

# ── Parcelable ───────────────────────────────────────────────────────────────
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# ── Enums ────────────────────────────────────────────────────────────────────
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ── Attributes ───────────────────────────────────────────────────────────────
-keepattributes Signature, Exceptions, InnerClasses, EnclosingMethod
