# SpotThat - Android
## About

<p><i>SpotThat</i> is a music app that utilizes the Spotify API and allows users to aggregate playlist from local areas. Spothat can perform queries and be used to view local playlist shared by other users around the world.
Additionally, users can pin their favorite playlist on the map for other users to listen to within the app!</p>
<p>Requires Android 6.0 +</p>

[Video Demo](https://www.youtube.com/watch?v=dPKEnE1twSo&ab_channel=JekkoGray)

This demo is running Android 10 on a <b>Google Pixel 2XL</b> device.

The APK to run SpotThat on your own Android device is available <a href="https://github.com/jekkogray/SpotThat/build/SpotThat-demo.apk" download> here.</a>

## SpotThat Screenshots
#### [Signin Activity](https://github.com/GWU-CSCI-4237-Fall-2020/project-2-byoi-jekko-s-solo-team/blob/master/SpotThat/app/src/main/java/com/example/spotthat/SignInActivity.kt)
<img align="left" src="/readme-imgs/SigninActivity.png" alt="Sign in Activity" width=200"/>
<blockquote>
<p>
<b>Signin Activity</b> handles login authentication using Firebase Authentication.
An error also appears if the user enters the wrong username or password.
Instead of typing the same username and password again, users can tap on Remember me. 
</p>
</blockquote>


#### [Signup Activity](https://github.com/GWU-CSCI-4237-Fall-2020/project-2-byoi-jekko-s-solo-team/blob/master/SpotThat/app/src/main/java/com/example/spotthat/SignUpActivity.kt)
<img align="left" src="/readme-imgs/SignActivity.png" alt="Sign up Activity" width=200"/>
<blockquote>
<p>
<b>Signup Activity</b> handles user registration using <b>Firebase Authentication</b>. Users are required to enter their password twice to make sure they entered the password correctly Additionally, if the user enters a registered username they will be notified accordingly.
</p>
</blockquote>

#### [Main Activity](https://github.com/GWU-CSCI-4237-Fall-2020/project-2-byoi-jekko-s-solo-team/blob/master/SpotThat/app/src/main/java/com/example/spotthat/MainActivity.kt)
<img align="left" src="/readme-imgs/MainActivity.png" alt="Main Activity" width=200"/>
<blockquote>
<p><b>Main Activity</b> is the user dashboard. Newly logged in users will have an empty spotThat feed list. Users can bring their own playlist from Spotify into the app by tapping <b>ADD YOUR SPOTIFY PLAYLIST</b>.</p>
</blockquote>

#### [Authentication](https://github.com/GWU-CSCI-4237-Fall-2020/project-2-byoi-jekko-s-solo-team/blob/master/SpotThat/app/src/main/java/com/example/spotthat/MainActivity.kt)
<img align="left" src="/readme-imgs/Authentication.png" alt="Authentication" width=200"/>
<blockquote>
<p>
<b>Authentication</b> allows users to sign in with their Spotify Application. This requests permission to enter their login of their preferred choice. Additionally if the user has Spotify installed, authentication will not require the user to login since they are already logged in on the app. 
</p>
</blockquote>

#### [SpotMap Activity](https://github.com/GWU-CSCI-4237-Fall-2020/project-2-byoi-jekko-s-solo-team/blob/master/SpotThat/app/src/main/java/com/example/spotthat/SpotMapActivity.kt)
<img align="left" src="/readme-imgs/SpotMapActivity-day.png" alt="SpotMap Activity" width=200"/>
<blockquote>
<p>
<b>SpotMap Activity</b> allows users to explore the map freely. Users can pin on any location and check out music people have shared in this app on the pinned location.
</p>
</blockquote>

#### [SpotThat Activity](https://github.com/GWU-CSCI-4237-Fall-2020/project-2-byoi-jekko-s-solo-team/blob/master/SpotThat/app/src/main/java/com/example/spotthat/SpotThatActivity.kt)
<img align="left" src="/readme-imgs/SpotThatActivity.png" alt="SpotThat Activity" width=200"/>
<blockquote>
<p>
<b>SpotThat Activity</b> views the playlist people have spotted on that location. SpotThat performs a search queue on recommended playlist based on the location pinned. Additionally, users can view spot their own playlist on the screen using <b>SpotMyPlaylist</b>.
</p>
</blockquote>

#### [Results Activity](https://github.com/GWU-CSCI-4237-Fall-2020/project-2-byoi-jekko-s-solo-team/blob/master/SpotThat/app/src/main/java/com/example/spotthat/ResultsActivity.kt)
<img align="left" src="/readme-imgs/SignActivity.png" alt="Sign up Activity" width=200"/>
<blockquote>
<p>
<b>Results Activity</b> shows the results of a query from the <b>Main Activity</b> or a pinned location in <b>SpotMap</b>.
</p>
</blockquote>


## Spotting a Song
Users can either <b> Spot This</b> or <b>Spot Here</b> depending on the Activity.

#### Spot This Example: SpotMap Results
<img align="left" src="/readme-imgs/SpotMapActivity-results.png" alt="Spot This Example: SpotMap Results" width=200"/>
<blockquote>
<p>
When a user enters a location. Users can choose <b>Spot This</b> which allows the user to keep that playlist and save it in their own spotted playlists. Users can also remove the spotted playlist by going through <b> Spot My Playlist </b> which brings up their own playlist. By tapping on (-) users can remove that spotted playlist from their own. 
</p>
</blockquote>

#### Spot Here Example: SpotMap Results
<img align="left" src="/readme-imgs/SpotMapActivity-results.png" alt="Spot Here Example: SpotMap Results" width=200"/>
<blockquote>
<p>
In <b> Spot My Playlist </b> users can view their own playlist within a pinned location. Additionally, when they enter clicking on <b>Spot Here</b> enables users to pin their own playlist in the given location for everyone to see.
</p>
</blockquote>

## Day/Night Mode
#### Day

<img align="left" src="/readme-imgs/SpotMapActivity-day.png" alt="SpotMapActivity-day" width=200"/>

#### Night

<img align="left" src="/readme-imgs/SpotMapActivity-night.png" alt="SpotMapActivity-day" width=200"/>

## Language Support

#### Tagalog 
<img align="left" src="/readme-imgs/SigninActivity-Tagalog.png" width=200"/>
<img align="left" src="/readme-imgs/MainActivity-Tagalog.png" width=200"/>
<img align="left" src="/readme-imgs/SpotThatActivity-Tagalog.png" width=200"/>








