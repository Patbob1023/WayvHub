# WayvHub
WAYV is a novel app that seeks to replace traditional ticketing solutions such as physical passes and cards by using sound waves to authenticate users. Utilizing the Chirp API, WAYV allows users to easily present their credentials to a receiving device by transmitting a unique "sonic barcode." Consisting of an Android app, a database (Google Firebase,) and Wix website, WAYV is not just an app; it is a sophisticated system of components which work together to create a seamless and magical experience for its users. This is the receiving component of the WAYV system.

## Background
This app was created by myself and three of my colleagues at Yale Hack 2018. It made extensive use of the Chrip API — an incredibly unique API that enables the transmission of data over sound. 

## Usage
Open the project folder in Android studio. Then perform a "Gradle Sync."

```bash
./gradlew build
```
For immediate testing or debugging, build and install on a device or emulator using


```bash
./gradlew installDebug
```

Upon launching the app, the authentication database is automatically linked and the app begins to listen. To change the authentication whitelist, create or modify a Firebase Database and update the MainActivity.java file with your credentials as follows:

```java
String KEY = "your key";
String SECRET = "your secret";
String CONFIG = "your config string";
```
