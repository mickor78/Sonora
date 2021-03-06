package mickor78.GUIConrollers;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import mickor78.FileOrganizer.SearcherOfSources;
import mickor78.FileOrganizer.Track;
import mickor78.FileOrganizer.TrackList;
import mickor78.MainApp;
import mickor78.Utility.PieceViewer;
import mickor78.Utility.PlayerUtil;
import mickor78.Utility.TrackListUtil;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Class controlling Player.fxml
 *
 *  @author Michal Korzeniewski
 */
public class PlayerController {

    @FXML
    private ListView<Track> playlistView;
    private ObservableList<Track> observablePlayListView;

    @FXML
    private AnchorPane MainPane;

    @FXML
    private Label titleLabel;
    @FXML
    private Label autorLabel;

    @FXML
    private Label trackInfo;

    @FXML
    private Label fastLabel;

    @FXML
    private Slider sliderFast;


    @FXML
    private ProgressBar progressBar;
    private ChangeListener<Duration> progressChangeListener;

    @FXML
    private Label timeLabel;

    @FXML
    private Button stopButton;

    @FXML
    private ListView<Track> listPlayback;
    private ObservableList<Track> observablePlaybackList;

    @FXML
    private Button playButton;

    @FXML
    private Button nextTrackButton;

    @FXML
    private Button cutButton;

    @FXML
    private TextField lowCutTextField;

    @FXML
    private TextField highCutTextView;


    @FXML
    private Button previousTrackButton;

    @FXML
    private Button removeTracklistButton;

    @FXML
    private Button addToPlaybackButton;

    @FXML
    private Button shuffleButton;

    @FXML
    private Button repeatButton;

    @FXML
    private Button closeButton;

    @FXML
    private Button addAllToPlayback;

    @FXML
    private Button addPlaylistButton;

    @FXML
    private Button maxButton;

    @FXML
    private ListView<TrackList> trackListView;
    private ObservableList<TrackList> observableTrackList;


    private MainApp mainApp;

    private boolean played;
    private boolean repeat;
    private boolean maximalized;
    private PlayerUtil playerUtil;
    //private Stage dialogStage;

    //current variables
    private TrackList playBackQueue;
    private TrackList currentPlaylist;
    private Track fooTrack;
    private double speed;
    private Track addToPlayBackTrack;


    /**
     * Set mainApp
     *
     * @param mainApp
     */
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    /**
     * Custom close button
     */
    @FXML
    void handleClose() {
        Platform.exit();
        System.exit(0);
    }

    /**
     * maximize button handle
     */

    @FXML
    void handleMaximalize() {
        if (maximalized) {
            maximalized = false;
            mainApp.getPrimaryStage().setMaximized(false);
            mainApp.getPrimaryStage().setMaxHeight(600);
            mainApp.getPrimaryStage().setMinWidth(300);
            mainApp.getPrimaryStage().centerOnScreen();
        } else {
            maximalized = true;
            mainApp.getPrimaryStage().setMaximized(true);
        }
    }

    /**
     * Method set up TrackLists view
     */

    private void setupTrackListView() {
        observableTrackList = playerUtil.getAll();
        trackListView.setItems(observableTrackList);

        // Set custom cell view
        trackListView.setCellFactory((ListView<TrackList> p) -> {
            ListCell<TrackList> cell = new ListCell<TrackList>() {
                @Override
                protected void updateItem(TrackList trackList, boolean bln) {
                    super.updateItem(trackList, bln);
                    if (trackList != null) {
                        setText(trackList.getName().getValue());
                    }
                }
            };
            return cell;
        });

        //Handle view playlist on one click
        trackListView.setOnMouseClicked((MouseEvent click) -> {
            if (click.getClickCount() == 1) {
                currentPlaylist.deletePlaylist();
                currentPlaylist = trackListView.getSelectionModel().getSelectedItem();
                refreshList(playlistView);
                setupPlaylistView();
            }
        });
    }

    /**
     * Cutting music service
     *
     * @throws IOException
     * @throws UnsupportedAudioFileException
     */

