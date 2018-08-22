# https://www.guardsquare.com/en/proguard/manual/usage
-repackageclasses com.talmir.mickinet.android.library.package.internal
-allowaccessmodification
-optimizationpasses 10
-mergeinterfacesaggressively
-overloadaggressively
-dontusemixedcaseclassnames
-keep class android.support.v7.widget.SearchView { *; }