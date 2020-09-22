package com.example.musicmoji;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.language_translator.v3.LanguageTranslator;
import com.ibm.watson.language_translator.v3.model.TranslateOptions;
import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CountDownLatch;


/*
    Most of the stuff in here is all temporary just to test it.
    Need to modify for API calls.
 */
public class PlaySong extends AppCompatActivity {

    Button playBtn;
    SeekBar timeBar;

    TextView songTitle;
    TextView songArtist;
    TextView endTime;
    TextView elapsedTime;
    TextView lyric_container;

    String strTitle;


    MediaPlayer mp;
    int totalTime;

    String preferredLanguage;
    String lyricsLanguageTag ="en";
    // If no emojis are generated during lyricProcessing(), we'll retry once as if the song language and language preference are in English
    // If it fails again, we'll just return the original lyrics
    Boolean retried = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_song);

        // Initialize variables
        playBtn = (Button) findViewById(R.id.playBtn);
        songTitle = (TextView) findViewById(R.id.songTitle);
        songArtist = (TextView) findViewById(R.id.songArtist);
        endTime = (TextView) findViewById(R.id.endTime);
        elapsedTime = (TextView) findViewById(R.id.elapsedTime);
        lyric_container = (TextView) findViewById(R.id.lyric_container);


        // Get the intent and set the title and artist
        Intent intent = getIntent();
        songTitle.setText(intent.getStringExtra("Title"));
        songArtist.setText(intent.getStringExtra("Artist"));

        strTitle = intent.getStringExtra("Title");

        // set the movement for the scroll bar for lyric container
        lyric_container.setMovementMethod(new ScrollingMovementMethod());

        // check the song title and load the correct songs that is preloaded into the app
        if (strTitle.equals("Boulevard of Broken Dreams")) {
            mp = MediaPlayer.create(this, R.raw.boulevard_of_broken_dreams);
        }
        else if(strTitle.equals("Viva La Vida")) {
            mp = MediaPlayer.create(this, R.raw.viva_la_vida);
        }
        else if(strTitle.equals("Halo")) {
            mp = MediaPlayer.create(this, R.raw.halo);
        }
        else if(strTitle.equals("Fireflies")) {
            mp = MediaPlayer.create(this, R.raw.fireflies);
        }
        else if(strTitle.equals("Rolling in the Deep")) {
            mp = MediaPlayer.create(this, R.raw.rolling_in_the_deep);
        }
        else if(strTitle.equals("Sugar")) {
            mp = MediaPlayer.create(this, R.raw.sugar);
        }
        else if(strTitle.equals("Royals")) {
            mp = MediaPlayer.create(this, R.raw.royals);
        }
        else if(strTitle.equals("We Are Young")) {
            mp = MediaPlayer.create(this, R.raw.we_are_young);
        }
        else if(strTitle.equals("Counting Stars")) {
            mp = MediaPlayer.create(this, R.raw.counting_stars);
        }
        else {
            mp = MediaPlayer.create(this, R.raw.castle_on_the_hill);
        }

        retrieveSharedPreferenceInfo(); // Set preferredLanguage to whatever's saved

        // Doing it like this avoids NetworkOnMainThreadException
        final CountDownLatch latch = new CountDownLatch(1);
        new Thread(new Runnable(){
            @Override
            public void run(){
                lyricProcessing();
                latch.countDown();
            }
        }).start();
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        // loop through the song when finished
        mp.setLooping(true);
        // start at 0
        mp.seekTo(0);
        // get the total time of song
        totalTime = mp.getDuration();
        // set endTime
        endTime.setText(createTimeLabel(totalTime));
        mp.start();


        //seek bar initialization and listener
        //updates seek bar by user action
        timeBar = (SeekBar) findViewById(R.id.timeBar);
        timeBar.setMax(totalTime);
        timeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mp.seekTo(progress);
                    timeBar.setProgress(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        // sets the button listener to listen to user clicks
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // If song is not playing, start song
                if (!mp.isPlaying()){
                    mp.start();
                    playBtn.setBackgroundResource(R.drawable.pause);
                }
                else {
                    // if song is playing, pause it
                    mp.pause();
                    playBtn.setBackgroundResource(R.drawable.play);
                }
            }
        });


        // Temp for media player
        // Create thread to update timeBar progress and the elapsed time
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mp != null) {
                    try {
                        Message msg = new Message();
                        msg.what = mp.getCurrentPosition();
                        handler.sendMessage(msg);

                        Thread.sleep(1000);
                    } catch (InterruptedException e) {

                    }
                }
            }
        }).start();
    }

    // Override the original onPause by stopping the song whenever
    // the user leaves the activity
    @Override
    public void onPause() {
        super.onPause();
        // stop and reset song so it doesn't play in the
        // background even when you left the activity
        mp.stop();
        mp.reset();
    }

    // Media player handler to handle the changes
    // as the thread runs to progress the seekbar
    // and update the time
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            int currentPosition = msg.what;
            timeBar.setProgress(currentPosition);

            //Update time label
            String elapsedtime = createTimeLabel(currentPosition);
            elapsedTime.setText(elapsedtime);
        }
    };

    // Create the label/string to set for the times
    public String createTimeLabel(int time) {
        String timelabel = "";
        int min = time / 1000 / 60;
        int sec = time / 1000 % 60;

        timelabel = min + ":";
        if (sec < 10) timelabel += "0";
        timelabel += sec;

        return timelabel;
    }

    // a single onClick function (early binding) declared in xml file
    public void goBack(View view) {
        // stop and reset song so it doesn't play in the
        // background even when you left the activity
        mp.stop();
        mp.reset();
        // go back to previous activity
        this.finish();
    }

    public void lyricProcessing() {

        // If there's no preferred language, just keep the language that the song is in
        // If the lyric API call didn't return a language tag, just default it to English
        if (preferredLanguage.equals("") || preferredLanguage.equals("<missing>")) {
            if (!lyricsLanguageTag.equals("")) {
                preferredLanguage = lyricsLanguageTag;
            } else {
                preferredLanguage = "en";
            }
        }

        String lyrics = ("*I walk a lonely road\r\nThe only one that I have ever known\r\nDon't know where it goes\r\nBut it's home to me and I walk alone\r\n\r\nI walk this empty street\r\nOn the Boulevard of Broken Dreams\r\nWhere the city sleeps\r\nand I'm the only one and I walk alone\r\n\r\nI walk alone\r\nI walk alone\r\n\r\nI walk alone\r\nI walk a...\r\n\r\nMy shadow's the only one that walks beside me\r\nMy shallow heart's the only thing that's beating\r\nSometimes I wish someone out there will find me\r\n'Til then I walk alone\r\n\r\nAh-ah, Ah-ah, Ah-ah, Aaah-ah,\r\nAh-ah, Ah-ah, Ah-ah\r\n\r\nI'm walking down the line\r\nThat divides me somewhere in my mind\r\nOn the border line\r\nOf the edge and where I walk alone\r\n\r\nRead between the lines\r\nWhat's fucked up and everything's alright\r\nCheck my vital signs\r\nTo know I'm still alive and I walk alone\r\n\r\nI walk alone\r\nI walk alone\r\n\r\nI walk alone\r\nI walk a...\r\n\r\nMy shadow's the only one that walks beside me\r\nMy shallow heart's the only thing that's beating\r\nSometimes I wish someone out there will find me\r\n'Til then I walk alone\r\n\r\nAh-ah, Ah-ah, Ah-ah, Aaah-ah\r\nAh-ah, Ah-ah\r\n\r\nI walk alone\r\nI walk a...\r\n\r\nI walk this empty street\r\nOn the Boulevard of Broken Dreams\r\nWhere the city sleeps\r\nAnd I'm the only one and I walk a...\r\n\r\nMy shadow's the only one that walks beside me\r\nMy shallow heart's the only thing that's beating\r\nSometimes I wish someone out there will find me\r\n'Til then I walk alone...").replace("\n", " *\n* ").replace("\r", " *\r* ").replace(",", " ,").replace("?", " ?").replace(".", " .");
        //String lyrics = ("I walk a lonely road\r\nThe only one that I have ever known\r\nDon't know where it goes\r\nBut it's home to me and I walk alone\r\n\r\nI walk this empty street\r\nOn the Boulevard of Broken Dreams\r\nWhere the city sleeps\r\nand I'm the only one and I walk alone\r\n\r\nI walk alone\r\nI walk alone\r\n\r\nI walk alone\r\nI walk a...\r\n\r\nMy shadow's the only one that walks beside me\r\nMy shallow heart's the only thing that's beating\r\nSometimes I wish someone out there will find me\r\n'Til then I walk alone\r\n\r\nAh-ah, Ah-ah, Ah-ah, Aaah-ah,\r\nAh-ah, Ah-ah, Ah-ah\r\n\r\nI'm walking down the line\r\nThat divides me somewhere in my mind\r\nOn the border line\r\nOf the edge and where I walk alone\r\n\r\nRead between the lines\r\nWhat's fucked up and everything's alright\r\nCheck my vital signs\r\nTo know I'm still alive and I walk alone\r\n\r\nI walk alone\r\nI walk alone\r\n\r\nI walk alone\r\nI walk a...\r\n\r\nMy shadow's the only one that walks beside me\r\nMy shallow heart's the only thing that's beating\r\nSometimes I wish someone out there will find me\r\n'Til then I walk alone\r\n\r\nAh-ah, Ah-ah, Ah-ah, Aaah-ah\r\nAh-ah, Ah-ah\r\n\r\nI walk alone\r\nI walk a...\r\n\r\nI walk this empty street\r\nOn the Boulevard of Broken Dreams\r\nWhere the city sleeps\r\nAnd I'm the only one and I walk a...\r\n\r\nMy shadow's the only one that walks beside me\r\nMy shallow heart's the only thing that's beating\r\nSometimes I wish someone out there will find me\r\n'Til then I walk alone...");//.replace("\n", " *\n* ").replace("\r", " *\r* ").replace(",", " ,").replace("?", " ?").replace(".", " .");
        //String lyrics = ("*I walk a lonely road\r\nThe only one that I have ever known\r\nDon't know where it goes\r\nBut it's home to me and I walk alone\r\n\r\nI walk this empty street\r\nOn the Boulevard of Broken Dreams\r\nWhere the city sleeps\r\nand I'm the only one and I walk alone\r\n\r\nI walk alone\r\nI walk alone\r\n\r\nI walk alone\r\nI walk a...\r\n\r\nMy shadow's the only one that walks beside me\r\nMy shallow heart's the only thing that's beating\r\nSometimes I wish someone out there will find me\r\n'Til then I walk alone\r\n\r\nAh-ah, Ah-ah, Ah-ah, Aaah-ah,\r\nAh-ah, Ah-ah, Ah-ah\r\n\r\nI'm walking down the line\r\nThat divides me somewhere in my mind\r\n'Til then I walk alone...").replace("\n", " *\n* ").replace("\r", " *\r* ").replace(",", " ,").replace("?", " ?").replace(".", " .");

        //String lyrics = ("*Ay\r\nOh no\r\nOh no\r\nHey yeah\r\n\r\nSim, já faz um tempo que eu 'to te olhando \r\nHoje eu só danço com você \r\nSei o seu olhar já 'tava me chamando \r\nChego até sem você perceber \r\n\r\nVocê tem a força do imã e eu sou o metal \r\nVocê dançando altera meu emocional \r\nE só de pensar nisso quase explode o peito \r\nJá fiquei encantado mais do que o normal \r\nConfunde meus sentidos, 'to passando mal \r\nVamos devagar vou te mostrar meu jeito \r\n\r\nDevagar \r\nSó quero sentir teu corpo devagar \r\nFalar ao teu ouvido, até te arrepiar \r\nQuando ficar sozinha vai lembrar de mim \r\nDevagar \r\nQuero beijar teus lábios muito devagar \r\nOuvir os teus gemidos te fazer voar \r\nNão, eu tenho pressa gosto mesmo assim \r\n\r\nQuero o balanço do cabelo \r\nQuero ser seu ritmo \r\nMostra para minha boca \r\nOs seus lugares favoritos \r\nMe dê mais tempo para conhecer os seus caminhos \r\nSeus segredos escondidos \r\nVão se revelar comigo \r\nDespacito \r\n\r\nSei que sua pele já está me esperando \r\nVou caindo nessa tentação \r\nVem porque essa noite só 'tá começando \r\nVamos mergulhar nessa paixão \r\n\r\nVocê tem a força do imã e eu sou o metal \r\nVocê dançando altera meu emocional \r\nE só de pensar nisso quase explode o peito \r\nJá fiquei encantado mais do que o normal \r\nConfunde meus sentidos, 'to passando mal \r\nVamos devagar vou te mostrar meu jeito \r\n\r\nDevagar \r\nSó quero sentir teu corpo devagar \r\nFalar ao teu ouvido, até te arrepiar \r\nQuando ficar sozinha vai lembrar de mim \r\nDevagar \r\nQuero beijar teus lábios muito devagar \r\nOuvir os teus gemidos te fazer voar \r\nEu não tenho pressa gosto mesmo assim \r\n\r\nQuero o balanço do cabelo quero ser seu ritmo \r\nMostra para minha boca \r\nOs seus lugares favoritos \r\nMe dê mais tempo para conhecer os seus caminhos \r\nSeus segredos escondidos \r\nVão se revelar comigo \r\nDespacito \r\n\r\nVamos a hacerlo en una playa en puerto rico \r\nHasta que las olas griten ¡ay, bendito! \r\nPara que mi sello se quede contigo \r\n\r\nQuero o balanço do cabelo \r\nQuero ser seu ritmo \r\nMostra para minha boca \r\nOs seus lugares favoritos \r\nMe dê mais tempo para conhecer os seus caminhos \r\nSeus segredos escondidos \r\nVão se revelar comigo \r\nDespacito").replace("\n", " *\n* ").replace("\r", " *\r* ").replace(",", " ,").replace("?", " ?").replace(".", " .");

        //String lyrics2 = "";

        //String lyrics = "sad sad sad sad sad sad sad sad sad sad";

        // SHORT_WORDS contains all the 2-letter words that will match emojis
        // all others would return a flag emoji (song lyrics would most likely use the full name of the country,
        // not the abbreviation, so these would be errors) or cause problems with the checks
        final HashSet<String> SHORT_WORDS = new HashSet<>(Arrays.asList("cd", "id", "ok", "on", "ox", "tv", "up", "vs"));

        // All of these are valid English words (possible lyrics) that will return an emoji that does not fit the meaning
        // Generated by applying the reverse version of the checks on each tag and alias
        // (ex. alias "struggling" -> struggling -> struggl, struggle, strugg, struggler),
        // checked for validity using Python's nltk.corpus package,
        // and going through results manually to determine if emoji is correct
        final HashSet<String> WILL_RETURN_INCORRECT_EMOJI = new HashSet<>(Arrays.asList("aer", "aes", "ager", "agin'", "aging",
                "ain't", "ary", "ass", "awin'", "awing", "bac", "badge", "badgerer", "ban", "barb", "barbe", "bas", "bater", "batin'",
                "bating", "batter", "bearder", "beardin'", "bearding", "bearer", "bearin'", "bearing", "bein'", "being", "bel", "belly",
                "beni", "bent", "bentin'", "benting", "ber", "bes", "bice", "bier", "bin'", "bing", "bis", "blindlin'", "blindling",
                "blo", "bloc", "boa", "board", "bom", "bon", "bonair", "boo", "boomer", "booty", "bowl", "boyer", "brin'", "bring", "broo",
                "bur", "burg", "buss", "busy", "cal", "came", "cand", "carer", "cater", "card", "cha", "char", "chil", "chin", "chin'",
                "ching", "cho", "clappin'", "clapping", "class", "clin'", "cling", "clow", "club", "coater", "coatin'",
                "coating", "col", "colin'", "coling", "come", "consol", "consolin'", "consoling", "coo", "cooin'", "cooing",
                "coper", "copin'", "coping", "copy", "cor", "corin'", "coring", "corner", "cos", "coucher", "couchin'", "couching",
                "couplin'", "coupling", "cower", "craber", "cran", "crea", "creamer", "creamy", "cricketer", "cricketin'", "cricketing",
                "crow", "crowin'", "crowing", "crus", "crusher", "crushin'", "crushing", "cub", "curr", "custom", "dang", "dar",
                "darin'", "daring", "darter", "das", "dee", "deserter", "dicin'", "dicing", "din'", "ding", "director", "doer", "doin'",
                "doing", "dos", "drago", "dram", "dresser", "dressin'", "dressing", "duckin'", "ducking", "earin'", "earing",
                "eer", "eggin'", "egging", "eighty", "engage", "envelop", "errin'", "erring", "ers", "ess", "factor", "fair", "fairin'",
                "fairing", "fastin'", "fasting", "fee", "fence", "filin'", "filing", "filmy", "fir", "flighter", "flightin'", "flighting",
                "flow", "flush", "fogy", "foo", "fou", "fourer", "franc", "fro", "galler", "gam", "gas", "ger", "gif", "gin'", "ging",
                "goa", "gol", "grapin'", "graping", "gree", "grind", "grouper", "halter", "han", "hander", "happin'", "happing", "hasher",
                "hater", "helin'", "heling", "hin'", "hing", "hippin'", "hipping", "hop", "huer", "icin'", "icing", "ide",
                "innin'", "inning", "iter", "its", "it's", "jeer", "jin'", "jing", "kier", "kin", "land", "las", "lawin'", "lawing",
                "lea", "leafer", "leave", "ledge", "ledgin'", "ledging", "leger", "lier", "lin", "lin'", "ling", "linin'",
                "lining", "lis", "loo", "lys", "mal", "malt", "maltin'", "malting", "mang", "many", "mas", "mil", "min'", "ming", "mone",
                "moo", "mooin'", "mooing", "mooner", "moonin'", "mooning", "neer", "newin'", "newing", "nig", "nigh", "not",
                "numb", "numbin'", "numbing", "ode", "oer", "offer", "offin'", "offing", "omer", "oner", "ons", "owin'", "owing",
                "owler", "owlin'", "owling", "page", "pain", "painin'", "paining", "pant", "pantin'", "panting", "park",
                "parkin", "part", "past", "pastin'", "pasting", "pea", "peacher", "peer", "per", "pes", "peter", "phon", "phot",
                "pian", "picker", "pilin'", "piling", "pin'", "ping", "plan", "pooler", "pounder", "poundin'", "pounding",
                "pow", "presenter", "pressin'", "pressing", "pridin'", "priding", "privateer", "pursed", "rabbi", "rag", "rainer", "rame",
                "rater", "ratin'", "rating", "register", "rewin", "ringin'", "ringing", "roc", "roer", "ruer", "ruin'", "ruing",
                "sal", "sant", "scar", "scoot", "scout", "scoutin'", "scouting", "scree", "screener", "screenin'", "screening",
                "secre", "seed", "seein'", "seeing", "seer", "senega", "ser", "sevener", "shee", "sher", "shi", "shiel", "shielin'",
                "shieling", "shin", "sho", "shoo", "show", "showin'", "showing", "sic", "sier", "sill", "sin", "sis", "sker", "skid",
                "slin'", "sling", "sober", "soss", "spade", "spaer", "sparkless", "speeder", "speedin'", "speeding", "spor",
                "springer", "springin'", "springing", "starer", "starin'", "staring", "startin'", "starting", "stin'", "sting",
                "stoper", "stopin'", "stoping", "sunn", "swa", "sweater", "swing", "tad", "targe", "tax", "taxin'", "taxing", "tearer",
                "tearin'", "tearing", "ten", "theat", "thin", "thin'", "thing", "tig", "tige", "tin'", "ting", "tire", "tog",
                "tong", "too", "toot", "tra", "track", "trainer", "trainin'", "training", "trainy", "ust", "vas", "veer", "vier", "vis",
                "wale", "wat", "wear", "wearin'", "wearing", "weatherin'", "weathering", "wer", "whin", "win'", "winder",
                "windin'", "winding", "wing", "wint", "wiper", "wips", "wis", "woo", "wooin'", "wooing", "yar", "yer", "zer",
                "zin'", "zing", "zombi", "zoo"));

// If song does not return lyrics for some reason, no need to go through the whole checking process
        if (lyrics.equals("")) { //TODO fix when we can get the response for no lyrics --> maybe a try-catch


            lyric_container.setText("Lyrics unavailable for this song");
            return;
        }

        else {

            lyricsLanguageTag = "en"; //TODO get language from lyrics

            // This will get set to true if any lyric is converted to an emoji
            // If it's false at the end, there was most likely an issue with translation/language tags
            Boolean  canGenerateEmojis = false;

            // So we can go through word by word
            String[] lyricList = lyrics.split("\\*");
            // Build a new string so we can include the emojis
            StringBuilder emojiLyrics = new StringBuilder(); // Final string

            // Get ready for translation
            /* Has to be declared for translations to work, and it's better to declare it once here and have it
             *  not be needed than to do it for every line when it is needed (putting it in an if statement here doesn't work) */
            IamAuthenticator authenticator = new IamAuthenticator("CVF005Kw664hmGIKkrb0Ne32LDR1JXK9hN3428wrABwx");
            LanguageTranslator languageTranslator = new LanguageTranslator("2018-05-01", authenticator);
            languageTranslator.setServiceUrl("https://api.eu-gb.language-translator.watson.cloud.ibm.com/instances/f7e68812-ed0c-449f-8af5-3beb318abde6");


// 1. Read each line. Translate if need be then split on spaces so we can read each word

            // If we need to translate to English or between non-English languages, do it line by line
            for (String line : lyricList) {
                StringBuilder lineLyrics = new StringBuilder();

                if (!lyricsLanguageTag.equals(preferredLanguage) && line.trim().equals("")) { // Something in the split made lines with just spaces part of the list (only a problem when translating)
                    continue;
                }

                // Line break tags will be removed by the translator
                if (line.contains("\n") || line.contains("\r") || line.contains("\r\n") || line.contains("\r\n\r\n")) {
                    emojiLyrics.append(line); // If we need to translate, line breaks trigger the "Translation unavailable" response
                    continue;
                }

                // Emoji checking only works for English words
                // If it's a non-English song with no translation selected, we'll do the translations to get emojis during inner loop (since we don't need to display original lyrics separately)
                if (!lyricsLanguageTag.equals("en") && !lyricsLanguageTag.equals(preferredLanguage)) {
                    emojiLyrics.append(line).append("\n"); // We'll display the original lyrics then the translated ones on the next line

                    // Words like 'Til mess with the translation
                    if (line.startsWith("'")) {
                        line.replace("'", "");
                    }
                    if (!lyricsLanguageTag.equals("")) {
                        try {
                            TranslateOptions translateOptions = new TranslateOptions.Builder()
                                    .addText(line)
                                    .modelId(lyricsLanguageTag + "-en")
                                    .build();

                            line = languageTranslator.translate(translateOptions)
                                    .execute().getResult().getTranslations().get(0).getTranslation();
                        } catch (Exception e){
                            try {
                                TranslateOptions translateOptions = new TranslateOptions.Builder()
                                        .addText(line)
                                        .target("en")
                                        .build();

                                line = languageTranslator.translate(translateOptions)
                                        .execute().getResult().getTranslations().get(0).getTranslation();
                            } catch (Exception e2) {
                                // Translator can't determine source language

                                emojiLyrics.append("(Translation unavailable (translator cannot determine source language)) \n\n");
                                continue;
                            }
                        }
                    }
                }


                String[] lineList = line.split(" ");

// 2. Read each word. The words will need to be in English if they aren't already.
//    If it's a non-English song and we are not translating it, we need to translate it word-by-word to English

                for (String lyric : lineList) {

                    String originalLyric = "";

                    // If a non-English song doesn't need a translation, translate word by word
                    // For each word, add the original word to lineLyrics and just use English translation to match emojis,
                        // adding the emoji after the original word
                    if (!lyricsLanguageTag.equals("en") && lyricsLanguageTag.equals(preferredLanguage)) {
                        originalLyric = lyric;

                        // Words like 'Til mess with the translation
                        if (lyric.startsWith("'")) {
                            lyric.replace("'", "");
                        }

                        try {
                            TranslateOptions translateOptions = new TranslateOptions.Builder()
                                    .addText(lyric)
                                    .modelId(lyricsLanguageTag + "-en")
                                    .build();

                            lyric = languageTranslator.translate(translateOptions)
                                    .execute().getResult().getTranslations().get(0).getTranslation();
                        } catch (Exception e) {
                            try {
                                TranslateOptions translateOptions = new TranslateOptions.Builder()
                                        .addText(lyric)
                                        .target("en")
                                        .build();

                                lyric = languageTranslator.translate(translateOptions)
                                        .execute().getResult().getTranslations().get(0).getTranslation();
                            } catch (Exception e2) {
                                // Translator can't determine source language
                            }
                        }

                    }
                    if (!originalLyric.equals("")) {
                        lineLyrics.append(originalLyric).append(" ");
                    } else {
                        lineLyrics.append(lyric).append(" ");
                    }

// 3. Convert lyrics to emojis

                    // All the emoji tags and aliases are in lower case
                    lyric = lyric.toLowerCase();


                    // We don't need single letter emojis (or number emojis if the lyric is in number form anyway
                    if (lyric.length() <= 2 && !SHORT_WORDS.contains(lyric)) {
                        continue;
                    }


                    if (WILL_RETURN_INCORRECT_EMOJI.contains(lyric)
                            || (lyric.length() == 4 && lyric.charAt(2) == '\'')) { // These are for 2 letter words that have an 's at
                        continue;                                                  // the end
                    }


// 3a. These are some possible exceptions to the checks (detailed later; they would return an incorrect emoji or none at all)
// It's easier just to check for them individually than try to make a rule
                    // -hard to define
                    // -only one occurrence (faster for everything else to make this one check than call getForAlias and getForTag an extra time for a new rule)
                    // -would interfere with other emojis
                    // -alias has underscores and no tags (would never match a lyric)
                    // - etc.

                    // Returns person emoji
                    // Would not return anything but the emoji fits the word
                    if (lyric.equals("face")) {
                        lineLyrics.append("(\uD83E\uDDD1) ");
                        canGenerateEmojis = true;
                        continue;
                    }

                    // Returns man emoji
                    // Would not return anything but the emoji fits the words
                    if (lyric.equals("he") || lyric.equals("him") || lyric.equals("his") || lyric.equals("he's")) {
                        lineLyrics.append("(\uD83D\uDC68) ");
                        canGenerateEmojis = true;
                        continue;
                    }

                    // Returns woman emoji
                    // Would not return anything but the emoji fits the words
                    if (lyric.equals("she") || lyric.equals("her") || lyric.equals("hers") || lyric.equals("she's")) {
                        lineLyrics.append("(\uD83D\uDC69) ");
                        canGenerateEmojis = true;
                        continue;
                    }

                    // Returns American flag emoji
                    //) "us" is the alias, but it's an actual word and these words are more likely to appear in a song anyway
                    if (lyric.equals("usa") || lyric.equals("u.s.a.") || lyric.equals("american") || lyric.equals("america")) {
                        lineLyrics.append("(\uD83C\uDDFA\uD83C\uDDF8) ");
                        canGenerateEmojis = true;
                        continue;
                    }

                    // Returns ocean emoji
                    // Would return waving hand in plural check
                    if (lyric.equals("waves")) {
                        lineLyrics.append("(\uD83C\uDF0A) ");
                        canGenerateEmojis = true;
                        continue;
                    }

                    // Returns lying face emoji
                    // The alias contains underscores and there are no tags
                    if (lyric.equals("lying") || lyric.equals("lie") || lyric.equals("lyin'") || lyric.equals("lies")
                            || lyric.equals("liar")) {
                        lineLyrics.append("(\uD83E\uDD25) ");
                        canGenerateEmojis = true;
                        continue;
                    }

                    // Returns punch emoji
                    // Would not return anything but this emoji fits the word
                    if (lyric.equals("fight") || lyric.equals("fights")) {
                        lineLyrics.append("(\uD83D\uDC4A) ");
                        canGenerateEmojis = true;
                        continue;
                    }

                    // Returns foot emoji
                    // Would not return anything but this emoji fits the word
                    if (lyric.equals("kick") || lyric.equals("kicking")) {
                        lineLyrics.append("(\uD83E\uDDB6) ");
                        canGenerateEmojis = true;
                        continue;
                    }

                    // Returns smiling devil emoji
                    // Would not return anything but this emoji fits the word
                    if (lyric.equals("bad")) {
                        lineLyrics.append("(\uD83D\uDE08) ");
                        canGenerateEmojis = true;
                        continue;
                    }

                    // Returns brain emoji
                    // Would not return anything but this emoji fits the word
                    if (lyric.equals("mind")) {
                        lineLyrics.append("(\uD83E\uDDE0) ");
                        canGenerateEmojis = true;
                        continue;
                    }

                    // Returns boy emoji
                    // There is no alias or tag for) "kid"
                    if (lyric.equals("kid") || lyric.equals("kids")) {
                        lineLyrics.append("(\uD83D\uDC3A) ");
                        canGenerateEmojis = true;
                        continue;
                    }

                    // Returns dancer emoji
                    // A rule for lyric + last letter + -er words (win -> winner) would have too many exceptions
                    if (lyric.equals("dance") || lyric.equals("dances") || lyric.equals("danced")) {
                        lineLyrics.append("(\uD83C\uDFC3) ");
                        canGenerateEmojis = true;
                        continue;
                    }

                    // Returns trophy emoji
                    // A rule for lyric + last letter + -er words (win -> winner) would have too many exceptions
                    if (lyric.equals("win")) {
                        lineLyrics.append("(\uD83C\uDFC3) ");
                        canGenerateEmojis = true;
                        continue;
                    }

                    // Returns runner emoji
                    // A rule for lyric + last letter + -er words (run -> runner) would have too many exceptions
                    // Past tense is irregular
                    if (lyric.equals("run") || lyric.equals("ran")) {
                        lineLyrics.append("(\uD83C\uDFC3) ");
                        canGenerateEmojis = true;
                        continue;
                    }

                    // Returns fishing pole emoji
                    // The alias contains underscores and there are no tags
                    if (lyric.equals("fishing")) {
                        lineLyrics.append("(\uD83c\uDFA3) ");
                        canGenerateEmojis = true;
                        continue;
                    }

                    // Returns tree emoji
                    // There are no emojis with just "tree" as a tag or alias
                    if (lyric.equals("tree") || lyric.equals("trees")) {
                        lineLyrics.append("(\uD83C\uDF33) ");
                        canGenerateEmojis = true;
                        continue;
                    }

                    // Returns swimmer emoji
                    // A rule for lyric + last letter + -er words (swim -> swimmer) would have too many exceptions
                    // Past tense is irregular
                    if (lyric.equals("swim") || lyric.equals("swam")) {
                        lineLyrics.append("(\uD83C\uDFCA) ");
                        canGenerateEmojis = true;
                        continue;
                    }

                    // Returns cityscape emoji
                    // There is no alias or tag for "city"
                    if (lyric.equals("city") || lyric.equals("cities")) {
                        lineLyrics.append("(\uD83C\uDFD9) ");
                        canGenerateEmojis = true;
                        continue;
                    }

                    // Returns game die emoji
                    // Would return poop emoji but this fits the word
                    if (lyric.equals("craps")) {
                        lineLyrics.append("(\uD83C\uDFB2) ");
                        canGenerateEmojis = true;
                        continue;
                    }

                    // Returns newspaper emoji
                    // Would not return anything but this fits the word
                    if (lyric.equals("news")) {
                        lineLyrics.append("(\uD83D\uDCF0) ");
                        canGenerateEmojis = true;
                        continue;
                    }

                    // Returns music notes emoji
                    // Would not return anything but this fits the word
                    if (lyric.equals("rhythm")) {
                        lineLyrics.append("(\uD83C\uDFB6) ");
                        canGenerateEmojis = true;
                        continue;
                    }

                    // Returns mouth emoji
                    // Would not return anything because "mouth" is not an alias or tag
                    if (lyric.equals("mouth")) {
                        lineLyrics.append("(\uD83D\uDC44) ");
                        canGenerateEmojis = true;
                        continue;
                    }

                    // Returns page facing up emoji
                    // Would not return anything but this fits the word
                    if (lyric.equals("page")) {
                        lineLyrics.append("(\uD83D\uDCC4) ");
                        canGenerateEmojis = true;
                        continue;
                    }

                    // Returns microphone emoji
                    // Would not return anything but this fits the word
                    if (lyric.equals("singers")) {
                        lineLyrics.append("(\uD83C\uDFA4) ");
                        canGenerateEmojis = true;
                        continue;
                    }

                    // Returns speaking head in silhouette emoji
                    // Would not return anything but this fits the words
                    if (lyric.equals("speak") || lyric.equals("speaks") || lyric.equals("speaking") || lyric.equals("speakin'")) {
                        lineLyrics.append("(\uD83D\uDDE3) ");
                        canGenerateEmojis = true;
                        continue;
                    }

                    // Returns spy emoji
                    // Would not return anything but this fits the words
                    if (lyric.equals("sneak") || lyric.equals("sneaks") || lyric.equals("sneaky") || lyric.equals("sneaking") ||
                            lyric.equals("sneakin'")) {
                        lineLyrics.append("(\uD83D\uDD75) ");
                        canGenerateEmojis = true;
                        continue;
                    }

                    // Returns cyclone emoji
                    // Would not return anything but this fits the word
                    if (lyric.equals("hurricane")) {
                        lineLyrics.append("(\uD83C\uDF00) ");
                        canGenerateEmojis = true;
                        continue;
                    }

                    // Returns thunder cloud and rain emoji
                    // Would not return anything but this fits the word
                    if (lyric.equals("storm") || lyric.equals("storms") || lyric.equals("stormy") || lyric.equals("thunderstruck")) {
                        lineLyrics.append("(\u26C8) ");
                        canGenerateEmojis = true;
                        continue;
                    }

                    // Returns speaking head in silhouette emoji
                    // Would not return anything but this fits the words
                    if (lyric.equals("street") || lyric.equals("streets")) {
                        lineLyrics.append("(\uD83D\uDEE3) ");
                        canGenerateEmojis = true;
                        continue;
                    }

                    // Returns honey pot emoji
                    // The alias contains underscores and there are no tags
                    if (lyric.equals("honey")) {
                        lineLyrics.append("(\uD83C\uDF6F) ");
                        canGenerateEmojis = true;
                        continue;
                    }

                    // Returns lightning bolt emoji
                    // Would not return anything but this fits the word
                    if (lyric.equals("electric") || lyric.equals("electricity")) {
                        lineLyrics.append("(\u26A1) ");
                        canGenerateEmojis = true;
                        continue;
                    }

                    // Returns sleepy face emoji
                    // Would not return anything but this fits the word
                    if (lyric.equals("sleep") || lyric.equals("sleeps")) {
                        lineLyrics.append("(\uD83D\uDE34) ");
                        canGenerateEmojis = true;
                        continue;
                    }

                    // Returns walking emoji
                    // Would not return anything but this fits the word
                    if (lyric.equals("walks")) {
                        lineLyrics.append("(\uD83D\uDEB6) ");
                        canGenerateEmojis = true;
                        continue;
                    }

                    // Returns panda face emoji
                    // The alias contains underscores and there are no tags
                    if (lyric.equals("panda")) {
                        lineLyrics.append("(\uD83D\uDC3C) ");
                        canGenerateEmojis = true;
                        continue;
                    }

                    // Returns bust in silhouette emoji
                    // Would not return anything but this fits the word
                    if (lyric.equals("shadow") || lyric.equals("shadows") || lyric.equals("shadow's")) {
                        lineLyrics.append("(\uD83D\uDC64) ");
                        canGenerateEmojis = true;
                        continue;
                    }

                    // Returns wolf face emoji
                    // The plural follows the -f -> -ves rule and it is the only emoji like this
                    //      -> This is faster than adding a rule to the plural checks
                    if (lyric.equals("wolves")) {
                        lineLyrics.append("(\uD83D\uDC3A) ");
                        canGenerateEmojis = true;
                        continue;
                    }

                    // Returns castle emoji
                    // The alias contains underscores and there are no tags
                    if (lyric.equals("castle")) {
                        lineLyrics.append("(\uD83C\uDFF0) ");
                        canGenerateEmojis = true;
                        continue;
                    }

// End of exceptions

// 3b. Try matching for the alias

                    // Unicode will display as emoji in the app
                    // "(emoji) "
                    Emoji emoji = EmojiManager.getForAlias(lyric);
                    if (emoji != null) { // Match found
                        lineLyrics.append("(").append(emoji.getUnicode()).append(") ");
                        canGenerateEmojis = true;
                        continue;
                    }

// 3c. Try matching the tags

                    // If no alias match, try to match the tags
                    // This returns a Set<Emoji> (because there might be more than one match for tags)
                    Set<Emoji> emojis = EmojiManager.getForTag(lyric); // method from emoji library
                    if (emojis != null) { // Matches found
                        // Pick a random number index in range(emojis.size())
                        // Iterate index - 1 times
                        // The final iteration gives the chosen random emoji, so add it to lineLyrics
                        Random rand = new Random();
                        int index = rand.nextInt(emojis.size());
                        Iterator iter = emojis.iterator();
                        for (int i = 0; i < index; i++) {
                            iter.next();
                        }
                        // "(emoji) "
                        Emoji randomChoice = (Emoji) iter.next();
                        lineLyrics.append("(").append((randomChoice.getUnicode())).append(") ");
                        canGenerateEmojis = true;

                        continue;
                    }

// 4. ***CHECKS***
// Check if word is different form of an alias/tag (plural, -ing, etc.)


// 4a. Add all possible forms of the lyric to ArrayList
                    ArrayList<String> checks = new ArrayList<>();

                    // No need to check length since "ing" wouldn't be a lyric
                    // Logic for first 3 checks taken from: https://github.com/notwaldorf/emoji-translate/blob/master/emoji-translate.js
                    if (lyric.contains("ing") || lyric.contains("in'")) {
                        String verb = lyric.substring(0, lyric.length() - 3);
                        // eating -> eat
                        checks.add(verb);
                        // dancing -> dance
                        checks.add(verb + 'e');
                        // stopping -> stop
                        checks.add(verb.substring(0, verb.length() - 1));
                        // -ing to -er form (swimming -> swimmer, surfing -> surfer)
                        checks.add(verb + "er");
                    }

                    // adjective -> noun (cloudy -> cloud)
                    // -y -> -ies (fry -> fries)
                    // There are exceptions to this rule but none that would return an incorrect emoji
                    else if (lyric.substring(lyric.length() - 1).equals("y")) {
                        String minusY = lyric.substring(0, lyric.length() - 1);
                        checks.add(minusY);
                        checks.add(minusY + "ies");
                    }

                    // -ies -> -y (parties -> party)
                    else if (lyric.length() > 3 && lyric.substring(lyric.length() - 3).equals("ies")) {
                        checks.add(lyric.substring(0, lyric.length() - 3) + 'y');
                    } else {

                        // Don't do if - else-if for these 2 since there is overlap between the groups that can't easily be accounted for

                        // Unpluralize (sings -> sing)
                        // past -> present (smiled -> smile)
                        String lastOne = lyric.substring(lyric.length() - 1);
                        if (lastOne.equals("s") || lastOne.equals("d")) {
                            checks.add(lyric.substring(0, lyric.length() - 1));
                        }

                        // Undo possessive (man's -> man)
                        // past -> infinitive (blushed -> blush)
                        String lastTwo = lyric.substring(lyric.length() - 2);
                        if (lastTwo.equals("'s") || lastTwo.equals("ed")) {
                            checks.add(lyric.substring(0, lyric.length() - 2));
                        }

                        // Plural
                        // Logic taken from: https://github.com/notwaldorf/emoji-translate/blob/master/emoji-translate.js
                        checks.add(lyric + 's');

                        // Verb to -ing form (walk -> walking)
                        checks.add(lyric + "ing");
                        // Verb to -er form (climb -> climber)
                        checks.add(lyric + "er");
                        // infinitive to past tense/adjective (amaze -> amazed)
                        checks.add(lyric + "d");
                        // infinitive to past tense/adjective (disappoint -> disappointed)
                        checks.add(lyric + "ed");
                        // sleep -> sleepy
                        checks.add(lyric + "y");
                    }

// 4b. Now go through the check variations and try to find a match
                    for (String possibleMatch : checks) {
                        // Try matching for the alias
                        // Unicode will display as emoji in the app
                        // "(emoji) "
                        emoji = EmojiManager.getForAlias(possibleMatch);
                        if (emoji != null) { // Match found
                            lineLyrics.append("(").append(emoji.getUnicode()).append(") ");
                            canGenerateEmojis = true;
                            continue;
                        }

                        // If no alias match, try to match the tags
                        // This returns a Set<Emoji> (because there might be more than one match for tags)
                        emojis = EmojiManager.getForTag(possibleMatch);
                        if (emojis != null) { // Matches found
                            // Pick a random number index in range(emojis.size())
                            // Iterate index - 1 times
                            // The final iteration gives the chosen random emoji, so add it to lineLyrics
                            Random rand = new Random();
                            int index = rand.nextInt(emojis.size());
                            for (int i = 0; i < index; i++) {
                                emojis.iterator().next();
                            }
                            // "(emoji) "
                            lineLyrics.append("(").append((emojis.iterator().next().getUnicode())).append(") ");
                            canGenerateEmojis = true;
                        }
                    }
                }
// 5. Add the processed line to emojiLyrics

                // If translating from English, add the processed original line here
                if (lyricsLanguageTag.equals("en") && !preferredLanguage.equals("en")) {
                    emojiLyrics.append(lineLyrics.toString()).append("\n");
                }
                // lineLyrics are already in English, so we only need to re-translate if preferred is another language
                if (!lyricsLanguageTag.equals(preferredLanguage) && (!preferredLanguage.equals("en"))) {
                    String toBeTranslated = lineLyrics.toString();
                    // Words like 'Til mess with the translation
                    if (toBeTranslated.startsWith("'")) {
                        toBeTranslated.replace("'", "");
                    }
                    try {
                        TranslateOptions translateOptions = new TranslateOptions.Builder()
                                .addText(toBeTranslated)
                                .modelId("en-" + preferredLanguage)
                                .build();
                        try {
                            String result = languageTranslator.translate(translateOptions)
                                    .execute().getResult().getTranslations().get(0).getTranslation();

                            emojiLyrics.append("{{{").append(result).append("}}}").append("\n\n");
                        } catch (Exception e) {
                            return;
                        }
                    }
                    catch (Exception e) {
                        try {
                            TranslateOptions translateOptions = new TranslateOptions.Builder()
                                    .addText(toBeTranslated)
                                    .target(preferredLanguage)
                                    .build();

                            try {
                                String result = languageTranslator.translate(translateOptions)
                                        .execute().getResult().getTranslations().get(0).getTranslation();

                                emojiLyrics.append("{{{").append(result).append("}}}").append("\n\n");
                            } catch (Exception e2) {
                            }

                        } catch (Exception e2) {
                            // Translator can't determine source language
                        }
                    }
                    continue;
                }
                if (lyricsLanguageTag.equals(preferredLanguage)) { // The line will already contain emojis
                    emojiLyrics.append(lineLyrics.toString());
                } else {
                    emojiLyrics.append("{{{").append(lineLyrics.toString()).append("}}}").append("\n\n");
                }

            }
            if (!canGenerateEmojis) {
                // If no emojis can be generated for the song for whatever reason, let the user know and then just display the lyrics
                if (retried) {
                    String warning = "**There was a problem determining the language of this song**\n**Emojis/translations could not be generated**\n\n";
                    lyric_container.setText(warning + lyrics.replace(" *\n* ", "\n").replace(" *\r* ", "\r").replace(" ,", ",").replace(" ?", "?").replace(" .", "."));
                    lyric_container.setMovementMethod(new ScrollingMovementMethod());
                }
                // If no emojis are generated during lyricProcessing(), we'll retry once as if the song language and language preference are in English
                // If it fails again, we'll just return the original lyrics
                else {
                    retried = true;
                    lyricsLanguageTag = "en";
                    preferredLanguage = "en";
                    lyricProcessing();
                }
            }
            else {
                // Get rid of extra spaces before punctuation
                lyric_container.setText((emojiLyrics.toString().replace(" ,", ",").replace(" ?", "?")));
            }
        }
    }

    void retrieveSharedPreferenceInfo(){
        SharedPreferences simpleAppInfo = getSharedPreferences("LanguageSelection", Context.MODE_PRIVATE);

        preferredLanguage = simpleAppInfo.getString("language", "<missing>");
    }

}
