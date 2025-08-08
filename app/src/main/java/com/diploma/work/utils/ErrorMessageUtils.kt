package com.diploma.work.utils

import io.grpc.Status
import io.grpc.StatusRuntimeException
import com.diploma.work.utils.ErrorHandler.ErrorContext

object ErrorMessageUtils {
    

    fun getReadableErrorMessage(error: Throwable): String {
        return when (error) {
            is StatusRuntimeException -> getGrpcErrorMessage(error)
            else -> getGenericErrorMessage(error)
        }
    }
    
    private fun getGrpcErrorMessage(error: StatusRuntimeException): String {
        val statusCode = error.status.code
        val description = error.status.description?.lowercase() ?: ""
        
        return when (statusCode) {
            Status.Code.UNAVAILABLE -> "Сервис временно недоступен. Проверьте подключение к интернету и попробуйте снова."
            
            Status.Code.DEADLINE_EXCEEDED -> "Время ожидания истекло. Попробуйте еще раз."
            
            Status.Code.UNAUTHENTICATED -> when {
                description.contains("invalid email or password") -> "Неверный email или пароль."
                description.contains("invalid credentials") -> "Неверные учетные данные."
                description.contains("password") -> "Неверный пароль."
                description.contains("email") -> "Неверный email."
                else -> "Сессия истекла. Необходимо войти в систему заново."
            }
            
            Status.Code.PERMISSION_DENIED -> "У вас нет прав для выполнения этого действия."
            
            Status.Code.NOT_FOUND -> when {
                description.contains("user") -> "Пользователь не найден."
                description.contains("test") -> "Тест не найден."
                description.contains("session") -> "Сессия тестирования не найдена."
                description.contains("article") -> "Статья не найдена."
                description.contains("achievement") -> "Достижение не найдено."
                else -> "Запрашиваемые данные не найдены."
            }
            
            Status.Code.ALREADY_EXISTS -> when {
                description.contains("email") -> "Пользователь с таким email уже существует."
                description.contains("username") -> "Пользователь с таким именем уже существует."
                description.contains("session") -> "Сессия тестирования уже создана."
                else -> "Данные уже существуют."
            }
            
            Status.Code.INVALID_ARGUMENT -> when {
                description.contains("email") -> "Неверный формат email адреса."
                description.contains("password") -> "Пароль не соответствует требованиям безопасности."
                description.contains("token") -> "Неверный код подтверждения."
                description.contains("answer") -> "Неверный формат ответа на вопрос."
                else -> "Переданы некорректные данные."
            }
            
            Status.Code.FAILED_PRECONDITION -> when {
                description.contains("email") -> "Email адрес не подтвержден. Проверьте почту и подтвердите регистрацию."
                description.contains("session") -> "Тест уже завершен или сессия недействительна."
                description.contains("test") -> "Тест недоступен для прохождения."
                else -> "Не выполнены условия для выполнения операции."
            }
            
            Status.Code.RESOURCE_EXHAUSTED -> "Превышен лимит запросов. Попробуйте позже."
            
            Status.Code.CANCELLED -> "Операция была отменена."
            
            Status.Code.INTERNAL -> "Произошла внутренняя ошибка сервера. Попробуйте позже."
            
            Status.Code.ABORTED -> "Операция была прервана. Попробуйте снова."
            
            else -> "Произошла ошибка при обращении к серверу. Попробуйте позже."
        }
    }
    

