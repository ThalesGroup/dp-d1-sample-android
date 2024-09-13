


-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-dontnote com.google.android.gms.internal.**,com.google.android.gms.common.**,com.google.android.material.**,com.google.firebase.**,com.journeyapps.barcodescanner.**,util.**,android.net.**,org.apache.**,org.apache.harmony.xnet.provider.jsse.NativeCrypto,android.os.WorkSource,kotlin.internal.jdk8.JDK8PlatformImplementations,kotlin.internal.JRE8PlatformImplementations,kotlin.internal.JRE7PlatformImplementations,kotlin.reflect.jvm.internal.ReflectionFactoryImpl,com.gemalto.mfs.mwsdk.utils.async.AbstractAsyncHandler,util.w.**,okhttp3.**,util.v.**,util.v.**
-dontwarn com.squareup.okhttp.**,org.apache.http.**,javax.naming.**,util.a.z.**,com.sun.jna.**,javax.naming.**,util.**,com.gemalto.mfs.mwsdk.**,java.awt.*,util.w.**,okhttp3.**,com.samsung.android.sdk.samsungpay.**,util.v.**,java.awt.*,util.v.**,java.awt.*




-keepclassmembers,allowobfuscation class * {
    @util.y.z.zz
    <fields>;}

# Google play
-keep class com.google.android.gms.internal.** {
    <fields>;    <methods>;
}

# Others
-keep public class androidx.viewpager.widget.ViewPager {
    <fields>;    <methods>;
}

-keep class util.* {
    <fields>;    <methods>;
}

-keepclasseswithmembers,allowshrinking class * {
    native <methods>;
}

# Global JNA Rules
-keep,allowobfuscation interface  com.sun.jna.Library

-keep,allowobfuscation interface  com.sun.jna.Callback

-keep,allowobfuscation interface  com.sun.jna.Function

-keep,allowobfuscation interface  * extends com.sun.jna.Library

-keep,allowobfuscation interface  * extends com.sun.jna.Callback

-keepclassmembers interface  * extends com.sun.jna.Library {
    <methods>;
}

-keepclassmembers interface  * extends com.sun.jna.Callback {
    <methods>;
}

-keep class com.sun.jna.CallbackReference {
    void dispose();
    com.sun.jna.Callback getCallback(java.lang.Class,com.sun.jna.Pointer,boolean);
    com.sun.jna.Pointer getFunctionPointer(com.sun.jna.Callback,boolean);
    com.sun.jna.Pointer getNativeString(java.lang.Object,boolean);
    java.lang.ThreadGroup initializeThread(com.sun.jna.Callback,com.sun.jna.CallbackReference$AttachOptions);
}

-keep,includedescriptorclasses class com.sun.jna.Native {
    com.sun.jna.Callback$UncaughtExceptionHandler callbackExceptionHandler;    void dispose();
    java.lang.Object fromNative(com.sun.jna.FromNativeConverter,java.lang.Object,java.lang.reflect.Method);
    com.sun.jna.NativeMapped fromNative(java.lang.Class,java.lang.Object);
    com.sun.jna.NativeMapped fromNative(java.lang.reflect.Method,java.lang.Object);
    java.lang.Class nativeType(java.lang.Class);
    java.lang.Object toNative(com.sun.jna.ToNativeConverter,java.lang.Object);
    int getNativeSize(java.lang.Class);
}

-keep class com.sun.jna.Native$ffi_callback {
    void invoke(long,long,long);
}

-keep class com.sun.jna.Structure {
    long typeInfo;    com.sun.jna.Pointer memory;    <init>(int);
    void autoRead();
    void autoWrite();
    com.sun.jna.Pointer getTypeInfo();
    com.sun.jna.Structure newInstance(java.lang.Class,long);
}

-keep class com.sun.jna.Structure$FFIType$FFITypes {
    <fields>;}

-keep class com.sun.jna.Structure$ByValue

-keep class com.sun.jna.CallbackReference$AttachOptions {
    <fields>;}

-keep class com.sun.jna.Callback$UncaughtExceptionHandler {
    void uncaughtException(com.sun.jna.Callback,java.lang.Throwable);
}

-keep class com.sun.jna.ToNativeConverter {
    java.lang.Class nativeType();
}

