# Reglas ProGuard/R8. La app no usa minificación en el build de depuración,
# pero se dejan reglas básicas por si se activa minifyEnabled en el futuro.
-keep class com.ecoingenieria.depuradora.data.local.entity.** { *; }
-keepattributes *Annotation*
