package com.mnemo.angler;

import com.mnemo.angler.data.AnglerRepository;
import com.mnemo.angler.data.RepositoryModule;
import com.mnemo.angler.player.service.AnglerServicePresenter;
import com.mnemo.angler.ui.local_load_activity.activity.LocalLoadPresenter;
import com.mnemo.angler.ui.local_load_activity.fragments.cover_crop.CoverCropPresenter;
import com.mnemo.angler.ui.local_load_activity.fragments.image_folder.ImageFolderPresenter;
import com.mnemo.angler.ui.local_load_activity.fragments.landscape_crop.LandscapeCropPresenter;
import com.mnemo.angler.ui.local_load_activity.fragments.portrait_crop.PortraitCropPresenter;
import com.mnemo.angler.ui.main_activity.fragments.albums.album_configuration.AlbumConfigurationPresenter;
import com.mnemo.angler.ui.main_activity.fragments.albums.albums.AlbumsPresenter;
import com.mnemo.angler.ui.main_activity.fragments.artists.artist_albums.ArtistAlbumsPresenter;
import com.mnemo.angler.ui.main_activity.fragments.artists.artist_bio.ArtistBioPresenter;
import com.mnemo.angler.ui.main_activity.fragments.artists.artist_tracks.ArtistTracksPresenter;
import com.mnemo.angler.ui.main_activity.fragments.artists.artists.ArtistsPresenter;
import com.mnemo.angler.ui.main_activity.fragments.artists.artist_configuration.ArtistConfigurationPresenter;
import com.mnemo.angler.ui.main_activity.fragments.background_changer.background_changer.BackgroundChangerPresenter;
import com.mnemo.angler.ui.main_activity.fragments.equalizer.audio_effects.AudioEffectsPresenter;
import com.mnemo.angler.ui.main_activity.fragments.equalizer.bands.BandsPresenter;
import com.mnemo.angler.ui.main_activity.fragments.equalizer.equalizer.EqualizerPresenter;
import com.mnemo.angler.ui.main_activity.fragments.music_player.artist_tracks.PlaylistArtistTracksPresenter;
import com.mnemo.angler.ui.main_activity.fragments.music_player.artists.PlaylistArtistsPresenter;
import com.mnemo.angler.ui.main_activity.fragments.music_player.main_playlist.MainPlaylistPresenter;
import com.mnemo.angler.ui.main_activity.fragments.music_player.music_player.MusicPlayerPresenter;
import com.mnemo.angler.ui.main_activity.activity.MainActivityPresenter;
import com.mnemo.angler.ui.main_activity.fragments.playlists.add_tracks_to_playlist.AddTracksPresenter;
import com.mnemo.angler.ui.main_activity.fragments.playlists.playlist_configuration.PlaylistConfigurationPresenter;
import com.mnemo.angler.ui.main_activity.fragments.playlists.playlist_create.PlaylistCreatePresenter;
import com.mnemo.angler.ui.main_activity.fragments.playlists.playlist_delete.PlaylistDeletePresenter;
import com.mnemo.angler.ui.main_activity.fragments.playlists.playlists.PlaylistsPresenter;
import com.mnemo.angler.ui.main_activity.misc.add_track_to_playlist.AddTrackToPlaylistPresenter;
import com.mnemo.angler.ui.main_activity.misc.contextual_menu.ContextualMenuPresenter;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {RepositoryModule.class})
public interface AnglerComponent {


    // Repository
    void injectAnglerRepository(AnglerRepository anglerRepository);

    // Service
    void injectAnglerServicePresenter(AnglerServicePresenter anglerServicePresenter);

    // Main activity
    void injectMainActivityPresenter(MainActivityPresenter mainActivityPresenter);

    // Music player
    void injectMusicPlayerPresenter(MusicPlayerPresenter musicPlayerPresenter);
    void injectMainPlaylistPresenter(MainPlaylistPresenter mainPlaylistPresenter);
    void injectPlaylistArtistsPresenter(PlaylistArtistsPresenter playlistArtistsPresenter);
    void injectPlaylistArtistTracksPresenter(PlaylistArtistTracksPresenter playlistArtistTracksPresenter);

    // Playlists
    void injectPlaylistsPresenter(PlaylistsPresenter playlistsPresenter);
    void injectPlaylistCreatePresenter(PlaylistCreatePresenter playlistCreatePresenter);
    void injectPlaylistConfigurationPresenter(PlaylistConfigurationPresenter playlistConfigurationPresenter);
    void injectAddTracksPresenter(AddTracksPresenter addTracksPresenter);
    void injectPlaylistDeletePresenter(PlaylistDeletePresenter playlistDeletePresenter);

    // Albums
    void injectAlbumsPresenter(AlbumsPresenter albumsPresenter);
    void injectAlbumConfigurationPresenter(AlbumConfigurationPresenter albumConfigurationPresenter);

    // Artists
    void injectArtistsPresenter(ArtistsPresenter artistsPresenter);
    void injectArtistConfigurationPresenter(ArtistConfigurationPresenter artistConfigurationPresenter);
    void injectArtistTracksPresenter(ArtistTracksPresenter artistTracksPresenter);
    void injectArtistAlbumsPresenter(ArtistAlbumsPresenter artistAlbumsPresenter);
    void injectArtistBioPresenter(ArtistBioPresenter artistBioPresenter);

    // Equalizer
    void injectEqualizerPresenter(EqualizerPresenter equalizerPresenter);
    void injectBandsPresenter(BandsPresenter bandsPresenter);
    void injectAudioEffectsPresenter(AudioEffectsPresenter audioEffectsPresenter);

    // Background
    void injectBackgroundChangerPresenter(BackgroundChangerPresenter backgroundChangerPresenter);

    // Miscellaneous
    void injectAddTrackToPlaylistPresenter(AddTrackToPlaylistPresenter addTrackToPlaylistPresenter);
    void injectContextualMenuPresenter(ContextualMenuPresenter contextualMenuPresenter);

    // Local load activity
    void injectLocalLoadPresenter(LocalLoadPresenter localLoadPresenter);
    void injectImageFolderPresenter(ImageFolderPresenter imageFolderPresenter);
    void injectPortraitCropPresenter(PortraitCropPresenter portraitCropPresenter);
    void injectLandscapeCropPresenter(LandscapeCropPresenter landscapeCropPresenter);
    void injectCoverCropPresenter(CoverCropPresenter coverCropPresenter);
}
