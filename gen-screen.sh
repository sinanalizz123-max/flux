#!/bin/bash
if [ -z "$1" ]; then
  echo "Usage: ./gen-screen.sh ScreenName"
  exit 1
fi

NAME=$1
LOWER=$(echo "$NAME" | tr '[:upper:]' '[:lower:]')
PKG="app/src/main/java/com/flux/app/ui"
RES="app/src/main/res/layout"

# Create XML
cat <<XML > $RES/fragment_$LOWER.xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:gravity="center"
    android:background="@color/deep_black">
    <TextView
        android:text="$NAME Screen"
        android:textColor="@color/leica_white"
        android:textSize="24sp"
        android:textStyle="bold"/>
</LinearLayout>
XML

# Create Kotlin
cat <<KT > $PKG/${NAME}Fragment.kt
package com.flux.app.ui
import androidx.fragment.app.Fragment
import com.flux.app.R
class ${NAME}Fragment : Fragment(R.layout.fragment_$LOWER)
KT

echo "✅ Created ${NAME}Fragment.kt & fragment_$LOWER.xml"
