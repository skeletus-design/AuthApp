# Данный документ является README аннотацией к дипломному проекту студента 4-го курса ИСИП Воронина Н.В.

## Предметная область

Мобильное ПО для быстрой авторизации пользователей.

## Задача

Обеспечить быструю и удобную авторизацию на условной платформе курсов образовательной организации ВШП

## Цели

1. Изучить предметную область 
2. Обеспечить постоянную авторизацию и базовый функционал приложения
3. Создать временный токен авторизации

## Последний [релиз](https://github.com/skeletus-design/AuthApp/releases/tag/now3)

## Примеры реализованных запросов к API

1. Авторизация на my.vshp.online

Пример пустого тела запроса:
```
{

    "login": "jane.doe@vshp.online",

    "password": "some_pass123"

}
```

Пример ответа
```
{
  "user": {
    "id": 2659,
    "name": "Джейн Доу Джоновна",
    "first-name": "Джейн",
    "middle-name": "Доу",
    "last-name": "Джоновна",
    "email": "user-945@example.com",
    "phone": "+79504233832",
    "activation-state": "active",
    "unread-messages-count": 0,
    "activated-at": "2023-10-04T19:56:36.000+03:00",
    "created-at": "2023-10-04T19:56:36.000+03:00",
    "referral-token": "W1p8XbK7VeDnjg9S",
    "alumni-network": false,
    "country-id": null,
    "country-code": null,
    "phone-code": null,
    "consultant-position": "Онлайн-консультант",
    "consultant-online": false,
    "consultant-name": "Воробьев Василиса",
    "consultant-avatar-url": "/assets/fallbacks/user/avatar-no-photo-2c43ce536d45db9975727484db7e50fe27c07b3560e14d8ed3026ccc8b3dd569.png",
    "photo": {
      "original": "/assets/fallbacks/user/no-photo-af5380359a85a1c90e6c5ebac4d1cc8cadf3460e96cc2137723747fbfe2bc8bb.png",
      "avatar": "/assets/fallbacks/user/avatar-no-photo-2c43ce536d45db9975727484db7e50fe27c07b3560e14d8ed3026ccc8b3dd569.png",
      "profile": "/assets/fallbacks/user/profile-no-photo-b1b1896a086e5ed09acb7d329b931fb551944b199be3f69be82fb36606345d27.png"
    },
    "photo-exists": false,
    "referral-agent": false,
    "staff": false,
    "locale": "ru"
  },
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoyNjU5LCJleHAiOjE2OTcwNDMzOTZ9.Ia1LDa3EnWN2qRQAB_3V8_gjTSHc553A45cbJbPVeW8"
}
```

2. Получение шестизначного OTP кода

Пример пустого тела запроса:
```
{
  "method": "POST",
  "url": "https://api.vshp.online/api/v1/user/otp",
  "headers": {
    "Authorization": "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjozNzIyMzI2LCJleHAiOjE3NDk3NjIyNTIsImlhdCI6MTc0OTE1NzQ1Mn0.3Qf7lLyUHlGjsydpsU16udjjH_d8CkBnKJjFMUkRypA",
    "Content-Type": "multipart/form-data"
  },
  "body": {
    "lifetime": "300"
  }
}
```

Пример ответа:
```
{
    "code": "190097"
}
```
