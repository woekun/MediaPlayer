package mediaplayer2;

import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;

public class GUIController implements Initializable {

    private int j = 0;
    private int repeat = 0;

    private boolean play = false;
    private boolean full = false;
    private boolean mute = false;
    private boolean atEndOfMedia = false;
    private boolean go = false;

    private static final Image DEFAULT_ALBUM_COVER = new Image("icon/defaultAlbum.png");
    private File file;
    private MediaPlayer mp;
    private Duration duration;

    private List<File> tempList;
    private final List<File> fileList = new ArrayList<>();
    private final ObservableList<String> listName = FXCollections.observableArrayList();

    @FXML
    private ListView<String> listView;
    @FXML
    private AnchorPane root;
    @FXML
    private ImageView statusDisplay;
    @FXML
    private Pane display;
    @FXML
    private MediaView mediaView;
    @FXML
    private Label albumDis;
    @FXML
    private Label artistDis;
    @FXML
    private Label yearDis;
    @FXML
    private Label titleDis;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Slider timeSlider;
    @FXML
    private ProgressBar volumeBar;
    @FXML
    private Slider volumeSlider;
    @FXML
    private ImageView btnPaFull;
    @FXML
    private AnchorPane npView;
    @FXML
    private Label timeProgress;
    @FXML
    private ImageView volumeButton;
    @FXML
    private AnchorPane mView;
    @FXML
    private ImageView playButton;
    @FXML
    private ImageView stopButton;
    @FXML
    private ImageView albumCover;
    @FXML
    private ImageView btgMain;
    @FXML
    private ImageView repeatButton;
    @FXML
    private ImageView backButton;
    @FXML
    private ImageView nextButton;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        listView.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
            j = listView.getSelectionModel().getSelectedIndex();
            if (!fileList.isEmpty()) {
                statusDisplay.toBack();
                file = new File(fileList.get(j).getPath());
                String MEDIA_URL = file.toURI().toString();
                Media media = new Media(MEDIA_URL);
                albumCover.setImage(DEFAULT_ALBUM_COVER);
                
                media.getMetadata().addListener((Change<? extends String, ? extends Object> ch) -> {
                    if (ch.wasAdded()) {
                        handleMetadata(ch.getKey(), ch.getValueAdded());
                    }
                });
                
                if (MEDIA_URL.endsWith(".mp3") || MEDIA_URL.endsWith(".MP3")) {
                    playAudio(media);
                }
                if (MEDIA_URL.endsWith(".mp4") || MEDIA_URL.endsWith(".MP4")) {
                    playVideo(media);
                }
                onPlay();
            }
        });
    }

    private void handleMetadata(String key, Object value) {
        switch (key) {
            case "album":
                albumDis.setText(value.toString());
                break;
            case "artist":
                artistDis.setText(value.toString());
                break;
            case "title":
                titleDis.setText(value.toString());
                break;
            case "year":
                yearDis.setText(value.toString());
                break;
            case "image":
                albumCover.setImage((Image) value);
                break;
        }
    }

    protected void updateValues() {
        if (timeProgress != null && timeSlider != null && volumeSlider != null) {
            Platform.runLater(() -> {
                Duration currentTime = mp.getCurrentTime();
                timeProgress.setText(formatTime(currentTime, duration));
                timeSlider.setDisable(duration.isUnknown());
                if (!timeSlider.isDisabled()
                        && duration.greaterThan(Duration.ZERO)
                        && !timeSlider.isValueChanging()) {
                    timeSlider.setValue(mp.getCurrentTime().divide(duration).toMillis() * 100.0);
                    progressBar.setProgress(timeSlider.getValue() / 100);
                }
                if (!volumeSlider.isValueChanging()) {
                    volumeSlider.setValue(mp.getVolume());
                    if (mp.getVolume() == 0.0) {
                        volumeButton.setImage(new Image("icon/Volume_button/mute.png"));
                    } else {
                        volumeButton.setImage(new Image("icon/Volume_button/volume.png"));
                    }
                }
            });
        }
    }

    private void playAudio(Media media) {
        if (mediaView.isVisible() == true) {
            mediaView.setVisible(false);
            display.setVisible(true);
        }
        if (mp != null) {
            mp.dispose();
        }

        mp = new MediaPlayer(media);
        mp.play();

        mp.setAudioSpectrumListener((double timestamp, double duration1, float[] magnitudes, float[] phases) -> {
            display.getChildren().clear();
            int i = 0;
            int x = 10;
            double y = root.getScene().getHeight() / 2;
            Random rand = new Random(System.currentTimeMillis());
            for (float phase : phases) {
                int red = rand.nextInt(255);
                int green = rand.nextInt(255);
                int blue = rand.nextInt(255);
                Circle circle = new Circle(10);
                circle.setCenterX(x + i);
                circle.setCenterY(y + (phase * 100));
                circle.setFill(Color.rgb(red, green, blue, .70));
                display.getChildren().add(circle);
                i += 10;
            }
        });
    }

    private void playVideo(Media media) {
        timeProgress.setTextFill(Color.WHITE);
        npView.setVisible(true);
        mView.toBack();
        btnPaFull.setImage(new Image("icon/Full_button/full.png"));
        go = true;

        if (mediaView.isVisible() == false) {
            mediaView.setVisible(true);
            display.setVisible(false);
        }

        if (mp != null) {
            mp.dispose();
        }

        mp = new MediaPlayer(media);
        mediaView.setMediaPlayer(mp);
        mediaView.setPreserveRatio(true);

        final DoubleProperty width = mediaView.fitWidthProperty();
        final DoubleProperty height = mediaView.fitHeightProperty();

        width.bind(Bindings.selectDouble(mediaView.sceneProperty(), "width"));
        height.bind(Bindings.selectDouble(mediaView.sceneProperty(), "height"));

        mp.play();
    }

    private void onPlay() {
        playButton.setImage(new Image("icon/PlayPause_button/pause.png"));

        mp.setOnReady(() -> {
            duration = mp.getMedia().getDuration();
            updateValues();
        });

        mp.currentTimeProperty().addListener((Observable ov) -> {
            Stage stage = (Stage) root.getScene().getWindow();
            if (stage.isFullScreen() == false) {
                full = false;
            }
            updateValues();
        });

        mp.setOnPaused(() -> {
            playButton.setImage(new Image("icon/PlayPause_button/play.png"));
        });

        mp.setOnEndOfMedia(() -> {
            switch (repeat) {
                case 0:
                    if (fileList.size() - 1 == 0) {
                        mp.stop();
                        mp.seek(mp.getStartTime());
                    } else {
                        listView.getSelectionModel().selectNext();
                    }
                    break;

                case 1:
                    mp.seek(Duration.ZERO);
                    mp.setCycleCount(MediaPlayer.INDEFINITE);
                    break;

                case 2:
                    if (j == fileList.size() - 1) {
                        if (fileList.size() - 1 == 0) {
                            mp.setCycleCount(MediaPlayer.INDEFINITE);
                        } else {
                            listView.getSelectionModel().selectFirst();
                        }
                    } else {
                        listView.getSelectionModel().selectNext();
                    }
                    break;
            }
            atEndOfMedia = true;
        });

        timeSlider.valueProperty().addListener((Observable ov) -> {
            if (timeSlider.isValueChanging()) {
                progressBar.setProgress(timeSlider.getValue() / 100);
                mp.seek(duration.multiply(timeSlider.getValue() / 100.0));
            }
        });

        volumeSlider.valueProperty().addListener((ObservableValue<? extends Number> ov, Number old_val, Number new_val) -> {
            mp.setVolume(new_val.doubleValue());
            volumeBar.setProgress(new_val.doubleValue());
        });

        play = true;
        volumeSlider.setMouseTransparent(!play);
        timeSlider.setMouseTransparent(!play);
    }

    private static String formatTime(Duration elapsed, Duration duration) {
        int intElapsed = (int) Math.floor(elapsed.toSeconds());
        int elapsedHours = intElapsed / (60 * 60);
        if (elapsedHours > 0) {
            intElapsed -= elapsedHours * 60 * 60;
        }
        int elapsedMinutes = intElapsed / 60;
        if (elapsedMinutes > 0) {
            intElapsed -= elapsedMinutes * 60;
        }
        int elapsedSeconds = intElapsed;

        if (duration.greaterThan(Duration.ZERO)) {
            int intDuration = (int) Math.floor(duration.toSeconds());
            int durationHours = intDuration / (60 * 60);
            if (durationHours > 0) {
                intDuration -= durationHours * 60 * 60;
            }
            int durationMinutes = intDuration / 60;
            if (durationMinutes > 0) {
                intDuration -= durationMinutes * 60;
            }
            int durationSeconds = intDuration;
            if (durationHours > 0) {
                return String.format("%d:%02d:%02d/%d:%02d:%02d",
                        elapsedHours, elapsedMinutes, elapsedSeconds,
                        durationHours, durationMinutes, durationSeconds);
            } else {
                return String.format("%02d:%02d/%02d:%02d",
                        elapsedMinutes, elapsedSeconds, durationMinutes,
                        durationSeconds);
            }
        } else {
            if (elapsedHours > 0) {
                return String.format("%d:%02d:%02d", elapsedHours,
                        elapsedMinutes, elapsedSeconds);
            } else {
                return String.format("%02d:%02d", elapsedMinutes,
                        elapsedSeconds);
            }
        }
    }

    @FXML
    private void handleOpen(ActionEvent event) {

        Stage stage = (Stage) root.getScene().getWindow();

        FileChooser fileChooser = new FileChooser();

        //Open directory from existing directory
        if (fileList != null) {
            if (!fileList.isEmpty()) {
                File existDirectory = fileList.get(0).getParentFile();
                fileChooser.setInitialDirectory(existDirectory);
            }
        }

        //Set extension filter
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Media Files", "*.mp4", "*.mp3",
                        "*.wav", "*.fxm", "*.flv"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));

        //Show open file dialog, with primaryStage blocked.
        tempList = fileChooser.showOpenMultipleDialog(stage);
        fileList.addAll(tempList);

        if (fileList != null) {
            listName.clear();
            fileList.stream().forEach((fileList1) -> {
                listName.add(fileList1.getName());
            });
            listView.setItems(listName);

        }
    }

    @FXML
    private void gotoNP(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            if (go == false) {
                npView.setVisible(true);
                mView.toBack();
                timeProgress.setTextFill(Color.WHITE);
                Stage stage = (Stage) root.getScene().getWindow();
                stage.setResizable(true);
                btnPaFull.setImage(new Image("icon/Full_button/full.png"));
                go = true;
            } else {
                if (play == true) {
                    Stage stage = (Stage) root.getScene().getWindow();
                    if (full == false) {
                        stage.setFullScreen(true);
                        btnPaFull.setImage(new Image("icon/Full_button/full_off.png"));
                        full = true;
                    } else {
                        stage.setFullScreen(false);
                        btnPaFull.setImage(new Image("icon/Full_button/full.png"));
                        full = false;
                    }
                }
            }
        }
    }

    @FXML
    private void gotoMain(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            btnPaFull.setImage(new Image("icon/Go_button/goNP.png"));
            timeProgress.setTextFill(Color.BLACK);
            Stage stage = (Stage) root.getScene().getWindow();
            if (stage.isFullScreen() == true) {
                stage.setFullScreen(false);
            }

            mView.setVisible(true);
            npView.toBack();
            stage.setResizable(false);
            go = false;
        }
    }

    @FXML
    private void Foward(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            statusDisplay.toBack();
            if (j == fileList.size() - 1) {
                listView.getSelectionModel().selectFirst();
            } else {
                listView.getSelectionModel().selectNext();
            }
        }
    }

    @FXML
    private void Previous(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            statusDisplay.toBack();
            if (j == 0) {
                listView.getSelectionModel().selectLast();
            } else {
                listView.getSelectionModel().selectPrevious();
            }
        }
    }

    @FXML
    private void handleProgress(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            if (play = true) {
                mp.seek(Duration.seconds((mp.getTotalDuration().toSeconds() * timeSlider.getValue() / 100)));
            }
        }
    }

    @FXML
    private void Play(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            if (j == 0) {
                listView.getSelectionModel().select(j);
                listView.getFocusModel().focus(j);
            }

            if (mp != null) {
                MediaPlayer.Status status = mp.getStatus();
                if (status == MediaPlayer.Status.UNKNOWN || status == MediaPlayer.Status.HALTED) {
                    return;
                }
                if (status == MediaPlayer.Status.PAUSED || status == MediaPlayer.Status.READY || status == MediaPlayer.Status.STOPPED) {
                    if (atEndOfMedia) {
                        atEndOfMedia = false;
                    }
                    onPlay();
                    mp.play();
                    statusDisplay.toBack();
                    play = true;

                } else {
                    mp.pause();
                    statusDisplay.toFront();
                    play = false;
                }
            }
        }
    }

    @FXML
    private void Stop(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            if (mp != null) {
                mp.stop();
                playButton.setImage(new Image("icon/PlayPause_button/play.png"));
                mp.seek(mp.getStartTime());
                play = false;
            }
        }
    }

    @FXML
    private void setVolume(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            if (play = true) {
                if (mute == false) {
                    mp.setVolume(0.0);
                    mute = true;
                } else {
                    mp.setVolume(1.0);
                    mute = false;
                }
            }
        }
    }

    @FXML
    private void Repeat(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            repeat++;
            switch (repeat) {
                case 1:
                    mp.setCycleCount(MediaPlayer.INDEFINITE);
                    repeatButton.setImage(new Image("icon/Replay_button/replay_one.png"));
                    break;
                case 2:
                    mp.setCycleCount(0);
                    repeatButton.setImage(new Image("icon/Replay_button/replay_all.png"));
                    break;
                case 3:
                    repeat = 0;
                    repeatButton.setImage(new Image("icon/Replay_button/replay.png"));
                    break;
            }
        }
    }

    @FXML
    private void btplayChange1(MouseEvent event) {
        if (play == false) {
            playButton.setImage(new Image("icon/PlayPause_button/play_hover.png"));
        } else {
            playButton.setImage(new Image("icon/PlayPause_button/pause_hover.png"));
        }
    }

    @FXML
    private void btplayChange2(MouseEvent event) {
        if (play == false) {
            playButton.setImage(new Image("icon/PlayPause_button/play.png"));
        } else {
            playButton.setImage(new Image("icon/PlayPause_button/pause.png"));
        }
    }

    @FXML
    private void btgoMChange2(MouseEvent event) {
        btgMain.setImage(new Image("icon/Go_button/goM.png"));
    }

    @FXML
    private void btgoMChange1(MouseEvent event) {
        btgMain.setImage(new Image("icon/Go_button/goM_hover.png"));
    }

    @FXML
    private void btnextChange2(MouseEvent event) {
        nextButton.setImage(new Image("icon/RewardPrevious_button/next.png"));
    }

    @FXML
    private void btnextChange1(MouseEvent event) {
        nextButton.setImage(new Image("icon/RewardPrevious_button/next_hover.png"));
    }

    @FXML
    private void btvolumeChange2(MouseEvent event) {
        if (volumeSlider.getValue() != 0) {
            volumeButton.setImage(new Image("icon/Volume_button/volume.png"));
        } else {
            volumeButton.setImage(new Image("icon/Volume_button/mute.png"));
        }
    }

    @FXML
    private void btvolumeChange1(MouseEvent event) {
        if (volumeSlider.getValue() != 0) {
            volumeButton.setImage(new Image("icon/Volume_button/volume_hover.png"));
        } else {
            volumeButton.setImage(new Image("icon/Volume_button/mute_hover.png"));
        }
    }

    @FXML
    private void btbackChange2(MouseEvent event) {
        backButton.setImage(new Image("icon/RewardPrevious_button/previous.png"));
    }

    @FXML
    private void btbackChange1(MouseEvent event) {
        backButton.setImage(new Image("icon/RewardPrevious_button/previous_hover.png"));
    }

    @FXML
    private void btstopChange2(MouseEvent event) {
        stopButton.setImage(new Image("icon/Stop_button/stop.png"));
    }

    @FXML
    private void btstopChange1(MouseEvent event) {
        stopButton.setImage(new Image("icon/Stop_button/stop_hover.png"));
    }

    @FXML
    private void btgoNPChange2(MouseEvent event) {
        if (go == false) {
            btnPaFull.setImage(new Image("icon/Go_button/goNP.png"));
        } else if (full == false) {
            btnPaFull.setImage(new Image("icon/Full_button/full.png"));
        } else {
            btnPaFull.setImage(new Image("icon/Full_button/full_off.png"));
        }
    }

    @FXML
    private void btgoNPChange1(MouseEvent event) {
        if (go == false) {
            btnPaFull.setImage(new Image("icon/Go_button/goNP_hover.png"));
        } else if (full == false) {
            btnPaFull.setImage(new Image("icon/Full_button/full_hover.png"));
        } else {
            btnPaFull.setImage(new Image("icon/Full_button/full_off_hover.png"));
        }

    }

    @FXML
    private void Close(ActionEvent event) {
        if (mp != null) {
            mp.dispose();
            progressBar.setProgress(0);
            timeSlider.setValue(0.0);
            timeProgress.setText("00:00/00:00");
        }
    }

    @FXML
    private void Exit(ActionEvent event) {
        System.exit(0);
    }

    @FXML
    private void deleteList(ActionEvent event) {
        if (mp != null) {
            mp.dispose();
        }
        if (!fileList.isEmpty() || !listName.isEmpty()) {
            fileList.clear();
            listName.clear();
            listView.setItems(listName);
        }
    }
}
