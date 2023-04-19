# SVF API

API design for SVF Enrollment System

## API Reference

## Base URL
/api/v1/student/enrollment/

### Login validation
This endpoint is used to validate the student's login credentials.

```http
POST /login/
```

`Body`
```json
{
  "studentCod": "",
  "password": ""
}
```

`Response`
```json
{
  "accessToken": ""
}
```

### Validation by SMS
This endpoint is used to verify a student's identity via SMS code.

```http
POST /smsvalidation 
```

`Params`
```json
{
  "tempToken": "",
  "studentCod": ""
}
```

`Body`
```json
{
  "sms": ""
}
```

`Response`
```json
{
  "tempToken": ""
}
```

### Update student password
This endpoint is used to update the student's password.

```http
PUT /updatepassword/
```
`Header`
```json
{
  "Authorization": "Bearer YOUR_ACCESS_TOKEN_HERE"
}
```

`Body`
```json
{
  "newPassword": ""
}
```


`Response`
```json
{
  "accessToken": ""
}
```

### Student information
This endpoint is used to retrieve the student information

```http
POST /
```
`Header`
```json
{
  "Authorization": "Bearer YOUR_ACCESS_TOKEN_HERE"
}
```


`Response`
```json
{
  "studentCod": "",
  "names": "",
  "lastNames": "",
  "dni": "",
  "newLevel": "",
  "newGrade": ""
}
```

### Enrollment details
This endpoint is used to retrieve the enrollment details.

```http
GET /details
```

`Response`
```json
{
  "schoolYear": "",
  "mainInfo": "",
  "terms": [
      {
          "title": "",
          "body": ""
      },
      {
          "title": "",
          "body": ""
      },
      {
          "title": "",
          "body": ""
      }
  ],
  "amounts": [
      {
          "level": "",
          "cost": ""
      },
      {
          "level": "",
          "cost": ""
      },
      {
          "level": "",
          "cost": ""
      }
  ]
}
```
#### Enrollment process
This endpoint is used to process the student's enrollment

```http
POST /process
```

`Header`
```json
{
  "Authorization": "Bearer YOUR_ACCESS_TOKEN_HERE"
}
```

`Body`
```json
{
  "studentCod": "",
  "totalAmount": "",
  "date": "",
  "level": {"levelId": "", "levelName": ""},
  "paymentId": {"paymentId": "", "paymentName": ""}
}
```

`Response`
```json
{
  "enrollmentId": ""
}
```
