###Building Delivery Droid Steps

1. Get a google maps API key and add it to your environment variables
2. Download Android Studio
3. Clone and import the progect in to Android Studio
4. Build and run on your android phone


## Getting a google maps api key

*You will need to enable billing to get a google maps api key. While the services that delivery droid uses are free it is possible to encur billing on that key so I can not share the one I use for the app.*

First go to the google link to get started [Get started with google maps platform](https://cloud.google.com/maps-platform/#get-started "Get started with google maps platform")
1. Go ahead and create a My Project and enable all the maps and routing stuff. 
2. It will ask you for a credif card and you must enter one.
3. Finally you will get an API key its a bunch of random letters about 30 letters long
4. On a mac you can edit your ~/.bash_profile and add the api key

    export GoogleMapsApiKey=THIS_IS_WHERE_THE_KEY_GOES
    export GoogleAnalyticsTrackingId=THIS_IS_WHERE_THE_TRACKING_ID_GOES

5. Replace **THIS_IS_WHERE_THE_KEY_GOES** with the key you got from google. On windows its a little different you can google how to set environment variables in windows.
6. If you want google analytics add a tracking id in the place of **THIS_IS_WHERE_THE_TRACKING_ID_GOES** 




## Download Android Studio

This one is easy just go to [//https://developer.android.com/studio/](//https://developer.android.com/studio/) and download AndroidStudio and install it.


## Clone this repo and import

TODO

## Build and run

TODO
