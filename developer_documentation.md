# OpiNet developer documentation

Author: Tomáš Hůla

## Client Requirements

All requirements can be found at: https://moodle.spsejecna.cz/mod/page/view.php?id=6889

## Database architecture

Database diagram:

![Database diagram](developer-documentation-res/db_diagram.png)

## Application architecture and structure

The application follows the MVVM architecture.
Each screen in the application consists of its model, (service) view (screen) and viewmodel (screenmodel)
Each logical entity has its directory, which contains data access logic.
Each application screen has its directory (suffix `screen`), which contains the screen's view and viewmodel logic.

## Third-party libraries

Can be found in the `gradle/libs.verstions.toml` file.
