# Changelog

## [3.1.0] 18-10-2024

### Added

- **Behavior Change**: The run task endpoint now returns the task ID in the `Location` header upon successful execution.

### Changed

- **Endpoint Update**: Changed endpoint from `POST /api/tasks/run` to `POST /api/tasks`.

## [3.0.1] 17-10-2024

### Fixed

- **League Match Retrieval Bug**: Resolved a bug that prevented League matches for cached characters from being
  retrieved correctly across different queue types.
    - This fix ensures accurate and reliable match data for all queue types, improving the integrity of character
      performance information.

## [3.0.0] 13-10-2024

### Added

- **Background Task Management API**: Implemented a comprehensive API for managing background tasks:
    - **Immediate Execution**: Added an endpoint to execute background tasks immediately.
    - **Task Retrieval**: Introduced endpoints to retrieve all tasks and to fetch a specific task by its ID.
        - This enhancement improves task management capabilities and provides users with better control and visibility
          over background processes.

### Changed

- **Task Execution Timing**: Modified the execution logic for background tasks so that they are not executed every time
  the server starts. Instead, the first execution is delayed, taking into account the last time each task was executed.

## [2.0.0] 05-10-2024

### Added

- **Multi-Game View Creation**: Expanded functionality to allow the creation of both World of Warcraft views and League
  of Legends views.
    - Users can now create, manage, and customize views for both games within the same application, enhancing
      versatility and user engagement.

- **New Field: Game**: Introduced a new field, "game," within the scope of views, which can be "WOW" or "LOL." This
  field is required in both create and edit requests for views.
- **Character Request Types**: Specified that the types `com.kos.characters.WowCharacterRequest`
  and `com.kos.characters.LolCharacterRequest` need to be sent as part of the view requests when adding characters due
  to a software limitation.

## [1.3.0] 30-09-2024

### Added

- **Background Task Execution Results Registration**: Enhanced the existing background task execution logging by now
  registering the results of each execution in the database.
    - This addition allows for detailed tracking of task outcomes, improving the monitoring and analysis capabilities of
      background processes.

## [1.2.0] 23-09-2024

### Added

- **PATCH Method for Views**: Implemented a PATCH feature over views with its endpoint.
    - Users can now modify view fields one by one, offering greater flexibility compared to the previous PUT method,
      which required submitting all fields.

## [1.1.0] 09-06-2024

### Added

- **View Publishing Feature**: Introduced the ability to publish views.
    - Views can now be marked as published, making them visible to everyone, or kept unpublished, allowing only the
      owner to view and edit them.

## [1.0.0] 25-04-2024

### Added

- **Role-Based Access Control**: Implemented endpoint protection by activities, where users are assigned roles
  containing the activities they can perform.
    - This change enhances application security and user management, allowing for fine-grained access control.

## [0.3.0] 24-01-2024

### Fixed

- **Character Duplication Bug**: Resolved a bug that allowed the creation of characters with the same name due to
  capitalization differences.
    - Now, character names are normalized to ensure uniqueness regardless of letter casing, preventing duplication.

## [0.2.0] 29-11-2023

### Added

- **Character Existence Check**: Implemented a validation step to check for the existence of characters in the external
  API before allowing their creation in the system.
    - This ensures that only valid and existing characters are created, improving data integrity and reducing potential
      errors.

## [0.1.9] 22-11-2023

### Changed

- **Cache Data Task Optimization**: Updated the character caching background task to retrieve data from the external API
  concurrently instead of sequentially.
    - This enhancement significantly improves the speed and efficiency of the caching process, reducing overall latency
      and resource consumption.

## [0.1.8] 17-11-2023

### Added

- **Background Tasks**: Implemented two new background tasks to improve application performance and maintenance:
    - **Character Caching Task**: A scheduled task to cache character data, enhancing retrieval speed and reducing load
      on the database.
    - **Expired Token Cleanup Task**: A scheduled task to regularly clean up expired tokens, ensuring efficient use of
      storage and maintaining security.

## [0.1.7] 11-11-2023

### Added

- **Password Encryption**: Implemented encryption for user passwords before storing them in the repository.
    - Passwords are now hashed using a secure algorithm, enhancing security and protecting user data.

## [0.1.6] 11-11-2023

### Added

- **Refresh Token Feature**: Implemented a new refresh token mechanism to enhance user authentication.
    - The login response now delivers two tokens:
        - **Access Token**: Used for authenticating API requests.
        - **Refresh Token**: Allows users to obtain a new access token without re-entering credentials.

### Changed

- **Login Response**: Updated the login endpoint to return both the access token and the refresh token, improving
  session management and security.

## [0.1.5] 08-11-2023

### Added

- **Access Control System**: Implemented a new authentication system using access tokens and user credentials.

### Changed

- **Endpoint Authorization**: Updated existing API endpoints to require authentication via access tokens or credentials.

## [0.1.4] 07-11-2023

### Added

- **Edit Password Endpoint**: Introduced a new endpoint `PUT /api/credentials` for users to update their passwords.

## [0.1.3] 04-11-2023

### Added

- **Delete View Feature**: Implemented the ability to delete views.
    - Users can now remove views they no longer need, enhancing view management capabilities.

## [0.1.2] 04-11-2023

### Added

- **Expired Token Prevention**: Implemented logic to prevent the use of expired tokens.
    - API endpoints now validate token expiration before processing requests, ensuring enhanced security and user
      experience.
- **Persistent Tokens**: Introduced persistent tokens that never expire.
    - These tokens allow users to maintain long-term sessions without needing to re-authenticate frequently.

## [0.1.1] 04-11-2023

### Added

- **Name Field for Views**: Introduced an extra field, **name**, for views.
    - This field is now required when creating and editing views, enhancing the identification and management of views.

## [0.1.0] 02-11-2023

### Fixed

- **View Editing Issue**: Resolved a problem that prevented users from editing views correctly.
    - The fix ensures that all necessary data is correctly loaded and updated during the edit process, enhancing user
      experience and functionality.

## [0.0.4] 01-11-2023

### Added

- **Dockerfile**: Created a Dockerfile to simplify the deployment process.
    - This Dockerfile allows for easy containerization of the application, ensuring consistent environments across
      different deployment targets.

## [0.0.3] 14-09-2023

### Added

- **Database Configuration**: Introduced configuration settings for connecting to the database, including connection
  strings and credentials.

### Changed

- **Repository Upgrade**: Transitioned the repository from a volatile in-memory storage to a real database.
    - This change enhances data persistence, reliability, and scalability.

## [0.0.2] 26-07-2024

### Added

- **Maximum Views Limit**: Set the maximum number of views that a user can create to **2**.
    - This limit is enforced to optimize resource usage and ensure fair access for all users.

## [0.0.1] 26-07-2024

### Added

- **Code Coverage Enforcement**: Implemented code coverage checks for the project.
    - Pull requests will now fail if tests do not cover at least **75%** of the code, ensuring better testing practices
      and higher code quality.

### Changed

- **Continuous Integration Configuration**: Updated CI pipeline to include code coverage metrics as part of the testing
  process.

## [0.0.0] 25-07-2023

### Added

- Initial release with core features.