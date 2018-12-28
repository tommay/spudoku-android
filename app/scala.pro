# This is android-proguard.config from sbt-android,
#  https://github.com/scala-android/sbt-android/blob/master/resources/android-proguard.config
# See http://scala-on-android.taig.io/proguard/

###
# Generic proguard options useful in non-release builds
###
-dontobfuscate

-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable,Signature

-flattenpackagehierarchy

-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*,!code/allocation/variable

-dontnote android.support.**
-dontnote org.apache.http.**
-dontnote android.net.http.**
-dontnote android.annotation.**

-dontnote com.android.vending.licensing.**
-dontnote com.google.vending.licensing.**

# For debugIncludesTests
-keep class * extends junit.framework.TestCase { *; }
-keepclasseswithmembers class * { @org.junit.** *; }
-dontwarn junit.**
-dontnote junit.**
-dontwarn org.junit.**
-dontnote org.junit.**
-dontwarn org.hamcrest.**
-dontwarn com.squareup.**
-dontnote org.hamcrest.**
-keep class android.support.test.** { *; }

###
# Scala-specific proguard config
###
# keep Dynamic because proguard cache fails to handle it gracefully
-keep class scala.Dynamic { *; }
-dontnote scala.concurrent.util.Unsafe
-dontnote scala.Enumeration**
-dontnote scala.ScalaObject
-dontnote org.xml.sax.EntityResolver
-dontnote scala.concurrent.forkjoin.**
-dontwarn scala.beans.ScalaBeanInfo
-dontwarn scala.concurrent.**
-dontnote scala.reflect.**
-dontwarn scala.reflect.**
-dontwarn scala.sys.process.package$
-dontwarn **$$anonfun$*
-dontwarn scala.collection.immutable.RedBlack$Empty
-dontwarn scala.tools.**,plugintemplate.**

-keep public class scala.reflect.ScalaSignature
# This is gone in 2.11
-keep public interface scala.ScalaObject

-keepclassmembers class * {
    ** MODULE$;
}

-keep class scala.collection.SeqLike {
    public java.lang.String toString();
}

-keepclassmembernames class scala.concurrent.forkjoin.ForkJoinPool {
    long eventCount;
    int  workerCounts;
    int  runControl;
    scala.concurrent.forkjoin.ForkJoinPool$WaitQueueNode syncStack;
    scala.concurrent.forkjoin.ForkJoinPool$WaitQueueNode spareStack;
}

-keepclassmembernames class scala.concurrent.forkjoin.ForkJoinWorkerThread {
    int base;
    int sp;
    int runState;
}

-keepclassmembernames class scala.concurrent.forkjoin.ForkJoinTask {
    int status;
}

-keepclassmembernames class scala.concurrent.forkjoin.LinkedTransferQueue {
    scala.concurrent.forkjoin.LinkedTransferQueue$PaddedAtomicReference head;
    scala.concurrent.forkjoin.LinkedTransferQueue$PaddedAtomicReference tail;
    scala.concurrent.forkjoin.LinkedTransferQueue$PaddedAtomicReference cleanMe;
}
