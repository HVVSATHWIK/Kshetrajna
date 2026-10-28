# Privacy Policy for Kshetrajna (क्षेत्रज्ञ)

**Effective Date:** October 29, 2026  
**Last Updated:** October 29, 2026  

Kshetrajna ("we", "our", or "the app") is committed to respecting and protecting user privacy. Kshetrajna is engineered from the ground up on the principle of **absolute on-device privacy and non-destructive provenance**.

---

## 1. Information Collected and Processed

Kshetrajna operates as an offline-first, local-first utility. All data captured remains under your exclusive ownership on your device.

### a. Camera Access (`android.permission.CAMERA`)
* **Purpose:** The app requires access to the device camera hardware solely to allow you to capture photographs and preview frames within the application.
* **Storage:** Captured images are saved exclusively into the application's private scoped storage (`filesDir/photos/`).
* **Transmission:** Camera data is **never transmitted** to external servers, cloud providers, or third-party analytical networks.

### b. Geodetic Location (`android.permission.ACCESS_FINE_LOCATION`, `android.permission.ACCESS_COARSE_LOCATION`)
* **Purpose:** Precise or approximate GPS coordinates, altitude, bearing, and geocoded locality are read at the moment of shutter release solely to compute honest geodetic stamps, coordinate overlays, and local journal sorting.
* **Voluntary:** Location permissions are entirely optional. If declined, Kshetrajna operates normally with clear "No location recorded" flags.
* **Transmission:** Geodetic data is **never uploaded** to any tracking server or remote telemetry service.

### c. Local Storage & Journal Records
* Photo metadata, observation notes, and timestamps are persisted in an on-device SQLite/Room database.
* No external read permissions to your general media gallery are required; imports utilize Android's secure Photo Picker system contract (`PickVisualMedia`).

---

## 2. Third-Party Services and Telemetry

Kshetrajna contains **no background trackers, advertising SDKs, or user surveillance mechanisms**.
* Optional cloud integrations (e.g. Google Firebase or AI Studio features) are dormant unless explicitly configured with your own project credentials.
* No analytics or crash reports containing personally identifiable information (PII) or location coordinates are collected by the developer.

---

## 3. Data Retention and Deletion

* You retain full control over your data.
* You may delete individual photos and journal entries or choose "Clear All Data" directly within the app settings.
* Uninstalling the application automatically deletes all private data stored in the application directory.

---

## 4. Children's Privacy

Kshetrajna does not knowingly collect or solicit any personal information from children under the age of 13.

---

## 5. Changes to This Privacy Policy

We may periodically update this policy. Any changes will be reflected with a revised "Last Updated" date in this repository.

---

## 6. Contact

For inquiries regarding this privacy policy or application security:
* **Developer:** HVVSATHWIK (Hariveerendranath Venkata Sathwik)
* **GitHub Repository:** [https://github.com/HVVSATHWIK/Kshetrajna](https://github.com/HVVSATHWIK/Kshetrajna)
