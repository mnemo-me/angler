package com.mnemo.angler;

import com.mnemo.angler.data.AnglerRepository;
import com.mnemo.angler.data.RepositoryModule;
import com.mnemo.angler.ui.main_activity.fragments.music_player.artist_tracks.ArtistTracksPresenter;
import com.mnemo.angler.ui.main_activity.fragments.music_player.artists.ArtistsPresenter;
import com.mnemo.angler.ui.main_activity.fragments.music_player.main_playlist.MainPlaylistPresenter;
import com.mnemo.angler.ui.main_activity.fragments.music_player.music_player.MusicPlayerPresenter;
import com.mnemo.angler.ui.main_activity.activity.MainActivityPresenter;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {RepositoryModule.class})
public interface AnglerComponent {

    void injectAnglerRepository(AnglerRepository anglerRepository);
    void injectMainActivityPresenter(MainActivityPresenter mainActivityPresenter);
    void injectMusicPlayerPresenter(MusicPlayerPresenter musicPlayerPresenter);
    void injectMainPlaylistPresenter(MainPlaylistPresenter mainPlaylistPresenter);
    void injectArtistsPresenter(ArtistsPresenter artistsPresenter);
    void injectArtistTracksPresenter(ArtistTracksPresenter artistTracksPresenter);

}
