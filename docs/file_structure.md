# Диаграмма файлов приложения

## Основная структура проекта
```mermaid
graph TD
    Root[Project Root] --> App[app module]
    App --> Src[src/main]
    Src --> Java[java/com/example/map]
    Java --> MA[MainActivity.kt]
    Java --> Theme[ui/theme]
    Src --> Res[res]
    Res --> Drawable[drawable]
    Drawable --> MapImg[map.jpg]
    Root --> CI[.github/workflows/android.yml]
    Root --> Docs[docs/]
```

## Описание файлов
- **MainActivity.kt**: Содержит всю логику приложения: Jetpack Compose интерфейс, обработку жестов трансформации, логику работы с точками и их сохранение.
- **map.jpg**: Фоновое изображение, используемое в качестве карты.
- **android.yml**: Конфигурация GitHub Actions для автоматической сборки APK и прогона тестов.
- **docs/**: Набор Markdown-файлов для документации (GitHub Pages).
