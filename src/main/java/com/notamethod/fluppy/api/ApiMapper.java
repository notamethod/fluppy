package com.notamethod.fluppy.api;

import com.notamethod.fluppy.api.igdb.GameApiBean;
import com.notamethod.fluppy.api.igdb.Genre;
import com.notamethod.fluppy.core.GameApp;
import com.notamethod.fluppy.core.GenreApp;
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
    GameApp toGameApp(GameApiBean bean);
    //@Mapping(target = "id", ignore = true)
    @Mapping( source = "slug", target = "id")
    GenreApp toGenreApp(Genre bean);
    GameApiBean toApiBean(GameApp gameApp);


    List<GameApp> toGameApps(List<GameApiBean> entityList);
    List<GameApiBean> toApiBeans(List<GameApp> entityList);
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
