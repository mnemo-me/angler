package com.mnemo.angler;

import com.mnemo.angler.data.AnglerRepository;
import com.mnemo.angler.data.RepositoryModule;
import com.mnemo.angler.ui.main_activity.fragments.albums.album_configuration.AlbumConfigurationPresenter;
import com.mnemo.angler.ui.main_activity.fragments.albums.albums.AlbumsPresenter;
import com.mnemo.angler.ui.main_activity.fragments.music_player.artist_tracks.ArtistTracksPresenter;
import com.mnemo.angler.ui.main_activity.fragments.music_player.artists.ArtistsPresenter;
import com.mnemo.angler.ui.main_activity.fragments.music_player.main_playlist.MainPlaylistPresenter;
import com.mnemo.angler.ui.main_activity.fragments.music_player.music_player.MusicPlayerPresenter;
import com.mnemo.angler.ui.main_activity.activity.MainActivityPresenter;
import com.mnemo.angler.ui.main_activity.fragments.playlists.add_tracks_to_playlist.AddTracksPresenter;
import com.mnemo.angler.ui.main_activity.fragments.playlists.playlist_configuration.PlaylistConfigurationPresenter;
import com.mnemo.angler.ui.main_activity.fragments.playlists.playlist_create.PlaylistCreatePresenter;
import com.mnemo.angler.ui.main_activity.fragments.playlists.playlist_delete.PlaylistDeletePresenter;
import com.mnemo.angler.ui.main_activity.fragments.playlists.playlists.PlaylistsPresenter;

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

    void injectPlaylistsPresenter(PlaylistsPresenter playlistsPresenter);
    void injectPlaylistCreatePresenter(PlaylistCreatePresenter playlistCreatePresenter);
    void injectPlaylistConfigurationPresenter(PlaylistConfigurationPresenter playlistConfigurationPresenter);
    void injectAddTracksPresenter(AddTracksPresenter addTracksPresenter);
    void injectPlaylistDeletePresenter(PlaylistDeletePresenter playlistDeletePresenter);

    void injectAlbumsPresenter(AlbumsPresenter albumsPresenter);
    void injectAlbumConfigurationPresenter(AlbumConfigurationPresenter albumConfigurationPresenter);

}
