# Add project specific ProGuard rules here.
-keepattributes *Annotation*
-keepclassmembers class * {
    @dagger.* *;
    @javax.inject.* *;
    @com.google.dagger.* *;
}
