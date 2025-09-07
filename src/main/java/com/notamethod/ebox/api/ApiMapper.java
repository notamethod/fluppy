package com.notamethod.ebox.api;

import com.notamethod.ebox.api.igdb.Game;
import com.notamethod.ebox.api.igdb.Genre;
import com.notamethod.ebox.core.GameApp;
import com.notamethod.ebox.core.GenreApp;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Mapper
public interface ApiMapper {
    ApiMapper INSTANCE = Mappers.getMapper(ApiMapper.class);


    @Mapping( source = "first_release_date", target = "year", qualifiedByName = "StrYearConvert")
    GameApp toGameApp(Game bean);
    //@Mapping(target = "id", ignore = true)
    @Mapping( source = "slug", target = "id")
    GenreApp toGenreApp(Genre bean);
    Game toApiBean(GameApp gameApp);


    List<GameApp> toGameApps(List<Game> entityList);
    List<Game> toApiBeans(List<GameApp> entityList);
    List<GenreApp> toGenres(List<Genre> genreList);

    @Named("StrYearConvert")
    default  Integer strYearConvert(String year ) {
        if (year==null)
            return null;
        try {
            return Integer.valueOf(year);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    default String map(Path path) {
        if (path==null){
            return null;
        }
        return path.toAbsolutePath().toString();
    }

    default Path map(String path) {
        if (path==null){
            return null;
        }
        return Paths.get(path);
    }
}
