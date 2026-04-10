## Setup

- Only works with GMAIL for now
- Don't add quotation marks in the .env file, just the email and the password directly



1. Turn on 2-Step Verification on the Google account you want to use as sender.
2. Open Google Account > Security > App passwords.
3. Create a new app password for Mail.
4. Copy the generated 16-character password.
5. Create `src/backend/.env`.
6. Set these two values (there is a `src/backend/.env_example` file to copy from):

```dotenv
APP_NOTIFICATION_EMAIL=your.name@gmail.com
APP_NOTIFICATION_EMAIL_PASSWORD=your-16-character-app-password
```
