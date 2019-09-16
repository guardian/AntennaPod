package de.danoeh.antennapod.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.danoeh.antennapod.R;
import de.danoeh.antennapod.core.feed.PodexContent;
import de.danoeh.antennapod.core.glide.ApGlideSettings;
import de.danoeh.antennapod.core.service.playback.PlayerStatus;
import de.danoeh.antennapod.core.util.playback.Playable;
import de.danoeh.antennapod.core.util.playback.PlaybackController;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static de.danoeh.antennapod.core.service.playback.PlaybackService.INVALID_TIME;

/**
 * Displays the cover and the title of a FeedItem.
 *
 * Extended to show podex images
 */
public class CoverFragment extends Fragment {

    private static final String TAG = "CoverFragment";

    private View root;
    private TextView txtvPodcastTitle;
    private TextView txtvEpisodeTitle;
    private ImageView imgvCover;
    private PlaybackController controller;
    private Disposable disposable;
    private CompositeDisposable podexContentDisposable = new CompositeDisposable();
    Observable<PodexContent> podexContentObservable;

    private PlaybackController newCoverPlaybackController() {
        return new PlaybackController(getActivity(), false) {
            @Override
            public void onPositionObserverUpdate() {
                CoverFragment.this.updatePodexObservable(this.getPosition());
            }

            @Override
            public void onPlaybackResumed() {
                CoverFragment.this.subscribePodexContentObservable();
            }


            @Override
            public boolean loadMediaInfo() {
                CoverFragment.this.loadMediaInfo();
                return true;
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setRetainInstance(true);
        root = inflater.inflate(R.layout.cover_fragment, container, false);
        txtvPodcastTitle = root.findViewById(R.id.txtvPodcastTitle);
        txtvEpisodeTitle = root.findViewById(R.id.txtvEpisodeTitle);
        imgvCover = root.findViewById(R.id.imgvCover);
        imgvCover.setOnClickListener(v -> onPlayPause());
        return root;
    }

    private void loadMediaInfo() {
        if (disposable != null) {
            disposable.dispose();
        }
        disposable = Maybe.create(emitter -> {
                    Playable media = controller.getMedia();
                    if (media != null) {
                        emitter.onSuccess(media);
                    } else {
                        emitter.onComplete();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(media -> displayMediaInfo((Playable) media),
                        error -> Log.e(TAG, Log.getStackTraceString(error)));
    }

    private void displayMediaInfo(@NonNull Playable media) {
        txtvPodcastTitle.setText(media.getFeedTitle());
        txtvEpisodeTitle.setText(media.getEpisodeTitle());
        Glide.with(this)
                .load(media.getImageLocation())
                .apply(new RequestOptions()
                    .diskCacheStrategy(ApGlideSettings.AP_DISK_CACHE_STRATEGY)
                    .dontAnimate()
                    .fitCenter())
                .into(imgvCover);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // prevent memory leaks
        root = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        controller = newCoverPlaybackController();
        controller.init();
        loadMediaInfo();
    }

    @Override
    public void onStop() {
        super.onStop();
        controller.release();
        controller = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (disposable != null) {
            disposable.dispose();
        }

        if (podexContentDisposable != null) {
            podexContentDisposable.dispose();
        }
    }

    //todo pass an instance of the playercontroller to set the delay dynamically on subscribe to minimise desync
    private void updatePodexObservable(final int position) {
        if (position == INVALID_TIME) {
            return;
        }

        //creating our list of observable podex content
        podexContentDisposable.add(
                Single.fromCallable(() -> {
                    List<Observable<PodexContent>> unmergedPodexContentObservable = new ArrayList<>();
                    for (PodexContent content : controller.getMedia().getPodexContent()) {

                        if (content.getStart() > position) {
                            //add emissions for start and end
                            unmergedPodexContentObservable.add(
                                    Observable.just(content)
                                            .delay(content.getStart() - position, TimeUnit.MILLISECONDS)
                            );

                            unmergedPodexContentObservable.add(
                                    Observable.just(content)
                                            .delay(content.getStart() - position, TimeUnit.MILLISECONDS)
                            );
                        } else if (content.getEnd() > position) {
                            processPodexEvent(content, position);
                        }
                    }

                    return Observable.mergeDelayError(unmergedPodexContentObservable);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        newPodexContentObservable -> {
                            podexContentObservable = newPodexContentObservable;
                        },
                        throwable -> {
                            Log.e(TAG, "error initialising podex data");
                        }
                )
        );
    }


    private void subscribePodexContentObservable() {
        podexContentDisposable.add(podexContentObservable.subscribe(
                podexContent -> {
                    processPodexEvent(podexContent, controller.getPosition());
                },
                throwable -> {
                    Log.e(TAG, "Podex: podex observable has failed");
                }
        ));
    }

    private void processPodexEvent(PodexContent content, int time) {
        //todo
    }

    void onPlayPause() {
        if (controller == null) {
            return;
        }
        controller.playPause();
    }
}
