# Solar Calculator

## Build Information

1. Before you run your application, you need a Google Maps API key with API access for `Maps SDK for Android` and `Time Zone API`. Follow the directions here: [Link](https://developers.google.com/maps/documentation/android/start#get-key)
2. Create a `gradle.properties` file in your project root with the following variables (replace the `<...>` with your own data)
    ```
    AndroidProject.signing=<path_to_keystore>
    GOOGLE_MAPS_KEY_DEBUG=<google_maps_debug_key>
    GOOGLE_MAPS_KEY_RELEASE=<google_maps_release_key>
    ```
3. Run the app

## Screenshots

<img src="https://raw.githubusercontent.com/joydeepsaha05/android-job-hiring-task/master/assets/Screenshot_1547838662.png" width="350">

<img src="https://raw.githubusercontent.com/joydeepsaha05/android-job-hiring-task/master/assets/Screenshot_1547838685.png" width="350">

<img src="https://raw.githubusercontent.com/joydeepsaha05/android-job-hiring-task/master/assets/Screenshot_1547838749.png" width="350">

## Download APK
[Link](https://github.com/joydeepsaha05/android-job-hiring-task/blob/master/assets/SolarCalculator-v1.apk?raw=true)


## What we'll build
One of the crucial elements of pursuing Photography is understanding to use Earth's natural light sources ( *Sun and Moon* ) to our advantage. A trick to naturally take better photographs is to shoot in the [Golden Hour](https://en.wikipedia.org/wiki/Golden_hour_(photography)). As the name suggests this begins approximately an hour before sunset. 

In order to assist Photographers with planning of their photoshoots, we'll build an Application that provides Rising & Setting time of Sun and the Moon. **Rising & Setting time will be referred to as _Phasetime_ hereafter**. 

**Phasetime** can be calculated using this [Algorithm](https://web.archive.org/web/20161202180207/http://williams.best.vwh.net/sunrise_sunset_algorithm.htm). Implementation requires a date and location ( _longitude and lattitude_ ) as an input. User can provide the desired location using a Search Bar, move the Red pin or the Application will use current GPS location as default. For quick access, Application will be able to show past persisted locations by the user.


## Mockup
![Solar Calculator](https://i.imgur.com/cSeNZga.png)

Provided Mockup is a sample representation of the Application Interface. It is by no means restrictive of how your submission should look and feel. 

## Expectations
*  Requisite permissions for accessing features should be obtained from the User providing proper rationalle. 
* Generate a System tray notification ( **GCM not required** ) at the start of the **Golden Hour**
*  _For Android :_ API keys and secure tokens should be securely stored in the Application. Your application should be guarded against [Reverse Engineering](http://tinypic.com/r/24zjbe8/9). Provide explanatory comments wherever required.

## Guidelines
*  You are expected to share code hosted on a public repository at Github
* _For Android :_ You are expected to build a [Signed APK](https://developer.android.com/studio/publish/app-signing) and it should be present in the release directory.


## Bonus
* Plot lines ( As shown in Mockup ) according to Rising / Setting directions of Sun and the Moon.
