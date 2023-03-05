package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class SpotifyRepository {
    public HashMap<Artist, List<Album>> artistAlbumMap;
    public HashMap<Album, List<Song>> albumSongMap;
    public HashMap<Playlist, List<Song>> playlistSongMap;
    public HashMap<Playlist, List<User>> playlistListenerMap;
    public HashMap<User, Playlist> creatorPlaylistMap;
    public HashMap<User, List<Playlist>> userPlaylistMap;
    public HashMap<Song, List<User>> songLikeMap;

    public List<User> users;
    public List<Song> songs;
    public List<Playlist> playlists;
    public List<Album> albums;
    public List<Artist> artists;

    public SpotifyRepository(){
        //To avoid hitting apis multiple times, initialize all the hashmaps here with some dummy data
        artistAlbumMap = new HashMap<>();
        albumSongMap = new HashMap<>();
        playlistSongMap = new HashMap<>();
        playlistListenerMap = new HashMap<>();
        creatorPlaylistMap = new HashMap<>();
        userPlaylistMap = new HashMap<>();
        songLikeMap = new HashMap<>();

        users = new ArrayList<>();
        songs = new ArrayList<>();
        playlists = new ArrayList<>();
        albums = new ArrayList<>();
        artists = new ArrayList<>();
    }

    public User createUser(String name, String mobile) {
        User user=new User(name,mobile);
        users.add(user);
        return user;
    }

    public Artist createArtist(String name) {
        Artist artist=new Artist(name);
        artists.add(artist);
        return artist;
    }

    public Album createAlbum(String title, String artistName) {
        Artist artist = null;
        boolean b=false;
        for(Artist x:artists){
            if(x.getName().equals(artistName)){
                b=true;
                artist=x;
                break;
            }
        }
        if(b==false) {
            artist = createArtist(artistName);
            artists.add(artist);
        }
        Album album = new Album(title);
        albums.add(album);
        if(artistAlbumMap.containsKey(artistName)){
            artistAlbumMap.get(artist).add(album);
        }
        else{
            List<Album>albums1=new ArrayList<>();
            albums1.add(album);
            artistAlbumMap.put(artist,albums1);
        }
        return album;
    }

    public Song createSong(String title, String albumName, int length) throws Exception{
        for(Album x:albums){
            if(x.getTitle().equals(albumName)){
                Song song=new Song(title,length);
                songs.add(song);
                if(albumSongMap.containsKey(x)) {
                    albumSongMap.get(x).add(song);
                }
                else{
                    List<Song>songs1=new ArrayList<>();
                    songs1.add(song);
                    albumSongMap.put(x,songs1);
                }
                return song;
            }
        }
        throw new Exception("Album does not exist");
    }

    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {
        for (User x : users) {
            if (x.getMobile().equals(mobile)) {
                Playlist playlist = new Playlist(title);
                if(userPlaylistMap.containsKey(x)){
                    userPlaylistMap.get(x).add(playlist);
                }
                else{
                    List<Playlist>p=new ArrayList<>();
                    p.add(playlist);
                    userPlaylistMap.put(x,p);
                }
                List<Song> songs1 = new ArrayList<>();
                for (Song s : songs) {
                    if (s.getLength() == length) {
                        songs1.add(s);
                    }
                }
                playlists.add(playlist);
                playlistSongMap.put(playlist, songs1);
                creatorPlaylistMap.put(x, playlist);
                List<User> list = new ArrayList<>();
                list.add(x);
                playlistListenerMap.put(playlist, list);
                return playlist;
            }
        }
        throw new Exception("User does not exist");
    }

    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {
        for (User x : users) {
            if (x.getMobile().equals(mobile)) {
                Playlist playlist = new Playlist(title);
                if(userPlaylistMap.containsKey(x)){
                    userPlaylistMap.get(x).add(playlist);
                }
                else{
                    List<Playlist>p=new ArrayList<>();
                    p.add(playlist);
                    userPlaylistMap.put(x,p);
                }
                List<Song> songs1 = new ArrayList<>();
                for (String s : songTitles) {
                    for (Song m : songs) {
                        if (s.equals(m.getTitle())) {
                            songs1.add(m);
                            break;
                        }
                    }
                }
                playlists.add(playlist);
                playlistSongMap.put(playlist, songs1);
                creatorPlaylistMap.put(x, playlist);
                List<User> list = new ArrayList<>();
                list.add(x);
                playlistListenerMap.put(playlist, list);
                return playlist;
            }
        }
         throw new Exception("User does not exist");
    }

    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {
        for (User u : users) {
            if (u.getMobile().equals(mobile)) {
                for (Playlist x : playlists) {
                    if (x.getTitle().equals(playlistTitle)) {
                        if (creatorPlaylistMap.get(u).getTitle().equals(x.getTitle()) || playlistListenerMap.get(x).contains(u)) {
                          return x;
                        }
                        else {
                            userPlaylistMap.get(u).add(x);
                            playlistListenerMap.get(x).add(u);
                            return x;
                        }
                    }
                }
                throw new Exception("Playlist does not exist");
            }
        }
        throw new Exception("User does not exist");
    }

    public Song likeSong(String mobile, String songTitle) throws Exception {
        for (User u : users) {
            if (u.getMobile().equals(mobile)) {
                for (Song s : songs) {
                    if (s.getTitle().equals(songTitle)) {
                        int co=0;
                        if (songLikeMap.containsKey(s)) {
                            if (!songLikeMap.get(s).contains(u)) {
                                songLikeMap.get(s).add(u);
                                co++;
                            }
                            s.setLikes(songLikeMap.get(s).size());
                            Artist a=findArtist(s);
                            if(a!=null) {
                                a.setLikes(s.getLikes()+co);
                            }
                        } else {
                            List<User> list = new ArrayList<>();
                            list.add(u);
                            songLikeMap.put(s, list);
                            s.setLikes(songLikeMap.get(s).size());
                            Artist a=findArtist(s);
                            if(a!=null) {
                                a.setLikes(s.getLikes()+1);
                            }
                        }
                        return s;
                    }
                }
                throw new Exception("Song does not exist");
            }
        }
        throw new Exception("User does not exist");
    }


    public String mostPopularArtist() {
        int n=0;
        String ans="";
        for(Artist s:artists){
            if(s.getLikes()>n){
                n=s.getLikes();
                ans=s.getName();
            }
        }
        return ans;
    }

    public String mostPopularSong() {
        int n=0;
        String ans="";
        for(Song s:songs){
            if(s.getLikes()>n){
                n=s.getLikes();
                ans=s.getTitle();
            }
        }
        return ans;
    }
    public Artist findArtist(Song song){
        Album album = null;
        for(Album x:albumSongMap.keySet()){
            if(albumSongMap.get(x).contains(song)){
                album=x;
                break;
            }
        }
        for(Artist artist:artistAlbumMap.keySet()){
            if(artistAlbumMap.get(artist).contains(album)){
                return artist;
            }
        }
        return null;
    }
}
