**This project was made as a school assignment and has no real purpose!**

**School:** Střední Průmyslová Škola Elektrotechnická Ječná  
**Author:** Tomáš Hůla (tomashula06@gmail.com)  
**Date of elaboration:** 16.01.2025

# OpiNet

OpiNet (opinion net) is a desktop graphical imaginary opinion sharing social media platform, where anyone can create an account and then upload posts, up/downvote and comment posts
of other people.

Features:
- Uses SQL database for data storage
  - Support for H2, MySQL and MSSQL
- User registration and login
- Posts
  - Posting private/public posts
  - Commenting on posts
  - Upvoting/downvoting posts
  - Editing your posts
  - Deleting your posts
- Adding and managing friends
- Make posts public/private
- Importing users and posts from CSV
- Exporting summary report in JSON

This project has no purpose as it directly interacts with the database, which makes no sense. Not for use!

## Running

### Prerequisites

The program requires Java 21 or newer to run.

### From executable (JAR)

1. Go to [GitHub releases](https://github.com/Tomasan7/OpiNet/releases) open the latest release and download the JAR file for your system. If there is no JAR file for your system, or you have problems with the one you downloaded, download the `universal` JAR.
2. Open a terminal and navigate to the directory where the JAR file is located.
3. Run the JAR file: `java -jar <jarfile>`

### From code

1. Clone the repository: `git clone https://github.com/Tomasan7/OpiNet.git`
2. Navigate to the project directory: `cd OpiNet`
3. Build and run the project: `./gradlew run` (Linux) or `gradlew.bat run` (Windows)

### Configuration

When the project is run for the first time a `opinet.conf` file is created in the working directory.
Open it to see what is configurable.
[Default config](src/main/resources/opinet.conf)

### Import

Import options can be found in the `Management Screen` which is accessible a button at the bottom-right of the Login Screen.

#### Users

Users can be imported from CSV.
Note, that CSV delimiter can be configured in the config.
CSV must follow this format:
```
username,firstName,lastName,password,gender
```
Where `gender` is one of the following values: `MALE`, `FEMALE`, `NON_BINARY`

#### Posts

Posts can be imported from CSV.
Note, that CSV delimiter and date format can be configured in the config.
CSV must follow this format:
```
authorUsername,public,title,content
```
Where `public` can be either `true` (or `1`) or `false` (or `0`)

### Export

Export options can be found in the `Management Screen` which is accessible a button at the bottom-right of the Login Screen.
The management screen shows a report summary table, however the export contains the same reports, but with more information.

#### Report

Multiple reports can be exported as a whole.
The resulting file is a JSON of the following structure:
```
{
    mostActiveUser: {
        entity: {
            username: string,
            firstName: string,
            lastName: string,
            gender: Gender,
            id: int
        },
        value: int
    },
    mostActivePost: {
        entity: {
            title: string,
            content: string,
            uploadDate: date,
            authorId: int,
            public: boolean,
            id: int
        },
        value: int
    },
    mostUpvotedPost: {
        entity: {
            title: string,
            content: string,
            uploadDate: date,
            authorId: int,
            public: boolean,
            id: int
        },
        value: int
    },
    mostDownvotedPost: {
        entity: {
            title: string,
            content: string,
            uploadDate: date,
            authorId: int,
            public: boolean,
            id: int
        },
        value: int
    },
    mostCommentedPost: {
        entity: {
            title: string,
            content: string,
            uploadDate: date,
            authorId: int,
            public: boolean,
            id: int
        },
        value: int
    }
}
```
