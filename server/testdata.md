# Test data

## Hospes data

In the following data set the username and password is the email address.  

```
{
  "persons": [
    {
      "email": "foo.bar@java.no",
      "firstname": null,
      "lastname": null,
      "address": null,
      "phonenumber": null,
      "locale": "nb_NO",
      "id": 1,
      "password_pw": "Z4555Fx3L6ExnoK2mm0QdFD2dMs=",
      "password_slt": "9cGtDqdKk6wdOlba",
      "uniqueid": "4AYN0SSTP5MH3TY0WEYGNS5XHIFDGKKA",
      "validated": false,
      "timezone": "Europe/Oslo",
      "superuser": false,
      "openidkey": null
    },
    {
      "email": "beerduke@java.no",
      "firstname": "Beer",
      "lastname": "Duke",
      "address": "Teknologihuset",
      "phonenumber": "9111111",
      "locale": "nb_NO",
      "id": 2,
      "password_pw": "4ZSuspqv3E/CtszjecgYWjPc+Ic=",
      "password_slt": "oqNUBogO0RpTzzEe",
      "uniqueid": "4AYN0SSTP5MH3TY0WEYGNS5XHIFDGKKW",
      "validated": true,
      "timezone": "Europe/Oslo",
      "superuser": false,
      "openidkey": null
    },
    {
      "email": "beerduke2@java.no",
      "firstname": "Beer2",
      "lastname": "Duke2",
      "address": "Teknologihuset",
      "phonenumber": "9111121",
      "locale": "nb_NO",
      "id": 3,
      "password_pw": "KJbfqeSLPtOpZ0ZzDir3CP0dfuo=",
      "password_slt": "sS0EEZICbeRnPCPk",
      "uniqueid": "4AYN0SSTP5MH3TY0WEYGNS5XHIFDGKKA",
      "validated": true,
      "timezone": "Europe/Oslo",
      "superuser": false,
      "openidkey": null
    }
  ],
  "memberships": [
    {
      "member_person_id": 2,
      "id": 1,
      "year": 2011,
      "bought_by_person_id": 1,
      "boughtdate": "2011-02-17T15:42:28.194"
    },
    {
      "member_person_id": 2,
      "id": 2,
      "year": 2012,
      "bought_by_person_id": 1,
      "boughtdate": "2012-02-17T15:42:28.194"
    },
    {
      "member_person_id": 1,
      "id": 3,
      "year": 2012,
      "bought_by_person_id": 1,
      "boughtdate": "2012-02-17T15:42:28.194"
    },
    {
      "member_person_id": 3,
      "id": 4,
      "year": 2017,
      "bought_by_person_id": 1,
      "boughtdate": "2017-02-17T15:42:28.194"
    }
  ]
}
```

## Get a login token

Do a post to `/api/login` with a json containing the user and a password json.

```
{
  "username": "me@example.com",
  "password: "mySecretPwd"
}
```

## Import the hospes data into serenity

To import it just `POST` the content to the `/api/hospes/import` endpoint. It requires 
the token that you retrieved in the previous step and must be present in the http header
`X-Auth-Token`.