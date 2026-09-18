# kotlinx.serialization: keep the generated serializers for our @Serializable models.
-keepclassmembers class com.mohithash.ledgerly.**.** {
    *** Companion;
    *** serializer(...);
}
-keepclasseswithmembers class com.mohithash.ledgerly.**.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.mohithash.ledgerly.**.**$$serializer { *; }
-dontwarn org.slf4j.**
