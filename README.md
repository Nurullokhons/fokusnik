# Фокусник — Android MVP

Интерактивный фокус с 21 картой. Пользователь запоминает карту, трижды указывает стопку, в которой она находится, а приложение раскрывает карту после третьего раунда.

## Технологии
- Kotlin
- Jetpack Compose
- Min SDK 29 (Android 10)
- Полностью офлайн

## Сборка
Откройте проект в Android Studio (JDK 17, Android SDK 35), дождитесь синхронизации Gradle и выполните:

```bash
gradle :app:assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`

## GitHub Actions
Workflow `.github/workflows/android.yml` собирает debug APK при push в `main`/`master` или вручную через Actions.

## Логика фокуса
Карты раздаются по строкам в три стопки. При сборе стопка зрителя помещается между двумя остальными. После трёх повторений выбранная карта находится на позиции 11 (индекс 10).