    private fun getGenericErrorMessage(error: Throwable): String {
        val message = error.message?.lowercase() ?: ""
        
        return when {
            message.contains("network") || message.contains("connection") -> 
                "Проблемы с подключением к интернету. Проверьте соединение и попробуйте снова."
            
            message.contains("timeout") -> 
                "Время ожидания истекло. Попробуйте еще раз."
            
            message.contains("host") || message.contains("dns") -> 
                "Не удается подключиться к серверу. Проверьте подключение к интернету."
            
            message.contains("unauthorized") || message.contains("auth") -> 
                "Ошибка авторизации. Попробуйте войти в систему заново."
            
            message.contains("token") -> 
                "Сессия истекла. Необходимо войти в систему заново."
            
            message.contains("validation") || message.contains("invalid") -> 
                "Введены некорректные данные. Проверьте правильность заполнения полей."
            
            message.contains("email") -> 
                "Неверный формат email адреса."
            
            message.contains("password") -> 
                "Неверный пароль или пароль не соответствует требованиям."
            
            message.contains("file") || message.contains("read") || message.contains("write") -> 
                "Ошибка при работе с файлом. Попробуйте еще раз."
            
            message.contains("permission") -> 
                "Недостаточно прав для выполнения операции."
            
            message.contains("json") || message.contains("parse") -> 
                "Ошибка обработки данных. Попробуйте позже."
            
            message.contains("database") || message.contains("sql") -> 
                "Ошибка работы с базой данных. Попробуйте позже."
            
            message.contains("test") && message.contains("session") -> 
                "Ошибка при работе с тестом. Попробуйте начать тест заново."
            
            message.contains("question") -> 
                "Ошибка при загрузке вопросов теста."
            
            message.contains("answer") -> 
                "Ошибка при сохранении ответа. Попробуйте еще раз."
            
            else -> "Произошла непредвиденная ошибка. Попробуйте позже."
        }
    }
      fun getContextualErrorMessage(error: Throwable, context: ErrorContext): String {
        if (context == ErrorContext.LOGIN && error is StatusRuntimeException && 
            error.status.code == Status.Code.UNAUTHENTICATED) {
            val description = error.status.description?.lowercase() ?: ""
            if (description.contains("invalid email or password") || 
                description.contains("invalid credentials")) {
                return "Неверный email или пароль. Проверьте правильность ввода."
            }
        }
        
        val baseMessage = getReadableErrorMessage(error)
        
        return when (context) {
            ErrorContext.LOGIN -> when {
                baseMessage.contains("Неверный email или пароль") -> baseMessage
                baseMessage.contains("Неверный пароль") -> "Неверный email или пароль. Проверьте правильность ввода."
                baseMessage.contains("Неверные учетные данные") -> "Неверный email или пароль. Проверьте правильность ввода."
                baseMessage.contains("не найден") -> "Пользователь с таким email не найден."
                else -> baseMessage
            }
            
            ErrorContext.REGISTRATION -> when {
                baseMessage.contains("уже существует") -> "Пользователь с таким email уже зарегистрирован."
                else -> baseMessage
            }
            
            ErrorContext.EMAIL_CONFIRMATION -> when {
                baseMessage.contains("token") || baseMessage.contains("код") -> "Неверный код подтверждения. Проверьте код и попробуйте снова."
                else -> baseMessage
            }
            
            ErrorContext.TEST_SESSION -> when {
                baseMessage.contains("session") -> "Ошибка при работе с тестом. Попробуйте начать тест заново."
                baseMessage.contains("завершен") -> "Этот тест уже был завершен ранее."
                else -> baseMessage
            }
            
            ErrorContext.PROFILE_UPDATE -> when {
                baseMessage.contains("файл") -> "Ошибка при загрузке аватара. Попробуйте выбрать другое изображение."
                else -> baseMessage
            }
            
            ErrorContext.DATA_LOADING -> when {
                baseMessage.contains("подключение") -> "Не удается загрузить данные. Проверьте подключение к интернету."
                else -> baseMessage
            }
            
            ErrorContext.NETWORK -> when {
                baseMessage.contains("недоступен") || baseMessage.contains("подключение") -> "Проблемы с сетевым подключением. Проверьте интернет-соединение."
                baseMessage.contains("время ожидания") -> "Превышено время ожидания. Попробуйте позже."
                else -> "Ошибка сети. Проверьте подключение к интернету."
            }
            
            ErrorContext.FEEDBACK -> when {
                baseMessage.contains("unauthorized") || baseMessage.contains("unauthenticated") -> "Необходимо войти в систему для отправки обратной связи."
                baseMessage.contains("invalid") -> "Проверьте правильность заполнения полей формы."
                baseMessage.contains("too long") -> "Сообщение слишком длинное. Сократите текст."
                baseMessage.contains("empty") || baseMessage.contains("required") -> "Заполните все обязательные поля."
                else -> "Не удалось отправить сообщение. Попробуйте позже."
            }
            
            ErrorContext.GENERIC -> baseMessage.ifEmpty { "Произошла ошибка. Попробуйте еще раз." }
        }
    }
}