-keep class com.sun.jna.NativeMapped {
    java.lang.Object toNative();
}

-keep class com.sun.jna.IntegerType {
    long value;}

-keep class com.sun.jna.PointerType {
    com.sun.jna.Pointer pointer;}

-keep class com.sun.jna.LastErrorException {
    <init>(int);
    <init>(java.lang.String);
}

-keep class com.sun.jna.Pointer {
    long peer;    <init>(long);
}

-keep class com.sun.jna.WString {
    <init>(java.lang.String);
}

-keep class com.sun.jna.JNIEnv {
    <fields>;    <methods>;
}

-keep class com.sun.jna.* {
    <fields>;    <methods>;
}

-keepclassmembers class * extends com.sun.jna.* {
    public <fields>;    public <methods>;
}

# Global JNA Rules
-keep class com.sun.jna.** {
    <fields>;    <methods>;
}

-keep,allowobfuscation interface  com.sun.jna.Library

-keep,allowobfuscation interface  * extends com.sun.jna.Library

-keep,allowobfuscation interface  * extends com.sun.jna.Callback

-keepclassmembers interface  * extends com.sun.jna.Library {
    <methods>;
}

-keepclassmembers interface  * extends com.sun.jna.Callback {
    <methods>;
}

-keep class com.sun.jna.CallbackReference {
    void dispose();
    com.sun.jna.Callback getCallback(java.lang.Class,com.sun.jna.Pointer,boolean);
    com.sun.jna.Pointer getFunctionPointer(com.sun.jna.Callback,boolean);
    com.sun.jna.Pointer getNativeString(java.lang.Object,boolean);
    java.lang.ThreadGroup initializeThread(com.sun.jna.Callback,com.sun.jna.CallbackReference$AttachOptions);
}

-keep,includedescriptorclasses class com.sun.jna.Native {
    com.sun.jna.Callback$UncaughtExceptionHandler callbackExceptionHandler;    void dispose();
    java.lang.Object fromNative(com.sun.jna.FromNativeConverter,java.lang.Object,java.lang.reflect.Method);
    com.sun.jna.NativeMapped fromNative(java.lang.Class,java.lang.Object);
    com.sun.jna.NativeMapped fromNative(java.lang.reflect.Method,java.lang.Object);
    java.lang.Class nativeType(java.lang.Class);
    java.lang.Object toNative(com.sun.jna.ToNativeConverter,java.lang.Object);
}

-keep class com.sun.jna.Callback$UncaughtExceptionHandler {
    void uncaughtException(com.sun.jna.Callback,java.lang.Throwable);
}

-keep class com.sun.jna.Native$ffi_callback {
    void invoke(long,long,long);
}

-keep class com.sun.jna.Structure$FFIType$FFITypes {
    <fields>;}

-keep class com.sun.jna.ToNativeConverter {
    java.lang.Class nativeType();
}

-keep class com.sun.jna.NativeMapped {
    java.lang.Object toNative();
}

-keep class com.sun.jna.IntegerType {
    long value;}

-keep class com.sun.jna.PointerType {
    com.sun.jna.Pointer pointer;}

-keep class com.sun.jna.LastErrorException {
    <init>(int);
    <init>(java.lang.String);
}

-keep class com.sun.jna.Pointer {
    long peer;    <init>(long);
}

-keep class com.sun.jna.WString {
    <init>(java.lang.String);
}

-keep class com.sun.jna.Structure {
    long typeInfo;    com.sun.jna.Pointer memory;    <init>(int);
    void autoRead();
    void autoWrite();
    com.sun.jna.Pointer getTypeInfo();
    com.sun.jna.Structure newInstance(java.lang.Class,long);
}

-keepclassmembers class util.w.** {
    public <init>(...);
}

-keep class com.sun.jna.* {
    <fields>;    <methods>;
}

-keepclassmembers class * extends com.sun.jna.* {
    public <fields>;    public <methods>;
}

-keepclassmembers class util.v.** {
    public <init>(...);
}

-keep class com.sun.jna.* {
    <fields>;    <methods>;
}

-keepclassmembers class * extends com.sun.jna.* {
    public <fields>;    public <methods>;
}

-keep class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.jce.provider.BouncyCastleProvider
-dontwarn org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider