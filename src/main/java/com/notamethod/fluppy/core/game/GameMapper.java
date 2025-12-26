package com.notamethod.fluppy.core.game;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Mapper
public interface GameMapper {
    GameMapper INSTANCE = Mappers.getMapper(GameMapper.class);

    @Mapping(source = "gameExe", target = "game")
    @Mapping( target = "gameYear", source="year")
    @Mapping( target = "genres", ignore = true)
    GameEntity toEntity(GameApp gameApp);
    @Mapping(source = "game", target = "gameExe")
    @Mapping(source = "gameYear", target = "year")
    GameApp toGameApp(GameEntity gameEntity);

    GenreEntity toEntity(GenreApp genre);

    List<GameApp> toGameApps(List<GameEntity> entityList);

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
