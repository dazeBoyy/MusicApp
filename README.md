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
2. Загрузите файл `google-services.json` и поместите его в папку `app/` вашего Android проекта.
3. Добавьте SHA-1 вашего приложения в настройки Firebase:
 - В Android Studio откройте терминал и выполните команду для получения SHA-1:
   ```bash
   ./gradlew signingReport
   ```
 - Перейдите в консоль Firebase и добавьте SHA-1 в настройки вашего проекта.
 -  
  4. Убедитесь, что в вашем `build.gradle` (модуль `app`) включены зависимости для Firebase:
   ```gradle
   implementation 'com.google.firebase:firebase-auth:21.0.1'
   implementation 'com.google.firebase:firebase-firestore:24.0.2'
   ```

## Мобильное приложение демонстрация работы:

![image](https://github.com/user-attachments/assets/732c0d6d-c2cf-4016-9683-8ce51464d521)
![image](https://github.com/user-attachments/assets/9b298289-2264-4d9b-b83a-58bc2792d5b9)
![image](https://github.com/user-attachments/assets/5669d8f7-30ec-4ec9-820a-308f54d3e01a)
![image](https://github.com/user-attachments/assets/19a3e33d-eb86-429c-9363-b9e35677db61)
![image](https://github.com/user-attachments/assets/75f740e3-cfb2-4430-ad0b-eca762655ed1)
![image](https://github.com/user-attachments/assets/4cfcf41f-ede3-4fa0-b75d-841e32c4dcab)
![image](https://github.com/user-attachments/assets/cc39ce15-4c2b-4e1c-a1bd-ee6ea89a3b09)
![image](https://github.com/user-attachments/assets/85253b9b-732d-4c11-a870-b6854c6d8ac1)
