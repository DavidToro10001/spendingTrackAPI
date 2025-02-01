# spendingTrackAPI

This is an API for track personal spending and generate reports

## Database and framework
* Database was made using SQLite, when execute project for the first time it creates database as a file in the directory
* Exposed is the ORM used in this project
* Framework used is ktor, using REST

## Functionalities
* Allows to register users, edit user information and deactivate users (currently just change isActive, don't affect login) also uses jwt for authentication
* when use login endpoint, api returns a token that must be used in endpoints related with user and spends information.
* Password are stored encrypted
* When use logout endpoint, token used will be added to blacklist and cannot be used anymore

* Allow to register spends for a user with a category (must be one of the categories predefined)
* Can change or delete this spends
* Can get spend information grouped by category for a user in a range of dates, this will be used to generate reports in the frontend

## TODO
### Module users
* When user is not active, then login should not work
* Create endpoint to re-activate user
* Documentation with swagger
* Testing

### Module Spending
* Documentation with swagger
* Testing
