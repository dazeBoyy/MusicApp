package com.app.musicapp.track_adapter;

public interface Callback {
    void onSuccess(String filePath); // Успешное завершение с путем до файла
    void onError(String errorMessage); // Ошибка при загрузке
}