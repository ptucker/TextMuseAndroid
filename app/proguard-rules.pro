# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/wcheng/Downloads/adt-bundle-mac-x86_64-20140702/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

-printmapping mapping.txt

-assumenosideeffects class android.util.Log {
    public static int d(...);
}

-dontwarn com.squareup.okhttp.**
-dontwarn org.joda.convert.**

-keep class android.support.v7.widget.** { *; }
-keep interface android.support.v7.widget.** { *; }
-keep class android.support.v4.widget.** { *; }
-keep interface android.support.v4.widget.** { *; }
-keep class com.h6ah4i.android.** { *; }
-keep interface com.h6ah4i.android.** { *; }
-keep class com.microsoft.windowsazure.** { *; }
-keep interface com.microsoft.windowsazure.** { *; }


# used for guava stuff, used for azure libs
-dontwarn javax.annotation.**
-dontwarn javax.inject.**
-dontwarn sun.misc.Unsafe

##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# Gson specific classes
-keep class sun.misc.Unsafe { *; }
#-keep class com.google.gson.stream.** { *; }

# Some more gson stuff for more details in crashes
-keep class com.google.gson.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.laloosh.textmuse.datamodel.gson.DateTimeConverter { *; }
-keep class com.laloosh.textmuse.datamodel.gson.GsonConverter { *; }
-keep class com.laloosh.textmuse.datamodel.Category { <fields>; }
-keep class com.laloosh.textmuse.datamodel.LocalNotification { <fields>; }
-keep class com.laloosh.textmuse.datamodel.Note { <fields>; }
-keep class com.laloosh.textmuse.datamodel.TextMuseContact { <fields>; }
-keep class com.laloosh.textmuse.datamodel.TextMuseData { <fields>; }
-keep class com.laloosh.textmuse.datamodel.TextMuseGroup { <fields>; }
-keep class com.laloosh.textmuse.datamodel.TextMuseRecentContact { <fields>; }
-keep class com.laloosh.textmuse.datamodel.TextMuseSettings { <fields>; }
-keep class com.laloosh.textmuse.datamodel.TextMuseStoredContacts { <fields>; }

