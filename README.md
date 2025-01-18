# MusicApp

MusicApp — это приложение для поиска и скачивания музыкальных треков. Оно использует Last.fm API для поиска и получения информации о треках, а также позволяет скачивать треки с сервера.

## Требования

- **JDK 17+** для работы с серверной частью.
- **Android Studio** для работы с мобильным приложением.
- **Docker** (если планируется деплой приложения с использованием контейнеров).
- **Firebase** для работы с аутентификацией и взаимодействием с мобильным приложением.

## Серверная часть (API)

### Стартовые настройки

1. **Путь к папке для хранения треков (TRACKS_DIRECTORY)**:
   Укажите путь, где будут храниться скачанные треки. Вы можете использовать переменные окружения для конфигурации этого пути.

   Пример:
   - В `local.properties` или `gradle.properties` добавьте:
     ```
     TRACKS_DIRECTORY=C:/path/to/music/tracks
     ```
   - В коде Java:
     ```java
     private static final String MUSIC_DIRECTORY = System.getenv("TRACKS_DIRECTORY");
     ```

2. **URL для скачивания треков (APP_URL)**:
   Укажите URL вашего сервера, по которому будут доступны треки для скачивания.

   Пример:
   - В `local.properties` или `gradle.properties`:
     ```
     APP_URL=http://localhost:8080
     ```
   - В коде Java:
     ```java
     String fileUrl = System.getenv("APP_URL") + "/api/tracks/" + new File(trackPath).getName();
     ```

### Описание Endpoints

- **GET /api/tracks/{filename}**: 
  Получить трек по имени файла.
  Пример:
  GET http://localhost:8080/api/tracks/{filename}

- **GET /api/tracks/download**:
  Скачать трек, если он существует. Параметры: `artist`, `track`, `duration` (в миллисекундах).
  Пример:
  GET http://localhost:8080/api/tracks/download?artist=ArtistName&track=TrackName&duration=120000

- **GET /api/tracks/search**:
  Найти трек по имени артиста и трека.
  Пример:
  GET http://localhost:8080/api/tracks/search?artist=ArtistName&track=TrackName

- **POST /api/tracks/similar**:
  Получить список похожих треков. Параметры: `artist`, `track`, `limit`.
  Пример:
  POST http://localhost:8080/api/tracks/similar?artist=ArtistName&track=TrackName&limit=10
## Мобильное приложение

### Настройка Firebase

1. Создайте проект в [Firebase Console](https://console.firebase.google.com/).
2. Добавьте SHA-1 вашего приложения в настройки Firebase
3.  Загрузите файл `google-services.json` и поместите его в папку `app/` вашего Android проекта.:
 - В Android Studio откройте терминал и выполните команду для получения SHA-1:
   ```bash
   ./gradlew signingReport
   ```
 - Перейдите в консоль Firebase и добавьте SHA-1 в настройки вашего проекта.
 -  
  4. Убедитесь, что в вашем `build.gradle` (модуль `app`) включены зависимости для Firebase:
   ```gradle
  implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.navigation:navigation-fragment:2.7.7")
    implementation("androidx.navigation:navigation-ui:2.7.7")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.github.bumptech.glide:glide:4.11.0")
    implementation("com.google.firebase:firebase-database:21.0.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.11.0")
    implementation ("com.google.android.gms:play-services-auth:20.7.0")
    implementation("com.google.firebase:firebase-auth:23.1.0")
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.android.gms:play-services-auth:21.3.0")
   ```

## Мобильное приложение демонстрация работы:
![image](https://github.com/user-attachments/assets/ad7f2a0f-1855-47db-828b-2f67bc3d5bdb)
![image](https://github.com/user-attachments/assets/1e8d9daf-e5e8-4876-9e54-7430866843e3)
![image](https://github.com/user-attachments/assets/3fe38ae9-f8ad-4266-b3f1-60f049f90fb4)
![image](https://github.com/user-attachments/assets/b251e1cb-5169-416e-84a7-e456eb69b9cf)
![image](https://github.com/user-attachments/assets/2c328b45-44a3-4903-9ea3-39257c1fd9b8)
![image](https://github.com/user-attachments/assets/009dce56-ae64-48b8-868a-6920f9dd54c9)
