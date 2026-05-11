# ERP Billing — Android APK with Direct Thermal Printing
# Evercom Printer @ 192.168.1.127:9100

## How this works
The app uses a Java "bridge" (JavascriptInterface) injected into the WebView.
When you tap WiFi Print, JavaScript calls window.AndroidPrinter.printBase64(ip, port, data)
which opens a real TCP socket on port 9100 and sends ESC/POS bytes directly to the printer.
No third-party app needed. Works on Android 5.0+.

---

## BUILD STEPS (Android Studio)

### Step 1 — Install Android Studio
Download from: https://developer.android.com/studio

### Step 2 — Open this project
File → Open → select this EvercomPrinter folder

### Step 3 — Let Gradle sync
Wait for "Gradle sync finished" at the bottom. Takes 1-2 minutes first time.

### Step 4 — Build APK
Build → Build Bundle(s) / APK(s) → Build APK(s)
APK will be at:
  app/build/outputs/apk/debug/app-debug.apk

### Step 5 — Install on Android
Connect phone via USB (enable USB debugging in Developer Options)
Run → Run 'app'   OR   drag the .apk file to your phone and install

---

## KEY FILES

  app/src/main/java/com/erp/billing/MainActivity.java
    → PrinterBridge class handles TCP connection to printer
    → printBase64() method opens Socket to IP:9100 and sends ESC/POS bytes
    → testConnection() checks if printer is reachable

  app/src/main/assets/erp-billing.html
    → Full billing app
    → sendWifi() calls window.AndroidPrinter.printBase64(ip, port, base64)
    → Printer IP defaults to 192.168.1.127, port 9100

  app/src/main/AndroidManifest.xml
    → INTERNET permission
    → usesCleartextTraffic="true" — REQUIRED for local IP printing on Android 9+

---

## PRINTER SETTINGS IN APP

The app already has your printer pre-configured:
  IP:   192.168.1.127
  Port: 9100

Go to Printer tab → Test Connection to verify → Save → done.

---

## TROUBLESHOOTING

Problem: "Cannot connect" on test
Fix: Make sure phone and printer are on the SAME WiFi network.
     Print a test page from the printer to confirm its IP hasn't changed.

Problem: Prints garbage characters
Fix: Change cut mode to "No Cut" in Printer tab and test again.

Problem: Nothing prints but no error
Fix: Check printer is powered on and paper is loaded.
     Try turning printer off and on (reconnects WiFi).

Problem: App crashes
Fix: Check that AndroidManifest.xml has usesCleartextTraffic="true"
     This is the most common reason local IP printing fails on Android 9+.
