package com.mnemo.angler.data.database.DAO;

import com.mnemo.angler.data.database.Entities.FolderLink;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import io.reactivex.Flowable;
import io.reactivex.Single;

@Dao
public interface FolderLinkDAO {

    @Query("SELECT * FROM folders ORDER BY title COLLATE NOCASE ASC")
    Flowable<List<FolderLink>> getFolders();

    @Query("SELECT count(path) from folders WHERE path=:folderPath")
    Single<Integer> checkLink(String folderPath);

    @Insert
    void insert(FolderLink... folderLinks);

    @Query("DELETE FROM folders WHERE path=:folderPath")
    void delete(String folderPath);
}
