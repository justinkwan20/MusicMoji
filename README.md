
<h1 align="center">
  <br>
  <img src="https://github.com/justinkwan20/MusicMoji/blob/master/recordHeadphones.png" width="200"></a>
  <br>
  MusicMoji 🎵
  <br>
</h1>

<h4 align="center">MusicMoji is a app designed to enjoy music with emoticons 😀.</h4>

<p align="center">
  <a ref="#Introduction">Introduction</a> •
  <a ref="#key-features">Key Features</a> •
  <a ref="#download">Download</a> •
  <a ref="#Questions">Questions</a> •
  <a ref="#Problem">Report a problem & Feedback</a> 

</p>

![screenshot](https://github.com/justinkwan20/MusicMoji/blob/master/MusicMoji.png)

## 👋 Introduction
Our Android application is designed to have the user experience music in a different way. MusicMoji combines displaying the lyrics of the song with corresponding emoticons to provide an interactive way to enjoy the music you're listening to. An additional features include translation of lyrics in different languages to provide an interactive way to learn a different language.

## 🔑 Key Features

* Listen to your favorite songs on Spotify
* Translate Lyrics to English, Spanish, Portuguese, French, and Chinese!
* Listen to some songs locally on your device
* Emoticons in place of lyrics to have a more interactive way of enjoying your music
* Feel free to use our app at your next Karaoke night

<p float="left">
  <img src="https://github.com/justinkwan20/MusicMoji/blob/master/titleScreen2.png" width="23%" />
  <img src="https://github.com/justinkwan20/MusicMoji/blob/master/Description2.png" width="23%" /> 
  <img src="https://github.com/justinkwan20/MusicMoji/blob/master/languageNew2.png" width="23%" />
  <img src="https://github.com/justinkwan20/MusicMoji/blob/master/example2.png" width="23%" />
</p>

## 📲 Download
Feel free to download our app and login with your Spotify Account to try it out and its totally FREE!

## 📬 Questions
Any concerns or problems feel free to reach out and contact me and I can help address any issues that you maybe facing. I am always here and happy to help 😀.

## 🤝 Report a problem & Feedback
Feel free to file a new issue with a respective title and description on the repository. If you already found a solution to your problem, we would love to review your pull request!

## 🔧 How it works
- Login and Authentication is through Spotify Api and signing into user's Account.
- The lyrics are retrieved through ApiSeeds lyrics which allowed retrieval of lyrics based of song title and artist which was obtained through Spotify.
- The emoji's were parsed though the lyrics and the use of a java library which matches corresponding emoji's to the specific words we wanted.
- Translation as done through IBM Watson Translation API and we translated from the language which lyrics were given to the target language. 
- Some songs used for demonstration purposes and to show some sample songs were stored using a firebase database. 