    @FXML
    private void handleCut() throws IOException, UnsupportedAudioFileException {
        double secFrom = getSec(lowCutTextField.getText());
        playerUtil.getPlayer().seek(Duration.seconds(secFrom));
        if(!highCutTextView.getText().isEmpty()) {
            double secTo = getSec(highCutTextView.getText());
        }
    }


    /**
     * Method convert MM:SS in string to second
     *
     * @param in
     * @return double time in second
     */

    private double getSec(String in) {
        String time = in;
        System.out.println(in);
        String[] timeSplited = time.split(":");
        double min = Double.parseDouble(timeSplited[0]);
        double sec = Double.parseDouble(timeSplited[1]);
        sec = sec+min*60;
        return sec;
    }

    /**
     * Method sets up PlaylistView
     */

    private void setupPlaylistView() {
        observablePlayListView = currentPlaylist.getPlaylist();
        playlistView.setItems((observablePlayListView));

        playlistView.setCellFactory((ListView<Track> p) -> {
            ListCell<Track> cell = new ListCell<Track>() {
                @Override
                protected void updateItem(Track track, boolean bln) {
                    super.updateItem(track, bln);
                    if (track != null) {
                        setText(track.getTitle().getValue() + " " + track.getArtist().getValue());
                    }
                }
            };
            return cell;
        });

        // select track to add to playback queue
        playlistView.setOnMouseClicked((MouseEvent click) -> {
            if (click.getClickCount() == 1) {
                addToPlayBackTrack = playlistView.getSelectionModel().getSelectedItem();
            }
        });

    }

    /**
     * removes Tracklist
     */

    @FXML
    void removeTrackListHandle() {
        playerUtil.removeTracklist(currentPlaylist);
        setupTrackListView();
    }

    /**
     * add addToPlayBackTrack to playBack
     */

    @FXML
    void addToPlaybackHandle() {
        playerUtil.addTrackToCurrentTracklist(addToPlayBackTrack);
        refreshList(listPlayback);
        setupPlaybackView();
    }

    /**
     * Add all to playback
     */
    @FXML
    void addAllToPlaybackHandle() {
        playerUtil.addPlaylistToPlayback(currentPlaylist);
        refreshList(listPlayback);
        setupPlaybackView();
    }


    /**
     * sets up Playback View
     */
    private void setupPlaybackView() {
        observablePlayListView = playerUtil.getCurrentTracklist().getPlaylist();
        listPlayback.setItems(observablePlayListView);

        listPlayback.setCellFactory((ListView<Track> p) -> {
            ListCell<Track> cell = new ListCell<Track>() {
                @Override
                protected void updateItem(Track track, boolean bln) {
                    super.updateItem(track, bln);
                    if (track != null) {
                        setText(track.getTitle().getValue() + " " + track.getArtist().getValue());
                    }
                }
            };
            return cell;
        });


        // play track if double clicked
        // select if one clicked
        listPlayback.setOnMouseClicked((MouseEvent click) -> {
            if (click.getClickCount() == 2) {
                //playerUtil.pause();
                playerUtil.setCurrentMedia(listPlayback.getSelectionModel().getSelectedItem());
                setMediaInfo(playerUtil.getCurrentTrack());
                //playerUtil.initialPlayer();
                handlePlayTrigger();
            } else if (click.getClickCount() == 1) {
                playerUtil.setCurrentMedia(listPlayback.getSelectionModel().getSelectedItem());
                if(!playerUtil.isPlayerInitialized())setMediaInfo(playerUtil.getCurrentTrack());
            }
        });
    }


    /**
     * repeat track
     */
    @FXML
    private void handleRepeatTrigger() {
        if (!repeat) {
            playerUtil.repeat(true);
            repeatButton.setText("NO REPEAT");
            repeat = true;
        } else {
            playerUtil.repeat(false);
            repeatButton.setText("REPEAT");
            repeat=false;
        }
    }

    /**
     * shuffle playback queue
     */
    @FXML
    private void handleShuffleTrigger() {
        TrackListUtil trackListUtil= new TrackListUtil(playerUtil.getCurrentTracklist());
        trackListUtil.shuffle();
        playerUtil.setCurrentTracklist(trackListUtil.getTrackList());
        refreshList(listPlayback);
        setupPlaybackView();
    }

    /**
     * play track if currentTrack is in playerUtil
     */
    @FXML
    private void handlePlayTrigger() {

        if(!playerUtil.isPlayerInitialized()) playerUtil.initialPlayer();
        //if selected track are not playing set selected as currentTrack
        if(!playerUtil.getPlayer().getMedia().equals(playerUtil.getCurrentTrack())){
            setMediaInfo(playerUtil.getCurrentTrack());
            playerUtil.initialPlayer();
        }


        played = playerUtil.getPlayer().getStatus().equals(MediaPlayer.Status.PLAYING);
        if (!played) {
            playerUtil.play();
            playButton.setText("Pause");
        } else {
            playerUtil.pause();
            playButton.setText("Play");
        }
            handleProgresBar();


    }

    /**
     * stop track
     */

    @FXML
    private void handleStopTrgger() {
        progressBar.setProgress(0);
        playerUtil.stop();
        playButton.setText("Play");
    }

    /**
     * Add playlist to list of TrackList
     */
    @FXML
    void handleAddPlaylist() {
        playerUtil.addPlaylistToListOfPlaylist(SearcherOfSources.invoke());
    }

    /**
     * Allert with track info
     */

    @FXML
    private void handleTrackInfo() {
        PieceViewer.invoke(playerUtil);
    }


    /**
     * next track
     */
    @FXML
    private void handleNextTrack() {

        playerUtil.playNext();
        setMediaInfo(playerUtil.getCurrentTrack());
        playButton.setText("Pause");
        handleProgresBar();
    }

    /**
     * previous track
     */
    @FXML
    private void handlePreviousTrack() {
        playerUtil.playPrevious();
        setMediaInfo(playerUtil.getCurrentTrack());
        playButton.setText("Pause");
        handleProgresBar();
    }

    /**
     * initialize method
     */

    @FXML
    void initialize() {
        playerUtil = new PlayerUtil();
        currentPlaylist = new TrackList();

        playingSpeedHandler();
        setupTrackListView();
    }

    /**
     * set up progressBar to show duration
     */
    private void handleProgresBar() {
        // Add progressbar listener to show current song percent
        progressChangeListener = (ObservableValue<? extends Duration> observableValue,
                                  Duration oldValue, Duration newValue) -> {
            double currentTimeMillis = playerUtil.getPlayer().getCurrentTime().toMillis();
            double totalDurationMillis = playerUtil.getPlayer().getTotalDuration().toMillis();
            progressBar.setProgress(1.0 * currentTimeMillis / totalDurationMillis);

            // Set time count in label
            double currentTimeSeconds = playerUtil.getPlayer().getCurrentTime().toSeconds();
            int minutes = (int) (currentTimeSeconds % 3600) / 60;
            int seconds = (int) currentTimeSeconds % 60;
            String formattedMinutes = String.format("%02d", minutes);
            String formattedSeconds = String.format("%02d", seconds);
            timeLabel.setText(formattedMinutes + ":" + formattedSeconds);

        };
        playerUtil.getPlayer().currentTimeProperty().addListener(progressChangeListener);

    }

    /**
     * speed player handler
     */
    private void playingSpeedHandler() {
        sliderFast.valueProperty().addListener(((observable, oldValue, newValue) -> {
            speed = Math.exp(Math.pow(newValue.doubleValue() / 50, 1.0));
            fastLabel.setText(String.format("%.2f", speed) + "x");
            playerUtil.setRate(speed);
        }));
    }

    /**
     * refresh input list
     * @param listView
     */

    public void refreshList(ListView listView) {
        ObservableList<TrackList> items = listView.getItems();
        listView.setItems(null);
        listView.setItems(items);
    }

    /**
     * set media info in labels
     * @param track
     */

    public void setMediaInfo(Track track) {
        titleLabel.setText(track.getTitle().getValue());
        autorLabel.setText(track.getArtist().getValue());
    }


}


